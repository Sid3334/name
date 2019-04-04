package com.example.closest;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class driverLoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    private EditText driverEmail, driverPassword;
    private Button driverRegister, driverLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login);

        mAuth = FirebaseAuth.getInstance();

        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user!=null)
                {
                    Intent intent = new Intent(driverLoginActivity.this,driverMapActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }

            }
        };


        driverEmail = findViewById(R.id.etCustomerEmail);
        driverPassword = findViewById(R.id.etCustomerPassword);
        driverRegister = findViewById(R.id.btnRegister);
        driverLogin = findViewById(R.id.btnLogin);


        driverRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = driverEmail.getText().toString().trim();
                final String password = driverPassword.getText().toString().trim();
                if(Patterns.EMAIL_ADDRESS.matcher(email).matches()&& !email.isEmpty() && !password.isEmpty())
                {
                    mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(driverLoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {
                                String user_id = mAuth.getCurrentUser().getUid();
                                DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(user_id);
                                current_user_db.setValue(true);
                                Toast.makeText(getApplicationContext(),"Registered successfully",Toast.LENGTH_SHORT).show();
                            }

                            else
                            {


                                Toast.makeText(getApplicationContext(),"Something went wrong! please try again later",Toast.LENGTH_LONG).show();


                            }


                        }
                    });

                }
                else
                {
                    driverEmail.setError("Invalid email syntax");
                    driverEmail.requestFocus();
                }
            }
        });




        driverLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String email = driverEmail.getText().toString().trim();
                final String password = driverPassword.getText().toString().trim();

                if(Patterns.EMAIL_ADDRESS.matcher(email).matches() && !email.isEmpty() && !password.isEmpty())
                {
                    mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(driverLoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(!task.isSuccessful())
                            {
                                Toast.makeText(getApplicationContext(),"Login failed",Toast.LENGTH_SHORT).show();

                            }

                        }
                    });
                }

            }
        });








    }


    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthListener);

    }
}
