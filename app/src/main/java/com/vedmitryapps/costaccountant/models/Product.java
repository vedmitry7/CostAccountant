package com.vedmitryapps.costaccountant.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Product extends RealmObject {

    @PrimaryKey
    long id;

    String name;
    float defPrice;
    boolean useDefPrice;
    Category category;
    String categoryName;
    String categoryName1;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getDefPrice() {
        return defPrice;
    }

    public void setDefPrice(float defPrice) {
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
