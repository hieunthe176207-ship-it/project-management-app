package com.fpt.myapplication.controller;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.fpt.myapplication.R;
import com.fpt.myapplication.dto.ResponseError;
import com.fpt.myapplication.dto.ResponseSuccess;
import com.fpt.myapplication.dto.request.ProjectCreateRequest;
import com.fpt.myapplication.dto.response.UserResponse;
import com.fpt.myapplication.model.ProjectModel;
import com.fpt.myapplication.util.SessionPrefs;
import com.fpt.myapplication.view.bottomSheet.AddProjectBottomSheet;
import com.fpt.myapplication.view.fragment.ListProjectFragment;
import com.fpt.myapplication.view.fragment.MyTaskFragment;
import com.fpt.myapplication.view.fragment.NotificationFragment;
import com.fpt.myapplication.view.fragment.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class HomeActivity extends AppCompatActivity {

    private ProjectModel model;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.home_layout);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setBackground(null);
        TextView tvGreeting = findViewById(R.id.tvGreeting);

        UserResponse user = SessionPrefs.get(this).getUser();
        model = new ProjectModel(this);
        tvGreeting.setText("Hi, "+user.getDisplayName());
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            new AddProjectBottomSheet()
                    .setOnProjectCreated((name, desc, due) -> {
                        ProjectCreateRequest body = new ProjectCreateRequest(name, desc, due);
                        model.createApi(body, new ProjectModel.CreateProjectCallBack() {
                            @Override
                            public void onSuccess(ResponseSuccess data) {
                                new SweetAlertDialog(HomeActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                                        .setTitleText("Thông báo")
                                        .setContentText(data.getMessage())
                                        .setConfirmText("ok")
                                        .show();
                            }

                            @Override
                            public void onError(ResponseError error) {
                                new SweetAlertDialog(HomeActivity.this, SweetAlertDialog.ERROR_TYPE)
                                        .setTitleText("Thông báo")
                                        .setContentText(error.message)
                                        .setConfirmText("ok")
                                        .show();
                            }

                            @Override
                            public void onLoading() {
                                Toast.makeText(HomeActivity.this, "đang tải", Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .show(getSupportFragmentManager(), "addProject");
        });

        setCurrentFragment(new ListProjectFragment(), "home");

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if(item.getItemId() == R.id.nav_home){
                setCurrentFragment(new ListProjectFragment(), "home");
            }
            else if(item.getItemId() == R.id.nav_project){
                setCurrentFragment(new MyTaskFragment(), "profile");
            }
            else if(item.getItemId() == R.id.nav_discussion){
                setCurrentFragment(new NotificationFragment(), "discuss");
            }
            else{
                setCurrentFragment(new ProfileFragment(), "profile");
            }
            return true;
        });



    }

    private void setCurrentFragment(@NonNull Fragment fragment, @NonNull String tag) {
        // Kiểm tra fragment hiện tại
        Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        // Nếu fragment hiện tại có cùng tag thì không cần replace (tránh reload)
        if (current != null && tag.equals(current.getTag())) {
            return;
        }

        // Gắn fragment vào container
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment, tag)
                .commit();
    }
}
