package com.tolgazorlu.lab.order;

import com.tolgazorlu.lab.dealer.Dealer;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dealer_id")
    private Dealer dealer;

    private String status;
    private long totalCents;
    private String paymentRef;
    private Instant createdAt;

    // LAB: LAZY means "load the items only when someone touches them".
    // Touching them in a loop is the classic N+1 problem.
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();

    protected Order() {
    }

    public Order(Dealer dealer) {
        this.dealer = dealer;
        this.status = "NEW";
        this.createdAt = Instant.now();
    }

    public void addItem(OrderItem item) {
        items.add(item);
        totalCents += item.lineTotalCents();
    }

    public void markPaid(String paymentRef) {
        this.status = "PAID";
        this.paymentRef = paymentRef;
    }

    public void markFailed() {
        this.status = "FAILED";
    }

    public Long getId() { return id; }
    public Dealer getDealer() { return dealer; }
    public String getStatus() { return status; }
    public long getTotalCents() { return totalCents; }
    public String getPaymentRef() { return paymentRef; }
    public Instant getCreatedAt() { return createdAt; }
    public List<OrderItem> getItems() { return items; }
}
