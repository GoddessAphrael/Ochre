package com.teesside.yellowann;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class LoginFragment extends Fragment
{
    private Button register, login;
    private EditText userEmail, userPassword;
    private FirebaseAuth mAuth;
    private TextView passwordReset;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState)
    {
        super.onViewCreated(v, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        userEmail = v.findViewById(R.id.EmailEntry);
        userPassword = v.findViewById(R.id.PasswordEntry);
        login = v.findViewById(R.id.LoginButton);
        register = v.findViewById(R.id.RegisterButton);
        passwordReset = v.findViewById((R.id.PasswordRecovery));

        register.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String email = userEmail.getText().toString();
                String password = userPassword.getText().toString();

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

        login.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String email = userEmail.getText().toString();
                String password = userPassword.getText().toString();

                userLogin(email, password);
            }
        });

        passwordReset.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendToPasswordReset(v);
            }
        });
    }

    // register new user with firebase
    private void userRegister(String email, String password)
    {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        // if successful, send email verification
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
                                                Toast.makeText(getActivity(),
                                                        "Please check Email for Verification",Toast.LENGTH_SHORT).show();
                                            }
                                            else
                                            {
                                                Log.w(TAG, "sendEmailVerification:failure", task.getException());
                                                Toast.makeText(getActivity(),"Unable to Verify: "
                                                        + task.getException().getMessage(),Toast.LENGTH_SHORT).show();

                                            }
                                        }
                                    });
                        }
                        else
                        {
                            Log.w(TAG, "createUserWithEmailAndPassword:failure", task.getException());
                            Toast.makeText(getActivity(), "Unable to Register: "
                                    + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // validate format
    private boolean isEmailValid(CharSequence email)
    {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // validate format
    private boolean isPasswordValid(String password)
    {
        Pattern PASSWORD_PATTERN = Pattern.compile("((?=.*[a-z])(?=.*\\d)(?=.*[A-Z])(?=.*[!£$%^&*#@?+=_:;,.<>()']).{8,16})");

        return PASSWORD_PATTERN.matcher(password).matches();
    }

    private boolean validate(String email, String password)
    {
        String TAG = "LoginActivity.validate";

        // validate foo is not null
        if(TextUtils.isEmpty(email))
        {
            Log.w(TAG, "emailIsEmpty:failure");
            Toast.makeText(getActivity(),"Please enter Email",Toast.LENGTH_SHORT).show();
        }
        // validate bar is not null
        else if (TextUtils.isEmpty(password))
        {
            Log.w(TAG, "passwordIsEmpty:failure");
            Toast.makeText(getActivity(),"Please enter Password",Toast.LENGTH_SHORT).show();
        }
        else
        {
            // validate foo format
            boolean temp = isEmailValid(email);

            if (temp)
            {
                Log.d(TAG, "isEmailValid:success");

                // validate bar format
                temp = isPasswordValid(password);

                if (!temp)
                {
                    Log.w(TAG, "isPasswordValid:failure");
                    Toast.makeText(getActivity(), "Invalid Password - Password must be 8 to 16 characters, " +
                            "contain one lower case character, one upper case character and one symbol", Toast.LENGTH_SHORT).show();
                }
            }
            else
             {
                Log.w(TAG, "isEmailValid:failure");
                Toast.makeText(getActivity(), "Invalid Email Format - Please Check and Retry", Toast.LENGTH_SHORT).show();
            }
            return temp;
        }
        return false;
    }

    private void userLogin(String email, String password)
    {
        String TAG = "LoginActivity.userLogin";
        // validate foo is not null
        if(TextUtils.isEmpty(email))
        {
            Log.w(TAG, "emailIsEmpty:failure");
            Toast.makeText(getActivity(),"Please enter Email",Toast.LENGTH_SHORT).show();
        }
        // validate bar not null
        else if (TextUtils.isEmpty(password))
        {
            Log.w(TAG, "passwordIsEmpty:failure");
            Toast.makeText(getActivity(),"Please enter Password",Toast.LENGTH_SHORT).show();
        }
        else
        {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            String TAG = "LoginActivity.userLogin";
                            if (task.isSuccessful())
                            {
                                Log.d(TAG, "signInWithEmailAndPassword:success");
                                sendToMain();
                            }
                            else
                            {
                                Log.w(TAG, "signInWithEmailAndPassword:failure", task.getException());
                                Toast.makeText(getActivity(), "Unable to Login: "
                                        + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    // initiate MainActivity
    private void sendToMain()
    {
        Intent mainIntent = new Intent(getActivity(), MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        getActivity().finish();
    }

    // initiate ResetPasswordFragment
    private void sendToPasswordReset(View v)
    {
        ResetPasswordFragment reset = new ResetPasswordFragment();

        Bundle bundle = new Bundle();
        bundle.putString("email", userEmail.getText().toString());
        reset.setArguments(bundle);

        getFragmentManager().beginTransaction().replace(R.id.login_fragment_container,
                reset).addToBackStack(null).commit();
    }
}
