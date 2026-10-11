package com.tolgazorlu.lab.payment;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Optional;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

/**
 * Talks to the (fake) payment provider running in WireMock on port 4189.
 * LAB: no timeout. If the provider hangs, this thread hangs with it.
 */
@Component
public class PaymentClient {

    private final RestClient http;

    public PaymentClient(RestClient.Builder builder, @Value("${lab.payment.base-url}") String baseUrl) {
        // HTTP/1.1 only: the JDK client otherwise tries an HTTP/2 upgrade that WireMock rejects.
        // LAB: note what is missing here. No connect timeout, no read timeout.
        // Mission 07: fail fast. Connect within 500 ms, answer within 2 s, or give up.
        HttpClient jdk = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofMillis(500))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(jdk);
        factory.setReadTimeout(Duration.ofSeconds(2));
        this.http = builder.baseUrl(baseUrl).requestFactory(factory).build();
    }

    /** The merchant reference we send with every charge, so we can ask the provider about it later. */
    public static String reference(long orderId) {
        return "order-" + orderId;
    }

    public String charge(long orderId, Long dealerId, long amountCents) {
        ChargeResponse response = http.post()
                .uri("/charges")
                .body(Map.of("reference", reference(orderId), "dealerId", dealerId, "amountCents", amountCents))
                .retrieve()
                .body(ChargeResponse.class);
        return response.id();
    }

    /**
     * Mission 07: after a timeout we do not know whether the provider charged. Ask it.
     * Returns the payment id if a charge with this order's reference exists, empty if it does not.
     * Throws if the provider cannot answer either; the caller tries again later.
     */
    public Optional<String> lookup(long orderId) {
        try {
            ChargeResponse found = http.get()
                    .uri(uri -> uri.path("/charges").queryParam("reference", reference(orderId)).build())
                    .retrieve()
                    .body(ChargeResponse.class);
            return Optional.of(found.id());
        } catch (HttpClientErrorException.NotFound none) {
            return Optional.empty();
        }
    }

    record ChargeResponse(String id, String status) {
    }
}
