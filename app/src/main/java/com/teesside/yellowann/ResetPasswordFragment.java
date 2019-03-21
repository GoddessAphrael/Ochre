package com.teesside.yellowann;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordFragment extends Fragment
{
    private Button Cancel, ResetPassword;
    private EditText UserEmail;
    private FirebaseAuth mAuth;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

        if (actionBar != null) { actionBar.setTitle(R.string.reset); }
        return inflater.inflate(R.layout.fragment_reset_password, container, false);
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState)
    {
        super.onViewCreated(v, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        Cancel = v.findViewById(R.id.CancelButton);
        ResetPassword = v.findViewById(R.id.ResetButton);
        UserEmail = v.findViewById(R.id.EmailRecovery);

        Bundle arguments = getArguments();
        if (arguments != null)
        {
            String email  = arguments.getString("email");
            UserEmail.setText(email);
        }

        Cancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                getActivity().onBackPressed();
            }
        });

        ResetPassword.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String email = UserEmail.getText().toString();
                String TAG = "ResetPasswordFragment.ResetPassword";

                if (TextUtils.isEmpty(email))
                {
                    Log.w(TAG, "ResetPassword:failure");
                    Toast.makeText(getActivity(), "Please enter Email", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    mAuth.sendPasswordResetEmail(UserEmail.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<Void>()
                            {
                                String TAG = "ResetPasswordFragment.ResetPassword";

                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if(task.isSuccessful())
                                    {
                                        Log.d(TAG, "sendPasswordResetEmail:success");
                                        Toast.makeText(getActivity(),
                                                "Please check Email for Password Reset", Toast.LENGTH_SHORT).show();
                                    }
                                    else
                                    {
                                        Log.w(TAG, "sendPasswordResetEmail:failure", task.getException());
                                        try
                                        {
                                            Toast.makeText(getActivity(),"Unable to Send Password Reset: "
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
