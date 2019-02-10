package com.vedmitryapps.costaccountant;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;

import com.vedmitryapps.costaccountant.models.Day;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;

public class RemoveSpendingActivity extends AppCompatActivity {

    Calendar calendar;
    DateFormat dateFormatDB = new SimpleDateFormat("dd.MM.yyyy");
    DateFormat dateFormat;

    Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_spending);

        ButterKnife.bind(this);
        realm = Realm.getDefaultInstance();


        calendar = Calendar.getInstance();
        dateFormat  = android.text.format.DateFormat.getDateFormat(getApplicationContext());
    }

    void removeSpendingDay(String id){
        Day day = realm.where(Day.class).equalTo("id", id).findFirst();
        if(day != null){
            realm.beginTransaction();
            day.getList().clear();
            realm.commitTransaction();
        }
    }

    @OnClick(R.id.removeSpendingDay)
    public void removeDay(View v){

            int mYear = calendar.get(Calendar.YEAR);
            int mMonth = calendar.get(Calendar.MONTH);
            int mDay = calendar.get(Calendar.DAY_OF_MONTH);

            // инициализируем диалог выбора даты текущими значениями
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            calendar.set(year, monthOfYear, dayOfMonth);
                            AlertDialog alertDialog = new AlertDialog.Builder(RemoveSpendingActivity.this).create();
                            alertDialog.setMessage("Вы уверены что хотите очистить все траты на: " + dateFormat.format(calendar.getTime()) + "?");
                            //alertDialog.setMessage("Alert message to be shown");
                            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            removeSpendingDay(dateFormatDB.format(calendar.getTime()));
                                            dialog.dismiss();
                                            showDialogDataWasRemowed();
                                        }
                                    });
                            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                            alertDialog.show();

                        }
                    }, mYear, mMonth, mDay);

            datePickerDialog.show();
    }

    @OnClick(R.id.removeSpendingTerm)
    public void removeTerm(View v){
        final ArrayList<String> list = new ArrayList<>();

        android.support.v7.app.AlertDialog.Builder dialogBuilder = new android.support.v7.app.AlertDialog.Builder(RemoveSpendingActivity.this);
        //dialogBuilder.setTitle("Трата");
        LayoutInflater inflater = (LayoutInflater) RemoveSpendingActivity.this.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
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
                    list.add(dateFormatDB.format(calendarStart.getTime()));
                    for (;true;) {
                        calendarStart.add(Calendar.DAY_OF_MONTH, 1);
                        list.add(dateFormatDB.format(calendarStart.getTime()));
                        Log.i("TAG21", dateFormat.format(calendarStart.getTime()));

                        if(calendarStart.get(Calendar.DAY_OF_MONTH)==calendarEnd.get(Calendar.DAY_OF_MONTH)
                                && calendarStart.get(Calendar.MONTH)==calendarEnd.get(Calendar.MONTH)
                                && calendarStart.get(Calendar.YEAR)==calendarEnd.get(Calendar.YEAR)){
                            Log.i("TAG21", "Stop cycle");
                            break;
                        }
                    }

                } else {
                    if(calendarStart.get(Calendar.DAY_OF_MONTH)==calendarEnd.get(Calendar.DAY_OF_MONTH)
                            && calendarStart.get(Calendar.MONTH)==calendarEnd.get(Calendar.MONTH)
                            && calendarStart.get(Calendar.YEAR)==calendarEnd.get(Calendar.YEAR)){
                        Log.i("TAG21", "Add one day");
                        list.add(dateFormatDB.format(calendarStart.getTime()));
                    } else {
                        Toast.makeText(RemoveSpendingActivity.this, "Неверный формат дат", Toast.LENGTH_SHORT).show();
                    }
                }

                AlertDialog alertDialog = new AlertDialog.Builder(RemoveSpendingActivity.this).create();
                alertDialog.setMessage("Вы уверены что хотите очистить все траты на период с: "
                        + dateFormat.format(calendarStart2.getTime()) + " по "
                        + dateFormat.format(calendarEnd.getTime()) + "?");
                //alertDialog.setMessage("Alert message to be shown");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                for (String s:list
                                        ) {
                                    Log.i("TAG21", "Delete day " + s);

                                    removeSpendingDay(s);
                                }
                                showDialogDataWasRemowed();
                                dialog.dismiss();
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();


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

        final android.support.v7.app.AlertDialog b = dialogBuilder.create();
        b.show();
    }

    void showDialogDataWasRemowed(){
        AlertDialog alertDialog = new AlertDialog.Builder(RemoveSpendingActivity.this).create();
        alertDialog.setMessage("Затраты удалены");
        //alertDialog.setMessage("Alert message to be shown");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

}
