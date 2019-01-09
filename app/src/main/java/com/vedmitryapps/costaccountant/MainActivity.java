package com.vedmitryapps.costaccountant;

import android.app.usage.UsageEvents;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.TextInputLayout;
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
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

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

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.dateTextView)
    TextView dateTextView;

    @BindView(R.id.dayCount)
    TextView dayCount;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

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
                        double price;

                        if(!priceEditText.getText().toString().equals("")){
                            price = Double.parseDouble(priceEditText.getText().toString());
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
}
