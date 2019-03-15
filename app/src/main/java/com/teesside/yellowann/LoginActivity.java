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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity
{

    private Button Register, Login;
    private EditText UserEmail, UserPassword;
    private FirebaseAuth mAuth;
    private TextView PasswordReset;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        UserEmail = findViewById(R.id.EmailEntry);
        UserPassword = findViewById(R.id.PasswordEntry);
        Login = findViewById(R.id.LoginButton);
        Register = findViewById(R.id.RegisterButton);
        PasswordReset = findViewById((R.id.PasswordRecovery));

        Register.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String email = UserEmail.getText().toString();
                String password = UserPassword.getText().toString();

                boolean valid = validate(email, password);

                if (valid)
                {
                    userRegister(email, password);
                }
                else
                {
                    String TAG ="LoginActivity.Register.onClick";
                    Log.w(TAG, "Register.onClick:failure");
                }
            }
        });

        Login.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String email = UserEmail.getText().toString();
                String password = UserPassword.getText().toString();

                userLogin(email, password);
            }
        });

        PasswordReset.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendToPasswordReset();
            }
        });
    }

    protected void onStart()
    {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null)
        {
            sendToMain();
        }
    }

    private void userRegister(String email, String password)
    {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        String TAG = "LoginActivity.userRegister";
                        if (task.isSuccessful())
                        {
                            Log.d(TAG, "createUserWithEmailAndPassword:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            user.sendEmailVerification()
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            String TAG = "LoginActivity.userRegister.EmailVerify";
                                            if (task.isSuccessful())
                                            {
                                                Log.d(TAG, "sendEmailVerification:success");
                                                Toast.makeText(LoginActivity.this,
                                                        "Please check Email for Verification",Toast.LENGTH_SHORT).show();
                                            }
                                            else
                                            {
                                                Log.w(TAG, "sendEmailVerification:failure", task.getException());
                                                try
                                                {
                                                    Toast.makeText(LoginActivity.this,"Unable to Verify: "
                                                            + task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                                }
                                                catch (NullPointerException e)
                                                {

                                                }
                                            }
                                        }
                                    });
                        }
                        else
                        {
                            Log.w(TAG, "createUserWithEmailAndPassword:failure", task.getException());
                            try
                            {
                                Toast.makeText(LoginActivity.this, "Unable to Register: "
                                        + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                            catch (NullPointerException e)
                            {

                            }
                        }
                    }
                });
    }

    private boolean isEmailValid(CharSequence email)
    {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isPasswordValid(String password)
    {
        Pattern PASSWORD_PATTERN = Pattern.compile("((?=.*[a-z])(?=.*\\d)(?=.*[A-Z])(?=.*[!£$%^&*#@?+=_:;,.<>()']).{8,16})");

        return PASSWORD_PATTERN.matcher(password).matches();
    }

    private boolean validate(String email, String password)
    {
        String TAG = "LoginActivity.validate";

        if(TextUtils.isEmpty(email))
        {
            Log.w(TAG, "emailIsEmpty:failure");
            Toast.makeText(this,"Please enter Email",Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(password))
        {
            Log.w(TAG, "passwordIsEmpty:failure");
            Toast.makeText(this,"Please enter Password",Toast.LENGTH_SHORT).show();
        }
        else
        {
            boolean temp = isEmailValid(email);

            if (temp) {
                Log.d(TAG, "isEmailValid:success");
                temp = isPasswordValid(password);

                if (!temp) {
                    Log.w(TAG, "isPasswordValid:failure");
                    Toast.makeText(this, "Invalid Password - Password must be 8 to 16 characters, " +
                            "contain one lower case character, one upper case character and one symbol", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.w(TAG, "isEmailValid:failure");
                Toast.makeText(this, "Invalid Email Format - Please Check and Retry", Toast.LENGTH_SHORT).show();
            }
            return temp;
        }
        return false;
    }

    private void userLogin(String email, String password)
    {
        String TAG = "LoginActivity.userLogin";
        if(TextUtils.isEmpty(email))
        {
            Log.w(TAG, "emailIsEmpty:failure");
            Toast.makeText(this,"Please enter Email",Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(password))
        {
            Log.w(TAG, "passwordIsEmpty:failure");
            Toast.makeText(this,"Please enter Password",Toast.LENGTH_SHORT).show();
        }
        else
        {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            String TAG ="LoginActivity.userLogin";
                            if (task.isSuccessful())
                            {
                                Log.d(TAG, "signInWithEmailAndPassword:success");
                                sendToMain();
                            }
                            else
                            {
                                Log.w(TAG, "signInWithEmailAndPassword:failure", task.getException());
                                try
                                {
                                Toast.makeText(LoginActivity.this,"Unable to Login: "
                                        + task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                }
                                catch (NullPointerException e)
                                {

                                }
                            }
                        }
                    });
        }
    }

    private void sendToPasswordReset()
    {
        Intent resetIntent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
        startActivity(resetIntent);
    }

    private void sendToMain()
    {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
