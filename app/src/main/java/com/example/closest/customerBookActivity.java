package com.example.closest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class customerBookActivity extends AppCompatActivity {
    Spinner spinner;
    Button map;
    EditText name,phone,quantity;
    String nam,phn,wsttype,quan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_book);
        map = findViewById(R.id.btnOpenMap);
        name = findViewById(R.id.etName);
        phone = findViewById(R.id.etPhone);
        quantity = findViewById(R.id.etQuantity);
        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("CustomerRequests").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                if (name.getText().toString().trim().length()>0 && phone.getText().toString().trim().length()>0) {
                    nam = name.getText().toString().trim();
                    phn = phone.getText().toString().trim();
                    wsttype = spinner.getSelectedItem().toString().trim();
                    quan = quantity.getText().toString().trim();
                    //reference.child("Name").setValue(name.getText().toString().trim());
                    //reference.child("Phone").setValue(phone.getText().toString().trim());
                    //reference.child("Type of waste").setValue(wsttype);

                    Intent intent = new Intent(customerBookActivity.this, customerMapActivity.class);
                    intent.putExtra("Name",nam);
                    intent.putExtra("Phone",phn);
                    intent.putExtra("Type",wsttype);
                    intent.putExtra("Quantity",quan);
                    startActivity(intent);
                    finish();
                }
                else
                {
                    Toast.makeText(customerBookActivity.this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });
        spinner = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.typesOfWaste, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spinner.setAdapter(adapter);

    }
}
