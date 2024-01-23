package com.example.mydiary;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //FirebaseAuth.getInstance().signOut();  // used to clear cache
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if(currentUser == null) {
                    Log.d("tag", "No user found, redirecting to LoginActivity");
                    startActivity(new Intent(SplashActivity.this, AfterSplash1.class));
                } else {
                    Log.d("tag", "User found, redirecting to MainActivity");
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                }
                finish();  // when we go back, it terminates the activity
            }
        }, 2500);
    }

}
