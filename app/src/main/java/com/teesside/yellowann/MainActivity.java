package com.teesside.yellowann;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
    private DrawerLayout Drawer;
    private NavigationView NavView;
    private Toolbar toolbar;
    private ActionBarDrawerToggle drawerToggle;
    private TextView UserAccount, UserLogout;
    private FirebaseAuth mAuth;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Drawer = findViewById(R.id.drawer_layout);
        NavView = findViewById(R.id.navigation_view);
        toolbar = findViewById(R.id.toolbar);
        UserAccount = findViewById(R.id.account);
        UserLogout = findViewById(R.id.logout);

        mAuth = FirebaseAuth.getInstance();

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.home);

        drawerToggle = new ActionBarDrawerToggle(MainActivity.this, Drawer, toolbar,
                R.string.drawer_open, R.string.drawer_close);
        drawerToggle.syncState();

        Drawer.addDrawerListener(drawerToggle);

        NavView.setNavigationItemSelectedListener(MainActivity.this);

        UserAccount.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendToPasswordReset(v);
            }
        });

        UserLogout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
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

        if (savedInstanceState == null)
        {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new HomeFragment()).commit();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem)
    {
        switch(menuItem.getItemId())
        {
            case R.id.nav_home:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new HomeFragment()).commit();
                break;
            case R.id.nav_favourites:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new FavouriteFragment()).commit();
                break;
            case R.id.nav_image:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ImageFragment()).commit();
                break;
            case R.id.nav_text:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new TextFragment()).commit();
                break;
            case R.id.nav_analysis:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new AnalysisFragment()).commit();
                break;
        }
        Drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed()
    {
        if (Drawer.isDrawerOpen(GravityCompat.START))
        {
            Drawer.closeDrawer(GravityCompat.START);
        }
        else
        {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (drawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendToPasswordReset(View v)
    {
        ResetPasswordFragment reset = new ResetPasswordFragment();

        Bundle bundle = new Bundle();
        bundle.putString("email", mAuth.getCurrentUser().getEmail());
        reset.setArguments(bundle);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                reset).addToBackStack(null).commit();

        Drawer.closeDrawer(GravityCompat.START);
    }
}