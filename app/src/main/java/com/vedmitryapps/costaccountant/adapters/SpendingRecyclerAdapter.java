package com.vedmitryapps.costaccountant.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vedmitryapps.costaccountant.Events;
import com.vedmitryapps.costaccountant.R;
import com.vedmitryapps.costaccountant.Util;
import com.vedmitryapps.costaccountant.models.Day;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SpendingRecyclerAdapter extends RecyclerView.Adapter<SpendingRecyclerAdapter.ViewHolder>{

    Context context;
    Day day;

    public SpendingRecyclerAdapter(Day day) {
        this.day = day;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(context==null){
            context = parent.getContext();
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.spending_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        holder.productName.setText(day.getList().get(position).getProduct().getName() + " " + day.getList().get(position).getProduct().getId());
        holder.productPrice.setText(Util.floatToString(day.getList().get(position).getPrice()));

        holder.productCategory.setText(day.getList().get(position).getProduct().getCategory().getName());
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return day.getList().size();
    }

    public void update(Day day) {
        this.day = day;
        notifyDataSetChanged();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.productName)
        TextView productName;

        @BindView(R.id.productPrice)
        TextView productPrice;

        @BindView(R.id.productCategory)
        TextView productCategory;

        ViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EventBus.getDefault().post(new Events.ClickProduct(getAdapterPosition()));
                }
            });
        }
    }

}