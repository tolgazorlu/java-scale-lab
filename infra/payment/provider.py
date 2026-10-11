#!/usr/bin/env python3
"""Fake payment provider for Scale Lab. Standard library only.

Behaves like a real provider where it matters for the missions:
- it keeps state: every charge it makes is recorded, by merchant reference and by Idempotency-Key,
- a repeated Idempotency-Key returns the original charge instead of taking money twice,
- GET /charges?reference=... tells the truth about whether a charge exists,
- requests are counted the moment they arrive, even if the caller later gives up.

Chaos modes (POST /__lab/mode {"mode": "..."}):
  normal     ~150 ms (lognormal), then charge
  slow       2 s, then charge
  very-slow  9 s, then charge
  hang       never answers (120 s), never charges
  flaky      every third request: 503, no charge
  slow-ack   charges at once, but answers only after 3 s
  errors     503 for every request, no charge
  down       resets the connection, no charge
Lookups (GET /charges) always answer quickly and truthfully, whatever the mode.
"""
import json
import random
import socket
import struct
import threading
import time
import uuid
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib.parse import parse_qs, urlparse

MODES = {"normal", "slow", "very-slow", "hang", "flaky", "slow-ack", "errors", "down"}

lock = threading.Lock()
state = {
    "mode": "normal",
    "flaky_counter": 0,
    "by_key": {},        # Idempotency-Key -> charge
    "by_reference": {},  # reference -> list of charges
    "stats": None,
}


def fresh_stats():
    return {"since": time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime()), "requests": 0,
            "requests_without_key": 0, "replayed_by_key": 0, "charges": 0, "amount_cents": 0,
            "references_charged_more_than_once": 0}


state["stats"] = fresh_stats()


def charge(body, key):
    """Take money once per Idempotency-Key. Returns (charge, replayed)."""
    with lock:
        if key and key in state["by_key"]:
            state["stats"]["replayed_by_key"] += 1
            return state["by_key"][key], True
        c = {"id": "pay_" + uuid.uuid4().hex[:12], "status": "succeeded",
             "reference": body.get("reference"), "amountCents": body.get("amountCents", 0)}
        s = state["stats"]
        s["charges"] += 1
        s["amount_cents"] += int(c["amountCents"] or 0)
        if key:
            state["by_key"][key] = c
        ref = c["reference"]
        if ref:
            existing = state["by_reference"].setdefault(ref, [])
            if len(existing) == 1:
                s["references_charged_more_than_once"] += 1
            existing.append(c)
        return c, False


class Handler(BaseHTTPRequestHandler):
    protocol_version = "HTTP/1.1"

    def log_message(self, format, *args):
        pass

    def send_json(self, status, payload):
        data = json.dumps(payload).encode()
        try:
            self.send_response(status)
            self.send_header("Content-Type", "application/json")
            self.send_header("Content-Length", str(len(data)))
            self.end_headers()
            self.wfile.write(data)
        except (BrokenPipeError, ConnectionResetError):
            pass  # the caller gave up; the outcome on our side stands

    def read_body(self):
        n = int(self.headers.get("Content-Length") or 0)
        raw = self.rfile.read(n) if n else b""
        try:
            return json.loads(raw or b"{}")
        except ValueError:
            return {}

    def do_GET(self):
        url = urlparse(self.path)
        if url.path == "/health":
            return self.send_json(200, {"status": "UP"})
        if url.path == "/__lab/stats":
            with lock:
                return self.send_json(200, dict(state["stats"], mode=state["mode"]))
        if url.path == "/charges":
            ref = (parse_qs(url.query).get("reference") or [""])[0]
            with lock:
                found = state["by_reference"].get(ref)
            if not found:
                return self.send_json(404, {"error": "no such charge", "reference": ref})
            return self.send_json(200, found[0])
        self.send_json(404, {"error": "not found"})

    def do_POST(self):
        url = urlparse(self.path)
        body = self.read_body()
        if url.path == "/__lab/mode":
            mode = body.get("mode")
            if mode not in MODES:
                return self.send_json(400, {"error": f"unknown mode {mode}", "modes": sorted(MODES)})
            with lock:
                state["mode"], state["flaky_counter"] = mode, 0
            return self.send_json(200, {"mode": mode})
        if url.path == "/__lab/stats/reset":
            with lock:
                state["stats"] = fresh_stats()
            return self.send_json(200, state["stats"])
        if url.path != "/charges":
            return self.send_json(404, {"error": "not found"})

        key = self.headers.get("Idempotency-Key")
        with lock:
            state["stats"]["requests"] += 1
            if not key:
                state["stats"]["requests_without_key"] += 1
            mode = state["mode"]
            state["flaky_counter"] += 1
            third = state["flaky_counter"] % 3 == 1

        if mode == "errors" or (mode == "flaky" and third):
            return self.send_json(503, {"error": "provider overloaded"})
        if mode == "down":
            # RST instead of FIN: the caller sees "connection reset by peer"
            self.connection.setsockopt(socket.SOL_SOCKET, socket.SO_LINGER, struct.pack("ii", 1, 0))
            self.close_connection = True
            return
        if mode == "hang":
            time.sleep(120)
            self.close_connection = True
            return
        if mode == "slow-ack":
            c, _ = charge(body, key)
            time.sleep(3)
            return self.send_json(200, c)
        delay = {"slow": 2.0, "very-slow": 9.0}.get(mode, random.lognormvariate(0, 0.4) * 0.150)
        time.sleep(delay)
        c, _ = charge(body, key)
        self.send_json(200, c)


class Server(ThreadingHTTPServer):
    daemon_threads = True
    request_queue_size = 1024


if __name__ == "__main__":
    print("fake payment provider on :8080", flush=True)
    Server(("0.0.0.0", 8080), Handler).serve_forever()
