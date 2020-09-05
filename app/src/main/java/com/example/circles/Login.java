package com.example.circles;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {
    EditText email;
    EditText pass;
    Button login;
    TextView logToReg;
    FirebaseAuth firebaseAuth;
    FirebaseAuth.AuthStateListener authStateListener;
    ProgressDialog progressLoad;
    TextView forgotPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();
        email = findViewById(R.id.email);
        pass = findViewById(R.id.password);
        login = findViewById(R.id.login);
        logToReg = findViewById(R.id.logToReg);
        forgotPass = findViewById(R.id.forgotPass);


        //when Forgot Password button is clicked
        forgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, ResetPassword.class);
                startActivity(intent);
            }
        });

        //check to see if user is logged in with email & pass --> take to HomePage
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null) {
                    Intent intent = new Intent(Login.this, MainActivity.class);
                    startActivity(intent);
                }
            }
        };

        //loading progressDialog after signing in
        progressLoad = new ProgressDialog(this);
        progressLoad.setMessage("Loading...");
        progressLoad.setCancelable(false);
        progressLoad.setIndeterminate(true);



        //if user is logged out then let user type credentials in and click on LOGIN button
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressLoad.show();

                String e = email.getText().toString();
                String p = pass.getText().toString();

                if(e.isEmpty()) {
                    email.setError("Please enter your email");
                    email.requestFocus();
                    progressLoad.dismiss();
                }

                else if(p.isEmpty()) {
                    pass.setError("Please enter your password");
                    pass.requestFocus();
                    progressLoad.dismiss();
                }

                //if all required inputs provided --> login
                else if(!(e.isEmpty() && p.isEmpty())){
                    firebaseAuth.signInWithEmailAndPassword(e, p).addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(!task.isSuccessful()) {
                                Toast.makeText(Login.this, "Login unsuccessful! Please login again", Toast.LENGTH_SHORT).show();
                                progressLoad.dismiss();
                            }
                            else {
                                FirebaseUser user = firebaseAuth.getCurrentUser();
                                Intent intent = new Intent(Login.this, MainActivity.class);
                                startActivity(intent);
                            }
                        }
                    });
                }
                else {
                    Toast.makeText(Login.this, "Error occurred!", Toast.LENGTH_SHORT).show();
                    progressLoad.dismiss();
                }
            }
        });


        //takes user to Register screen if he/she has no account
        logToReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, Register.class);
                startActivity(intent);
            }
        });
    }

    //called in UI thread every time user changes authentication state
    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }
}
