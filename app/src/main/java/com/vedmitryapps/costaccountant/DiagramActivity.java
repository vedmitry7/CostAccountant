package com.vedmitryapps.costaccountant;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.vedmitryapps.costaccountant.adapters.StatisticRecyclerAdapter;
import com.vedmitryapps.costaccountant.models.Day;
import com.vedmitryapps.costaccountant.models.DayPair;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmList;

public class DiagramActivity extends AppCompatActivity {

    private RelativeLayout mainLayout;
    // we're going to display pie chart for smartphones martket shares
    private float[] yData = { 5, 10, 15, 30, 40 };
    private String[] xData = { "Sony", "Huawei", "LG", "Apple", "Samsung" };

    @BindView(R.id.chart)
    PieChart mChart;

    @BindView(R.id.additionalInfo)
    TextView additionalInfo;

    @BindView(R.id.statisticRecyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.period)
    TextView period;

    Realm realm;

    ArrayList<Pair<String, Float>> list = new ArrayList<>();

    StatisticRecyclerAdapter adapter;

    Animation animation;

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
        animation = new AlphaAnimation(1.0f, 0.0f);
        animation.setDuration(1000);
        animation.setStartOffset(1000);
        animation.setFillAfter(true);
        mChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {

            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (e == null)
                    return;
               // Toast.makeText(DiagramActivity.this, e.getY() + " " + e.getX() , Toast.LENGTH_SHORT).show();
                additionalInfo.setVisibility(View.VISIBLE);
                additionalInfo.setText(Util.floatToString(e.getY()));
                additionalInfo.startAnimation(animation);
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

        initRecycler();
        // add data
        //addData(realm.where(Day.class).findAll());
        currentMonth();

        period.setText("Текущий месяц");

    }

    private void initRecycler() {
        adapter = new StatisticRecyclerAdapter(list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void addData(List<Day> days) {


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

        if(list!=null)
            list.clear();

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
        mChart.notifyDataSetChanged();
        // update pie chart
        mChart.invalidate();
    }



    @OnClick(R.id.changeView)
    public void changeView(View v){
        Log.i("TAG21", "cl");

        additionalInfo.setVisibility(View.GONE);

        if(recyclerView.getVisibility()==View.VISIBLE){
            recyclerView.setVisibility(View.GONE);
            mChart.setVisibility(View.VISIBLE);
            ((ImageView)v).setImageDrawable(getResources().getDrawable(R.drawable.ic_list));
        }
        else {
            ((ImageView)v).setImageDrawable(getResources().getDrawable(R.drawable.ic_chart_pie));
            recyclerView.setVisibility(View.VISIBLE);
            mChart.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.constraintLayout2)
    public void constraintLayout2(View v){
        Log.i("TAG21", "cl");

        final ArrayList<String> list = new ArrayList<>();

        PopupMenu popupMenu = new PopupMenu(this, period);
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
                        updatePairList(list);
                        period.setText("Сегодня");
                        break;
                    case R.id.yesterday:
                        calendar.add(Calendar.DAY_OF_MONTH, -1);
                        list.add(dateFormat.format(calendar.getTime()));
                        updatePairList(list);
                        period.setText("Вчера");
                        break;
                    case R.id.lastSevenDays:
                        list.add(dateFormat.format(calendar.getTime()));
                        for (int i = 0; i < 6; i++) {
                            calendar.add(Calendar.DAY_OF_MONTH, -1);
                            list.add(dateFormat.format(calendar.getTime()));
                            Log.i("TAG21", dateFormat.format(calendar.getTime()));
                        }
                        updatePairList(list);
                        period.setText("Последние 7 дней");
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
                        updatePairList(list);
                        period.setText("В прошлом месяце");

                        break;
                    case R.id.lastThirtyDays:
                        list.add(dateFormat.format(calendar.getTime()));
                        for (int i = 0; i < 29; i++) {
                            calendar.add(Calendar.DAY_OF_MONTH, -1);
                            list.add(dateFormat.format(calendar.getTime()));
                            Log.i("TAG21", dateFormat.format(calendar.getTime()));
                            updatePairList(list);
                        }
                        period.setText("Последние 30 дней");
                        break;
                    case R.id.allTime:
                        updatePairList(null);
                        period.setText("Весь период");
                        break;
                    case R.id.currentMonth:
                        int days =  calendar.get(Calendar.DAY_OF_MONTH);

                        calendar.set(Calendar.DAY_OF_MONTH, 1);
                        list.add(dateFormat.format(calendar.getTime()));

                        for (int i = 1; i < calendar.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
                            calendar.add(Calendar.DAY_OF_MONTH, 1);
                            list.add(dateFormat.format(calendar.getTime()));
                            Log.i("TAG21", dateFormat.format(calendar.getTime()));
                        }
                        updatePairList(list);
                        period.setText("Текущий месяц");
                        break;
                    case R.id.custom:
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(DiagramActivity.this);
                        //dialogBuilder.setTitle("Трата");
                        LayoutInflater inflater = (LayoutInflater) DiagramActivity.this.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                        final View dialogView = inflater.inflate(R.layout.choose_custom_term_dialog, null);

                        dialogBuilder.setView(dialogView);

                        dialogBuilder.setNegativeButton("Отмена", null);

                        dialogBuilder.setCancelable(false);

                        DatePicker startDatePicker = dialogView.findViewById(R.id.startDatePicker);
                        DatePicker endDatePicker = dialogView.findViewById(R.id.endDatePicker);

                        int year = calendar.get(Calendar.YEAR);
                        int month = calendar.get(Calendar.MONTH);
                        int day = calendar.get(Calendar.DAY_OF_MONTH);

                        final Calendar calendarStart = Calendar.getInstance();
                        final Calendar calendarStart2 = Calendar.getInstance();
                        final Calendar calendarEnd = Calendar.getInstance();

                        dialogBuilder.setPositiveButton("Ок", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                if(calendarEnd.getTime().after(calendarStart2.getTime())){
                                    list.add(dateFormat.format(calendarStart.getTime()));
                                    for (;true;) {
                                        calendarStart.add(Calendar.DAY_OF_MONTH, 1);
                                        list.add(dateFormat.format(calendarStart.getTime()));
                                        Log.i("TAG21", dateFormat.format(calendarStart.getTime()));

                                        if(calendarStart.get(Calendar.DAY_OF_MONTH)==calendarEnd.get(Calendar.DAY_OF_MONTH)
                                                && calendarStart.get(Calendar.MONTH)==calendarEnd.get(Calendar.MONTH)
                                                && calendarStart.get(Calendar.YEAR)==calendarEnd.get(Calendar.YEAR)){
                                            Log.i("TAG21", "Stop cycle");
                                            break;
                                        }
                                    }

                                    updatePairList(list);
                                    period.setText(dateFormat.format(calendarStart2.getTime()) + " - " + dateFormat.format(calendarEnd.getTime()));
                                } else {
                                    if(calendarStart.get(Calendar.DAY_OF_MONTH)==calendarEnd.get(Calendar.DAY_OF_MONTH)
                                            && calendarStart.get(Calendar.MONTH)==calendarEnd.get(Calendar.MONTH)
                                            && calendarStart.get(Calendar.YEAR)==calendarEnd.get(Calendar.YEAR)){
                                        Log.i("TAG21", "Add one day");
                                        list.add(dateFormat.format(calendarStart.getTime()));
                                        updatePairList(list);
                                        period.setText(dateFormat.format(calendarStart2.getTime()) + " - " + dateFormat.format(calendarEnd.getTime()));

                                    } else {
                                        Toast.makeText(DiagramActivity.this, "Неверный формат дат", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });

                        startDatePicker.init(year, month, day, new DatePicker.OnDateChangedListener() {
                            @Override
                            public void onDateChanged(DatePicker datePicker, int i, int i1, int i2) {
                                calendarStart.set(i,i1,i2);
                                calendarStart2.set(i,i1,i2);
                            }
                        });

                        endDatePicker.init(year, month, day, new DatePicker.OnDateChangedListener() {
                            @Override
                            public void onDateChanged(DatePicker datePicker, int i, int i1, int i2) {
                                calendarEnd.set(i,i1,i2);
                            }
                        });

                        final AlertDialog b = dialogBuilder.create();
                        b.show();

                        break;
                }
                return false;
            }
        });

        popupMenu.show();
    }

    private void currentMonth(){
        final ArrayList<String> list = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        final DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        int days =  calendar.get(Calendar.DAY_OF_MONTH);

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        list.add(dateFormat.format(calendar.getTime()));

        for (int i = 1; i < calendar.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            list.add(dateFormat.format(calendar.getTime()));
            Log.i("TAG21", dateFormat.format(calendar.getTime()));
        }
        updatePairList(list);
    }


    public void updatePairList(ArrayList<String> list){

        if(list == null){
            addData(realm.where(Day.class).findAll());
            adapter.update(this.list);
            return;
        }
        ArrayList<Day> days = new ArrayList<>();
        for (String s:list
             ) {
            Day day = realm.where(Day.class).equalTo("id", s).findFirst();
            if(day!=null){
                days.add(day);
            }
        }
        addData(days);
        adapter.update(this.list);

    }
}
