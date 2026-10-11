# Scale Lab: agent rules

This repo is a LEARNING lab. The code is naive on purpose.

- Never fix anything marked `LAB:` unless the user is doing that mission and asks for it.
  Missing indexes, N+1, the stock race, the payment call inside the transaction, the client
  without timeouts are the curriculum.
- When asked to "write the next mission" ("sıradaki görevi yaz"), use the `java-scale-lab`
  Hermes skill. Each mission is two English files: `lessons/NN-slug.md` (the concept, 10-15 min
  read) and `labs/NN-slug.md` (the hands-on part), both based on numbers actually measured on this
  machine. Runnable commands sit alone on a line in a bash block starting with `./lab`, because
  dacospace turns those lines into Run buttons.
- dacospace (`~/projects/dacospace`) is the UI for this repo. It parses the `# Mission NN · Title`
  and `# Lesson NN · Title` headings and the PROGRESS.md table: keep those formats stable.
- Ports: cortex `playbooks/local-ports.md`, block 4180-4189. App 4180, extra instances 4190-4191.
- Build with Java 25: `./lab` sets JAVA_HOME from SDKMAN. `./lab test` runs the suite.
- Commits: English, one sentence, `type: subject`, no AI attribution.
