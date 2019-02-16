package com.vedmitryapps.costaccountant.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.vedmitryapps.costaccountant.R;
import com.vedmitryapps.costaccountant.Util;
import com.vedmitryapps.costaccountant.models.RepeatingSpending;
import com.vedmitryapps.costaccountant.models.RepeatingSpendingType;
import com.vedmitryapps.costaccountant.models.SpendingDay;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;

public class RepeatingSpendingRecyclerAdapter extends RecyclerView.Adapter<RepeatingSpendingRecyclerAdapter.ViewHolder>{

    Context context;

    Realm realm;

    RealmResults<RepeatingSpending> list;

    public RepeatingSpendingRecyclerAdapter() {
        realm = Realm.getDefaultInstance();
        list = realm.where(RepeatingSpending.class).findAll();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(context==null){
            context = parent.getContext();
        }
        View view = null;
        switch (viewType){
            case 0:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.repeating_spending_dayly_row, parent, false);
                break;
            case 1:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.repeating_spending_weekly_row, parent, false);
                break;
            case 2:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.repeating_spending_mouthly_row, parent, false);
                break;
        }
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.type.setText(list.get(position).getType());

        if(list.get(position).getType().equals(RepeatingSpendingType.DAILY.name())){
            holder.type.setText("Ежедневная");
        }
        if(list.get(position).getType().equals(RepeatingSpendingType.WEEKLY.name())){
            holder.type.setText("Еженедельная");

            DateFormat format = new SimpleDateFormat("EEE");
            Calendar calendar = Calendar.getInstance();
            calendar.setFirstDayOfWeek(Calendar.MONDAY);
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);


            StringBuilder stringBuilder = new StringBuilder();

            for (SpendingDay day:list.get(position).getDays()
                 ) {
                Log.d("TAG21", "day " + day.getDay());

                calendar.set(Calendar.DAY_OF_WEEK, day.getDay());
                stringBuilder.append(format.format(calendar.getTime()) + " ");
            }
            holder.dayOfMonth.setText(stringBuilder);

        }
        if(list.get(position).getType().equals(RepeatingSpendingType.MONTHLY.name())){
            holder.dayOfMonth.setText(list.get(position).getDays().get(0).getDay() +  " число");
            holder.type.setText("Ежемесячная");
        }


        holder.product.setText(list.get(position).getProduct().getName() + " " + list.get(position).getProduct().getId());
        holder.category.setText(list.get(position).getProduct().getCategory().getName());
        holder.price.setText(Util.floatToString(list.get(position).getPrice()));
        holder.switch1.setChecked(list.get(position).isEnabled());

        holder.switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                realm.beginTransaction();
                list.get(position).setEnabled(isChecked);
                realm.commitTransaction();
            }
        });
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(list.get(position).getType().equals(RepeatingSpendingType.DAILY.name())){
           return 0;
        }
        if(list.get(position).getType().equals(RepeatingSpendingType.WEEKLY.name())){
            return 1;
        }
        if(list.get(position).getType().equals(RepeatingSpendingType.MONTHLY.name())){
            return 2;
        }
        return 0;
    }

    public void update() {
        list = realm.where(RepeatingSpending.class).findAll();
        Log.d("TAG21", "sp size realm  " + realm.where(RepeatingSpending.class).findAll().size());

        notifyDataSetChanged();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.repeatingSpendingType)
        TextView type;
        @BindView(R.id.product)
        TextView product;
        @BindView(R.id.category)
        TextView category;
        @BindView(R.id.price)
        TextView price;
        @Nullable
        @BindView(R.id.dayOfMonth)
        TextView dayOfMonth;
        @BindView(R.id.switch1)
        Switch switch1;

        ViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }
    }

}