package com.widy.appwidy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.widy.appwidy.membaca.MemoryActivity;
import com.widy.appwidy.membaca.ReaderActivity;

public class BacaActivity extends AppCompatActivity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baca);


        Button btnRead = findViewById(R.id.btn_read);
        btnRead.setOnClickListener(this);

        Button btnReadMemory = findViewById(R.id.btn_read_memory);
        btnReadMemory.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_read) {
            Intent readIntent = new Intent(BacaActivity.this, ReaderActivity.class);
            startActivity(readIntent);
        } else if (view.getId() == R.id.btn_read_memory) {
            Intent readmemoryIntent = new Intent(BacaActivity.this, MemoryActivity.class);
            startActivity(readmemoryIntent);
        }
    }
}
