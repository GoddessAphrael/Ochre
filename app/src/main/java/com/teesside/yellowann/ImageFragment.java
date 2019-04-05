package com.teesside.yellowann;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;

import org.apache.commons.io.FilenameUtils;


public class ImageFragment extends Fragment
{
    private String currentPhotoPath;
    private ProgressBar progressBar;
    private ImageView imageCapture;
    private AppCompatImageButton editImage, convertImage;
    private CheckBox favouriteStar;
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private StorageReference mStorageRef;
    private StorageMetadata metaData;
    private UploadTask uploadTask;
    private DatabaseReference mDataRef, downloadRef;

    private ArrayList<String> list = new ArrayList<>();

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

        imageCapture = v.findViewById(R.id.new_text);
        progressBar = v.findViewById(R.id.uploadProgress_image);
        editImage = v.findViewById(R.id.edit_image);
        convertImage = v.findViewById(R.id.convert_image);
        favouriteStar = v.findViewById(R.id.favourite_star_image);

        list.add("Cloud Images Available:");

        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReference();
        mDataRef = FirebaseDatabase.getInstance().getReference();
        downloadRef = FirebaseDatabase.getInstance().getReference().child("user")
                .child(mAuth.getCurrentUser().getUid()).child("imageDownload");

        Bundle arguments = getArguments();

        if (arguments != null)
        {
            currentPhotoPath = arguments.getString("path");
            setPic();
        }

        favouriteStar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.w("FavouriteStar", "FavouriteStar:Pressed");
                Toast.makeText(getActivity(), "Unable to Favourite: Not Implemented",
                        Toast.LENGTH_SHORT).show();
            }
        });

        editImage.setOnClickListener(new View.OnClickListener()
        {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View v)
            {
                PopupMenu popup = new PopupMenu(getActivity(), editImage);
                popup.getMenuInflater().inflate(R.menu.popup_image, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
                {
                    public boolean onMenuItemClick(MenuItem menuItem)
                    {
                        switch(menuItem.getItemId())
                        {
                            case R.id.image_crop:
                                Log.w("editImage", "crop_Image:Pressed");
                                Toast.makeText(getActivity(), "Unable to Crop: Not Implemented",
                                        Toast.LENGTH_SHORT).show();
                                break;
                            case R.id.image_open_local:
                                loadLocalImageConfirm();
                                break;
                            case R.id.image_open_cloud:
                                loadCloudImageConfirm();
                                break;
                            case R.id.image_save:
                                uploadToCloud();
                                break;
                            case R.id.image_delete:
                                confirmDelete();
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

        convertImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (! Python.isStarted())
                {
                    Python.start(new AndroidPlatform(getActivity()));
                }
                else
                {
                    if (imageCapture.getDrawable() != null)
                    {
                        Python py = Python.getInstance();
                        PyObject text = (py.getModule("SimpleHRT.main").get("main")).call(currentPhotoPath);
                        sendToText(text.toString());
                    }
                    else
                    {
                        Log.w("convertImage", "onClick:failure");
                        Toast.makeText(getActivity(), "Unable to Convert: No current Image",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        downloadRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren())
                {
                   String downloadUrl = childSnapshot.child("downloadUrl").getValue().toString();
                   list.add(downloadUrl);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Log.d("downloadListener", "downloadListener:onCancelled", databaseError.toException());
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

    private void loadLocalImageConfirm()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setCancelable(true).setTitle("Load").setMessage("Load image?");

        builder.setPositiveButton(R.string.open_local, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                ((MainActivity) getActivity()).loadLocalImage();
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

    private void loadCloudImageConfirm()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setCancelable(true).setTitle("Load").setMessage("Load image?");

        builder.setPositiveButton(R.string.open_cloud, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                getImagesList();
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

    private void getImagesList()
    {
        try
        {
            final LoadFragment load = new LoadFragment();

            Bundle bundle = new Bundle();
            bundle.putSerializable("arrayList", list);
            load.setArguments(bundle);
            new Handler().post(new Runnable()
            {
                public void run()
                {
                    getFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            load).addToBackStack(null).commit();
                }
            });
        }
        catch (Exception e)
        {
            Log.w("getImagesList", "uploadToCloud.uploadTask:failure", e);
            Toast.makeText(getActivity(), "Error Occurred: "
                    + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadToCloud()
    {
        final String TAG = "uploadToCloud.uploadTask";
        try
        {
            final File f = new File(currentPhotoPath);
            final Uri uri = Uri.fromFile(f);
            metaData = new StorageMetadata.Builder().setContentType("image/jpg").build();
            final StorageReference ref = mStorageRef.child(mAuth.getCurrentUser().getUid() + "/Pictures/" + uri.getLastPathSegment());
            uploadTask = ref.putFile(uri, metaData);

            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>()
            {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot)
                {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(Math.toIntExact
                            ((taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount()) * 100));
                    Toast.makeText(getActivity(), "Upload in Progress", Toast.LENGTH_SHORT).show();
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
                    Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>()
                    {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception
                        {
                            if (!task.isSuccessful())
                            {
                                throw task.getException();
                            }
                            return ref.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task)
                        {
                            if (task.isSuccessful())
                            {
                                Uri downloadUri = task.getResult();
                                Image image = new Image(downloadUri.toString());
                                mDataRef.child("user/" + mAuth.getCurrentUser().getUid() + "/imageDownload/"
                                        + FilenameUtils.getBaseName(uri.getLastPathSegment())).setValue(image);                            }
                            else
                            {
                                Log.d(TAG, "uploadToCloud.uploadTask.continueWithTask:failure");
                            }

                            progressBar.setVisibility(View.GONE);
                            Log.d(TAG, "uploadToCloud.uploadTask:success");
                            Toast.makeText(getActivity(), "Upload Complete", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
        catch (Exception e)
        {
            Log.d(TAG, "uploadToCloud.imageCapture:null");
            Toast.makeText(getActivity(), "Unable to Upload: Please Load Local Image", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmDelete()
    {
        if (imageCapture.getDrawable() != null)
        {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setCancelable(true).setTitle("Delete").setMessage("Are you sure you want to delete this local image?");

            builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    imageCapture.setImageDrawable(null);
                    File file = new File(currentPhotoPath);
                    file.delete();
                    getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                            Uri.fromFile(new File(currentPhotoPath))));
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
        else
        {
            Log.w("confirmDelete", "getDrawable:failure");
            Toast.makeText(getActivity(), "Unable to Delete: No current Image",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void sendToText(String text)
    {
        final TextFragment textFragment = new TextFragment();

        Bundle bundle = new Bundle();
        bundle.putString("text", text);
        textFragment.setArguments(bundle);

        new Handler().post(new Runnable()
        {
            public void run()
            {
                getFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        textFragment).commit();
            }
        });
    }
}
