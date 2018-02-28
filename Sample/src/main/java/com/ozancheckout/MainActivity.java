package com.ozancheckout;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.MaterialDialog.Builder;
import com.afollestad.materialdialogs.MaterialDialog.InputCallback;
import com.ozancheckout.api.OzanCheckout;
import com.ozancheckout.api.model.CheckoutError;
import com.ozancheckout.api.model.CheckoutItem;
import com.ozancheckout.api.model.OzanEnvironment;
import com.ozancheckout.api.model.PaymentItem;
import com.ozancheckout.api.view.RippleEffectView;
import com.ozancheckout.api.view.RippleEffectView.OnRippleCompleteListener;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView textView_token;
    private RippleEffectView checkout;
    private ListView itemsListview;
    List<PaymentItem> mPaymentItems = new ArrayList<>();
    ArrayAdapter<PaymentItem> itemsAdapter;
    private android.widget.EditText merchantNameEdittext;
    private android.widget.EditText amountEdittext;
    private android.widget.EditText currencyEdittext;
    private android.widget.EditText apiKeyEdittext;
    private AppCompatSpinner enviromentSpinner;
    private OzanEnvironment mEnvironment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.currencyEdittext = (EditText) findViewById(R.id.currencyEdittext);
        this.enviromentSpinner = (AppCompatSpinner) findViewById(R.id.enviromentSpinner);
        this.amountEdittext = (EditText) findViewById(R.id.amountEdittext);
        this.merchantNameEdittext = (EditText) findViewById(R.id.merchantNameEdittext);
        this.apiKeyEdittext = (EditText) findViewById(R.id.apiKeyEdittext);
        textView_token = (TextView) findViewById(R.id.textView_token);
        itemsListview = (ListView) findViewById(R.id.itemsListview);
        checkout = (RippleEffectView) findViewById(R.id.button_checkout);
        checkout.setOnRippleCompleteListener(new OnRippleCompleteListener() {
            @Override
            public void onComplete(RippleEffectView rippleView) {
                checkFields();

            }
        });
        enviromentSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i){
                    case 0:
                        mEnvironment = OzanEnvironment.DEVELOPMENT;
                        break;
                    case 1:
                        mEnvironment = OzanEnvironment.QA;
                        break;
                    case 2:
                        mEnvironment = OzanEnvironment.SANDBOX;
                        break;
                    case 3:
                        mEnvironment = OzanEnvironment.PRODUCTION;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                mEnvironment = OzanEnvironment.QA;
            }
        });
        itemsAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, mPaymentItems);
        itemsListview.setAdapter(itemsAdapter);

    }

    private void checkFields() {
       /* if (mPaymentItems.size() == 0) {
            Toast.makeText(this, "Add some items", Toast.LENGTH_SHORT).show();
            return;
        }
        if (apiKeyEdittext.getText().toString().isEmpty()) {
            Toast.makeText(this, "ApiKey Please...", Toast.LENGTH_SHORT).show();
        }
        if (merchantNameEdittext.getText().toString().isEmpty() || amountEdittext.getText().toString().isEmpty()
                || currencyEdittext.getText().toString().isEmpty()) {
            Toast.makeText(this, "Give some about merchant/amount/currency", Toast.LENGTH_SHORT).show();
            return;
        }*/
        try {
            double amount = Double.parseDouble(amountEdittext.getText().toString());
            CheckoutItem checkoutItem = new CheckoutItem.Builder(apiKeyEdittext.getText().toString(),amount, currencyEdittext.getText().toString(), mPaymentItems)
                    .merchantName(merchantNameEdittext.getText().toString())
                    .build();
            OzanCheckout.with(checkoutItem, mEnvironment).startCheckout(MainActivity.this);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OzanCheckout.REQUEST_OZANCHECKOUT && data != null) {
            if (resultCode == RESULT_OK) {
                String token = data.getStringExtra(OzanCheckout.EXTRA_SUCCESS_PAYMENT_TOKEN);
                textView_token.setText("Payment Success Token : " + token);
            } else if (resultCode == OzanCheckout.RESULT_ERROR) {
                CheckoutError error = data.getParcelableExtra(OzanCheckout.EXTRA_ON_ERROR);
                AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).setTitle(String.valueOf(error.getErrorCode()))
                        .setMessage(error.getErrorMessage())
                        .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create();
                dialog.show();
            }
        } else if (resultCode == OzanCheckout.RESULT_CANCELED) {

            Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
        }
    }


    public void onAddItemClick(View view) {
        new Builder(this)
                .title("Item Description")
                .input("give name or desc", "", new InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        if (input.toString().isEmpty()) {
                            return;
                        }
                        mPaymentItems.add(new PaymentItem.Builder()
                                .description(input.toString())
                                .build());
                        itemsAdapter.notifyDataSetChanged();
                    }
                }).show();
    }
}
