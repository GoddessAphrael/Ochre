package com.teesside.yellowann;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;

public class ImageFragment extends Fragment
{
    private String currentPhotoPath;
    private ProgressBar progressBar;
    private ImageView imageCapture;
    private AppCompatImageButton editImage, convertImage;
    private FirebaseStorage mStorage;
    private StorageReference mStorageRef;
    private StorageMetadata metaData;
    private UploadTask uploadTask;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.images);
        return inflater.inflate(R.layout.fragment_image, container, false);
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState)
    {
        super.onViewCreated(v, savedInstanceState);

        imageCapture = v.findViewById(R.id.new_image);
        progressBar = v.findViewById(R.id.uploadProgress);
        editImage = v.findViewById(R.id.edit_image);
        convertImage = v.findViewById(R.id.convert_image);

        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReference();

        Bundle arguments = getArguments();

        if (arguments != null)
        {
            currentPhotoPath = arguments.getString("path");
            setPic();
        }

        editImage.setOnClickListener(new View.OnClickListener()
        {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View v)
            {
                PopupMenu popup = new PopupMenu(getActivity(), editImage);
                popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
                {
                    public boolean onMenuItemClick(MenuItem menuItem)
                    {
                        switch(menuItem.getItemId())
                        {
                            case R.id.popup_favourite:

                                break;
                            case R.id.popup_crop:

                                break;
                            case R.id.popup_open:

                                break;
                            case R.id.popup_save:
                                uploadToCloud();
                                break;
                            case R.id.popup_delete:

                                break;
                        }
                        return true;
                    }
                });
                MenuPopupHelper menuHelper = new MenuPopupHelper(getContext(), (MenuBuilder) popup.getMenu(), editImage);
                menuHelper.setForceShowIcon(true);
                menuHelper.show();
            }
        });
    }

    private void setPic()
    {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = Math.min(photoW/384, photoH/512);

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        imageCapture.setImageBitmap(bitmap);
    }

    public void uploadToCloud()
    {
        final String TAG = "uploadToCloud.uploadTask";
        Uri file = Uri.parse(currentPhotoPath);
        metaData = new StorageMetadata.Builder().setContentType("image/jpg").build();
        uploadTask = mStorageRef.child("images/" + file.getLastPathSegment()).putFile(file, metaData);

        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>()
        {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot)
            {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(Math.toIntExact
                        ((taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount()) * 100));
            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception exception)
            {
                progressBar.setVisibility(View.GONE);
                Log.w(TAG, "uploadToCloud.uploadTask:failure", exception);
                Toast.makeText(getActivity(), "Unable to Upload: "
                        + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
        {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
            {
                progressBar.setVisibility(View.GONE);
                Log.d(TAG, "uploadToCloud.uploadTask:success");
            }
        });
    }
}
