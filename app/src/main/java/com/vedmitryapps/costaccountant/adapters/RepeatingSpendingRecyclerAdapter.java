package com.vedmitryapps.costaccountant.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vedmitryapps.costaccountant.R;
import com.vedmitryapps.costaccountant.models.Day;
import com.vedmitryapps.costaccountant.models.RepeatingSpending;

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.repeating_spending_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.type.setText(list.get(position).getType());
        holder.product.setText(list.get(position).getProduct().getName());
        holder.category.setText(list.get(position).getProduct().getCategory().getName());
        holder.price.setText("" + list.get(position).getPrice());

    }

    // total number of rows
    @Override
    public int getItemCount() {
        return list.size();
    }

    public void update(Day day) {
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

        ViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }
    }

}