package com.vedmitryapps.costaccountant;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.vedmitryapps.costaccountant.models.Day;
import com.vedmitryapps.costaccountant.models.DayPair;

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

    @BindView(R.id.period)
    TextView period;


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
        description.setText("");
        mChart.setDescription(description);

        float scale = getResources().getDisplayMetrics().density;
        mChart.setCameraDistance(300);

        // enable hole and configure
        mChart.setDrawHoleEnabled(false);
       // mChart.setHoleColorTransparent(true);
        mChart.setHoleRadius(20);
        mChart.setTransparentCircleRadius(30);




        // enable rotation of the chart by touch
        mChart.setRotationAngle(0);
        mChart.setRotationEnabled(true);

        mChart.setDrawSlicesUnderHole(true);

        mChart.setEntryLabelColor(Color.BLACK);
        mChart.setEntryLabelTextSize(14f);

      //  mChart.setCenterTextSize(18f);

       // mChart.animateY(300);


        // set a chart value selected listener
        mChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {

            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (e == null)
                    return;

                period.setText("x - " + e.getY() + " " + (String) e.getData());

                Toast.makeText(DiagramActivity.this,
                        xData[(int)e.getX()] + " = " + e.getData() + "%", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected() {

            }
        });


        ViewPortHandler handler = mChart.getViewPortHandler();

        //handler.restrainViewPort(100, 200,150,300);;
        mChart.setExtraOffsets(20,0,20,0);
        // customize legends
        Legend l = mChart.getLegend();
        l.setPosition(Legend.LegendPosition.LEFT_OF_CHART);
        l.setXEntrySpace(7);
        l.setYEntrySpace(5);
        l.setTextSize(13f);


        mChart.calculateOffsets();
        mChart.invalidate();
        mChart.refreshDrawableState();

        // add data
        addData();
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

        // create pie data set
        PieDataSet dataSet = new PieDataSet(yVals1, "Категории");
       // dataSet.setValueLinePart1OffsetPercentage(20.f);
        dataSet.setSliceSpace(0.1f);
        dataSet.setSelectionShift(7);


        //value lines params
        dataSet.setValueLinePart1Length(0.6f);
        dataSet.setValueLinePart2Length(0.2f);
        dataSet.setValueTextColor(Color.BLACK);

        dataSet.setValueLinePart1OffsetPercentage(100);

        dataSet.setXValuePosition(PieDataSet.ValuePosition.INSIDE_SLICE);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);



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
        data.setValueTextSize(13f);
        data.setValueTextColor(Color.BLACK);

        mChart.setData(data);


/*        Highlight[] highlights = new Highlight[1];
        highlights [0] =  new Highlight(5, 7, 0);*/

        // undo all highlights
        mChart.highlightValues(null);

        // update pie chart
        mChart.invalidate();
    }
}
