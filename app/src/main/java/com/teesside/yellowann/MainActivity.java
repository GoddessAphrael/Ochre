package com.teesside.yellowann;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

public class MainActivity extends AppCompatActivity
{
    private DrawerLayout Drawer;
    private NavigationView NavView;
    private TextView UserAccount, UserLogout;
    private FirebaseAuth mAuth;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Drawer = findViewById(R.id.drawer_layout);
        NavView = findViewById(R.id.navigation_view);
        UserAccount = findViewById(R.id.account);
        UserLogout = findViewById(R.id.logout);

        mAuth = FirebaseAuth.getInstance();

        NavView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener()
        {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem)
            {
                menuItem.setChecked(true);
                Drawer.closeDrawers();

                return true;
            }
        });

        UserLogout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mAuth = FirebaseAuth.getInstance();

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                        .setCancelable(true).setTitle("Logout").setMessage("Are you sure you want to logout?");

                builder.setPositiveButton(R.string.logout, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        mAuth.signOut();
                        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(loginIntent);
                        finish();
                    }
                });

                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.cancel();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }
}