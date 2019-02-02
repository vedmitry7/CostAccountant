package com.vedmitryapps.costaccountant.models;

import io.realm.RealmObject;

public class SpendingDay extends RealmObject {
    int day;

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }
}
