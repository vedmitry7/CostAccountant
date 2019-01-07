package com.vedmitryapps.costaccountant.models;

import io.realm.RealmObject;

public class DayPair extends RealmObject {

    Product product;
    Double price;

    public DayPair() {
    }

    public DayPair(Product product, Double price) {
        this.product = product;
        this.price = price;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
