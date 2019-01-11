package com.vedmitryapps.costaccountant;


import android.util.Log;
import android.util.Pair;

import com.vedmitryapps.costaccountant.models.Day;
import com.vedmitryapps.costaccountant.models.DayPair;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;

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


    public static ArrayList<String> getPercent(ArrayList<Pair<String, Float>> list){

        DecimalFormat mFormat = new DecimalFormat("###,###,##0.0");

        ArrayList<String> strings = new ArrayList<>();
        float sum = 0f;

        for (Pair p:list
             ) {
            sum += (float) p.second;
        }

        Log.i("TAG21", "sum - " + sum);
        for (int i = 0; i < list.size(); i++) {
            float result = new BigDecimal(list.get(i).second/(sum/100)).setScale(1, RoundingMode.HALF_EVEN).floatValue();
            strings.add(i, result + " %");
        }

        return strings;
    }
}
