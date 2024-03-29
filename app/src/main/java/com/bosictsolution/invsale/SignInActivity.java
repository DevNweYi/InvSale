package com.bosictsolution.invsale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bosictsolution.invsale.api.Api;
import com.bosictsolution.invsale.common.AppConstant;
import com.bosictsolution.invsale.common.AppSetting;
import com.bosictsolution.invsale.common.ConnectionLiveData;
import com.bosictsolution.invsale.data.ClientData;
import com.bosictsolution.invsale.data.ConnectionData;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class SignInActivity extends AppCompatActivity {

    TextInputLayout inputPhone;
    EditText etPhone;
    Button btnContinue;
    private Context context=this;
    private ProgressDialog progressDialog;
    SharedPreferences sharedpreferences;
    ConnectionLiveData connectionLiveData;
    AppSetting appSetting=new AppSetting();
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback;
    FirebaseAuth auth;
    private String verificationCode;
    ClientData clientData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        getSupportActionBar().hide();
        setLayoutResource();
        init();

        checkConnection();
        startFirebaseLogin();

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validateControl()){
                    checkClient(etPhone.getText().toString(),true);
                }
            }
        });
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
        sharedpreferences = getSharedPreferences(AppConstant.MYPREFERENCES, Context.MODE_PRIVATE);
        progressDialog =new ProgressDialog(context);
        appSetting.setupProgress(progressDialog);
    }

    private void checkClient(String phone,boolean isSalePerson) {
        progressDialog.show();
        progressDialog.setMessage(getResources().getString(R.string.check_client));
        Api.getClient().checkClient(phone, isSalePerson).enqueue(new Callback<ClientData>() {
            @Override
            public void onResponse(Call<ClientData> call, Response<ClientData> response) {
                if (response.body() == null) {
                    progressDialog.dismiss();
                    Toast.makeText(context, response.message(), Toast.LENGTH_LONG).show();
                    return;
                }
                clientData = response.body();
                if (clientData.getClientID() != 0) {
                    PhoneAuthOptions options =
                            PhoneAuthOptions.newBuilder(auth)
                                    .setPhoneNumber("+95" + phone)       // Phone number to verify
                                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                    .setActivity(SignInActivity.this)                 // Activity (for callback binding)
                                    .setCallbacks(mCallback)          // OnVerificationStateChangedCallbacks
                                    .build();
                    PhoneAuthProvider.verifyPhoneNumber(options);
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(context, getResources().getString(R.string.registration_not_found), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ClientData> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(context, t.getMessage(), Toast.LENGTH_LONG).show();
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
    }

    private void startFirebaseLogin() {
        auth = FirebaseAuth.getInstance();
        mCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                progressDialog.dismiss();
                Toast.makeText(context,getResources().getString(R.string.verification_completed), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                progressDialog.dismiss();
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(verificationId, forceResendingToken);
                verificationCode = verificationId;
                Toast.makeText(context,getResources().getString(R.string.code_sent),Toast.LENGTH_SHORT).show();
                Intent i = new Intent(SignInActivity.this, OTPConfirmActivity.class);
                i.putExtra("ClientData", (Parcelable) clientData);
                i.putExtra("VerificationCode",verificationCode);
                i.putExtra("IsFromSignIn", true);
                i.putExtra("Phone",etPhone.getText().toString());
                startActivity(i);
                finish();
            }
        };
    }
}