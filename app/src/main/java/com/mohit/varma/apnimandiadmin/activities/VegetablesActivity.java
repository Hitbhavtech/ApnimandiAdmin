package com.mohit.varma.apnimandiadmin.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.mohit.varma.apnimandiadmin.R;
import com.mohit.varma.apnimandiadmin.adapters.VegetableItemAdapter;
import com.mohit.varma.apnimandiadmin.firebase.MyDatabaseReference;
import com.mohit.varma.apnimandiadmin.interfaces.IUpdateItemCallBack;
import com.mohit.varma.apnimandiadmin.model.UItem;
import com.mohit.varma.apnimandiadmin.utilities.Constant;
import com.mohit.varma.apnimandiadmin.utilities.IsInternetConnectivity;
import com.mohit.varma.apnimandiadmin.utilities.ShowSnackBar;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import static com.mohit.varma.apnimandiadmin.utilities.Constant.ITEMS;
import static com.mohit.varma.apnimandiadmin.utilities.Constant.ITEM_KEY;

public class VegetablesActivity extends AppCompatActivity {
    public static final String TAG = FruitsActivity.class.getSimpleName();
    private Toolbar VegetablesActivityToolbar;
    private TextInputEditText UpdateItemAlertDialogLayoutMaterialCutOffPriceEditText, UpdateItemAlertDialogLayoutMaterialPriceEditText;
    private RecyclerView VegetablesActivityRecyclerView;
    private TextView VegetablesActivityNoItemAddedYetTextView;
    private FloatingActionButton VegetablesActivityFab;
    private Context activity;
    private View VegetablesActivityRootView, alertView;
    private Intent intent;
    private List<UItem> uItemList = new LinkedList<>();
    private DatabaseReference databaseReference;
    private VegetableItemAdapter adapter;
    private ProgressDialog progressDialog;
    private AlertDialog updateAlertDialog;
    private String category = "";
    private UItem uItemUpdated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vegetables);

        initViews();
        showProgressDialog();
        //set toolbar
        setSupportActionBar(VegetablesActivityToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (getIntent().getStringExtra(ITEM_KEY) != null) {
            if (!getIntent().getStringExtra(ITEM_KEY).isEmpty()) {
                category = getIntent().getStringExtra(ITEM_KEY);
                Log.d(TAG, "onCreate: " + category);
            }
        }

        floatingActionButtonConsumer.accept(VegetablesActivityFab);

        VegetablesActivityToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        databaseReference.child(ITEMS).child(category).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    uItemList.clear();
                    if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                        for (DataSnapshot Items : dataSnapshot.getChildren()) {
                            UItem uItem = Items.getValue(UItem.class);
                            uItemList.add(uItem);
                        }
                        Log.d(TAG, "onDataChange: " + new Gson().toJson(uItemList));
                        if (uItemList != null && uItemList.size() > 0) {
                            if (adapter != null) {
                                dismissProgressDialog();
                                VegetablesActivityRecyclerView.setVisibility(View.VISIBLE);
                                VegetablesActivityNoItemAddedYetTextView.setVisibility(View.GONE);
                                adapter.notifyDataSetChanged();
                            } else {
                                dismissProgressDialog();
                                VegetablesActivityRecyclerView.setVisibility(View.VISIBLE);
                                VegetablesActivityNoItemAddedYetTextView.setVisibility(View.GONE);
                                setAdapter();
                            }
                        }
                    } else {
                        VegetablesActivityRecyclerView.setVisibility(View.GONE);
                        VegetablesActivityNoItemAddedYetTextView.setVisibility(View.VISIBLE);
                        dismissProgressDialog();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void initViews() {
        VegetablesActivityToolbar = findViewById(R.id.VegetablesActivityToolbar);
        VegetablesActivityRecyclerView = findViewById(R.id.VegetablesActivityRecyclerView);
        VegetablesActivityFab = findViewById(R.id.VegetablesActivityFab);
        VegetablesActivityNoItemAddedYetTextView = findViewById(R.id.VegetablesActivityNoItemAddedYetTextView);
        VegetablesActivityRootView = findViewById(R.id.VegetablesActivityRootView);
        this.activity = this;
        databaseReference = new MyDatabaseReference().getReference();
        progressDialog = new ProgressDialog(activity);
    }

    public void showProgressDialog() {
        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.setMessage("Wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
    }

    public void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    public void dismissUpdateAlertDialog() {
        if (updateAlertDialog != null && updateAlertDialog.isShowing()) {
            updateAlertDialog.dismiss();
        }
    }

    Consumer<FloatingActionButton> floatingActionButtonConsumer = floatingActionButton -> {
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (IsInternetConnectivity.isConnected(activity)) {
                    intent = new Intent(activity, AddItemActivity.class);
                    intent.putExtra(ITEM_KEY, category);
                    startActivity(intent);
                } else {
                    ShowSnackBar.snackBar(activity, VegetablesActivityRootView, activity.getResources().getString(R.string.please_check_internet_connectivity));
                }
            }
        });
    };

    public void setAdapter() {
        if (uItemList != null && uItemList.size() > 0) {
            VegetablesActivityRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
            VegetablesActivityRecyclerView.setHasFixedSize(true);
            adapter = new VegetableItemAdapter(activity, uItemList, databaseReference, VegetablesActivityRootView, new IUpdateItemCallBack() {
                @Override
                public void updateItem(UItem uItem) {
                    uItemUpdated = uItem;
                    showUpdateDialog();
                }
            });
            VegetablesActivityRecyclerView.setAdapter(adapter);
        }
    }

    public void showUpdateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setCancelable(false);
        alertView = LayoutInflater.from(activity).inflate(R.layout.update_item_aleart_dialog_layout, null, false);
        builder.setView(alertView);
        UpdateItemAlertDialogLayoutMaterialCutOffPriceEditText = alertView.findViewById(R.id.UpdateItemAlertDialogLayoutMaterialCutOffPriceEditText);
        UpdateItemAlertDialogLayoutMaterialPriceEditText = alertView.findViewById(R.id.UpdateItemAlertDialogLayoutMaterialPriceEditText);
        MaterialButton UpdateItemAlertDialogLayoutMaterialUpdateButton = alertView.findViewById(R.id.UpdateItemAlertDialogLayoutMaterialUpdateButton);
        UpdateItemAlertDialogLayoutMaterialUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(VegetablesActivityRootView.getWindowToken(), 0);
                }
                updateItem();
            }
        });
        updateAlertDialog = builder.create();
        updateAlertDialog.show();
    }

    public void updateItem() {
        String updatedItemCutOffPrice = UpdateItemAlertDialogLayoutMaterialCutOffPriceEditText.getText().toString();
        String updatedItemPrice = UpdateItemAlertDialogLayoutMaterialPriceEditText.getText().toString();
        if (IsInternetConnectivity.isConnected(activity)) {
            if (updatedItemCutOffPrice != null && !updatedItemCutOffPrice.isEmpty() && updatedItemPrice != null && !updatedItemPrice.isEmpty()) {
                dismissUpdateAlertDialog();
                showProgressDialog();
                if (uItemUpdated != null) {
                    UItem uItem = new UItem(uItemUpdated.getmItemId(), Integer.parseInt(updatedItemCutOffPrice),
                            Integer.parseInt(updatedItemPrice), uItemUpdated.getmItemName(),
                            uItemUpdated.getmItemImage(), uItemUpdated.getmItemWeight(), uItemUpdated.getmItemCategory(), uItemUpdated.isPopular(), uItemUpdated.getuItemDescription());
                    if (uItem != null) {
                        databaseReference.child(Constant.ITEMS).child(Constant.VEGETABLE).orderByChild("mItemId").equalTo(uItem.getmItemId()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot item : dataSnapshot.getChildren()) {
                                    item.getRef().setValue(uItem).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            dismissProgressDialog();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            dismissProgressDialog();
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }
            } else {
                ShowSnackBar.snackBar(activity, VegetablesActivityRootView, activity.getResources().getString(R.string.all_fields_are_mandetory));
            }
        } else {
            ShowSnackBar.snackBar(activity, VegetablesActivityRootView, activity.getResources().getString(R.string.please_check_internet_connectivity));
        }
    }
}
