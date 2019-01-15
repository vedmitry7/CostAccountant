package com.vedmitryapps.costaccountant;

import android.app.DatePickerDialog;
import android.app.usage.UsageEvents;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TextInputLayout;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.github.badoualy.datepicker.DatePickerTimeline;
import com.github.badoualy.datepicker.MonthView;
import com.github.badoualy.datepicker.TimelineView;
import com.shrikanthravi.collapsiblecalendarview.widget.CollapsibleCalendar;
import com.vedmitryapps.costaccountant.models.Category;
import com.vedmitryapps.costaccountant.models.Day;
import com.vedmitryapps.costaccountant.models.DayPair;
import com.vedmitryapps.costaccountant.models.Product;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity implements  NavigationView.OnNavigationItemSelectedListener{

    @BindView(R.id.dateTextView)
    TextView dateTextView;

    @BindView(R.id.dayCount)
    TextView dayCount;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.navigationView)
    NavigationView navigationView;

    @BindView(R.id.drawerLayout)
    DrawerLayout drawerLayout;

    @BindView(R.id.datePickerTimeline)
    DatePickerTimeline timeLine;

    @BindView(R.id.calendarView)
   CollapsibleCalendar  viewCalendar;

    Calendar calendar = Calendar.getInstance();
    String dateText;

    Realm realm;

    Day day;

    SpendingRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        realm = Realm.getDefaultInstance();

        showDate();
        initDay();

        adapter = new SpendingRecyclerAdapter(day);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);


        navigationView.setNavigationItemSelectedListener(this);

        timeLine.setDateLabelAdapter(new MonthView.DateLabelAdapter() {
            @Override
            public CharSequence getLabel(Calendar calendar, int index) {
               // return Integer.toString(calendar.get(Calendar.MONTH) + 1) + "/" + (calendar.get(Calendar.YEAR) % 2000);
                return "250";
            }
        });

        timeLine.setOnDateSelectedListener(new DatePickerTimeline.OnDateSelectedListener() {
            @Override
            public void onDateSelected(int year, int month, int day, int index) {
                Log.d("TAG21", "y"+year+"m"+month+"d"+day+"i"+index);


            }
        });


        final CollapsibleCalendar collapsibleCalendar = findViewById(R.id.calendarView);
        collapsibleCalendar.setShowWeek(false);
        collapsibleCalendar.callOnClick();
        collapsibleCalendar.setCalendarListener(new CollapsibleCalendar.CalendarListener() {
            @Override
            public void onDaySelect() {
                com.shrikanthravi.collapsiblecalendarview.data.Day day = viewCalendar.getSelectedDay();
                Log.i("TAG21", "Selected Day: "
                        + day.getYear() + "/" + (day.getMonth() + 1) + "/" + day.getDay());
            }

            @Override
            public void onItemClick(View view) {

            }

            @Override
            public void onDataUpdate() {

            }

            @Override
            public void onMonthChange() {

            }

            @Override
            public void onWeekChange(int i) {

            }
        });
    }

    private void initDay() {
        day = realm.where(Day.class).equalTo("id", dateText).findFirst();
        if(day==null){
            Log.d("TAG21", dateText + " is null. create...");

            realm.beginTransaction();
            day = realm.createObject(Day.class, dateText);
            realm.commitTransaction();
        }
        dayCount.setText("Всего: " + Util.countDayPrice(day));

    }

    private void showDate(){
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        Date date = calendar.getTime();
        dateText = dateFormat.format(date);
        dateTextView.setText(dateText);
    }

    @OnClick(R.id.rightButton)
    public void right(View v){
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        showDate();
        initDay();
        adapter.update(day);
    }

    @OnClick(R.id.leftButton)
    public void left(View v){
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        showDate();
        initDay();
        adapter.update(day);

    }


    @OnClick(R.id.imageButton3)
    public void imageButton3(View v){
        Intent intent = new Intent(this, DiagramActivity.class);
        startActivity(intent);

    }

    public void showKeyboard(){
        InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    @OnClick(R.id.bottomButton)
    public void bottomButton(View v){
        showCreateOrChangeDialog(null);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showCreateOrChangeDialog(final Events.ClickProduct event) {

        final boolean[] categoryWasFilledFirst = {false};

        final Product[] product = new Product[1];
        final Category[] category = new Category[1];

        if(event!=null){
            product[0] = day.getList().get(event.getPosition()).getProduct();
            category[0] = day.getList().get(event.getPosition()).getProduct().getCategory();
        }

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        //dialogBuilder.setTitle("Трата");
        LayoutInflater inflater = (LayoutInflater) this.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        final View dialogView = inflater.inflate(R.layout.add_roduct_ialog, null);

        dialogBuilder.setView(dialogView);

        dialogBuilder.setPositiveButton("Ok", null);

        dialogBuilder.setCancelable(false);

        final TextInputLayout productContainer =  dialogView.findViewById(R.id.containerProductEditText);
        final AutoCompleteTextView productNameEditText = dialogView.findViewById(R.id.productEditText);
        final AutoCompleteTextView categoryNameEditText = dialogView.findViewById(R.id.categoryEditText);
        final EditText priceEditText = dialogView.findViewById(R.id.priceEditText);

        final CheckBox rememberPriceCheckBox = dialogView.findViewById(R.id.rememberPriceCheckBox);

        productNameEditText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(product[0]!=null && product[0].getCategory()!=null){
                    Log.i("TAG21", "");
                    priceEditText.requestFocus();
                    priceEditText.setSelection(priceEditText.getText().length());
                } else {
                    categoryNameEditText.requestFocus();
                }
            }
        });

        final RealmResults<Product> products = realm.where(Product.class).findAll();
        Log.d("TAG21", "count - " + products.size());
        ArrayList<String> productsName = new ArrayList<>();
        for (Product p:products
                ) {
            productsName.add(p.getName());
        }
        productNameEditText.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, productsName));

        final RealmResults<Category> categories  = realm.where(Category.class).findAll();
        Log.d("TAG21", "count - " + products.size());
        ArrayList<String> categoriesName = new ArrayList<>();
        for (Category p:categories
                ) {
            categoriesName.add(p.getName());
        }
        categoryNameEditText.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, categoriesName));

        productNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                product[0] = null;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d("TAG21", "product enter - " + s + ".");

                product[0] = realm.where(Product.class).equalTo("name", Util.getTrimString(s.toString())).findFirst();

                if(product[0]!=null){
                    Log.d("TAG21", "Product found");
                    // fill price!

                    if(product[0].getCategory()!=null){
                        Log.d("TAG21", "Product category found");
                        if(event==null){
                            if(!categoryWasFilledFirst[0])
                            categoryNameEditText.setText(product[0].getCategory().getName());
                            rememberPriceCheckBox.setChecked(product[0].isUseDefPrice());
                            if(product[0].isUseDefPrice()){
                                priceEditText.setText(String.valueOf(product[0].getDefPrice()));
                            }
                        }
                    } else {
                        Log.d("TAG21", "Product category not found");
                    }
                } else {
                    Log.d("TAG21", "Product not found");
                    if(event==null) {
                        if(!categoryWasFilledFirst[0])
                            categoryNameEditText.setText("");
                    }
                }
            }
        });

        categoryNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                category[0] = null;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d("TAG21", "category enter - " + s + ".");

                if(productNameEditText.getText().length() == 0){
                    categoryWasFilledFirst[0] = true;
                    Log.d("TAG21", "Category was filled first");
                }

                category[0] = realm.where(Category.class).equalTo("name", Util.getTrimString(s.toString())).findFirst();

                if(category[0]!=null){
                    Log.d("TAG21", "Category found");
                    // fill price!
                }
            }
        });

        dialogBuilder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                closeKeyboard();
                dialog.dismiss();
            }
        });

        productNameEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    Log.i("TAG21", "Enter");
                    if(product[0]!=null && product[0].getCategory()!=null){
                        Log.i("TAG21", "");
                        priceEditText.requestFocus();
                        priceEditText.setSelection(priceEditText.getText().length());
                    } else {
                        categoryNameEditText.requestFocus();
                    }
                    return true;
                }
                return true;
            }
        });

        categoryNameEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    Log.i("TAG21", "Enter - " + priceEditText.getText().length());
                    priceEditText.requestFocus();
                    priceEditText.setSelection(priceEditText.getText().length());
                    return true;
                }
                return true;
            }
        });

            if(product[0]!=null){
                productNameEditText.setText(product[0].getName());
                productNameEditText.setSelection(product[0].getName().length());
                productNameEditText.dismissDropDown();
                priceEditText.setText(String.valueOf(day.getList().get(event.getPosition()).getPrice()));
                categoryNameEditText.setText(product[0].getCategory().getName());
                rememberPriceCheckBox.setChecked(product[0].isUseDefPrice());
            }




        final AlertDialog b = dialogBuilder.create();

        b.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {

                Button button = ((AlertDialog) b).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        // TODO Do something


                        if(Util.getTrimString(productNameEditText.getText().toString()).length()==0){
                            productContainer.setError("Поле не может быть пустым");
                            return;
                        }

                        Product p = product[0];
                        Category c = category[0];
                        float price;

                        if(!priceEditText.getText().toString().equals("")){
                            price = Float.parseFloat(priceEditText.getText().toString());
                        } else {
                            price = 0;
                        }

                        realm.beginTransaction();
                        if(p==null){
                            p = realm.createObject(Product.class, Util.getTrimString(productNameEditText.getText().toString()));
                        }

                        if(c==null){
                            c = realm.createObject(Category.class, Util.getTrimString(categoryNameEditText.getText().toString()));
                        }
                        Log.i("TAG21", "category - " + c.getName());

                        p.setCategory(c);
                        p.setCategoryName(c.getName());
                        p.setUseDefPrice(rememberPriceCheckBox.isChecked());
                        if(rememberPriceCheckBox.isChecked()){
                            p.setDefPrice(price);
                        } else {
                            //p.setDefPrice();
                        }
                        if(event!=null){
                            day.getList().add(event.getPosition(), new DayPair(p, price));
                            day.getList().remove(event.getPosition()+1);
                        } else {
                            day.getList().add(new DayPair(p, price));
                        }
                        realm.commitTransaction();


                        dayCount.setText("Всего: " + Util.countDayPrice(day));
                        b.dismiss();
                        closeKeyboard();
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });

        b.show();
        showKeyboard();
    }

    @OnClick(R.id.shooseDate)
    public void shooseDate(View v){


        /* AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(DiagramActivity.this);
                        //dialogBuilder.setTitle("Трата");
                        LayoutInflater inflater = (LayoutInflater) DiagramActivity.this.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                        final View dialogView = inflater.inflate(R.layout.choose_custom_term_dialog, null);

                        dialogBuilder.setView(dialogView);

                        dialogBuilder.setPositiveButton("Ок", null);
                        dialogBuilder.setNegativeButton("Отмена", null);

                        dialogBuilder.setCancelable(false);

                        final AlertDialog b = dialogBuilder.create();
                        b.show();*/

      /*  DatePickerDialog.OnDateSetListener myCallBack = new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                dateText = dayOfMonth+"."+(monthOfYear+1)+"."+year;
                Log.i("TAG21", "day  " + dateText);
                initDay();
              //  tvDate.setText("Today is " + myDay + "/" + myMonth + "/" + myYear);
            }
        };
        DatePickerDialog tpd = new DatePickerDialog(this, myCallBack, 2018, 11, 30);
        tpd.show();*/

        final Calendar cal = Calendar.getInstance();
        int mYear = cal.get(Calendar.YEAR);
        int mMonth = cal.get(Calendar.MONTH);
        int mDay = cal.get(Calendar.DAY_OF_MONTH);

        // инициализируем диалог выбора даты текущими значениями
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        String editTextDateParam = dayOfMonth + "." + (monthOfYear + 1) + "." + year;
                    }
                }, mYear, mMonth, mDay);

        datePickerDialog.show();
    }

    public void closeKeyboard(){
        InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }


    @Override
    protected void onStart() {
        EventBus.getDefault().register(this);
        super.onStart();
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.statistic:
                Intent intent = new Intent(this, DiagramActivity.class);
                startActivity(intent);
                drawerLayout.closeDrawer(navigationView);
                break;

        }
        return true;
    }
}
