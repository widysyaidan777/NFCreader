package com.widy.appwidy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button btnReader = findViewById(R.id.btn_readnfc);
        btnReader.setOnClickListener(this);



        Button btnWrite = findViewById(R.id.btn_write);
        btnWrite.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_readnfc) {
            Intent readnfcIntent = new Intent(MainActivity.this, ReadActivity.class);
            startActivity(readnfcIntent);
        } else if (view.getId() == R.id.btn_write) {
            Intent writeIntent = new Intent(MainActivity.this, WriteActivity.class);
            startActivity(writeIntent);
        }
    }
}


