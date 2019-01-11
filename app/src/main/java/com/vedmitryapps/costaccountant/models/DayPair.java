package com.vedmitryapps.costaccountant.models;

import io.realm.RealmObject;

public class DayPair extends RealmObject {

    Product product;
    float price;

    public DayPair() {
    }

    public DayPair(Product product, float price) {
        this.product = product;
        this.price = price;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }
}
