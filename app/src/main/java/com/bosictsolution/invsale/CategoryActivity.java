package com.bosictsolution.invsale;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;

import com.bosictsolution.invsale.adapter.ExpandableListAdapter;
import com.bosictsolution.invsale.common.AppSetting;
import com.bosictsolution.invsale.common.ConnectionLiveData;
import com.bosictsolution.invsale.common.DatabaseAccess;
import com.bosictsolution.invsale.data.ConnectionData;
import com.bosictsolution.invsale.data.MainMenuData;
import com.bosictsolution.invsale.data.SubMenuData;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CategoryActivity extends AppCompatActivity {

    ExpandableListView expList;
    ExtendedFloatingActionButton fab;
    List<MainMenuData> listDataHeader;
    HashMap<MainMenuData,List<SubMenuData>> listDataChild;
    ExpandableListAdapter expListAdapter;
    List<MainMenuData> lstMainMenu=new ArrayList<>();
    List<SubMenuData> lstSubMenu=new ArrayList<>();
    DatabaseAccess db;
    private Context context=this;
    private ProgressDialog progressDialog;
    AppSetting appSetting=new AppSetting();
    public static Activity activity;
    ConnectionLiveData connectionLiveData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        setLayoutResource();
        init();
        ActionBar actionbar = getSupportActionBar();
        ColorDrawable colorDrawable = new ColorDrawable(getResources().getColor(R.color.primary_500));
        actionbar.setBackgroundDrawable(colorDrawable);
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setDisplayShowTitleEnabled(true);
        setTitle(getResources().getString(R.string.categories));

        checkConnection();
        fillData();

        expList.setOnChildClickListener(new ExpandableListView.OnChildClickListener(){
            @Override
            public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long id){
                int subMenuId,mainMenuId;
                String subMenuName,mainMenuName;
                List<Integer> lstSubMenuID = new ArrayList<>();

                mainMenuId = lstMainMenu.get(groupPosition).getMainMenuID();
                mainMenuName=lstMainMenu.get(groupPosition).getMainMenuName();
                subMenuName = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).getSubMenuName();
                for (int i = 0; i < lstSubMenu.size(); i++) {
                    if (lstSubMenu.get(i).getMainMenuID() == mainMenuId) {
                        lstSubMenuID.add(lstSubMenu.get(i).getSubMenuID());
                    }
                }
                subMenuId = lstSubMenuID.get(childPosition);
                Intent i=new Intent(CategoryActivity.this, ProductActivity.class);
                i.putExtra("MainMenuName",mainMenuName);
                i.putExtra("SubMenuName",subMenuName);
                i.putExtra("SubMenuID",subMenuId);
                startActivity(i);
                return false;
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(CategoryActivity.this, SaleOrderSummaryActivity.class);
                startActivity(i);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setFab();
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

    private void init(){
        connectionLiveData = new ConnectionLiveData(context);
        activity=this;
        db=new DatabaseAccess(context);
        progressDialog =new ProgressDialog(context);
        appSetting.setupProgress(progressDialog);
    }

    private void fillData() {
        getMainMenu();
        getSubMenu();
        setDataToExpList();
    }

    private void setFab() {
        int totalSaleOrderItem = db.getTotalSaleOrderItem();
        int totalSaleOrderQty = db.getTotalSaleOrderQty();
        if (totalSaleOrderItem != 0) {
            fab.setText("Order:" + totalSaleOrderItem + "(" + totalSaleOrderQty + ") Items - " + db.getHomeCurrency() + getResources().getString(R.string.space) + appSetting.df.format(db.getTotalSaleOrderAmount()));
            fab.setVisibility(View.VISIBLE);
        } else fab.setVisibility(View.GONE);
    }

    private void getMainMenu(){
        lstMainMenu=db.getMainMenu();
    }

    private void getSubMenu(){
        lstSubMenu=db.getSubMenu();
    }

    private void setDataToExpList() {
        listDataHeader = new ArrayList<>();
        listDataChild = new HashMap<>();
        for (int i = 0; i < lstMainMenu.size(); i++) {
            int mainMenuID = lstMainMenu.get(i).getMainMenuID();

            MainMenuData mainMenuData=new MainMenuData();
            mainMenuData.setMainMenuName(lstMainMenu.get(i).getMainMenuName());
            mainMenuData.setPhotoUrl(lstMainMenu.get(i).getPhotoUrl());

            List<SubMenuData> lstSubMenuData = new ArrayList<>();
            for (int j = 0; j < lstSubMenu.size(); j++) {
                if (lstSubMenu.get(j).getMainMenuID() == mainMenuID) {
                    SubMenuData subMenuData=new SubMenuData();
                    subMenuData.setSubMenuName(lstSubMenu.get(j).getSubMenuName());
                    subMenuData.setPhotoUrl(lstSubMenu.get(i).getPhotoUrl());
                    lstSubMenuData.add(subMenuData);
                }
            }
            listDataChild.put(mainMenuData, lstSubMenuData);
            listDataHeader.add(mainMenuData);
        }
        expListAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);
        expList.setAdapter(expListAdapter);
    }

    private void setLayoutResource() {
        expList = findViewById(R.id.list);
        fab=findViewById(R.id.fab);
    }
}