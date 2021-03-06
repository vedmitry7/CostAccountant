package com.vedmitryapps.costaccountant;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TextInputLayout;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.codingending.popuplayout.PopupLayout;
import com.vedmitryapps.costaccountant.adapters.DaysRecyclerAdapter;
import com.vedmitryapps.costaccountant.adapters.SpendingRecyclerAdapter;
import com.vedmitryapps.costaccountant.models.Category;
import com.vedmitryapps.costaccountant.models.Day;
import com.vedmitryapps.costaccountant.models.DayPair;
import com.vedmitryapps.costaccountant.models.Product;
import com.vedmitryapps.costaccountant.models.RepeatingSpending;
import com.vedmitryapps.costaccountant.models.RepeatingSpendingType;
import com.vedmitryapps.costaccountant.models.SpendingDay;
import com.vedmitryapps.costaccountant.models.UniqProduct;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity implements  NavigationView.OnNavigationItemSelectedListener{

    @BindView(R.id.dateTextView)
    TextView dateTextView;

    @BindView(R.id.dayCount)
    TextView dayCount;

    @BindView(R.id.additionalInfo)
    TextView additionalInfo;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.daysRecyclerView)
    RecyclerView daysRecyclerView;

    @BindView(R.id.navigationView)
    NavigationView navigationView;

    @BindView(R.id.drawerLayout)
    DrawerLayout drawerLayout;

    @BindView(R.id.spendingPlaceHolder)
    ConstraintLayout spendingPlaceHolder;


    Calendar calendar = Calendar.getInstance();
    String dateText;

    Realm realm;

    Day day;

    DateFormat dateFormat;
    DateFormat dateFormatDB = new SimpleDateFormat("dd.MM.yyyy");

    String currentDayId;

    SpendingRecyclerAdapter adapter;
    DaysRecyclerAdapter daysAdapter;

    boolean b;

    AlphaAnimation animation;
    AlphaAnimation animation1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        realm = Realm.getDefaultInstance();

        initAnimation();






        dateFormat  = android.text.format.DateFormat.getDateFormat(getApplicationContext());

        initDay(dateFormatDB.format(calendar.getTime()));

        adapter = new SpendingRecyclerAdapter(day);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);


        daysAdapter = new DaysRecyclerAdapter();
        final LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        daysRecyclerView.setLayoutManager(layoutManager);

        daysRecyclerView.setAdapter(daysAdapter);
        daysRecyclerView.scrollToPosition(27);


        daysRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {

                int first = layoutManager.findFirstVisibleItemPosition();
                int last = layoutManager.findLastVisibleItemPosition();
                Log.d("TAG21", "first - " + first);
                Log.d("TAG21", "last - " + last);
                Log.d("TAG21", "count - " + layoutManager.getItemCount());

                showInfo(daysAdapter.getItemByPosition(first));

                int total = layoutManager.getItemCount();


                if(total - last <10){
                    Log.d("TAG21", "add days end - ");
                    daysAdapter.addEndDays();
                }


                if(first <10){
                    Log.d("TAG21", "add days first - ");
                    daysAdapter.addStartDays();
                    if(!b){
                        b = true;
                    }
                }

                super.onScrolled(recyclerView, dx, dy);
            }
        });


        navigationView.setNavigationItemSelectedListener(this);



    }

    @Override
    protected void onResume() {
        super.onResume();
        checkRepeatingSpendings();

    }

    void showInfo(String dayId){
        Calendar calendar = Calendar.getInstance();

        try {
            Date date = dateFormatDB.parse(dayId);
            SimpleDateFormat format = new SimpleDateFormat("MMM yyyy");
            additionalInfo.setText(format.format(date));

            additionalInfo.startAnimation(animation);
            additionalInfo.startAnimation(animation1);

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    void initAnimation(){
        animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(100);
        animation.setFillAfter(true);

        animation1 = new AlphaAnimation(1.0f, 0.0f);
        animation1.setDuration(500);
        animation1.setStartOffset(500);
        animation1.setFillAfter(true);
    }


    private void checkRepeatingSpendings() {
      /*  ProgressDialog progressDialog = ProgressDialog.show(this, "Проверка", "");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER    );

        progressDialog.show();*/

        RealmResults<RepeatingSpending> spendings = realm.where(RepeatingSpending.class).findAll();

        Calendar calendarStart = Calendar.getInstance();
        Calendar calendarEnd = Calendar.getInstance();
        //calendarEnd.set(2019,1,21);
        Day day;

        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");


        Log.d("TAG21", "cHeck " + spendings.size());

        for (RepeatingSpending spending:spendings
                ) {
            Log.d("TAG21", "type - " + spending.getType());

            if(spending.getLastCheckDate()==null ){
                Log.d("TAG21", "add last check date" );
                realm.beginTransaction();
                spending.setLastCheckDate(spending.getStartDate());
                realm.commitTransaction();
            }

            calendarStart.set(
                    Util.year(spending.getLastCheckDate()),
                    Util.month(spending.getLastCheckDate()),
                    Util.day(spending.getLastCheckDate()));

            calendarStart.set(Calendar.MINUTE, 0);
            calendarStart.set(Calendar.HOUR_OF_DAY, 0);
            Log.d("TAG21", "Check enabled");

            if(!spending.isEnabled()){
                realm.beginTransaction();
                spending.setLastCheckDate(dateFormat.format(calendarEnd.getTime()));
                realm.commitTransaction();
                Log.d("TAG21", "not enabled. return");
                continue;
            }
            Log.d("TAG21", "continue work...");

            if(spending.getType().equals(RepeatingSpendingType.DAILY.name())){
                Log.d("TAG21", "Iteration/ last check" + spending.getLastCheckDate());
                Log.d("TAG21", "Iteration/ start day" + spending.getStartDate());

                for (;true;){
                    Log.d("TAG21", "inner iteration");

                    if(calendarStart.after(calendarEnd)){
                        Log.d("TAG21", "last check tomorrow. Break");
                        break;
                    } else {
                        Log.d("TAG21", "last check before");
                        realm.beginTransaction();

                        day = realm.where(Day.class).equalTo("id", dateFormat.format(calendarStart.getTime())).findFirst();

                        if(day==null){
                            Log.d("TAG21", dateFormat.format(calendarStart.getTime()) + " is null. create...");
                            day = realm.createObject(Day.class, dateFormat.format(calendarStart.getTime()));
                        }

                        DayPair dayPair = new DayPair();
                        dayPair.setId(Util.getNextDayPairId(realm));
                        dayPair.setProduct(spending.getProduct());
                        Log.d("TAG21", "day pair.add product" + spending.getProduct().getId());
                        dayPair.setPrice(spending.getPrice());
                        day.getList().add(dayPair);

                        Log.d("TAG21", "add day");
                        calendarStart.add(Calendar.DAY_OF_MONTH, 1);
                        spending.setLastCheckDate(dateFormat.format(calendarStart.getTime()));
                        realm.commitTransaction();
                    }
                }
            }
            if(spending.getType().equals(RepeatingSpendingType.WEEKLY.name())){

                RealmList<SpendingDay> list = spending.getDays();
                Log.d("TAG21", "list size: " + list.size());

                for (int i = 0; i < list.size(); i++) {
                    Log.d("TAG21", "DAY " + list.get(i).getDay());
                }

                Log.d("TAG21", "Iteration/ last check" + spending.getLastCheckDate());
                Log.d("TAG21", "Iteration/ start day" + spending.getStartDate());


                for (;true;){
                    Log.d("TAG21", "inner iteration");

                    if(calendarStart.after(calendarEnd)){
                        Log.d("TAG21", "last check tomorrow. Break");
                        break;
                    } else {
                        Log.d("TAG21", "last check before");
                        realm.beginTransaction();

                        day = realm.where(Day.class).equalTo("id", dateFormat.format(calendarStart.getTime())).findFirst();

                        if(day==null){
                            Log.d("TAG21", dateFormat.format(calendarStart.getTime()) + " is null. create...");
                            day = realm.createObject(Day.class, dateFormat.format(calendarStart.getTime()));
                        }

                        int dayOfWeek = calendarStart.get(Calendar.DAY_OF_WEEK);

                        Log.d("TAG21", "dayOfWeek = " + dayOfWeek);

                        for (SpendingDay d:list
                             ) {
                            Log.d("TAG21", "day - " + d.getDay());

                            if(d.getDay() == dayOfWeek){

                                DayPair dayPair = new DayPair();
                                dayPair.setId(Util.getNextDayPairId(realm));
                                dayPair.setProduct(spending.getProduct());
                                Log.d("TAG21", "day pair.add product" + spending.getProduct().getId());
                                dayPair.setPrice(spending.getPrice());
                                day.getList().add(dayPair);
                            }
                        }

                        Log.d("TAG21", "add day");
                        calendarStart.add(Calendar.DAY_OF_MONTH, 1);
                        spending.setLastCheckDate(dateFormat.format(calendarStart.getTime()));
                        realm.commitTransaction();
                    }
                }
            }

            if(spending.getType().equals(RepeatingSpendingType.MONTHLY.name())){
                RealmList<SpendingDay> list = spending.getDays();
                Log.d("TAG21", "list size: " + list.size());

                for (int i = 0; i < list.size(); i++) {
                    Log.d("TAG21", "DAY " + list.get(i).getDay());
                }

                Log.d("TAG21", "Iteration/ last check" + spending.getLastCheckDate());
                Log.d("TAG21", "Iteration/ start day" + spending.getStartDate());


                for (;true;){
                    Log.d("TAG21", "inner iteration");

                    if(calendarStart.after(calendarEnd)){
                        Log.d("TAG21", "last check tomorrow. Break");
                        break;
                    } else {
                        Log.d("TAG21", "last check before");
                        realm.beginTransaction();

                        day = realm.where(Day.class).equalTo("id", dateFormat.format(calendarStart.getTime())).findFirst();

                        if(day==null){
                            Log.d("TAG21", dateFormat.format(calendarStart.getTime()) + " is null. create...");
                            day = realm.createObject(Day.class, dateFormat.format(calendarStart.getTime()));
                        }

                        int dayOfMonth = calendarStart.get(Calendar.DAY_OF_MONTH);

                        Log.d("TAG21", "dayOfWeek = " + dayOfMonth);

                        for (SpendingDay d:list
                                ) {
                            Log.d("TAG21", "day - " + d.getDay());

                            if(d.getDay() == dayOfMonth){

                                DayPair dayPair = new DayPair();
                                dayPair.setId(Util.getNextDayPairId(realm));
                                dayPair.setProduct(spending.getProduct());
                                Log.d("TAG21", "day pair.add product" + spending.getProduct().getId());
                                dayPair.setPrice(spending.getPrice());
                                day.getList().add(dayPair);
                            }
                        }

                        Log.d("TAG21", "add day");
                        calendarStart.add(Calendar.DAY_OF_MONTH, 1);
                        spending.setLastCheckDate(dateFormat.format(calendarStart.getTime()));
                        realm.commitTransaction();
                    }
                }


                for (;true;){
                    Log.d("TAG21", "inner iteration");

                    if(calendarStart.after(calendarEnd)){
                        Log.d("TAG21", "last check tomorrow. Break");
                        break;
                    } else {
                        Log.d("TAG21", "last check before");
                        realm.beginTransaction();




                        day = realm.where(Day.class).equalTo("id", dateFormat.format(calendarStart.getTime())).findFirst();

                        if(day==null){
                            Log.d("TAG21", dateFormat.format(calendarStart.getTime()) + " is null. create...");
                            day = realm.createObject(Day.class, dateFormat.format(calendarStart.getTime()));
                        }

                        int dayOfMonth = calendarStart.get(Calendar.DAY_OF_MONTH);

                        Log.d("TAG21", "dayOfWeek = " + dayOfMonth);

                        for (SpendingDay d:list
                                ) {
                            Log.d("TAG21", "day - " + d.getDay());

                            if(d.getDay() == dayOfMonth){

                                DayPair dayPair = new DayPair();
                                dayPair.setId(Util.getNextDayPairId(realm));
                                dayPair.setProduct(spending.getProduct());
                                Log.d("TAG21", "day pair.add product" + spending.getProduct().getId());
                                dayPair.setPrice(spending.getPrice());
                                day.getList().add(dayPair);
                            }
                        }

                        Log.d("TAG21", "add day");
                        calendarStart.add(Calendar.DAY_OF_MONTH, 1);
                        spending.setLastCheckDate(dateFormat.format(calendarStart.getTime()));
                        realm.commitTransaction();
                    }
                }
            }
        }

    }

    private void initDay(String dayId) {
        day = realm.where(Day.class).equalTo("id", dayId).findFirst();
        if(day==null){
            Log.d("TAG21", dateText + " is null. create...");
            Log.d("TAG21", "beginTransaction()");
            realm.beginTransaction();
            day = realm.createObject(Day.class, dayId);
            Log.d("TAG21", "commitTransaction()");
            realm.commitTransaction();
        }

        currentDayId = dayId;

        if(adapter!=null)
        adapter.update(day);

        if(day.getList().size()==0){
            spendingPlaceHolder.setVisibility(View.VISIBLE);
        } else {
            spendingPlaceHolder.setVisibility(View.GONE);
        }

        calendar.set(Util.year(dayId), Util.month(dayId), Util.day(dayId));
        dayCount.setText("Всего: " + Util.countDayPriceString(day));

        showDate();
    }

    private void showDate(){
        // DateFormat dateFormatDB = new SimpleDateFormat("dd.MM.yyyy");

        dateText = dateFormat.format(calendar.getTime());
        dateTextView.setText(dateText);
    }

    @OnClick(R.id.imageButton3)
    public void imageButton3(View v){
        Intent intent = new Intent(this, DiagramActivity.class);
        startActivity(intent);

    }


    @OnClick(R.id.bottomButton)
    public void bottomButton(View v){
        showCreateOrChangeDialog2(null);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void clickDay(final Events.ClickDay event) {
        initDay(event.getDayId());
    }


   /* public void showCreateOrChangeDialog42(final Events.ClickProduct event) {

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
        final View dialogView = inflater.inflate(R.layout.add_product_dialog, null);

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

        final RealmResults<UniqProduct> products = realm.where(UniqProduct.class).findAll();
        Log.d("TAG21", "count - " + products.size());
        ArrayList<String> productsName = new ArrayList<>();
        for (UniqProduct p:products
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
                            p = realm.createObject(Product.class, Util.getNextProductId(realm));
                        } else {
                            p = realm.createObject(Product.class, Util.getNextProductId(realm));
                        }

                        if(c==null){
                            c = realm.createObject(Category.class, Util.getTrimString(categoryNameEditText.getText().toString()));
                        }
                        Log.i("TAG21", "category - " + c.getName());
                        p.setName(Util.getTrimString(productNameEditText.getText().toString()));
                        p.setCategory(c);
                        p.setCategoryName(c.getName());
                        p.setUseDefPrice(rememberPriceCheckBox.isChecked());
                        if(rememberPriceCheckBox.isChecked()){
                            p.setDefPrice(price);
                        } else {
                            //p.setDefPrice();
                        }
                        if(event!=null){
                            DayPair pair = new DayPair();
                            pair.setId(Util.getNextDayPairId(realm));
                            pair.setProduct(p);
                            pair.setPrice(price);
                            day.getList().add(event.getPosition(), pair);
                            day.getList().remove(event.getPosition()+1);
                        } else {
                            DayPair pair = new DayPair();
                            pair.setId(Util.getNextDayPairId(realm));
                            pair.setProduct(p);
                            pair.setPrice(price);
                            day.getList().add(pair);
                        }
                        realm.commitTransaction();


                        dayCount.setText("Всего: " + Util.countDayPrice(day));
                        b.dismiss();
                        closeKeyboard();
                        adapter.notifyDataSetChanged();
                        daysAdapter.notifyDataSetChanged();
                    }
                });
            }
        });

        b.show();
        showKeyboard();
    }*/

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showCreateOrChangeDialog2(final Events.ClickProduct event) {

        PopupLayout popupLayout=PopupLayout.init(MainActivity.this, R.layout.add_product_dialog);
        popupLayout.show(PopupLayout.POSITION_TOP);
      //  popupLayout.show();



        final UniqProduct[] uniqProducts = new UniqProduct[1];
        final boolean[] categoryWasFilledFirst = {false};
        final Category[] category = new Category[1];
        final Product[] product = new Product[1];


        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        final View dialogView = inflater.inflate(R.layout.add_product_dialog, null);

        dialogBuilder.setView(dialogView);
        dialogBuilder.setPositiveButton("Ok", null);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                App.closeKeyboard(getApplicationContext());
                dialog.dismiss();
            }
        });

        /**
         * Init Views
         */
        final TextInputLayout productContainer =  dialogView.findViewById(R.id.containerProductEditText);
        final AutoCompleteTextView productNameEditText = dialogView.findViewById(R.id.productEditText);
        final AutoCompleteTextView categoryNameEditText = dialogView.findViewById(R.id.categoryEditText);
        final EditText priceEditText = dialogView.findViewById(R.id.priceEditText);
        final CheckBox rememberPriceCheckBox = dialogView.findViewById(R.id.rememberPriceCheckBox);

        /**
         * Find all uniqProducts and category. Fill Spinners
         */
        final RealmResults<UniqProduct> products = realm.where(UniqProduct.class).findAll();
        ArrayList<String> productsName = new ArrayList<>();
        for (UniqProduct p:products
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

        /**
         *    Text Change Listeners
         * */
        productNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                uniqProducts[0] = null;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d("TAG21", "uniqProducts enter - " + s + ".");

                uniqProducts[0] = realm.where(UniqProduct.class).equalTo("name", Util.getTrimString(s.toString())).findFirst();

                if(uniqProducts[0]!=null){
                    Log.d("TAG21", "Product found");
                    // fill price!
                    rememberPriceCheckBox.setChecked(uniqProducts[0].isUseDefPrice());

                    if(uniqProducts[0].getCategory()!=null){
                        Log.d("TAG21", "Product category found");
                        if(event==null){
                            if(!categoryWasFilledFirst[0])
                                categoryNameEditText.setText(uniqProducts[0].getCategory().getName());
                            rememberPriceCheckBox.setChecked(uniqProducts[0].isUseDefPrice());
                            if(uniqProducts[0].isUseDefPrice()){
                                priceEditText.setText(String.valueOf(uniqProducts[0].getDefPrice()));
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

        /**
         *      Spinner Item Click
         * */

        productNameEditText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(uniqProducts[0]!=null && uniqProducts[0].getCategory()!=null){
                    Log.i("TAG21", "");
                    priceEditText.requestFocus();
                    priceEditText.setSelection(priceEditText.getText().length());
                } else {
                    categoryNameEditText.requestFocus();
                }
            }
        });

        /**
         *  Enter key click listener
         * */

        productNameEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    Log.i("TAG21", "Enter");
                    if(uniqProducts[0]!=null && uniqProducts[0].getCategory()!=null){
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


        /**
         *      Init here because listeners set
         * */

        if(event!=null){
            product[0] = day.getList().get(event.getPosition()).getProduct();
            category[0] = day.getList().get(event.getPosition()).getProduct().getCategory();
            productNameEditText.setText(product[0].getName());
            productNameEditText.setSelection(product[0].getName().length());
            productNameEditText.dismissDropDown();
            priceEditText.setText(String.valueOf(day.getList().get(event.getPosition()).getPrice()));
            categoryNameEditText.setText(product[0].getCategory().getName());
        }

        /**
         *      OK click listener
         * */
        final AlertDialog b = dialogBuilder.create();
        if(event!=null)
        b.setButton(AlertDialog.BUTTON_NEUTRAL, "Удалить", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                App.closeKeyboard(MainActivity.this);

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
                dialogBuilder.setMessage("Вы уверены что хотите удалить " + product[0].getName() + " из затрат на " + dateFormat.format(calendar.getTime()) + "?");
                //alertDialog.setMessage("Alert message to be shown");
                dialogBuilder.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                realm.beginTransaction();
                                day.getList().remove(event.getPosition());
                                realm.commitTransaction();
                                adapter.notifyDataSetChanged();
                                dialog.dismiss();
                            }
                        });
                dialogBuilder.setNegativeButton( "Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                dialogBuilder.show();

            }});
        b.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {

                Button button = ((AlertDialog) b).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if(Util.getTrimString(productNameEditText.getText().toString()).length()==0){
                            productContainer.setError("Поле не может быть пустым");
                            return;
                        }

                        UniqProduct p = uniqProducts[0];
                        Category c = category[0];
                        float price;

                        if(!priceEditText.getText().toString().equals("")){
                            price = Float.parseFloat(priceEditText.getText().toString());
                        } else {
                            price = 0;
                        }

                        realm.beginTransaction();
                        if(p==null){
                            p = realm.createObject(UniqProduct.class, Util.getTrimString(productNameEditText.getText().toString()));
                        }

                        if(c==null){
                            c = realm.createObject(Category.class, Util.getNextCategoryId(realm));
                            c.setName(Util.getTrimString(categoryNameEditText.getText().toString()));
                        }

                        Log.i("TAG21", "category - " + c.getName());
                        p.setCategory(c);
                        p.setCategoryName(c.getName());
                        p.setUseDefPrice(rememberPriceCheckBox.isChecked());
                        if(rememberPriceCheckBox.isChecked()){
                            p.setDefPrice(price);
                        }

                        DayPair pair = new DayPair();
                        pair.setId(Util.getNextDayPairId(realm));

                        Product product = realm.createObject(Product.class, Util.getNextProductId(realm));
                        product.setName(p.getName());
                        product.setCategory(p.getCategory());
                        pair.setProduct(product);
                        pair.setPrice(price);

                        if(event!=null){
                            day.getList().add(event.getPosition(), pair);
                            day.getList().remove(event.getPosition()+1);
                        } else {
                            day.getList().add(pair);
                        }
                        realm.commitTransaction();

                        initDay(currentDayId);
                        b.dismiss();
                        App.closeKeyboard(getApplicationContext());
                        daysAdapter.notifyDataSetChanged();
                    }
                });
            }
        });

        b.show();
        App.showKeyboard(getApplicationContext());
    }

    @OnClick(R.id.menuButton)
    public void menu(View v){
        drawerLayout.openDrawer(navigationView);
    }

    @OnClick(R.id.dateTextView)
    public void chooseDate(View v){

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

            case R.id.categories:
                Intent intent1 = new Intent(this, CategoriesActivity.class);
                startActivity(intent1);
                drawerLayout.closeDrawer(navigationView);
                break;
            case R.id.repeating_spending:
                Intent intent2 = new Intent(this, RepeatingSpendingActivity.class);
                startActivity(intent2);
                drawerLayout.closeDrawer(navigationView);
                break;
            case R.id.remove_spending:
                Intent intent3 = new Intent(this, RemoveSpendingActivity.class);
                startActivity(intent3);
                drawerLayout.closeDrawer(navigationView);
                break;
            case R.id.settings:
                Intent intent4 = new Intent(this, SettingsActivity.class);
                startActivity(intent4);
                drawerLayout.closeDrawer(navigationView);
                break;
        }
        return true;
    }
}
