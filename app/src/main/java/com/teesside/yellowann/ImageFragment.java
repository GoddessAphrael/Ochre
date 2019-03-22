package com.teesside.yellowann;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ImageFragment extends Fragment
{
    private String currentPhotoPath;
    private ProgressBar progressBar;
    private ImageView imageCapture;
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

        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReference();

        Bundle arguments = getArguments();

        if (arguments != null)
        {
            currentPhotoPath = arguments.getString("path");
            setPic();
        }
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

    public void uploadToCloud(File image)
    {
        final String TAG = "uploadToCloud.uploadTask";

        final Uri file = Uri.fromFile(image);
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
                Toast.makeText(getActivity(), "Unable to Login: "
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
