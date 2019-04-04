package com.example.closest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class mapActivity extends AppCompatActivity {
    private Button logout;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mAuth=FirebaseAuth.getInstance();

        logout = findViewById(R.id.btnLogout);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                Intent intent = new Intent(mapActivity.this,driverLoginActivity.class);
                startActivity(intent);
                Toast.makeText(getApplicationContext(),"logged out",Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        });
    }
}
