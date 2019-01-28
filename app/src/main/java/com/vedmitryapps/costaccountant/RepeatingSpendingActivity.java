package com.vedmitryapps.costaccountant;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.vedmitryapps.costaccountant.adapters.RepeatingSpendingRecyclerAdapter;
import com.vedmitryapps.costaccountant.models.Category;
import com.vedmitryapps.costaccountant.models.DayPair;
import com.vedmitryapps.costaccountant.models.Product;
import com.vedmitryapps.costaccountant.models.UniqProduct;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RepeatingSpendingActivity extends AppCompatActivity {

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    RepeatingSpendingRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repeating_spending);
        ButterKnife.bind(this);


        adapter = new RepeatingSpendingRecyclerAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.setAdapter(adapter);

    }


    @OnClick(R.id.bottomButton)
    public void bottomButton(View v){
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
