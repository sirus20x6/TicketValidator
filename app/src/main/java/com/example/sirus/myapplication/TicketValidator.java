package com.example.sirus.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

public class TicketValidator extends Activity {

    private static ArrayList<String> validTickets = new ArrayList<String>() ;
    private static ArrayList<String> processedTickets = new ArrayList<String>() ;
    private static Context context;
    private static SharedPreferences validTicketsPrefs;
    private static SharedPreferences processedTicketsPrefs;
    private static SharedPreferences urlPrefs;
    private static SharedPreferences.Editor validTicketsPrefsEditor;
    private static SharedPreferences.Editor processedTicketsPrefsEditor;
    private static SharedPreferences.Editor urlPrefsEditor;
    private static String url = "";
    public static ConnectivityManager mConnectivityManager;

    public static void setUrl(String myUrl) {
        if(myUrl != null ){
        url = myUrl;
        }
        if(urlPrefsEditor != null) {
            urlPrefsEditor.clear();
            urlPrefsEditor.putString("url", url);
            urlPrefsEditor.commit();
        }
        SaveData();
    }
    public static String getUrl() {
        return url;
    }

    public static Context getContext() {
        return context;
    }

    public static void setProcessedTickets(ArrayList<String> processed) {
        processedTickets = processed;
    }
    public static void clearProcessedTickets() {
        processedTickets.clear();
        SaveData();
    }

    public static void LoadData()
    {
        String serialized;
        if(validTicketsPrefs.contains("processedTickets")){
            serialized = validTicketsPrefs.getString("processedTickets", null);
            TicketValidator.processedTickets = (ArrayList<String>) Arrays.asList(TextUtils.split(serialized, "\n"));
        }
        if(validTicketsPrefs.contains("validTickets")) {
            serialized = processedTicketsPrefs.getString("validTickets", null);
            TicketValidator.validTickets = (ArrayList<String>) Arrays.asList(TextUtils.split(serialized, "\n"));
        }
        if(validTicketsPrefs.contains("url")) {
            url = urlPrefs.getString("url", null);
        }
    }

    public static void SaveData()
    {
        if (validTicketsPrefsEditor != null && validTickets != null) {
            validTicketsPrefsEditor.clear();
            validTicketsPrefsEditor.putString("validTickets", TextUtils.join(",", validTickets));
            validTicketsPrefsEditor.commit();
        }
        if (processedTicketsPrefsEditor != null && processedTickets != null) {
            processedTicketsPrefsEditor.clear();
            processedTicketsPrefsEditor.putString("processedTickets", TextUtils.join(",", processedTickets));
            processedTicketsPrefsEditor.commit();
        }
        if (urlPrefsEditor != null && url != null) {
            urlPrefsEditor.clear();
            urlPrefsEditor.putString("url", url);
            urlPrefsEditor.commit();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
            SaveData();
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        LoadData();
    }

    private static boolean isNetworkAvailable() {
        mConnectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            String barcode = scanResult.getContents();
            TextView barcodeBox = (TextView) findViewById(R.id.barcodeBox);
            TextView validity = (TextView) findViewById(R.id.isValid);
            barcodeBox.setText(barcode);
            boolean processed = false;

            for (String x : processedTickets) {
                if (barcode.contentEquals(x)) {
                    validity.setText("Already Processed!");
                    processed = true;
                    break;
                }
            }
            if (!processed) {
                for (String s : validTickets) {
                    if (barcode.contentEquals(s)) {
                        validity.setText("Valid!");
                        processedTickets.add(barcode);
                        break;
                    } else {
                        validity.setText("Invalid!");
                    }
                }
            }

        }
    }

    public void scanOnClick(View view){
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.initiateScan();
    }
    public void settings(MenuItem useless) {
        Intent intent = new Intent(this, Settings.class);
        startActivity(intent);
    }

    public static void loadValidFromNet(){
        //validTicketsPrefsEditor.clear();
        validTickets.clear();
        if (isNetworkAvailable()) {
            new Thread(new Runnable() {
                public void run() {
                    DefaultHttpClient httpclient = new DefaultHttpClient();
                    HttpGet httppost = new HttpGet(url);
                    HttpResponse response = null;
                    try {
                        response = httpclient.execute(httppost);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    HttpEntity ht;
                    if (response != null) {
                        ht = response.getEntity();

                        BufferedHttpEntity buf = null;
                        try {
                            buf = new BufferedHttpEntity(ht);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        InputStream is = null;
                        if (buf != null) {
                            try {
                                is = buf.getContent();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        BufferedReader r;
                        if (is != null) {
                            r = new BufferedReader(new InputStreamReader(is));

                            String line;
                            try {
                                while ((line = r.readLine()) != null) {
                                    validTickets.add(line);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                    SaveData();
                }
            }).start();
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        validTicketsPrefs = context.getSharedPreferences("validTickets", Context.MODE_PRIVATE);
        processedTicketsPrefs = context.getSharedPreferences("processedTickets", Context.MODE_PRIVATE);
        urlPrefs = context.getSharedPreferences("url", Context.MODE_PRIVATE);
        validTicketsPrefsEditor = validTicketsPrefs.edit();
        processedTicketsPrefsEditor = processedTicketsPrefs.edit();
        urlPrefsEditor = urlPrefs.edit();
        if (savedInstanceState != null) {
            LoadData();
        }
        if (url.equals("")){
            Intent intent = new Intent(this, Settings.class);
            startActivity(intent);
        }

        setContentView(R.layout.activity_ticket_validator);
        }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.ticket_validator, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
