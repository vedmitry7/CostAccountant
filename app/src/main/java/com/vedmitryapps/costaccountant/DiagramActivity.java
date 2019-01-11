package com.vedmitryapps.costaccountant;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
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

    @BindView(R.id.statisticRecyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.period)
    TextView period;

    Realm realm;

    ArrayList<Pair<String, Float>> list = new ArrayList<>();

    StatisticRecyclerAdapter adapter;


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

        l.setEnabled(false);


        mChart.calculateOffsets();
        mChart.invalidate();
        mChart.refreshDrawableState();

        // add data
        addData();

        initRecycler();
    }

    private void initRecycler() {
        adapter = new StatisticRecyclerAdapter(list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void addData() {

        RealmResults<Day> days = realm.where(Day.class).findAll();

        HashMap<String, Float> map = new HashMap<>();

        for (Day d:days
             ) {
            RealmList<DayPair> pairs = d.getList();
            for (DayPair pair:pairs
                 ) {
                String key = pair.getProduct().getCategory().getName();
                if(key.length()==0){
                    key = "Без категории";
                }
                if(map.containsKey(key)){
                    float sum = map.get(pair.getProduct().getCategory().getName());
                    sum += pair.getPrice();
                    map.put(key, sum);
                } else {
                    map.put(key, pair.getPrice());
                }
            }
        }

        for (Map.Entry entry : map.entrySet()) {
            list.add(new Pair<>((String) entry.getKey(), (Float) entry.getValue()));
        }

        ArrayList<PieEntry> yVals1 = new ArrayList<PieEntry>();

        for (Pair p:list
             ) {
            yVals1.add(new PieEntry((float) p.second, (String) p.first));
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



    @OnClick(R.id.changeView)
    public void changeView(View v){
        Log.i("TAG21", "cl");

        if(recyclerView.getVisibility()==View.VISIBLE){
            recyclerView.setVisibility(View.GONE);
            mChart.setVisibility(View.VISIBLE);
        }
        else {
            recyclerView.setVisibility(View.VISIBLE);
            mChart.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.constraintLayout2)
    public void constraintLayout2(View v){
        Log.i("TAG21", "cl");

        final ArrayList<String> list = new ArrayList<>();

        PopupMenu popupMenu = new PopupMenu(this, v);
        final Calendar calendar = Calendar.getInstance();
        popupMenu.inflate(R.menu.popup);

        final DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.today:
                        Log.i("TAG21", dateFormat.format(calendar.getTime()));
                        list.add(dateFormat.format(calendar.getTime()));
                        break;
                    case R.id.yesterday:
                        calendar.add(Calendar.DAY_OF_MONTH, -1);
                        list.add(dateFormat.format(calendar.getTime()));
                        break;
                    case R.id.lastSevenDays:
                        list.add(dateFormat.format(calendar.getTime()));
                        for (int i = 0; i < 6; i++) {
                            calendar.add(Calendar.DAY_OF_MONTH, -1);
                            list.add(dateFormat.format(calendar.getTime()));
                            Log.i("TAG21", dateFormat.format(calendar.getTime()));
                        }
                        break;
                    case R.id.lastMonth:
                        calendar.add(Calendar.MONTH, -1);
                        calendar.set(Calendar.DAY_OF_MONTH, 1);
                        list.add(dateFormat.format(calendar.getTime()));
                        for (int i = 0; i < calendar.getActualMaximum(Calendar.DAY_OF_MONTH)-1; i++) {
                            calendar.add(Calendar.DAY_OF_MONTH, 1);
                            list.add(dateFormat.format(calendar.getTime()));
                            Log.i("TAG21", dateFormat.format(calendar.getTime()));
                        }
                        break;
                    case R.id.lastThirtyDays:
                        list.add(dateFormat.format(calendar.getTime()));
                        for (int i = 0; i < 29; i++) {
                            calendar.add(Calendar.DAY_OF_MONTH, -1);
                            list.add(dateFormat.format(calendar.getTime()));
                            Log.i("TAG21", dateFormat.format(calendar.getTime()));
                        }
                        break;
                    case R.id.allTime:

                        break;
                    case R.id.currentMonth:
                        int days =  calendar.get(Calendar.DAY_OF_MONTH);

                        calendar.set(Calendar.DAY_OF_MONTH, 1);
                        list.add(dateFormat.format(calendar.getTime()));

                        for (int i = 1; i < days; i++) {
                            calendar.add(Calendar.DAY_OF_MONTH, 1);
                            list.add(dateFormat.format(calendar.getTime()));
                            Log.i("TAG21", dateFormat.format(calendar.getTime()));
                        }
                        break;


                }
                return false;
            }
        });

        popupMenu.show();
    }


    public void updatePairList(ArrayList<String> list){

        ArrayList<Day> days = new ArrayList<>();
        for (String s:list
             ) {
            Day day = realm.where(Day.class).equalTo("id", s).findFirst();

            if(day!=null){
                days.add(day);
            }
        }
    }
}
