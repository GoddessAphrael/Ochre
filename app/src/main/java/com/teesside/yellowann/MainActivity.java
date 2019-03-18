package com.teesside.yellowann;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class MainActivity extends AppCompatActivity
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
        drawerToggle = setupDrawerToggle();
        drawerToggle.syncState();

        Drawer.addDrawerListener(drawerToggle);

        NavView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener()
        {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem)
            {
                //selectDrawerItem(menuItem);
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

    private ActionBarDrawerToggle setupDrawerToggle()
    {
        return new ActionBarDrawerToggle(MainActivity.this, Drawer, toolbar, R.string.drawer_open, R.string.drawer_close);
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

//    public void selectDrawerItem(MenuItem menuItem)
//    {
//        Fragment fragment = null;
//        Class fragmentClass;
//        switch(menuItem.getItemId())
//        {
//            case R.id.nav_first_fragment:
//                fragmentClass = FirstFragment.class;
//                break;
//            case R.id.nav_second_fragment:
//                fragmentClass = SecondFragment.class;
//                break;
//            case R.id.nav_third_fragment:
//                fragmentClass = ThirdFragment.class;
//                break;
//            default:
//                fragmentClass = FirstFragment.class;
//        }
//
//        try
//        {
//            fragment = (Fragment) fragmentClass.newInstance();
//        }
//        catch (Exception e)
//        {
//
//        }
//
//        FragmentManager fragmentManager = getSupportFragmentManager();
//        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
//
//        menuItem.setChecked(true);
//        setTitle(menuItem.getTitle());
//        Drawer.closeDrawers();
//    }
}