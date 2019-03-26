package com.teesside.yellowann;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class LoadFragment extends Fragment
{
    private NavigationView NavView;
    private ListView cloudList;
    private ArrayList<String> list = new ArrayList<>();
    private Uri downloadUri;
    private DownloadManager dLManager;
    private long dLID;
    private String currentPhotoPath;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.open_cloud);
        return inflater.inflate(R.layout.fragment_load, container, false);
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState)
    {
        NavView = v.findViewById(R.id.navigation_view);

        dLManager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);

        super.onViewCreated(v, savedInstanceState);

        getActivity().registerReceiver(onDownloadComplete,new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        list = (ArrayList<String>)getArguments().getSerializable("arrayList");

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>
                (getActivity(), android.R.layout.simple_list_item_1, list);

        cloudList = v.findViewById(R.id.loadList);

        cloudList.setAdapter(arrayAdapter);

        cloudList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View v, int position, long Long)
            {
                downloadUri = Uri.parse(cloudList.getItemAtPosition(position).toString());
                DownloadManager.Request dLRequest = new DownloadManager.Request(downloadUri);

                dLRequest.setDescription("Cloud Image Download");
                dLRequest.allowScanningByMediaScanner();
                dLRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                dLRequest.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);

                dLRequest.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, downloadUri.getLastPathSegment());

                dLID = dLManager.enqueue(dLRequest);
            }
        });
    }

    private BroadcastReceiver onDownloadComplete = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

            if (dLID == id)
            {
                File path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), downloadUri.getLastPathSegment());
                currentPhotoPath = path.getAbsolutePath();

                final ImageFragment image = new ImageFragment();

                Bundle bundle = new Bundle();
                bundle.putString("path", currentPhotoPath);
                image.setArguments(bundle);

                new Handler().post(new Runnable()
                {
                    public void run()
                    {
                        getFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                image).commit();
                    }
                });
            }
        }
    };

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        getActivity().unregisterReceiver(onDownloadComplete);
    }
}
