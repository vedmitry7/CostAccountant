package com.vedmitryapps.costaccountant.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.vedmitryapps.costaccountant.App;
import com.vedmitryapps.costaccountant.R;
import com.vedmitryapps.costaccountant.models.Category;
import com.vedmitryapps.costaccountant.models.Day;
import com.vedmitryapps.costaccountant.models.RepeatingSpending;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;

public class RepeatingSpendingRecyclerAdapter extends RecyclerView.Adapter<RepeatingSpendingRecyclerAdapter.ViewHolder>{

    Context context;

    Realm realm;

    RealmResults<RepeatingSpending> categories;

    public RepeatingSpendingRecyclerAdapter() {
        realm = Realm.getDefaultInstance();

        categories = realm.where(RepeatingSpending.class).findAll();
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

    }

    // total number of rows
    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void update(Day day) {
        notifyDataSetChanged();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.categoryName)
        TextView name;

        ViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }
    }

}