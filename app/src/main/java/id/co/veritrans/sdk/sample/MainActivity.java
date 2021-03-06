package id.co.veritrans.sdk.sample;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.UUID;

import id.co.veritrans.sdk.core.Constants;
import id.co.veritrans.sdk.core.LocalDataHandler;
import id.co.veritrans.sdk.core.PaymentMethods;
import id.co.veritrans.sdk.core.TransactionRequest;
import id.co.veritrans.sdk.core.VeritransBuilder;
import id.co.veritrans.sdk.core.VeritransSDK;
import id.co.veritrans.sdk.eventbus.bus.VeritransBusProvider;
import id.co.veritrans.sdk.eventbus.callback.GetAuthenticationBusCallback;
import id.co.veritrans.sdk.eventbus.callback.TransactionFinishedCallback;
import id.co.veritrans.sdk.eventbus.events.AuthenticationEvent;
import id.co.veritrans.sdk.eventbus.events.GeneralErrorEvent;
import id.co.veritrans.sdk.eventbus.events.NetworkUnavailableEvent;
import id.co.veritrans.sdk.eventbus.events.TransactionFinishedEvent;
import id.co.veritrans.sdk.models.BillInfoModel;
import id.co.veritrans.sdk.models.ItemDetails;
import id.co.veritrans.sdk.models.PaymentMethodsModel;
import id.co.veritrans.sdk.sample.core.CoreFlowActivity;
import id.co.veritrans.sdk.scancard.ScanCard;

public class MainActivity extends AppCompatActivity implements GetAuthenticationBusCallback, TransactionFinishedCallback {

    ProgressDialog dialog;
    private TextView authToken;
    private Button coreBtn, uiBtn;
    private Button coreCardRegistration, uiCardRegistration,
            getAuthenticationToken, refresh_token;
    private RadioButton normal, twoClick, oneClick;
    private ArrayList<PaymentMethodsModel> selectedPaymentMethods;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        VeritransBusProvider.getInstance().register(this);
        setContentView(R.layout.activity_main);
        initSDK();
        initView();
    }

    @Override
    protected void onDestroy() {
        VeritransBusProvider.getInstance().unregister(this);
        super.onDestroy();
    }
    /**
     * Initialize Veritrans SDK using VeritransBuilder.
     */
    private void initSDK() {
        VeritransBuilder veritransBuilder = new
                VeritransBuilder(this, BuildConfig.CLIENT_KEY, BuildConfig.BASE_URL);
        veritransBuilder.enableLog(true);
        veritransBuilder.setExternalScanner(new ScanCard());
        veritransBuilder.buildSDK();

        VeritransSDK veritransSDK = VeritransSDK.getVeritransSDK();
        veritransSDK.setDefaultText("open_sans_regular.ttf");
        veritransSDK.setSemiBoldText("open_sans_semibold.ttf");
        veritransSDK.setBoldText("open_sans_bold.ttf");

        veritransSDK.setSelectedPaymentMethods(PaymentMethods.getAllPaymentMethods(this));
    }

    /**
     * Initialize transaction data.
     *
     * @return the transaction request.
     */
    private TransactionRequest initializePurchaseRequest() {
        // Create new Transaction Request
        TransactionRequest transactionRequestNew = new
                TransactionRequest(UUID.randomUUID().toString(), 360000);

        // Define item details
        ItemDetails itemDetails = new ItemDetails("1", 120000, 1, "Trekking Shoes");
        ItemDetails itemDetails1 = new ItemDetails("2", 100000, 1, "Casual Shoes");
        ItemDetails itemDetails2 = new ItemDetails("3", 140000, 1, "Formal Shoes");

        // Add item details into item detail list.
        ArrayList<ItemDetails> itemDetailsArrayList = new ArrayList<>();
        itemDetailsArrayList.add(itemDetails);
        itemDetailsArrayList.add(itemDetails1);
        itemDetailsArrayList.add(itemDetails2);
        transactionRequestNew.setItemDetails(itemDetailsArrayList);
        // Set Bill info
        BillInfoModel billInfoModel = new BillInfoModel("demo_label", "demo_value");
        transactionRequestNew.setBillInfoModel(billInfoModel);

        // Create transaction request
        String cardClickType = "";
        if (normal.isChecked()) {
            cardClickType = getString(R.string.card_click_type_none);
        } else if (twoClick.isChecked()) {
            cardClickType = getString(R.string.card_click_type_two_click);
        } else {
            cardClickType = getString(R.string.card_click_type_one_click);
        }
        transactionRequestNew.setCardPaymentInfo(cardClickType, true);

        return transactionRequestNew;
    }

    /**
     * Initialize the view.
     */
    private void initView() {

        //Initialize progress dialog
        dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        dialog.setMessage("Loading");

        // Initialize radio button
        normal = (RadioButton) findViewById(R.id.radio_card_normal);
        twoClick = (RadioButton) findViewById(R.id.radio_card_two_click);
        oneClick = (RadioButton) findViewById(R.id.radio_card_one_click);

        //
        coreBtn = (Button) findViewById(R.id.show_core_example);
        coreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CoreFlowActivity.class);
                startActivity(intent);
            }
        });
        uiBtn = (Button) findViewById(R.id.show_ui_flow);
        uiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VeritransSDK.getVeritransSDK().setTransactionRequest(initializePurchaseRequest());
                VeritransSDK.getVeritransSDK().startPaymentUiFlow(MainActivity.this);
            }
        });

        // Handle Card registration using core flow
        coreCardRegistration = (Button)findViewById(R.id.btn_card_registration_core);
        coreCardRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CardRegistrationActivity.class);
                startActivity(intent);
            }
        });

        // Handle Card registration using UI flow
        uiCardRegistration = (Button)findViewById(R.id.btn_card_registration_ui);
        uiCardRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VeritransSDK.getVeritransSDK().startRegisterCardUIFlow(MainActivity.this);
            }
        });

        authToken = (TextView) findViewById(R.id.txt_auth_token);
        getAuthenticationToken = (Button) findViewById(R.id.btn_get_token);
        getAuthenticationToken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VeritransSDK.getVeritransSDK().getAuthenticationToken();
            }
        });
        refreshAuthenticationContainer();
    }

    private void refreshAuthenticationContainer() {
        if(VeritransSDK.getVeritransSDK().readAuthenticationToken()!=null
                && !VeritransSDK.getVeritransSDK().readAuthenticationToken().equals("")) {
            getAuthenticationToken.setText(R.string.btn_refresh_token);
            authToken.setText(VeritransSDK.getVeritransSDK().readAuthenticationToken());
        } else {
            getAuthenticationToken.setText(R.string.btn_get_token);
            authToken.setText("Not Available");
        }
    }

    @Subscribe
    @Override
    public void onEvent(AuthenticationEvent authenticationEvent) {
        String auth = authenticationEvent.getResponse().getxAuth();
        LocalDataHandler.saveString(Constants.AUTH_TOKEN, auth);
        refreshAuthenticationContainer();
    }

    @Subscribe
    @Override
    public void onEvent(NetworkUnavailableEvent networkUnavailableEvent) {
        // Handle network not available condition
        dialog.dismiss();
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage("Network is unavailable")
                .create();
        dialog.show();
    }

    @Subscribe
    @Override
    public void onEvent(GeneralErrorEvent generalErrorEvent) {
        // Handle generic error condition
        dialog.dismiss();
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage("Unknown error: " + generalErrorEvent.getMessage() )
                .create();
        dialog.show();
    }

    @Subscribe
    @Override
    public void onEvent(TransactionFinishedEvent transactionFinishedEvent) {
        Log.i(MainActivity.class.getSimpleName(), "Transaction Finished. ID: " + transactionFinishedEvent.getResponse().getTransactionId());
        Toast.makeText(this, "Transaction Finished. ID: " + transactionFinishedEvent.getResponse().getTransactionId(), Toast.LENGTH_LONG).show();
    }
}
