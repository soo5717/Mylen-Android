package com.example.mylen.feature.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mylen.R;

public class MainChild2Fragment extends Fragment {

    @Nullable
    @Override //메인 렌즈 착용 시 프래그먼트
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_child1, container, false);
    }
}
