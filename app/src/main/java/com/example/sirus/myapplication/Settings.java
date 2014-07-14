package com.example.sirus.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.example.sirus.myapplication.R;

public class Settings extends Activity {
    private static EditText updateUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settingslayout);
        updateUrl = (EditText) findViewById(R.id.urlToUpdate);
        updateUrl.setText(TicketValidator.getUrl());
    }


    public static void flushProcessedTickets(View view){
        TicketValidator.clearProcessedTickets();
    }

    public void updateValidTickets(View view){
        TicketValidator.setUrl(updateUrl.getText().toString());
        TicketValidator.loadValidFromNet();
        Intent intent = new Intent(this, TicketValidator.class);
        startActivity(intent);
    }
}
