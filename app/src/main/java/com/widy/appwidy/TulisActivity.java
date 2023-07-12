package com.widy.appwidy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import com.widy.appwidy.menulis.WriteActivity;

public class TulisActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tulis);


        Button btnWrite1 = findViewById(R.id.btn_write1);
        btnWrite1.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_write1) {
            Intent tulis1Intent = new Intent(TulisActivity.this, WriteActivity.class);
            startActivity(tulis1Intent);
        }
    }
}
