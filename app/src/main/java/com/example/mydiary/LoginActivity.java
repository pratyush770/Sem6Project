package com.example.mydiary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    EditText email,pass;
    Button login;
    ProgressBar progressBar;
    TextView signup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        email = findViewById(R.id.editText1);
        pass = findViewById(R.id.editText2);
        login = findViewById(R.id.button2);
        signup = findViewById(R.id.textView4);
        progressBar = findViewById(R.id.progressBar2);

        login.setOnClickListener((v)->loginUser());
        signup.setOnClickListener((v)->startActivity(new Intent(LoginActivity.this,CreateAccountActivity.class)));
    }

    public void loginUser(){
        String myemail = email.getText().toString();
        String mypass = pass.getText().toString();
        boolean isValidate = validateData(myemail,mypass);
        if(!isValidate)
        {
            return;  // stops the activity if false
        }
        loginAccountInFirebase(myemail,mypass);
    }
    public void loginAccountInFirebase(String myemail,String mypass){
        changeInProgress(true);  // for progress bar
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInWithEmailAndPassword(myemail,mypass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                changeInProgress(false);  // shows login button after completion
                if(task.isSuccessful()){
                    // account logged in successfully
                    startActivity(new Intent(LoginActivity.this,MainActivity.class));
                    //Utility.showToast(LoginActivity.this, "Logged in successfully");
                    finish();
                }
                else{
                    // account is not logged in successfully
                    Utility.showToast(LoginActivity.this, task.getException().getLocalizedMessage());
                }
            }
        });

    }
    public void changeInProgress(boolean inProgress){
        if(inProgress)
        {
            progressBar.setVisibility(View.VISIBLE);  // progress bar becomes visible
            login.setVisibility(View.GONE);  // create account becomes invisible
        }
        else
        {
            progressBar.setVisibility(View.GONE);  // progress bar becomes invisible
            login.setVisibility(View.VISIBLE);  // create account becomes visible
        }
    }
    public boolean validateData(String myemail,String mypass){
        // validate the data which is entered by the user
        if(!Patterns.EMAIL_ADDRESS.matcher(myemail).matches()){
            email.setError("Invalid email");
            return false;
        }
        if(mypass.length()<8){
            pass.setError("Password should contain at least 8 characters");
            return false;
        }
        return true;
    }
}