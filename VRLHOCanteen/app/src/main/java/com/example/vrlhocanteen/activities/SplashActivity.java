package com.example.vrlhocanteen.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.example.vrlhocanteen.R;
import com.example.vrlhocanteen.Util.AU;


public class SplashActivity extends AppCompatActivity {
    String userName,pass;
    boolean isLoggedIn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        SharedPreferences preferences = getSharedPreferences(AU.SP.MyPREFERENCES,0);
        userName = preferences.getString(AU.SP.USN,"");
        pass = preferences.getString(AU.SP.PASS,"");
        isLoggedIn = preferences.getBoolean(AU.SP.IS_LOGGED_IN,false);
        Log.e("TAG ","MainActivity, onCreate, userName: "+userName+" password: "+pass+" isLoggedIn: "+isLoggedIn);
        if(!preferences.getBoolean(AU.SP.IS_LOGGED_IN,false)){
            procudeNext();
        }else {
            final Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000 * 3);
                    }catch (Exception ee) {
                        Toast.makeText(SplashActivity.this, "Exception "+ee.toString(), Toast.LENGTH_SHORT).show();
                    }finally {
                        startActivity(new Intent(SplashActivity.this,MainActivity.class));
                        SplashActivity.this.finish();
                    }
                }
            });
            thread.start();
        }
    }

    public void procudeNext(){
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000 * 3);
                }catch (Exception ee) {
                    Toast.makeText(SplashActivity.this, "Exception "+ee.toString(), Toast.LENGTH_SHORT).show();
                }finally {
                    startActivity(new Intent(SplashActivity.this,LoginActivity.class));
                    SplashActivity.this.finish();
                }
            }
        });
        thread.start();
    }

}
