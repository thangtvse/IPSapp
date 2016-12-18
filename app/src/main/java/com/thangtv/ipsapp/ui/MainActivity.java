package com.thangtv.ipsapp.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.thangtv.ipsapp.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void collectData(View v) {
        Intent intent = new Intent(this, CollectDataActivity.class);
        startActivity(intent);
    }

    public void positioning(View v) {
        Intent intent = new Intent(this, PositioningActivity.class);
        startActivity(intent);
    }
}
