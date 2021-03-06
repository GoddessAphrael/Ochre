package com.teesside.yellowann;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity
{
    private FirebaseAuth mAuth;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
    }

    // initiate MainActivity if mAuth exists, else display LoginFragment
    protected void onStart()
    {
        super.onStart();

        getSupportFragmentManager().beginTransaction().replace(R.id.login_fragment_container,
                new LoginFragment()).addToBackStack(null).commit();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null)
        {
            Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(mainIntent);
            finish();
        }
    }
}
