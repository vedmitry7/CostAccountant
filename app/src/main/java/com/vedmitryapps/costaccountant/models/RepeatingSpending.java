package com.vedmitryapps.costaccountant.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RepeatingSpending extends RealmObject {

    @PrimaryKey
    long id;
    Product product;
    String startDate;
    String lastCheckDate;
    float price;


}
