package com.example.mylen.feature.home.add;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mylen.R;
import com.example.mylen.feature.sign.SignInActivity;

public class AddLiquid1Activity extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_liquid1);
    }

    //back 버튼 클릭 이벤트: 로그인 페이지로 이동
    public void backButtonClick(View view) {
        finish();
    }

    //clear 버튼 클릭 이벤트: 로그인 페이지로 이동
    public void clearButtonClick(View view) {
        finish();
    }
}
