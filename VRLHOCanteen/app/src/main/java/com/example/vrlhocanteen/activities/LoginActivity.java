package com.example.vrlhocanteen.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.example.vrlhocanteen.R;
import com.example.vrlhocanteen.Util.AU;
import com.example.vrlhocanteen.Util.GetResponse;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

  /*
   * @param op = lgn
   * @param u = username
   * @param p = password
   */

public class LoginActivity extends AppCompatActivity {

    String url = "http://61.0.236.134/hotel/htl_ajaxpage.aspx";
    private EditText inputName, inputPassword;
    private TextInputLayout inputLayoutName, inputLayoutPassword;
    private Button btnSignin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        inputLayoutName = (TextInputLayout) findViewById(R.id.input_layout_usn);

        inputLayoutPassword = (TextInputLayout) findViewById(R.id.input_layout_password);
        inputName = (EditText) findViewById(R.id.input_usn);

        inputPassword = (EditText) findViewById(R.id.input_password);
        btnSignin = (Button) findViewById(R.id.buttonLogin);
        inputName.addTextChangedListener(new MyTextWatcher(inputName));
        inputPassword.addTextChangedListener(new MyTextWatcher(inputPassword));

        btnSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitForm();
            }
        });
    }

    /**
     * Validating form
     */
    private void submitForm() {
        if (!validateName()) {
            return;
        }

        if (!validatePassword()) {
            return;
        }

        new VerifyLogin().execute(inputName.getText().toString(),inputPassword.getText().toString());
    }

    private boolean validateName() {
        if (inputName.getText().toString().trim().isEmpty()) {
            inputLayoutName.setError("Enter valid emp id");
            requestFocus(inputName);
            return false;
        } else {
            inputLayoutName.setErrorEnabled(false);
        }

        return true;
    }

    private boolean validatePassword() {
        if (inputPassword.getText().toString().trim().isEmpty()) {
            inputLayoutPassword.setError("Enter password");
            requestFocus(inputPassword);
            return false;
        } else {
            inputLayoutPassword.setErrorEnabled(false);
        }

        return true;
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private class MyTextWatcher implements TextWatcher {

        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.input_usn:
                    validateName();
                    break;
                case R.id.input_password:
                    validatePassword();
                    break;
            }
        }
    }

    public void showAlertMessage(String message,String title, String buttonName){

        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setCancelable(false);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setInverseBackgroundForced(true);
        builder.setPositiveButton(buttonName, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    class VerifyLogin extends AsyncTask<String,String,String>{
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(LoginActivity.this);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setMessage("Authenticating...");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("op","lgn"));
            nameValuePairs.add(new BasicNameValuePair("u",strings[0]));
            nameValuePairs.add(new BasicNameValuePair("p",strings[1]));
            GetResponse getResponse = new GetResponse();
            String respTxt = getResponse.getResponse(url,nameValuePairs,"");
            Log.e("TAG","LOGINACTIVITY respTxt "+respTxt);
            return respTxt;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(!s.equalsIgnoreCase("False")){
                Intent ii = new Intent(LoginActivity.this,MainActivity.class);

                SharedPreferences preferences = getSharedPreferences(AU.SP.MyPREFERENCES,0);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(AU.SP.USN,inputName.getText().toString());
                editor.putString(AU.SP.PASS,inputPassword.getText().toString());
                editor.putBoolean(AU.SP.IS_LOGGED_IN,true);
                editor.commit();
                startActivity(ii);
                LoginActivity.this.finish();
            }else {
                showAlertMessage("invalid Emp id or Password","Error","OK");
            }
            progressDialog.dismiss();
        }
    }
}
