package com.example.closest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class customer2Activity extends AppCompatActivity {
    private Button book, previous, dLocation;
    EditText name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer2);
        book = findViewById(R.id.btnBook);
        previous = findViewById(R.id.btnPrevious);
        dLocation = findViewById(R.id.btnDriverLocation);

        book.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent  = new Intent(customer2Activity.this,customerBookActivity.class);
                startActivity(intent);
                finish();

            }
        });

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        dLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                Intent intent = new Intent(customer2Activity.this,customerMapActivity.class);
                startActivity(intent);
                finish();

            }
        });
    }
}
