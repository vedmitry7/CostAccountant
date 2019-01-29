package com.vedmitryapps.costaccountant;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.vedmitryapps.costaccountant.adapters.RepeatingSpendingRecyclerAdapter;
import com.vedmitryapps.costaccountant.models.Category;
import com.vedmitryapps.costaccountant.models.DayPair;
import com.vedmitryapps.costaccountant.models.Product;
import com.vedmitryapps.costaccountant.models.RepeatingSpending;
import com.vedmitryapps.costaccountant.models.RepeatingSpendingType;
import com.vedmitryapps.costaccountant.models.UniqProduct;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RepeatingSpendingActivity extends AppCompatActivity {

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    TextView time;

    RepeatingSpendingRecyclerAdapter adapter;

    int hourOfDay = 9;
    int minute = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repeating_spending);
        ButterKnife.bind(this);


        adapter = new RepeatingSpendingRecyclerAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.setAdapter(adapter);

    }



    public void chooseTime(){
        TimePickerDialog dialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                RepeatingSpendingActivity.this.hourOfDay = hourOfDay;
                RepeatingSpendingActivity.this.minute = minute;
                time.setText(hourOfDay + ":"+minute);
            }
        }, 9,0, true);

        dialog.show();
    }

    @OnClick(R.id.bottomButton)
    public void bottomButton(View v){
        PopupMenu popupMenu = new PopupMenu(this, v);

        popupMenu.inflate(R.menu.type);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()){
                    case R.id.everyday:
                        showCreateDialog(RepeatingSpendingType.EVERYDAY.name());
                        break;
                    case R.id.everyweek:
                        showCreateDialog(RepeatingSpendingType.EVERYWEEK.name());
                        break;
                    case R.id.everymonth:
                        showCreateDialog(RepeatingSpendingType.EVERYMONTH.name());
                        break;
                }

                return false;
            }
        });

        popupMenu.show();
    }



    void showCreateDialog(String s){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        final View dialogView = inflater.inflate(R.layout.add_repeating_spending, null);

        dialogBuilder.setView(dialogView);
        dialogBuilder.setPositiveButton("Ok", null);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                App.closeKeyboard(getApplicationContext());
                dialog.dismiss();
            }
        });

        ConstraintLayout daysOfWeek = dialogView.findViewById(R.id.daysOfWeekContainer);
        ConstraintLayout dayOfMonth = dialogView.findViewById(R.id.dayOfMonthContainer);
        ConstraintLayout timeContainer = dialogView.findViewById(R.id.timeContainer);

        time = dialogView.findViewById(R.id.spendingTime);

        if(time == null){
            Log.i("TAG21", "time is null");
        } else {
            Log.i("TAG21", "time is not null");
        }

        timeContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseTime();
            }
        });




        if(s.equals(RepeatingSpendingType.EVERYDAY.name())){
            dialogBuilder.setTitle("Ежедневная трата");
            daysOfWeek.setVisibility(View.GONE);
            dayOfMonth.setVisibility(View.GONE);
        }
        if(s.equals(RepeatingSpendingType.EVERYWEEK.name())){
            dialogBuilder.setTitle("Ежеднедельная трата");
            dayOfMonth.setVisibility(View.GONE);
        }
        if(s.equals(RepeatingSpendingType.EVERYMONTH.name())){
            dialogBuilder.setTitle("Ежемесячная трата");
            daysOfWeek.setVisibility(View.GONE);
        }

        Spinner spinner = dialogView.findViewById(R.id.daySpinner);
        List<String> days = new ArrayList<>();
        for (int i = 0; i < 31; i++) {
           days.add(""+ (i+1));
        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(this,  android.R.layout.simple_spinner_dropdown_item, days);
        adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);


        final AlertDialog b = dialogBuilder.create();
        b.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {

                Button button = ((AlertDialog) b).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                    }
                });
            }
        });

        b.show();
        App.showKeyboard(getApplicationContext());

    }


}
