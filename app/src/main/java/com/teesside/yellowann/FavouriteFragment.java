package com.teesside.yellowann;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class FavouriteFragment extends Fragment
{
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.favourites);
        return inflater.inflate(R.layout.fragment_favourite, container, false);
    }
}
