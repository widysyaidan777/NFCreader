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


        Button btnBaca = findViewById(R.id.btn_baca);
        btnBaca.setOnClickListener(this);


        Button btnTulis = findViewById(R.id.btn_tulis);
        btnTulis.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_baca) {
            Intent bacaIntent = new Intent(MainActivity.this, BacaActivity.class);
            startActivity(bacaIntent);
        } else if (view.getId() == R.id.btn_tulis) {
            Intent tulisIntent = new Intent(MainActivity.this, TulisActivity.class);
            startActivity(tulisIntent);
        }
    }
}


