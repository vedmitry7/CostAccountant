package com.vedmitryapps.costaccountant.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.vedmitryapps.costaccountant.Events;
import com.vedmitryapps.costaccountant.R;
import com.vedmitryapps.costaccountant.Util;
import com.vedmitryapps.costaccountant.models.Day;

import org.greenrobot.eventbus.EventBus;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

public class DaysRecyclerAdapter extends RecyclerView.Adapter<DaysRecyclerAdapter.ViewHolder>{

    Context context;

    Calendar calendar = Calendar.getInstance();
    DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
    DateFormat dayOfWeekFormat = new SimpleDateFormat("EE");
    Date date = calendar.getTime();

    int currentDayPos = 30;
    int choosenItem = 30;

    String dateText = dateFormat.format(date);
    List<String> list = new ArrayList<>();

    Realm realm;

    public DaysRecyclerAdapter() {
        realm = Realm.getDefaultInstance();

        calendar.add(Calendar.DAY_OF_MONTH, -31);

        for (int i = 0; i < 60; i++) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            list.add(dateFormat.format(calendar.getTime()));
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(context==null){
            context = parent.getContext();
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.day_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.dayOfMonth.setText(Util.returnDayByDate(list.get(position)));

        if(position == currentDayPos){
            holder.dayOfMonth.setTextColor(Color.WHITE);
            holder.dayOfMonth.setBackgroundResource(R.drawable.stroke);
        } else {
            holder.dayOfMonth.setBackgroundResource(0);
            holder.dayOfMonth.setTextColor(Color.WHITE);
        }

        if(position == choosenItem){
            holder.dayOfMonth.setBackgroundResource(R.drawable.fill_bg);
            holder.dayOfMonth.setTextColor(Color.BLACK);
            holder.indicator.setVisibility(View.VISIBLE);
        } else {
            holder.indicator.setVisibility(View.GONE);
        }

        calendar.set(Util.year(list.get(position)), Util.month(list.get(position)), Util.day(list.get(position)));

        holder.dayOfWeek.setText(dayOfWeekFormat.format(calendar.getTime()));


        Day day = realm.where(Day.class).equalTo("id", list.get(position)).findFirst();
        if(day == null){
            holder.daySpending.setText("");
        } else {
            float f = Util.countDayPrice(day);
            if(f!=0){
                holder.daySpending.setText(Util.countDayPriceString(day));
            } else {
                holder.daySpending.setText("");
            }
        }

    }

    // total number of rows
    @Override
    public int getItemCount() {
        return list.size();
    }


    public String getItemByPosition(int pos){
        return list.get(pos);
    }

    public void update(Day day) {
        notifyDataSetChanged();
    }

    public void addEndDays() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Util.year(list.get(list.size()-1)), Util.month(list.get(list.size()-1)), Util.day(list.get(list.size()-1)));

        for (int i = 0; i < 30; i++) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            list.add( dateFormat.format(calendar.getTime()));

        }
        notifyDataSetChanged();

        Log.d("TAG21", "size - " + list.size());

        for (int i = 0; i < list.size(); i++) {
            Log.d("TAG21", list.get(i));
        }
    }

    public void addStartDays() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Util.year(list.get(list.size()-1)), Util.month(list.get(list.size()-1)), Util.day(list.get(list.size()-1)));

        calendar.add(Calendar.DAY_OF_MONTH, -list.size());
        //list.add(0, dateFormat.format(calendar.getTime()));

        for (int i = 0; i < 30; i++) {
            list.add(0, dateFormat.format(calendar.getTime()));
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            Log.d("TAG21", "add - " + dateFormat.format(calendar.getTime()));
        }
        currentDayPos+=30;
        choosenItem+=30;
        notifyItemRangeInserted(0, 30);
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.dayOfMonth)
        TextView dayOfMonth;

        @BindView(R.id.dayOfWeek)
        TextView dayOfWeek;

        @BindView(R.id.daySpending)
        TextView daySpending;

        @BindView(R.id.indicator)
        ImageView indicator;

        ViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();

            ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
            layoutParams.width = display.getWidth()/7;

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EventBus.getDefault().post(new Events.ClickDay(list.get(getAdapterPosition())));
                    choosenItem = getAdapterPosition();
                    notifyDataSetChanged();

                }
            });
        }
    }

}