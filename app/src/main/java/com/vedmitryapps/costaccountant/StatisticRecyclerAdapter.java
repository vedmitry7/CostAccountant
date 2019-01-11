package com.vedmitryapps.costaccountant;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vedmitryapps.costaccountant.models.Day;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StatisticRecyclerAdapter extends RecyclerView.Adapter<StatisticRecyclerAdapter.ViewHolder>{


    Context context;

    ArrayList<Pair<String, Float>> list;
    ArrayList<String> percentValueList;

    public StatisticRecyclerAdapter(ArrayList<Pair<String, Float>> list) {
        this.list = list;
        percentValueList = Util.getPercent(list);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(context==null){
            context = parent.getContext();
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.statistic_row, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        holder.category.setText(list.get(position).first);
        holder.price.setText(String.valueOf(list.get(position).second));

        holder.percentValue.setText(percentValueList.get(position));

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void update(ArrayList<Pair<String, Float>> list){
        this.list = list;
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.categoryTv)
        TextView category;

        @BindView(R.id.priceTv)
        TextView price;

        @BindView(R.id.percentValue)
        TextView percentValue;

        ViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}