package com.teesside.yellowann;

import android.content.Intent;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.util.regex.Pattern;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity
{
    private Button Register, Cancel;
    private EditText UserEmail, UserPassword, UserPasswordConfirm;
    private FirebaseAuth mAuth;
    private ActionCodeSettings ActionCodeSettings;

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

        mAuth = FirebaseAuth.getInstance();

        Register.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                userRegister();
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

        ActionCodeSettings = ActionCodeSettings.newBuilder()
            .setUrl("https://www.Oracle.com/finishSignUp?cartId=1234")
                .setHandleCodeInApp(true)
                    .setAndroidPackageName("com.teesside.yellowann", true, 28)
                        .setDynamicLinkDomain("Oracle.page.link")
                            .build();
    }

    private void userRegister()
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
            boolean valid = validate(email, password, password2);

            if (valid)
            {
                registerUser(email, password);
            }
        }
    }

    private boolean isEmailValid(CharSequence email)
    {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isPasswordValid(String password, String password2)
    {
        Pattern PASSWORD_PATTERN = Pattern.compile("((?=.*[a-z])(?=.*\\d)(?=.*[A-Z])(?=.*[!£$%^&*#@?+=_:;,.<>()']).{8,16})");

        return PASSWORD_PATTERN.matcher(password).matches() && password.matches(password2);
    }

    private boolean validate(String email, String password, String password2)
    {
        boolean temp = isEmailValid(email);

        if (temp)
        {
            temp = isPasswordValid(password, password2);

            if (!temp)
            {
                Toast.makeText(this,"Invalid Password - Please Check and Retry",Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            Toast.makeText(this,"Invalid Email Format - Please Check and Retry",Toast.LENGTH_SHORT).show();
        }

        return temp;
    }

    private void registerUser(final String email, String password)
    {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>()
            {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task)
                {
                    if (task.isSuccessful())
                    {
                        mAuth.sendSignInLinkToEmail(email, ActionCodeSettings)
                            .addOnCompleteListener(new OnCompleteListener<Void>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if (task.isSuccessful())
                                    {
                                        Log.d(TAG, "Email sent.");
                                    }
                                }
                            });
                    }
                }
            });
    }

}
