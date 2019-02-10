package com.vedmitryapps.costaccountant;


import android.content.Intent;
import android.util.Log;
import android.util.Pair;

import com.vedmitryapps.costaccountant.models.Category;
import com.vedmitryapps.costaccountant.models.Day;
import com.vedmitryapps.costaccountant.models.DayPair;
import com.vedmitryapps.costaccountant.models.Product;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class Util {

    public static String getTrimString(String s){
        String result = s.trim();
        return result;
    }

    public static float countDayPrice(Day day){

        RealmList<DayPair> pairs = day.getList();

        float sum = 0;
        for (DayPair p:pairs
                ) {
            sum += p.getPrice();
        }

        return sum;
    }

    public static String countDayPriceString(Day day){

        float f = countDayPrice(day);
        return floatToString(f);

    }

    public static String floatToString(float f){
        String formattedFloat;
        if(SharedManager.getProperty(Constants.KEY_USE_DECIMAL)){
            //  formattedFloat = String.format("%.2f", f);
            formattedFloat = new DecimalFormat("#0.00").format(f);
            float x = f - (int) f;
            if(x==0f){
                Log.i("TAG21", "X == 0 " + formattedFloat);
                formattedFloat = formattedFloat.substring(0,formattedFloat.indexOf(","));
            }
            if(formattedFloat.endsWith("0")){
                formattedFloat = formattedFloat.substring(0,formattedFloat.length()-1);
            }
        } else {
            formattedFloat = new DecimalFormat("#0").format(f);
        }
        return formattedFloat;
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


    public static String returnDayByDate(String s){

        String result = s.substring(0, 2);
        if(result.startsWith("0")){
            result = result.substring(1,2);
        }
        return result;
    }

    public static int year(String s){

        String result = s.substring(6);
        return Integer.parseInt(result);
    }

    public static int month(String s){

        String result = s.substring(3,5);
        return Integer.parseInt(result)-1;
    }

    public static int day(String s){

        String result = s.substring(0,2);
        return Integer.parseInt(result);
    }


    public static long getNextProductId(Realm mRealm) {
        try {
            Number number = mRealm.where(Product.class).max("id");
            if (number != null) {
                return number.longValue() + 1;
            } else {
                return 0;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            return 0;
        }
    }

    public static long getNextDayPairId(Realm mRealm) {
        try {
            Number number = mRealm.where(DayPair.class).max("id");
            if (number != null) {
                return number.longValue() + 1;
            } else {
                return 0;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            return 0;
        }
    }

    public static long getNextCategoryId(Realm realm) {
        try {
            Number number = realm.where(Category.class).max("id");
            if (number != null) {
                return number.longValue() + 1;
            } else {
                return 0;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            return 0;
        }
    }
    public static long getNextId(Realm realm, Class c) {
        try {
            Number number = realm.where(c).max("id");
            if (number != null) {
                return number.longValue() + 1;
            } else {
                return 0;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            return 0;
        }
    }

}
