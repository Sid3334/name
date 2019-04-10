package com.example.closest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class driver2Activity extends AppCompatActivity {
    Button map,details;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver2);
        map = findViewById(R.id.btnMap);
        details = findViewById(R.id.btnDetails);
        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(driver2Activity.this,driverMapActivity.class);
                startActivity(intent);

            }
        });


        details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(driver2Activity.this,driverDetailActivity.class);
                startActivity(intent);

            }
        });
    }
}
