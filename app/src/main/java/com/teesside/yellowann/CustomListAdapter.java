package com.teesside.yellowann;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.Nullable;

public class CustomListAdapter extends ArrayAdapter<String>
{
    private Activity context;
    private ArrayList<Bitmap> imageList = new ArrayList<>();
    private ArrayList<Long> modifiedList = new ArrayList<>();
    private TextView recentModified;
    private ImageView recentThumb;

    public CustomListAdapter(Activity context, @Nullable ArrayList<Bitmap> imageList, @Nullable ArrayList<Long> modifiedList)
    {
        super(context, R.layout.recent_list);

        this.context=context;
        this.imageList=imageList;
        this.modifiedList=modifiedList;
    }

    public View getView(int position, View view, ViewGroup parent)
    {
        LayoutInflater inflater=context.getLayoutInflater();
        View v = inflater.inflate(R.layout.recent_list, null,true);

        recentModified = v.findViewById(R.id.modified);
        recentThumb = v.findViewById(R.id.recent_thumb);

        recentModified.setText(modifiedList.get(position).toString());
        recentThumb.setImageBitmap(imageList.get(position));
        return v;
    }
}

