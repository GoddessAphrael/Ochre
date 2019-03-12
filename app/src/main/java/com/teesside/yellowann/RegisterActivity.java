package com.teesside.yellowann;

import android.content.Intent;


import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity
{
    private Button Register, Cancel;
    private EditText UserEmail, UserPassword, UserPasswordConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        UserEmail = findViewById(R.id.EmailRegister);
        UserPassword = findViewById(R.id.PasswordRegister);
        UserPasswordConfirm = findViewById(R.id.PasswordRegisterConfirm);
        Cancel = findViewById(R.id.CancelButton);
        Register = findViewById(R.id.RegisterButton);


        Register.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UserRegister();
            }
        });

        Cancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });
    }

    private void UserRegister()
    {
        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();
        String password2 = UserPasswordConfirm.getText().toString();

        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(this,"Please enter Email to Register",Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(password))
        {
            Toast.makeText(this,"Please enter Password to Register",Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(password2))
        {
            Toast.makeText(this,"Please confirm Password to Register",Toast.LENGTH_SHORT).show();
        }
        else
        {
            Validate(email, password, password2);
        }
    }

    private boolean isEmailValid(CharSequence email)
    {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean Validate(String email, String password, String password2)
    {
        
    };
}
