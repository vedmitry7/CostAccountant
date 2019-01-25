package com.vedmitryapps.costaccountant;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import com.vedmitryapps.costaccountant.models.Category;
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
import io.realm.RealmList;
import io.realm.RealmResults;

public class CategoriesRecyclerAdapter extends RecyclerView.Adapter<CategoriesRecyclerAdapter.ViewHolder>{

    Context context;

    Realm realm;

    RealmResults<Category> categories;

    public CategoriesRecyclerAdapter() {
        realm = Realm.getDefaultInstance();

        categories = realm.where(Category.class).findAll();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(context==null){
            context = parent.getContext();
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.name.setText(categories.get(position).getName());

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


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
                    LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                    final View dialogView = inflater.inflate(R.layout.rename_category_dialog, null);

                    dialogBuilder.setView(dialogView);

                    final TextInputLayout container =  dialogView.findViewById(R.id.containerEditText);


                    final EditText editText = dialogView.findViewById(R.id.editText);
                    editText.setText(categories.get(getAdapterPosition()).getName());
                    editText.setSelection(categories.get(getAdapterPosition()).getName().length());


                    dialogBuilder.setMessage("Изменить название");

                    dialogBuilder.setNegativeButton("Отмена", null);
                    dialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            realm.beginTransaction();
                            if(editText.getText().length()<2){
                                container.setError("Ахуел?");
                            } else {
                                categories.get(getAdapterPosition()).setName(editText.getText().toString());
                                notifyItemChanged(getAdapterPosition());
                            }
                            realm.commitTransaction();
                            App.closeKeyboard(context);

                        }
                    });

                    App.showKeyboard(context);
                    dialogBuilder.create().show();
                }
            });
        }
    }

}