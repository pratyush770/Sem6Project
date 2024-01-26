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

public class CreateAccountActivity extends AppCompatActivity {

    EditText email,pass,conpass;
    Button create;
    ProgressBar progressBar;
    TextView login;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        email = findViewById(R.id.editText1);
        pass = findViewById(R.id.editText2);
        conpass = findViewById(R.id.editText3);
        create = findViewById(R.id.button2);
        login = findViewById(R.id.textView4);
        progressBar = findViewById(R.id.progressBar2);
//
        create.setOnClickListener(v->createAccount());  // lambda function
        login.setOnClickListener(v->finish());
    }
    public void createAccount(){
        String myemail = email.getText().toString();
        String mypass = pass.getText().toString();
        String myconpass = conpass.getText().toString();
        boolean isValidate = validateData(myemail,mypass,myconpass);
        if(!isValidate)
        {
            return;  // stops the activity if false
        }
        createAccountInFirebase(myemail,mypass);  // creates account in firebase
    }
    public void createAccountInFirebase(String myemail,String mypass){
        changeInProgress(true);  // for progress bar
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        // creates firebase account with email and password
        firebaseAuth.createUserWithEmailAndPassword(myemail,mypass).addOnCompleteListener(CreateAccountActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                changeInProgress(false);  // shows create account button after completion
                if(task.isSuccessful()){
                    // account is created successfully
                    startActivity(new Intent(CreateAccountActivity.this,LoginActivity.class));
                    //Utility.showToast(CreateAccountActivity.this,"Account created successfully");
                    finish();
                }
                else{
                    // account is not created successfully
                    Utility.showToast(CreateAccountActivity.this, task.getException().getLocalizedMessage());
                }
            }
        });

    }
    public void changeInProgress(boolean inProgress){
        if(inProgress)
        {
            progressBar.setVisibility(View.VISIBLE);  // progress bar becomes visible
            create.setVisibility(View.GONE);  // create account becomes invisible
        }
        else
        {
            progressBar.setVisibility(View.GONE);  // progress bar becomes invisible
            create.setVisibility(View.VISIBLE);  // create account becomes visible
        }
    }
    public boolean validateData(String myemail,String mypass,String myconpass){
        // validate the data which is entered by the user
        if(!Patterns.EMAIL_ADDRESS.matcher(myemail).matches()){
            email.setError("Invalid email");
            return false;
        }
        if(mypass.length()<8){
            pass.setError("Password should contain at least 8 characters");
            return false;
        }
        if(!mypass.equals(myconpass)){
            conpass.setError("Passwords do not match");
            return false;
        }
        return true;
    }
}