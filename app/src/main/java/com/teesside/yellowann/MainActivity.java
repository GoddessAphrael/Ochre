package com.teesside.yellowann;

import android.os.Bundle;

import com.google.android.material.navigation.NavigationView;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;


public class MainActivity extends AppCompatActivity
{
    private DrawerLayout Drawer;
    private NavigationView NavView;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Drawer = findViewById(R.id.drawer_layout);
        NavView = findViewById(R.id.navigation_view);

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
    }
}



