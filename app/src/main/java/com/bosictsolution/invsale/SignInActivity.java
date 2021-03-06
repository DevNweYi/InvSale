package com.bosictsolution.invsale;

import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bosictsolution.invsale.api.Api;
import com.bosictsolution.invsale.common.AppConstant;
import com.bosictsolution.invsale.data.ClientData;
import com.google.android.material.textfield.TextInputLayout;

public class SignInActivity extends AppCompatActivity {

    TextInputLayout inputPhone;
    EditText etPhone;
    Button btnContinue;
    private Context context=this;
    private ProgressDialog progressDialog;
    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        getSupportActionBar().hide();
        setLayoutResource();
        sharedpreferences = getSharedPreferences(AppConstant.MyPREFERENCES, Context.MODE_PRIVATE);

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validateControl()){
                    checkClient(etPhone.getText().toString());
                }
            }
        });
    }

    private void checkClient(String phone) {
        progressDialog.show();
        progressDialog.setMessage(getResources().getString(R.string.loading));
        Api.getClient().checkClient(phone).enqueue(new Callback<ClientData>() {
            @Override
            public void onResponse(Call<ClientData> call, Response<ClientData> response) {
                progressDialog.dismiss();
                ClientData data = response.body();
                if (data.getClientID() != 0) {
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putInt(AppConstant.ClientID, data.getClientID());
                    editor.putString(AppConstant.ClientName, data.getClientName());
                    editor.putString(AppConstant.ClientPassword, data.getClientPassword());
                    editor.putString(AppConstant.ClientShopName, data.getShopName());
                    editor.putString(AppConstant.ClientPhone, phone);
                    editor.putInt(AppConstant.ClientDivisionID, data.getDivisionID());
                    editor.putString(AppConstant.ClientDivisionName, data.getDivisionName());
                    editor.putInt(AppConstant.ClientTownshipID, data.getTownshipID());
                    editor.putString(AppConstant.ClientTownshipName, data.getTownshipName());
                    editor.putString(AppConstant.ClientAddress, data.getAddress());
                    editor.commit();
                    Intent i = new Intent(SignInActivity.this, LoginActivity.class);
                    startActivity(i);
                    finish();
                } else
                    Toast.makeText(context, getResources().getString(R.string.registration_not_found), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<ClientData> call, Throwable t) {
                progressDialog.dismiss();
                Log.e("SignInActivity", t.getMessage());
            }
        });
    }

    private boolean validateControl(){
        if(etPhone.getText().toString().length()==0){
            inputPhone.setError(getResources().getString(R.string.enter_value));
            return false;
        }
        return true;
    }

    private void setLayoutResource(){
        btnContinue=findViewById(R.id.btnContinue);
        etPhone=findViewById(R.id.etPhone);
        inputPhone=findViewById(R.id.inputPhone);

        progressDialog =new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
    }
}