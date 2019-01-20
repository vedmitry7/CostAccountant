package com.vedmitryapps.costaccountant.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class DayPair extends RealmObject {

    @PrimaryKey
    long id;

    Product product;
    float price;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public DayPair() {
    }

   /* public DayPair(Product product, float price) {
        this.product = product;
        this.price = price;
    }*/

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
