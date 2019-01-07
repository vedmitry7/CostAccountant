package com.vedmitryapps.costaccountant.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Product extends RealmObject {

    @PrimaryKey
    String name;
    Double defPrice;
    boolean useDefPrice;
    Category category;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getDefPrice() {
        return defPrice;
    }

    public void setDefPrice(Double defPrice) {
        this.defPrice = defPrice;
    }

    public boolean isUseDefPrice() {
        return useDefPrice;
    }

    public void setUseDefPrice(boolean useDefPrice) {
        this.useDefPrice = useDefPrice;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}
