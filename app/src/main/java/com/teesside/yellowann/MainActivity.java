package com.teesside.yellowann;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;

import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import static android.os.Environment.getExternalStoragePublicDirectory;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
    private DrawerLayout drawer;
    private NavigationView navView;
    private Toolbar toolbar;
    private ActionBarDrawerToggle drawerToggle;
    private TextView userAccount, userLogout;
    private FloatingActionButton fab;
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private String currentPhotoPath;

    static final protected Integer REQUEST_IMAGE_CAPTURE = 1;
    static final protected Integer RESULT_LOAD_IMG = 2;
    static final protected Integer RESULT_LOAD_TEXT = 3;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawer = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.navigation_view);
        toolbar = findViewById(R.id.toolbar);
        fab = findViewById(R.id.fab);
        userAccount = findViewById(R.id.account);
        userLogout = findViewById(R.id.logout);

        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance();

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.home);

        drawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawer, toolbar,
                R.string.drawer_open, R.string.drawer_close);
        drawerToggle.syncState();

        drawer.addDrawerListener(drawerToggle);

        navView.setNavigationItemSelectedListener(MainActivity.this);

        // if device has camera, create temporary file and take photo
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(!MainActivity.this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA))
                {
                    Toast.makeText(MainActivity.this, "Unable to Capture new Image - This device has no Camera",
                            Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                        File photoFile = null;
                        try
                        {
                            photoFile = createImageFile();
                        }
                        catch (IOException e)
                        {
                            Log.w("Fab", "createImageFile:failure");
                            Toast.makeText(MainActivity.this, "Error Occurred: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                        if (photoFile != null) {
                            Uri photoURI = FileProvider.getUriForFile(MainActivity.this,
                                    "com.teesside.yellowann.provider", photoFile);
                            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                            startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
                        }
                    }
                }
            }
        });

        userAccount.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendToPasswordReset(v);
            }
        });

        // dialog confirm user logout
        userLogout.setOnClickListener(new View.OnClickListener()
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
            new Handler().post(new Runnable()
            {
                public void run()
                {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new HomeFragment()).commit();
                }
            });
        }
    }

    // initiate new fragments
    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem)
    {
        switch(menuItem.getItemId())
        {
            case R.id.nav_home:
                new Handler().post(new Runnable()
                {
                    public void run()
                    {
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                new HomeFragment()).commit();
                    }
                });
                break;
            case R.id.nav_favourites:
                new Handler().post(new Runnable()
                {
                    public void run()
                    {
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                new FavouriteFragment()).commit();
                    }
                });
                break;
            case R.id.nav_image:
                new Handler().post(new Runnable()
                {
                    public void run()
                    {
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                new ImageFragment()).commit();
                    }
                });
                break;
            case R.id.nav_text:
                new Handler().post(new Runnable()
                {
                    public void run()
                    {
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                new TextFragment()).commit();
                    }
                });
                break;
            case R.id.nav_analysis:
                new Handler().post(new Runnable()
                {
                    public void run()
                    {
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                new AnalysisFragment()).commit();
                    }
                });
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // alter back button to close nav drawer if open
    @Override
    public void onBackPressed()
    {
        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
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

    // initiate ResetPasswordFragment
    private void sendToPasswordReset(View v)
    {
        final ResetPasswordFragment reset = new ResetPasswordFragment();

        Bundle bundle = new Bundle();
        bundle.putString("email", mAuth.getCurrentUser().getEmail());
        reset.setArguments(bundle);
        new Handler().post(new Runnable()
        {
           public void run()
           {
               getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                       reset).addToBackStack(null).commit();
           }
       });
        drawer.closeDrawer(GravityCompat.START);
    }

    // create temporary file and record image path
    public File createImageFile() throws IOException
    {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.UK).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName,".jpg", storageDir);

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    // save image to local
    private void galleryAddPic()
    {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    // load image from local
    public void loadLocalImage()
    {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
    }

    // load text from local
    public void loadLocalText()
    {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, RESULT_LOAD_TEXT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        final String TAG = "onActivityResult";

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK)
        {
            // initiate imageFragment with new photo
            if (requestCode== REQUEST_IMAGE_CAPTURE)
            {
                final ImageFragment image = new ImageFragment();
                galleryAddPic();
                navView.setCheckedItem(R.id.nav_image);

                Bundle bundle = new Bundle();
                bundle.putString("path", currentPhotoPath);
                image.setArguments(bundle);

                new Handler().post(new Runnable()
                {
                    public void run()
                    {
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                image).commit();
                    }
                });
            }
            // initiate imageFragment with loaded image
            else if (requestCode == RESULT_LOAD_IMG)
            {
                final ImageFragment image = new ImageFragment();

                    Uri loadImageUri = data.getData();
                    String[] filePath = { MediaStore.Images.Media.DATA };
                    Cursor cursor = getContentResolver().query(loadImageUri, filePath, null, null, null);
                    cursor.moveToFirst();
                    currentPhotoPath = cursor.getString(cursor.getColumnIndex(filePath[0]));
                    cursor.close();

                    Bundle bundle = new Bundle();
                    bundle.putString("path", currentPhotoPath);
                    image.setArguments(bundle);

                    new Handler().post(new Runnable()
                    {
                        public void run()
                        {
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                    image).commit();
                        }
                    });
            }
            // initiate textFragment with loaded text
            else if (requestCode == RESULT_LOAD_TEXT)
            {
                Uri loadTextUri = data.getData();
                File file = new File(loadTextUri.getPath());

                StringBuilder text = new StringBuilder();

                try
                {
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    String line = br.readLine();

                    text.append(line);
                    br.close();
                }
                catch (IOException e)
                {
                    Log.w(TAG, "RESULT_LOAD_TEXT:failure");
                    Toast.makeText(MainActivity.this, "Error Occurred: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }

                final TextFragment textFragment = new TextFragment();

                Bundle bundle = new Bundle();
                bundle.putString("text", text.toString());
                textFragment.setArguments(bundle);

                new Handler().post(new Runnable()
                {
                    public void run()
                    {
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                textFragment).commit();
                    }
                });
            }
        }
    }
}