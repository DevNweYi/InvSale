package com.bosictsolution.invsale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.bosictsolution.invsale.common.AppSetting;
import com.bosictsolution.invsale.common.ConnectionLiveData;
import com.bosictsolution.invsale.common.DatabaseAccess;
import com.bosictsolution.invsale.data.ConnectionData;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class SaleOrderSuccessActivity extends AppCompatActivity {

    FloatingActionButton fab;
    TextView tvDate,tvOrderNumber,tvTotal;
    ImageView imgSuccess;
    int total;
    String orderNumber;
    AppSetting appSetting=new AppSetting();
    DatabaseAccess db;
    ConnectionLiveData connectionLiveData;
    private Context context=this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sale_order_success);
        setLayoutResource();
        init();
        getSupportActionBar().hide();

        RotateAnimation anim = new RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatCount(1);
        anim.setDuration(500);
        imgSuccess.startAnimation(anim);

        Intent i=getIntent();
        orderNumber=i.getStringExtra("OrderNumber");
        total=i.getIntExtra("Total",0);

        checkConnection();
        fillData();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                orderFinished();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        orderFinished();
    }

    private void orderFinished(){
        if(SaleOrderSummaryActivity.activity !=null)SaleOrderSummaryActivity.activity.finish();
        if(ProductActivity.activity !=null)ProductActivity.activity.finish();
        if(CategoryActivity.activity !=null)CategoryActivity.activity.finish();
        finish();
    }

    private void init(){
        connectionLiveData = new ConnectionLiveData(context);
        db=new DatabaseAccess(this);
    }

    private void checkConnection(){
        connectionLiveData.observe(this, new Observer<ConnectionData>() {
            @Override
            public void onChanged(ConnectionData connectionData) {
                if (!connectionData.getIsConnected())
                    appSetting.showSnackBar(findViewById(R.id.layoutRoot));
            }
        });
    }

    private void fillData(){
        tvDate.setText(appSetting.getTodayDate());
        tvOrderNumber.setText(orderNumber);
        tvTotal.setText(db.getHomeCurrency()+getResources().getString(R.string.space)+appSetting.df.format(total));
    }

    private void setLayoutResource(){
        fab=findViewById(R.id.fab);
        tvDate=findViewById(R.id.tvDate);
        tvOrderNumber=findViewById(R.id.tvOrderNumber);
        tvTotal=findViewById(R.id.tvTotal);
        imgSuccess=findViewById(R.id.imgSuccess);
    }
}