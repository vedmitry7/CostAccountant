package com.vedmitryapps.costaccountant;


import com.vedmitryapps.costaccountant.models.Day;
import com.vedmitryapps.costaccountant.models.DayPair;

import io.realm.RealmList;
import io.realm.RealmResults;

public class Util {

    public static String getTrimString(String s){
        String result = s.trim();
        return result;
    }

    public static double countDayPrice(Day day){

        RealmList<DayPair> pairs = day.getList();

        Double sum = 0d;
        for (DayPair p:pairs
             ) {
            sum += p.getPrice();
        }

        return sum;
    }

}
