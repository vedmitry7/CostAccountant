package com.vedmitryapps.costaccountant;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.vedmitryapps.costaccountant.models.Category;
import com.vedmitryapps.costaccountant.models.Day;
import com.vedmitryapps.costaccountant.models.DayPair;
import com.vedmitryapps.costaccountant.models.Product;

import java.security.acl.Group;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class DiagramActivity extends AppCompatActivity {

    private RelativeLayout mainLayout;
    // we're going to display pie chart for smartphones martket shares
    private float[] yData = { 5, 10, 15, 30, 40 };
    private String[] xData = { "Sony", "Huawei", "LG", "Apple", "Samsung" };

    @BindView(R.id.chart)
    PieChart mChart;

    Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagram);

        ButterKnife.bind(this);
        realm = Realm.getDefaultInstance();

        // configure pie chart
        mChart.setUsePercentValues(true);
        Description description = new Description();
        description.setText("bla bla bla");
        mChart.setDescription(description);

        // enable hole and configure
        mChart.setDrawHoleEnabled(true);
       // mChart.setHoleColorTransparent(true);
        mChart.setHoleRadius(7);
        mChart.setTransparentCircleRadius(10);

        // enable rotation of the chart by touch
        mChart.setRotationAngle(0);
        mChart.setRotationEnabled(true);

        mChart.setDrawSlicesUnderHole(true);

        // set a chart value selected listener
        mChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {

            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (e == null)
                    return;

                Toast.makeText(DiagramActivity.this,
                        xData[(int)e.getX()] + " = " + e.getData() + "%", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected() {

            }
        });

        // add data
        addData();

        // customize legends
        Legend l = mChart.getLegend();
        l.setPosition(Legend.LegendPosition.RIGHT_OF_CHART);
        l.setXEntrySpace(7);
        l.setYEntrySpace(5);
    }

    private void addData() {

        RealmResults<Day> days = realm.where(Day.class).findAll();

        HashMap<String, Double> map = new HashMap<>();

        for (Day d:days
             ) {
            RealmList<DayPair> pairs = d.getList();
            for (DayPair pair:pairs
                 ) {
                if(map.containsKey(pair.getProduct().getCategory().getName())){
                    double sum = map.get(pair.getProduct().getCategory().getName());
                    sum += pair.getPrice();
                    map.put(pair.getProduct().getCategory().getName(), sum);
                } else {
                    map.put(pair.getProduct().getCategory().getName(), pair.getPrice());
                }
            }
        }

        ArrayList<PieEntry> yVals1 = new ArrayList<PieEntry>();

        for (Map.Entry entry : map.entrySet()) {

           double d = (Double) entry.getValue();
            Log.i("TAG21", "d - " + d + " name - " + entry.getKey());
            yVals1.add(new PieEntry((float) d, (String) entry.getKey()));
        }

      /*  for (int i = 0; i < yData.length; i++)
            yVals1.add(new PieEntry(yData[i], xData[i]));*/

  /*      ArrayList<String> xVals = new ArrayList<String>();

        for (int i = 0; i < xData.length; i++)
            xVals.add(xData[i]);*/

        // create pie data set
        PieDataSet dataSet = new PieDataSet(yVals1, "Категории");
        dataSet.setSliceSpace(3);
        dataSet.setSelectionShift(5);

        // add many colors
        ArrayList<Integer> colors = new ArrayList<Integer>();

        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);

        colors.add(ColorTemplate.getHoloBlue());
        dataSet.setColors(colors);

        // instantiate pie data object now
        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.GRAY);

        mChart.setData(data);


/*        Highlight[] highlights = new Highlight[1];
        highlights [0] =  new Highlight(5, 7, 0);*/

        // undo all highlights
        mChart.highlightValues(null);

        // update pie chart
        mChart.invalidate();
    }
}
