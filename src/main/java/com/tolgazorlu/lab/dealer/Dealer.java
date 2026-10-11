package com.tolgazorlu.lab.dealer;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "dealers")
public class Dealer {

    @Id
    private Long id;
    private String name;
    private String city;

    protected Dealer() {
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCity() { return city; }
}
