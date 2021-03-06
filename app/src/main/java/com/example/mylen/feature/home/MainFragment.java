package com.example.mylen.feature.home;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mylen.R;
import com.example.mylen.data.lens.LensKeepResponse;
import com.example.mylen.data.lens.LensOpenResponse;
import com.example.mylen.data.liquid.LiquidResponse;
import com.example.mylen.data.user.ProfileResponse;
import com.example.mylen.data.user.StatusResponse;
import com.example.mylen.feature.home.adapter.LensKeepAdpater;
import com.example.mylen.feature.home.adapter.LensOpenAdapter;
import com.example.mylen.feature.home.adapter.LiquidKeepAdapter;
import com.example.mylen.feature.home.adapter.LiquidOpenAdapter;
import com.example.mylen.feature.home.adapter.SearchLensAdapter;
import com.example.mylen.feature.home.add.AddLens1Activity;
import com.example.mylen.feature.home.add.AddLiquid1Activity;
import com.example.mylen.feature.home.search.SearchLensActivity;
import com.example.mylen.feature.others.NavigationDrawer;
import com.example.mylen.network.RetrofitClient;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainFragment extends Fragment implements View.OnClickListener{

    //전역변수 선언
    MainChild1Fragment main_child1;
    MainChild2Fragment main_child2;
    Button btn_open, btn_keep, btn_add;
    private TextView tv_user_name;

    AlertDialog.Builder builder, builder2, builder3;
    private int getItemId;

    RecyclerView rv_main_lens, rv_main_liquid;
    RecyclerView.LayoutManager layoutManager;
    LensKeepAdpater lensKeepAdpater;
    LiquidKeepAdapter liquidKeepAdapter;
    LensOpenAdapter lensOpenAdapter;
    LiquidOpenAdapter liquidOpenAdapter;

    //프래그먼트가 액티비티에 연결되었을 때 호출
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    //프래그먼트 뷰 계층을 리턴
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        //메인 이름 요청 메소드 호출
        tv_user_name = rootView.findViewById(R.id.tv_user_name);
        setUserName();

        //프래그먼트 구현 : 렌즈 등록 전 초기 화면
        main_child1 = new MainChild1Fragment();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.container, main_child1).commit();

        //다이얼로그 구현 1 : 렌즈 등록 / 세척액 등록
        builder = new AlertDialog.Builder(this.getActivity());
        builder.setItems(R.array.lens_add, (dialog, which) -> {
            Intent intent;
            switch (which){
                case 0: //렌즈 검색 페이지로 이동
                    intent = new Intent(getActivity(), SearchLensActivity.class);
                    startActivity(intent);
                    break;
                case 1: //세척액 등록1 페이지로 이동
                    intent = new Intent(getActivity(), AddLiquid1Activity.class);
                    startActivity(intent);
                    break;
            }
        });
        //다이얼로그 구현 2 : 세척액 사용 / 세척액 수정 / 세척액 삭제
        builder2 = new AlertDialog.Builder(this.getActivity());
        builder2.setItems(R.array.liquid_keep, (dialog, which) -> {
            Intent intent;
            switch (which){
                case 0: //세척액 사용
                    //세척액 사용 시작 요청 메소드 호출
                    requestOpenLiquid(getItemId);
                    break;
                case 1: //세척액 수정 => 수정해야 함
                    intent = new Intent(getActivity(), AddLiquid1Activity.class);
                    startActivity(intent);
                    break;
                case 2: //세척액 삭제
                    //세척액 삭제 요청 메소드 호출
                    requestDeleteLiquid(getItemId);
                    break;
            }
        });
        //다이얼로그 구현 3 : 렌즈 사용 / 렌즈 수정 / 렌즈 삭제
        builder3 = new AlertDialog.Builder(this.getActivity());
        builder3.setItems(R.array.lens_keep, (dialog, which) -> {
            Intent intent;
            switch (which){
                case 0: //렌즈 사용
                    //렌즈 사용 시작 요청 메소드 호출
                    requestOpenLens(getItemId);
                    break;
                case 1: //렌즈 수정 => 수정해야 함
                    intent = new Intent(getActivity(), AddLens1Activity.class);
                    startActivity(intent);
                    break;
                case 2: //렌즈 삭제
                    //렌즈 삭제 요청 메소드 호출
                    requestDeleteLens(getItemId);
                    break;
            }
        });
        //추가 버튼 이벤트 : 선택 다이얼로그
        btn_add = rootView.findViewById(R.id.btn_add);
        btn_add.setOnClickListener(v -> {
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });

        //버튼 리스너 등록
        btn_open = rootView.findViewById(R.id.btn_open);
        btn_open.setOnClickListener(this);
        btn_keep = rootView.findViewById(R.id.btn_keep);
        btn_keep.setOnClickListener(this);

        //리사이클러뷰
        rv_main_lens = rootView.findViewById(R.id.rv_main_lens);
        rv_main_liquid = rootView.findViewById(R.id.rv_main_liquid);

        requestLensOpen(); //개봉함 렌즈 조회 요청
        requestLiquidOpen(); //개봉함 세척액 조회 요청

        return rootView;
    }

    //메인 이름 요청 - GET  : Retrofit
    private void setUserName(){
        RetrofitClient.getService().userProfile().enqueue(new Callback<ProfileResponse>() {
            @SuppressLint({"DefaultLocale", "SetTextI18n"})
            @Override
            public void onResponse(@NotNull Call<ProfileResponse> call, @NotNull Response<ProfileResponse> response) {
                ProfileResponse result = response.body();
                assert result != null;
                //프로필 조회 요청 성공 시
                if(result.getSuccess()){
                    tv_user_name.setText(result.getName()+" 님");
                }else{
                    //실패 시 처리 코드 추후 추가 예정
                }
            }

            @Override
            public void onFailure(@NotNull Call<ProfileResponse> call, @NotNull Throwable t) {
                Log.e("메인 이름 조회 에러 발생", Objects.requireNonNull(t.getMessage()));
            }
        });
    }

    //버튼 이벤트
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_open: //개봉함 버튼 클릭 이벤트
                btn_open.setTextColor(getResources().getColor(R.color.soft_black));
                btn_keep.setTextColor(getResources().getColor(R.color.hint_grey));
                requestLensOpen(); //개봉함 렌즈 조회 요청
                requestLiquidOpen(); //개봉함 세척액 조회 요청
                break;
            case R.id.btn_keep: //보관함 버튼 클릭 이벤트
                btn_open.setTextColor(getResources().getColor(R.color.hint_grey));
                btn_keep.setTextColor(getResources().getColor(R.color.soft_black));
                requestLensKeep(); //보관함 렌즈 조회 요청
                requestLiquidKeep(); //보관함 세척액 조회 요청
                break;
            default:
                break;
        }
    }

    //개봉함 렌즈 조회 요청 - GET : Retrofit
    private void requestLensOpen(){
        RetrofitClient.getService().lensOpen().enqueue(new Callback<LensOpenResponse>() {
            @Override
            public void onResponse(Call<LensOpenResponse> call, Response<LensOpenResponse> response) {
                LensOpenResponse result = response.body();
                if(result.getSuccess()){
                    ArrayList<LensOpenResponse.LensInfo> lensInfos = new ArrayList<>(result.getLensInfo());
                    for(LensOpenResponse.LensInfo data : lensInfos)
                        Log.d("렌즈 결과 확인", data.getName());
                    //리사이클러 뷰 설정 메소드 호출
                    setLensOpenList(lensInfos);
                }
            }

            @Override
            public void onFailure(Call<LensOpenResponse> call, Throwable t) {
                Log.e("개봉함 렌즈 조회 요청 에러 발생", Objects.requireNonNull(t.getMessage()));
            }
        });
    }

    //개봉함 렌즈 리사이클러뷰
    private void setLensOpenList(ArrayList<LensOpenResponse.LensInfo> lensInfos){
        layoutManager = new LinearLayoutManager(getActivity());
        lensOpenAdapter = new LensOpenAdapter(getActivity(), lensInfos);
        rv_main_lens.setLayoutManager(layoutManager); //레이아웃 매니저 설정
        rv_main_lens.setAdapter(lensOpenAdapter); //리사이클러뷰 어댑터 설정

        lensOpenAdapter.setOnItemClickListener((v, pos) -> {
            //선택된 아이템 ID 가져오기
            getItemId = (int)lensOpenAdapter.getItemId(pos);
            //다이얼로그 구현
            AlertDialog alertDialog = builder3.create();
            alertDialog.show();
        });

        if(lensOpenAdapter != null)
            lensOpenAdapter.notifyDataSetChanged();
    }

    //개봉함 세척액 조회 요청 - GET : Retrofit
    private void requestLiquidOpen(){
        RetrofitClient.getService().liquidOpen().enqueue(new Callback<LiquidResponse>() {
            @Override
            public void onResponse(Call<LiquidResponse> call, Response<LiquidResponse> response) {
                LiquidResponse result = response.body();
                assert result != null;
                if(result.getSuccess()){
                    //결과값 추출
                    ArrayList<LiquidResponse.LiquidInfo> liquidInfos = new ArrayList<>(result.getLiquidInfo());
                    for(LiquidResponse.LiquidInfo data : liquidInfos)
                        Log.d("세척액 결과 확인", data.getName());
                    //리사이클러 뷰 설정 메소드 호출
                    setLiquidOpenList(liquidInfos);
                }
            }

            @Override
            public void onFailure(Call<LiquidResponse> call, Throwable t) {
                Log.e("개봉함 세척액 조회 요청 에러 발생", Objects.requireNonNull(t.getMessage()));
            }
        });
    }

    //개봉함 세척액 리사이클러뷰
    private void setLiquidOpenList(ArrayList<LiquidResponse.LiquidInfo> liquidInfos){
        layoutManager = new LinearLayoutManager(getActivity());
        liquidOpenAdapter = new LiquidOpenAdapter(getActivity(), liquidInfos);
        rv_main_liquid.setLayoutManager(layoutManager); //레이아웃 매니저 설정
        rv_main_liquid.setAdapter(liquidOpenAdapter); //리사이클러뷰 어댑터 설정

        liquidOpenAdapter.setOnItemClickListener((v, pos) -> {
            //선택된 아이템 ID 가져오기
            getItemId = (int)liquidOpenAdapter.getItemId(pos);
            //다이얼로그 구현
            AlertDialog alertDialog = builder2.create();
            alertDialog.show();
        });

        if(liquidOpenAdapter != null)
            liquidOpenAdapter.notifyDataSetChanged();
    }

    //보관함 렌즈 조회 요청 - GET : Retrofit
    private void requestLensKeep(){
        RetrofitClient.getService().lensKeep().enqueue(new Callback<LensKeepResponse>() {
            @Override
            public void onResponse(Call<LensKeepResponse> call, Response<LensKeepResponse> response) {
                LensKeepResponse result = response.body();
                if(result.getSuccess()){
                    ArrayList<LensKeepResponse.LensInfo> lensInfos = new ArrayList<>(result.getLensInfo());
                    for(LensKeepResponse.LensInfo data : lensInfos)
                        Log.d("렌즈 결과 확인", data.getName());
                    //리사이클러 뷰 설정 메소드 호출
                    setLensKeepList(lensInfos);
                }
            }

            @Override
            public void onFailure(Call<LensKeepResponse> call, Throwable t) {
                Log.e("보관함 렌즈 조회 요청 에러 발생", Objects.requireNonNull(t.getMessage()));
            }
        });
    }

    //보관함 렌즈 리사이클러뷰
    private void setLensKeepList(ArrayList<LensKeepResponse.LensInfo> lensInfos){
        layoutManager = new LinearLayoutManager(getActivity());
        lensKeepAdpater = new LensKeepAdpater(getActivity(), lensInfos);
        rv_main_lens.setLayoutManager(layoutManager); //레이아웃 매니저 설정
        rv_main_lens.setAdapter(lensKeepAdpater); //리사이클러뷰 어댑터 설정

        lensKeepAdpater.setOnItemClickListener((v, pos) -> {
            //선택된 아이템 ID 가져오기
            getItemId = (int)lensKeepAdpater.getItemId(pos);
            //다이얼로그 구현
            AlertDialog alertDialog = builder3.create();
            alertDialog.show();
        });

        if(lensKeepAdpater != null)
            lensKeepAdpater.notifyDataSetChanged();
    }

    //보관함 렌즈 삭제 요청 - DELETE : Retrofit
    private void requestDeleteLens(int id){
        RetrofitClient.getService().deleteLens(id).enqueue(new Callback<StatusResponse>() {
            @Override
            public void onResponse(@NotNull Call<StatusResponse> call, @NotNull Response<StatusResponse> response) {
                StatusResponse result = response.body();
                assert result != null;
                if(result.getSuccess())
                    refresh(); //프래그먼트 새로고침
            }

            @Override
            public void onFailure(@NotNull Call<StatusResponse> call, @NotNull Throwable t) {
                Log.e("렌즈 삭제 에러 발생", Objects.requireNonNull(t.getMessage()));
            }
        });
    }

    //보관함 사용 시작 요청 - PATCH : Retrofit
    private void requestOpenLens(int id){
        RetrofitClient.getService().openLens(id).enqueue(new Callback<StatusResponse>() {
            @Override
            public void onResponse(Call<StatusResponse> call, Response<StatusResponse> response) {
                StatusResponse result = response.body();
                assert result != null;
                if(result.getSuccess())
                    refresh(); //프래그먼트 새로고침
            }

            @Override
            public void onFailure(Call<StatusResponse> call, Throwable t) {
                Log.e("렌즈 사용 시작 에러 발생", Objects.requireNonNull(t.getMessage()));
            }
        });
    }

    //보관함 세척액 조회 요청 - GET : Retrofit
    private void requestLiquidKeep(){
        RetrofitClient.getService().liquidKeep().enqueue(new Callback<LiquidResponse>() {
            @Override
            public void onResponse(@NotNull Call<LiquidResponse> call, @NotNull Response<LiquidResponse> response) {
                LiquidResponse result = response.body();
                assert result != null;
                if(result.getSuccess()){
                    //결과값 추출
                    ArrayList<LiquidResponse.LiquidInfo> liquidInfos = new ArrayList<>(result.getLiquidInfo());
                    for(LiquidResponse.LiquidInfo data : liquidInfos)
                        Log.d("세척액 결과 확인", data.getName());
                    //리사이클러 뷰 설정 메소드 호출
                    setLiquidKeepList(liquidInfos);
                }
            }

            @Override
            public void onFailure(@NotNull Call<LiquidResponse> call, @NotNull Throwable t) {
                Log.e("보관함 세척액 조회 요청 에러 발생", Objects.requireNonNull(t.getMessage()));
            }
        });
    }

    //보관함 세척액 리사이클러뷰
    private void setLiquidKeepList(ArrayList<LiquidResponse.LiquidInfo> liquidInfo){
        layoutManager = new LinearLayoutManager(getActivity());
        liquidKeepAdapter = new LiquidKeepAdapter(getActivity(), liquidInfo);
        rv_main_liquid.setLayoutManager(layoutManager); //레이아웃 매니저 설정
        rv_main_liquid.setAdapter(liquidKeepAdapter); //리사이클러뷰 어댑터 설정

        liquidKeepAdapter.setOnItemClickListener((v, pos) -> {
            //선택된 아이템 ID 가져오기
            getItemId = (int)liquidKeepAdapter.getItemId(pos);
            //다이얼로그 구현
            AlertDialog alertDialog = builder2.create();
            alertDialog.show();
        });

        if(lensKeepAdpater != null)
            lensKeepAdpater.notifyDataSetChanged(); //여기 null 참고
    }

    //세척액 삭제 요청 - DELETE : Retrofit
    private void requestDeleteLiquid(int id){
        RetrofitClient.getService().deleteLiquid(id).enqueue(new Callback<StatusResponse>() {
            @Override
            public void onResponse(@NotNull Call<StatusResponse> call, @NotNull Response<StatusResponse> response) {
                StatusResponse result = response.body();
                assert result != null;
                if(result.getSuccess()){
                    refresh(); //프래그먼트 새로고침
                }
            }

            @Override
            public void onFailure(@NotNull Call<StatusResponse> call, @NotNull Throwable t) {
                Log.e("세척액 삭제 에러 발생", Objects.requireNonNull(t.getMessage()));
            }
        });
    }

    //세척액 사용 시작 요청 - PATCH : Retrofit
    private void requestOpenLiquid(int id){
        RetrofitClient.getService().openLiquid(id).enqueue(new Callback<StatusResponse>() {
            @Override
            public void onResponse(@NotNull Call<StatusResponse> call, @NotNull Response<StatusResponse> response) {
                StatusResponse result = response.body();
                assert result != null;
                if(result.getSuccess())
                    refresh(); //프래그먼트 새로고침
            }

            @Override
            public void onFailure(@NotNull Call<StatusResponse> call, @NotNull Throwable t) {
                Log.e("세척액 사용 시작 에러 발생", Objects.requireNonNull(t.getMessage()));
            }
        });
    }


    //프래그먼트 새로 고침
    private void refresh(){
        assert getFragmentManager() != null;
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.detach(this).attach(this).commit();
    }
}
