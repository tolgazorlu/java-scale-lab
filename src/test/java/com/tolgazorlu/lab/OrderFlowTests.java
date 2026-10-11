package com.tolgazorlu.lab;

import static org.assertj.core.api.Assertions.assertThat;

import com.tolgazorlu.lab.order.OrderService;
import com.tolgazorlu.lab.order.OrderView;
import com.tolgazorlu.lab.order.PlaceOrderRequest;
import com.tolgazorlu.lab.payment.PaymentClient;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * End-to-end through the real service and a real Postgres (Testcontainers).
 * Only the external payment provider is replaced.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
class OrderFlowTests {

    @Autowired OrderService orders;
    @Autowired JdbcTemplate jdbc;
    @MockitoBean PaymentClient payments;

    @Test
    void placingAnOrderLowersStockAndStoresThePaymentRef() {
        jdbc.update("INSERT INTO dealers (id, name, city) VALUES (1, 'Bayi 1', 'Istanbul') ON CONFLICT DO NOTHING");
        Long productId = jdbc.queryForObject(
                "INSERT INTO products (sku, name, category, price_cents, stock) VALUES ('T-1', 'Test', 'icecek', 1000, 5) RETURNING id",
                Long.class);
        when(payments.charge(anyLong(), anyLong())).thenReturn("pay_test");

        OrderView view = orders.place(new PlaceOrderRequest(1L, List.of(new PlaceOrderRequest.Line(productId, 2))));

        assertThat(view.status()).isEqualTo("PAID");
        assertThat(view.totalCents()).isEqualTo(2000);
        assertThat(view.paymentRef()).isEqualTo("pay_test");
        assertThat(jdbc.queryForObject("SELECT stock FROM products WHERE id = ?", Integer.class, productId)).isEqualTo(3);
    }
}
