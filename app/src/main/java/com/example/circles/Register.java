package com.example.circles;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class Register extends AppCompatActivity {
    EditText name;
    EditText email;
    EditText mobile;
    EditText pass;
    Button register;
    FirebaseAuth firebaseAuth;
    DatabaseReference reference;
    TextView regToLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //setup toolbar w/ back button
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //initializations
        firebaseAuth = FirebaseAuth.getInstance();
        name = findViewById(R.id.userName);
        email = findViewById(R.id.email);
        mobile = findViewById(R.id.mobile);
        pass = findViewById(R.id.password);
        register = findViewById(R.id.register);
        regToLog = findViewById(R.id.regToLog);


        //check to see if user provided all required info after REGISTER clicked
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String n = name.getText().toString();
                final String e = email.getText().toString();
                final String m = mobile.getText().toString();
                final String p = pass.getText().toString();

                if(n.isEmpty()) {
                    name.setError("Please enter your name");
                    name.requestFocus();
                }
                else if(e.isEmpty()) {
                    email.setError("Please enter your email");
                    email.requestFocus();
                }
                else if(m.isEmpty()) {
                    mobile.setError("Please enter your phone number");
                    mobile.requestFocus();
                }
                else if(p.isEmpty()) {
                    pass.setError("Please enter your password");
                    pass.requestFocus();
                }
                //if all required inputs are provided --> create user in firebase
                else if(!(n.isEmpty() && e.isEmpty() && m.isEmpty() && p.isEmpty())){
                    firebaseAuth.createUserWithEmailAndPassword(e, p)
                            .addOnCompleteListener(Register.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()) {
                                        FirebaseUser user = firebaseAuth.getCurrentUser();
                                        String userID = user.getUid();

                                        //get database reference to store into and see user credentials in firebase website
                                        reference = FirebaseDatabase.getInstance().getReference("USERS").child(userID);

                                        HashMap<String, String> hashMap = new HashMap<>();
                                        hashMap.put("ID", userID);
                                        hashMap.put("Name", n);
                                        hashMap.put("Email", e);
                                        hashMap.put("Mobile", m);
                                        hashMap.put("ProfilePic", "default");

                                        reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()) {
                                                    Intent intent = new Intent(Register.this, MainActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            }
                                        });
                                    }
                                    else {
                                        Toast.makeText(Register.this, "Register unsuccessful! Please try again", Toast.LENGTH_SHORT).show();
                                    }
                                }
                        });
                }
                //if register not successful --> error
                else {
                    Toast.makeText(Register.this, "Error occurred!", Toast.LENGTH_SHORT).show();
                }
            }
        });


        //takes user to Register screen after button clicked bc he/she has no account
        regToLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Register.this, Login.class);
                startActivity(intent);
            }
        });
    }
}

