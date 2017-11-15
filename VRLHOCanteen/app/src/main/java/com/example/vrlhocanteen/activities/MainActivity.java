package com.example.vrlhocanteen.activities;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.vrlhocanteen.R;
import com.example.vrlhocanteen.Util.AU;
import com.example.vrlhocanteen.Util.GetResponse;
import com.example.vrlhocanteen.Util.MDevice;
import com.example.vrlhocanteen.interphases.MCallBack;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    public final String ACTION_USB_PERMISSION = "com.vrl.lrbhat.usbproject10.USB_PERMISSION";
    private static UsbManager manager = null;
    private static UsbDevice device = null;
    private static UsbSerialDevice serialPort=null;
    private static UsbDeviceConnection connection=null;
    MDevice mDevice = new MDevice();
    private RadioGroup radioQuantity;
    private RadioButton radioQuantityButtons;
    String userName ,pass ;
    boolean isLoggedIn;
    WebView webViewRecentTrancation;
    EditText editTextQuantity;

    private void getDevice() {
        try {
            manager = (UsbManager) getSystemService(Context.USB_SERVICE);
            HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
            for (Map.Entry<String, UsbDevice> entry : deviceList.entrySet()) {
                UsbDevice d = entry.getValue();
                int deviceId=d.getVendorId();
                mDevice.setDeviceId(deviceId);
                if ( deviceId == 9025 || deviceId == 6790) //Arduino Vendor ID
                {
                    //found our Arduino device
                    device=d;
                    PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                    manager.requestPermission(device, pi);
                    String messageToast = "Device Connected is: "+device.getDeviceName()+" Ven Id : "+device.getVendorId()+" Prod Id : "+device.getProductId();
                    Toast.makeText(MainActivity.this, messageToast , Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e){
            Toast.makeText(this,"Error in getDevice : "+e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
        }
    }

    final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {

                boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) {
                    connection = manager.openDevice(device);
                    serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
                    if (serialPort != null) {

                        if (serialPort.open()) {

                            int baudRate;

                            if(mDevice.getDeviceId() == 6790) {
                                baudRate = 9600;
                            } else if(mDevice.getDeviceId() == 9025){
                                baudRate = 250000;
                            } else {
                                baudRate = 9600;
                            }
                            serialPort.setBaudRate(baudRate);
                            serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                            serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                            serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                            serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                            serialPort.read(new MCallBack() {
                                @Override
                                public void onDataRecieveComplete(final String dataRecieved) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            MediaPlayer mPlayer = MediaPlayer.create(MainActivity.this, R.raw.beep);
                                            mPlayer.start();
                                            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                                @Override
                                                public void onCompletion(MediaPlayer mediaPlayer) {
                                                  runOnUiThread(new Runnable() {
                                                      @Override
                                                      public void run() {
                                                          String data[] = dataRecieved.split("~");
                                                          String qty = editTextQuantity.getText().toString();
                                                          String message;
                                                          if(qty.equals("1")) {
                                                              message  = "Confirm "+qty+" cup of Tea for "+data[3];
                                                          } else {
                                                              message  = "Confirm "+qty+" cups of Tea for "+data[3];
                                                          }
                                                          String stendData = dataRecieved.replace("~","$$");
                                                          stendData = stendData+"$$"+qty+"$$"+userName+"$$"+pass;
                                                          showAlertMessageWithAction(message, "Confirm",stendData);
                                                      }
                                                  });
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }else {
                            Log.d("SERIAL", "PORT NOT OPEN");
                            showAlertMessage("PORT NOT OPEN","Warning","RELOAD");
                        }
                    }else {
                        Log.d("SERIAL", "PORT IS NULL");
                    }
                }
            }else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
            //Toast.makeText(MainActivity.this, "USB ATTACHED", Toast.LENGTH_SHORT).show();
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                //Toast.makeText(ContactlessCardActivity.this, "USB DETACHED", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        init();
        registerReciever();
        getDevice();
        radioQuantity.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {

                radioQuantityButtons = (RadioButton) findViewById(checkedId);

                switch (checkedId){

                    case R.id.one:
                        editTextQuantity.setEnabled(false);
                        editTextQuantity.setText(radioQuantityButtons.getText().toString());
                        break;

                    case R.id.two:
                        editTextQuantity.setEnabled(false);
                        editTextQuantity.setText(radioQuantityButtons.getText().toString());
                        break;

                    case R.id.three:
                        editTextQuantity.setEnabled(false);
                        editTextQuantity.setText(radioQuantityButtons.getText().toString());
                        break;

                    case R.id.four:
                        editTextQuantity.setEnabled(false);
                        editTextQuantity.setText(radioQuantityButtons.getText().toString());
                        break;

                    case R.id.five:
                        editTextQuantity.setEnabled(false);
                        editTextQuantity.setText(radioQuantityButtons.getText().toString());
                        break;

                    case R.id.more:
                        editTextQuantity.setEnabled(true);
                        editTextQuantity.setText("");
                        break;
                }
            }
        });
        if(isNetworkAvailable()){

            webViewRecentTrancation.loadUrl("http://61.0.236.134/hotel/CNTN.aspx?e="+userName+"");
            // webViewRecentTrancation.setWebViewClient(new WebViewClient());
        } else {
            webViewRecentTrancation.loadUrl("file:///android_asset/www/noInternetConnection.html");
        }
    }

    public void init() {
        SharedPreferences preferences = getSharedPreferences(AU.SP.MyPREFERENCES,0);
        userName = preferences.getString(AU.SP.USN,"");
        pass = preferences.getString(AU.SP.PASS,"");
        isLoggedIn = preferences.getBoolean(AU.SP.IS_LOGGED_IN,false);
        Log.e("TAG ","ContactLessCardActivity, onCreate, userName: "+userName+" password: "+pass+" isLoggedIn: "+isLoggedIn);

        webViewRecentTrancation = (WebView) findViewById(R  .id.webViewTransactionDetails);
        webViewRecentTrancation.getSettings().setJavaScriptEnabled(true);
        webViewRecentTrancation.getSettings().setLoadsImagesAutomatically(true);
        webViewRecentTrancation.clearCache(true);
        radioQuantity = (RadioGroup) findViewById(R.id.radioQuantity);
        editTextQuantity = (EditText) findViewById(R.id.quantity);
        int selectedId = radioQuantity.getCheckedRadioButtonId();
        if(selectedId != R.id.more){
            radioQuantityButtons = (RadioButton) findViewById(selectedId);
            editTextQuantity.setText(""+radioQuantityButtons.getText().toString());
        }

    }

    public void registerReciever() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(broadcastReceiver, filter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id){
            case R.id.action_reload:
                startActivity(new Intent(MainActivity.this,MainActivity.class));
                MainActivity.this.finish();
                return true;

            case R.id.action_settings:
                SharedPreferences preferences = getSharedPreferences(AU.SP.MyPREFERENCES,0);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(AU.SP.IS_LOGGED_IN,false);
                editor.putString(AU.SP.USN,"");
                editor.putString(AU.SP.PASS,"");
                editor.commit();
                onBackPressed();
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    public void showAlertMessageWithAction(String message, String title, final String dataToServer) {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setInverseBackgroundForced(true);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //new SubmitDataToServer().execute(dataToServer);
                //showAlertMessage(dataToServer,"Data is","OK");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new SubmitDataToServer().execute(dataToServer);
                    }
                });
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void showAlertMessage(String message,String title, String buttonName){

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setCancelable(false);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setInverseBackgroundForced(true);
        builder.setPositiveButton(buttonName, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startActivity(new Intent(MainActivity.this,MainActivity.class));
                MainActivity.this.finish();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    class SubmitDataToServer extends AsyncTask<String,Void,String> {

        ProgressDialog progressDialog;
        String url = "http://61.0.236.134/hotel/htl_ajaxpage.aspx";
        
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Please Wait");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            GetResponse getResponse = new GetResponse();
            Log.e("TAG",strings[0]);
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("op", "RF"));
            nameValuePairs.add(new BasicNameValuePair("SQL", strings[0]));
            Log.e("TAG", "nameValuePairs " + nameValuePairs);
            String responseText = getResponse.getResponse(url, nameValuePairs, "");
            return responseText;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.dismiss();
            if(s.equalsIgnoreCase("Success")){
                showAlertMessage("Transcation Completed...","Success","Done");
            }else{
                showAlertMessage("Transction failed","Fail..","Re try");
            }
        }
    }

}
