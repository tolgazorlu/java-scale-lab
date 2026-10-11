package com.tolgazorlu.lab.payment;

import java.net.ConnectException;
import java.net.http.HttpConnectTimeoutException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

/**
 * Mission 07: a failed charge call is not always a failed charge.
 *
 * <p>Only some failures prove that no money moved: the request never reached the provider (connection
 * refused, connect timeout), the provider rejected it (4xx), or the provider says it did not process it
 * (503, by its documented contract). Everything else, above all a read timeout, means "the provider may
 * have charged and we did not hear back": the outcome is UNKNOWN and must be reconciled, not assumed.
 */
public final class PaymentOutcome {

    private PaymentOutcome() {
    }

    public static boolean definitelyNotCharged(Throwable error) {
        for (Throwable t = error; t != null; t = t.getCause()) {
            if (t instanceof ConnectException || t instanceof HttpConnectTimeoutException) return true;
            if (t instanceof HttpClientErrorException) return true;
            if (t instanceof HttpServerErrorException.ServiceUnavailable) return true;
            if (t.getClass().getSimpleName().equals("CallNotPermittedException")) return true;
        }
        return false;
    }
}
