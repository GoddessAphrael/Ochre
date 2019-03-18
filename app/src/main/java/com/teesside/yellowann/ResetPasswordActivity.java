package com.teesside.yellowann;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {
    private Button Cancel, ResetPassword;
    private EditText UserEmail;
    private FirebaseAuth mAuth;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        mAuth = FirebaseAuth.getInstance();

        Cancel = findViewById(R.id.CancelButton);
        ResetPassword = findViewById(R.id.ResetButton);
        UserEmail = findViewById(R.id.EmailRecovery);
        UserEmail.setText(getIntent().getStringExtra("email"));

        Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ResetPassword.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String email = UserEmail.getText().toString();
                String TAG = "ResetPasswordActivity.ResetPassword";

                if (TextUtils.isEmpty(email))
                {
                    Log.w(TAG, "ResetPassword:failure");
                    Toast.makeText(ResetPasswordActivity.this, "Please enter Email", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    mAuth.sendPasswordResetEmail(UserEmail.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<Void>()
                            {
                                String TAG = "ResetPasswordActivity.ResetPassword";

                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if(task.isSuccessful())
                                    {
                                        Log.d(TAG, "sendPasswordResetEmail:success");
                                        Toast.makeText(ResetPasswordActivity.this,
                                                "Please check Email for Password Reset", Toast.LENGTH_SHORT).show();
                                    }
                                    else
                                    {
                                        Log.w(TAG, "sendPasswordResetEmail:failure", task.getException());
                                        try
                                        {
                                            Toast.makeText(ResetPasswordActivity.this,"Unable to Send Password Reset: "
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
        });
    }
}
