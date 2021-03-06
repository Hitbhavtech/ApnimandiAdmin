package com.mohit.varma.apnimandiadmin.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.mohit.varma.apnimandiadmin.R;
import com.mohit.varma.apnimandiadmin.adapters.MyOrderSummaryAdapter;
import com.mohit.varma.apnimandiadmin.firebase.MyDatabaseReference;
import com.mohit.varma.apnimandiadmin.model.OrderStatus;
import com.mohit.varma.apnimandiadmin.model.Orders;
import com.mohit.varma.apnimandiadmin.model.UCart;
import com.mohit.varma.apnimandiadmin.utilities.Session;

import java.util.List;

public class OngoingItemViewDetailsActivity extends AppCompatActivity {
    private static final String TAG = "OngoingItemViewActivity";
    private Toolbar NewOrderItemViewDetailsActivityToolbar;
    private TextView MyOrderSingleItemOrderNumber, MyOrderSingleItemPlacedDate, MyOrderSingleItemOrderUserName,
            MyOrderSingleCustomerPhoneNumber, MyOrderSingleItemCustomerCallView, MyOrderSingleCustomerEmail, MyOrderSingleItemCustomerEmailView, MyOrderSingleCustomerNavigation, MyOrderSingleItemCustomerNavigationView,
            SummarySingleItemName, SummarySingleItemQuantity, SummarySingleItemPrice,
            MyOrderSingleItemGrandTotalView, MyOrderSingleItemSubtotalView, MyOrderSingleItemDeliveryChargeView, MyOrderSingleItemServiceTaxView,
            MyOrderSingleItemDiscountView;
    private RecyclerView MyOrderSingleItemRecyclerView;
    private Spinner MyOrderSingleItemSpinnerView;
    private ArrayAdapter<String> orderStatusArrayAdapter;
    private String[] orderStatuses = {"Order Status", "Processing", "Delivered", "Shipped", "Cancelled"};
    private ConstraintLayout ConstraintLayoutRootView;
    private Orders orders;
    private Context context;
    private Session session;
    private DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ongoing_item_view_details);

        initViews();
        orderStatusArrayAdapter = new ArrayAdapter<String>(context,
                R.layout.spinner_layout, orderStatuses);
        // Drop down layout style - list view with radio button
        orderStatusArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        if (getIntent().getSerializableExtra("OngoingOrder") != null) {
            orders = (Orders) getIntent().getSerializableExtra("OngoingOrder");
        }

        if (orders != null) {
            calculation(orders.getGrandTotal());
            MyOrderSingleItemOrderNumber.setText("Order No - " + orders.getOrderId());
            MyOrderSingleItemPlacedDate.setText("Date: " + orders.getOrderDate());
            MyOrderSingleItemOrderUserName.setText(orders.getUserAddress().getUserName());
            MyOrderSingleCustomerPhoneNumber.setText(orders.getUserAddress().getPhoneNumber());
            MyOrderSingleCustomerEmail.setText("mv501049@gmail.com");
            MyOrderSingleCustomerNavigation.setText(orders.getUserAddress().getAddressLine1() + " " + orders.getUserAddress().getAddressLine2() + " " + orders.getUserAddress().getCityCode());
            if (orders.getuCartList() != null && orders.getuCartList().size() > 0) {
                ConstraintLayoutRootView.setVisibility(View.GONE);
                MyOrderSingleItemRecyclerView.setVisibility(View.VISIBLE);
                setAdapter(orders.getuCartList(), MyOrderSingleItemRecyclerView);
            } else {
                ConstraintLayoutRootView.setVisibility(View.VISIBLE);
                MyOrderSingleItemRecyclerView.setVisibility(View.GONE);
                SummarySingleItemName.setText(orders.getuItem().getmItemName());
                SummarySingleItemQuantity.setText("Qty: " + orders.getuItem().getmItemWeight());
                SummarySingleItemPrice.setText("\u20B9" + orders.getuItem().getmItemPrice());
            }
        }

        NewOrderItemViewDetailsActivityToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        MyOrderSingleItemSpinnerView.setAdapter(orderStatusArrayAdapter);

        MyOrderSingleItemSpinnerView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

                String item = parent.getItemAtPosition(pos).toString();
                switch (item) {
                    case "Processing":
                        updateOrderStatus(OrderStatus.PROCESSING);
                        break;
                    case "Delivered":
                        updateOrderStatus(OrderStatus.DELIVERED);
                        break;
                    case "Shipped":
                        updateOrderStatus(OrderStatus.SHIPPED);
                        break;
                    case "Cancelled":
                        updateOrderStatus(OrderStatus.CANCELLED);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }
    public void initViews() {
        NewOrderItemViewDetailsActivityToolbar = findViewById(R.id.NewOrderItemViewDetailsActivityToolbar);
        MyOrderSingleItemOrderNumber = findViewById(R.id.MyOrderSingleItemOrderNumber);
        MyOrderSingleItemPlacedDate = findViewById(R.id.MyOrderSingleItemPlacedDate);
        MyOrderSingleItemOrderUserName = findViewById(R.id.MyOrderSingleItemOrderUserName);
        MyOrderSingleCustomerPhoneNumber = findViewById(R.id.MyOrderSingleCustomerPhoneNumber);
        MyOrderSingleItemCustomerCallView = findViewById(R.id.MyOrderSingleItemCustomerCallView);
        MyOrderSingleCustomerEmail = findViewById(R.id.MyOrderSingleCustomerEmail);
        MyOrderSingleItemCustomerEmailView = findViewById(R.id.MyOrderSingleItemCustomerEmailView);
        MyOrderSingleCustomerNavigation = findViewById(R.id.MyOrderSingleCustomerNavigation);
        MyOrderSingleItemCustomerNavigationView = findViewById(R.id.MyOrderSingleItemCustomerNavigationView);
        MyOrderSingleItemRecyclerView = findViewById(R.id.MyOrderSingleItemRecyclerView);
        MyOrderSingleItemSpinnerView = findViewById(R.id.MyOrderSingleItemSpinnerView);
        MyOrderSingleItemGrandTotalView = findViewById(R.id.MyOrderSingleItemGrandTotalView);
        MyOrderSingleItemSubtotalView = findViewById(R.id.MyOrderSingleItemSubtotalView);
        MyOrderSingleItemServiceTaxView = findViewById(R.id.MyOrderSingleItemServiceTaxView);
        MyOrderSingleItemDeliveryChargeView = findViewById(R.id.MyOrderSingleItemDeliveryChargeView);
        MyOrderSingleItemDiscountView = findViewById(R.id.MyOrderSingleItemDiscountView);
        ConstraintLayoutRootView = findViewById(R.id.ConstraintLayoutRootView);
        SummarySingleItemName = findViewById(R.id.SummarySingleItemName);
        SummarySingleItemQuantity = findViewById(R.id.SummarySingleItemQuantity);
        SummarySingleItemPrice = findViewById(R.id.SummarySingleItemPrice);
        this.context = this;
        this.session = new Session(context);
        this.databaseReference = new MyDatabaseReference().getReference();
    }


    public void setAdapter(List<UCart> uCartList, RecyclerView recyclerView) {
        if (uCartList != null && uCartList.size() > 0) {
            MyOrderSummaryAdapter myOrderSummaryAdapter = new MyOrderSummaryAdapter(context, uCartList);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setHasFixedSize(true);
            recyclerView.setAdapter(myOrderSummaryAdapter);
        }
    }


    public void calculation(long grandTotal) {
        long deliveryCharge = session.getDeliveryCharge();
        long subTotal = grandTotal - deliveryCharge;
        MyOrderSingleItemDeliveryChargeView.setText("Delivery Charge : \u20B9" + deliveryCharge);
        MyOrderSingleItemSubtotalView.setText("Subtotal : \u20B9" + subTotal);
        MyOrderSingleItemGrandTotalView.setText("Grand Total : \u20B9" + grandTotal);
        try {
            if(orders.getuCartList()!=null && orders.getuCartList().size()>0){
                long discount = (((orders.getuCartList().get(0).getmItemCutOffPrice()) * grandTotal))/100;
                MyOrderSingleItemDiscountView.setText("-Discount "+"("+orders.getuCartList().get(0).getmItemCutOffPrice()+"%) : \u20B9"+discount);
            }else {
                long discount = (((orders.getuItem().getmItemCutOffPrice()) * grandTotal))/100;
                MyOrderSingleItemDiscountView.setText("-Discount "+"("+orders.getuItem().getmItemCutOffPrice()+"%) : \u20B9"+discount);
            }
        } catch (Exception e) {

        }
        MyOrderSingleItemServiceTaxView.setText("+Service Tax (20%) : \u20B9" + 0);
    }

    public void updateOrderStatus(OrderStatus orderStatus){
        orders.setOrderStatus(orderStatus);
        Log.d(TAG, "onClick: " + new Gson().toJson(orders));
        databaseReference.child("Orders").orderByChild("orderId").equalTo(orders.getOrderId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot item : dataSnapshot.getChildren()) {
                        item.getRef().setValue(orders).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(context,context.getResources().getString(R.string.order_status_updated), Toast.LENGTH_SHORT).show();
                                onBackPressed();
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
