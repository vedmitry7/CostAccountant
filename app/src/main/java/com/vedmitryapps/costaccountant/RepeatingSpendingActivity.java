package com.vedmitryapps.costaccountant;

import android.animation.Animator;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.vedmitryapps.costaccountant.adapters.RepeatingSpendingRecyclerAdapter;
import com.vedmitryapps.costaccountant.models.Category;
import com.vedmitryapps.costaccountant.models.Product;
import com.vedmitryapps.costaccountant.models.RepeatingSpending;
import com.vedmitryapps.costaccountant.models.RepeatingSpendingType;
import com.vedmitryapps.costaccountant.models.SpendingDay;
import com.vedmitryapps.costaccountant.models.UniqProduct;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class RepeatingSpendingActivity extends AppCompatActivity {

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.rootView)
    ConstraintLayout rootView;

    @BindView(R.id.container)
    ConstraintLayout container;

    @BindView(R.id.bottomButton)
    ConstraintLayout bottomButton;

    TextView time;
    TextView startDateTextView;

    RepeatingSpendingRecyclerAdapter adapter;

    int hourOfDay = 9;
    int minute = 0;

    String dateId;
    Calendar calendar;

    DateFormat dateFormat;


    Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repeating_spending);
        ButterKnife.bind(this);
        realm = Realm.getDefaultInstance();

        dateFormat  = android.text.format.DateFormat.getDateFormat(getApplicationContext());


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
                RepeatingSpendingActivity.this.hourOfDay = hourOfDay;
                RepeatingSpendingActivity.this.minute = minute;
            }
        }, 9,0, true);

        dialog.show();
    }

    public void chooseStartDate(){

        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH);
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);

        // инициализируем диалог выбора даты текущими значениями
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        calendar.set(year,monthOfYear,dayOfMonth);
                        startDateTextView.setText(dateFormat.format(calendar.getTime()));
                    }
                }, mYear, mMonth, mDay);

        datePickerDialog.show();
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
                        App.showKeyboard(getApplicationContext());
                        container.setVisibility(View.VISIBLE);
                        bottomButton.setVisibility(View.GONE);
                       // showCreateDialog(RepeatingSpendingType.DAILY.name(), null);
                        break;
                    case R.id.everyweek:
                        showCreateDialog(RepeatingSpendingType.WEEKLY.name(), null);
                        break;
                    case R.id.everymonth:
                        showCreateDialog(RepeatingSpendingType.MONTHLY.name(), null);
                        break;
                }

                return false;
            }
        });

        popupMenu.show();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    void showCreateDialog(final String s, final Events.ClickProduct event){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        final View dialogView = inflater.inflate(R.layout.add_repeating_spending, null);

        //ButterKnife.bind(this, dialogView);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setPositiveButton("Ok", null);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                App.closeKeyboard(getApplicationContext());
                dialog.dismiss();
            }
        });

        final CheckBox mon = dialogView.findViewById(R.id.checkBoxMon);
        final CheckBox tue = dialogView.findViewById(R.id.checkBoxTue);
        final CheckBox wed = dialogView.findViewById(R.id.checkBoxWed);
        final CheckBox thu = dialogView.findViewById(R.id.checkBoxThu);
        final CheckBox fri = dialogView.findViewById(R.id.checkBoxFri);
        final CheckBox sat = dialogView.findViewById(R.id.checkBoxSat);
        final CheckBox sun = dialogView.findViewById(R.id.checkBoxSun);


        ConstraintLayout daysOfWeek = dialogView.findViewById(R.id.daysOfWeekContainer);
        ConstraintLayout dayOfMonth = dialogView.findViewById(R.id.dayOfMonthContainer);
        ConstraintLayout timeContainer = dialogView.findViewById(R.id.timeContainer);
        ConstraintLayout startDateContainer = dialogView.findViewById(R.id.startDateContainer);
        startDateTextView = dialogView.findViewById(R.id.startDateTextView);


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
        startDateContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseStartDate();
            }
        });



        if(s.equals(RepeatingSpendingType.DAILY.name())){
            dialogBuilder.setTitle("Ежедневная трата");
            daysOfWeek.setVisibility(View.GONE);
            dayOfMonth.setVisibility(View.GONE);
        }
        if(s.equals(RepeatingSpendingType.WEEKLY.name())){
            dialogBuilder.setTitle("Ежеднедельная трата");
            dayOfMonth.setVisibility(View.GONE);
        }
        if(s.equals(RepeatingSpendingType.MONTHLY.name())){
            dialogBuilder.setTitle("Ежемесячная трата");
            daysOfWeek.setVisibility(View.GONE);
        }


        DateFormat dateFormatId = new SimpleDateFormat("dd.MM.yyyy");
        calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        dateId = dateFormatId.format(date);
        startDateTextView.setText(dateFormat.format(calendar.getTime()));


        Spinner spinner = dialogView.findViewById(R.id.daySpinner);
        List<String> days = new ArrayList<>();
        for (int i = 0; i < 31; i++) {
            days.add(""+ (i+1));
        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(this,  android.R.layout.simple_spinner_dropdown_item, days);
        adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);

        final int[] position = new int[1];
        position[0] = 1;
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                position[0] = pos+1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        final UniqProduct[] uniqProducts = new UniqProduct[1];
        final boolean[] categoryWasFilledFirst = {false};
        final Category[] category = new Category[1];
        final Product[] product = new Product[1];

        /**
         * Init Views
         */
        final TextInputLayout productContainer =  dialogView.findViewById(R.id.containerProductEditText);
        final AutoCompleteTextView productNameEditText = dialogView.findViewById(R.id.productEditText);
        final AutoCompleteTextView categoryNameEditText = dialogView.findViewById(R.id.categoryEditText);
        final EditText priceEditText = dialogView.findViewById(R.id.priceEditText);

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

                    if(uniqProducts[0].getCategory()!=null){
                        Log.d("TAG21", "Product category found");
                        if(event==null){
                            if(!categoryWasFilledFirst[0])
                                categoryNameEditText.setText(uniqProducts[0].getCategory().getName());
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

     /*   if(event!=null){
            product[0] = day.getList().get(event.getPosition()).getProduct();
            category[0] = day.getList().get(event.getPosition()).getProduct().getCategory();
            productNameEditText.setText(product[0].getName());
            productNameEditText.setSelection(product[0].getName().length());
            productNameEditText.dismissDropDown();
            priceEditText.setText(String.valueOf(day.getList().get(event.getPosition()).getPrice()));
            categoryNameEditText.setText(product[0].getCategory().getName());
        }*/


        final AlertDialog b = dialogBuilder.create();
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


                        Product product = realm.createObject(Product.class, Util.getNextProductId(realm));
                        product.setName(p.getName());
                        product.setCategory(p.getCategory());

                        RepeatingSpending repeatingSpending = realm.createObject(
                                RepeatingSpending.class,
                                Util.getNextId(realm, RepeatingSpending.class));


                        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

                        repeatingSpending.setProduct(product);
                        repeatingSpending.setPrice(price);
                        repeatingSpending.setStartDate(dateFormat.format(calendar.getTime()));
                        repeatingSpending.setType(s);
                        repeatingSpending.setHours(hourOfDay);
                        repeatingSpending.setMinutes(minute);

                        if(s.equals(RepeatingSpendingType.WEEKLY.name())){
                            Log.i("TAG21", "type - " + RepeatingSpendingType.WEEKLY.name());

                            RealmList<SpendingDay> list = repeatingSpending.getDays();
                            SpendingDay spendingDay;
                            if(mon.isChecked()){
                                spendingDay = realm.where(SpendingDay.class).equalTo("day", Calendar.MONDAY).findFirst();
                                if(spendingDay==null){
                                    spendingDay = realm.createObject(SpendingDay.class, Calendar.MONDAY);
                                    Log.i("TAG21", "add mon - " +  Calendar.MONDAY);
                                }
                                list.add(spendingDay);
                            }
                            if(tue.isChecked()){
                                spendingDay = realm.where(SpendingDay.class).equalTo("day", Calendar.TUESDAY).findFirst();
                                if(spendingDay==null){
                                    spendingDay = realm.createObject(SpendingDay.class, Calendar.TUESDAY);
                                }
                                Log.i("TAG21", "add tue - " +  Calendar.TUESDAY);
                                list.add(spendingDay);
                            }
                            if(wed.isChecked()){
                                spendingDay = realm.where(SpendingDay.class).equalTo("day", Calendar.WEDNESDAY).findFirst();
                                if(spendingDay==null){
                                    spendingDay = realm.createObject(SpendingDay.class, Calendar.WEDNESDAY);
                                }
                                list.add(spendingDay);
                            }
                            if(thu.isChecked()){
                                spendingDay = realm.where(SpendingDay.class).equalTo("day", Calendar.THURSDAY).findFirst();
                                if(spendingDay==null){
                                    spendingDay = realm.createObject(SpendingDay.class, Calendar.THURSDAY);
                                }
                                list.add(spendingDay);
                            }  if(fri.isChecked()){
                                spendingDay = realm.where(SpendingDay.class).equalTo("day", Calendar.FRIDAY).findFirst();
                                if(spendingDay==null){
                                    spendingDay = realm.createObject(SpendingDay.class, Calendar.FRIDAY);
                                }
                                list.add(spendingDay);
                            }
                            if(sat.isChecked()){
                                spendingDay = realm.where(SpendingDay.class).equalTo("day", Calendar.SATURDAY).findFirst();
                                if(spendingDay==null){
                                    spendingDay = realm.createObject(SpendingDay.class, Calendar.SATURDAY);
                                }
                                list.add(spendingDay);
                            }
                            if(sun.isChecked()){
                                spendingDay = realm.where(SpendingDay.class).equalTo("day", Calendar.SUNDAY).findFirst();
                                if(spendingDay==null){
                                    spendingDay = realm.createObject(SpendingDay.class, Calendar.SUNDAY);
                                }
                                list.add(spendingDay);
                            }
                        }

                        if(s.equals(RepeatingSpendingType.MONTHLY.name())){

                            RealmList<SpendingDay> list = repeatingSpending.getDays();
                            list.add(getSpendingDay(position[0]));

                        }

                        realm.commitTransaction();

                        b.dismiss();
                        App.closeKeyboard(getApplicationContext());

                    }
                });
            }
        });

        b.show();
        App.showKeyboard(getApplicationContext());

    }

    SpendingDay getSpendingDay(int i){
        SpendingDay spendingDay = realm.where(SpendingDay.class).equalTo("day", i).findFirst();
        if(spendingDay==null){
            spendingDay = realm.createObject(SpendingDay.class, i);
        }
        return spendingDay;
    }


}
