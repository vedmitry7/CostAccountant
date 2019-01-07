package com.vedmitryapps.costaccountant.models;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Day extends RealmObject{

    @PrimaryKey
    String id;

    private RealmList<DayPair> list;

    public String getId() {
        return id;
    }

    public RealmList<DayPair> getList() {
        return list;
    }

    public void setList(RealmList<DayPair> list) {
        this.list = list;
    }
}
