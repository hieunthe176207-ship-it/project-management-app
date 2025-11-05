package com.fpt.myapplication.view.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.fpt.myapplication.R;
import com.fpt.myapplication.view.adapter.MyTaskViewPagerAdapter;
import com.fpt.myapplication.viewmodel.MyTaskViewModel;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
public class MyTaskFragment  extends Fragment{

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private MyTaskViewModel myTaskViewModel;
    private MyTaskViewPagerAdapter viewPagerAdapter;

    public MyTaskFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.my_task_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tabLayout = view.findViewById(R.id.tab_layout);
        viewPager = view.findViewById(R.id.view_pager);

        myTaskViewModel = new ViewModelProvider(this).get(MyTaskViewModel.class);
        viewPagerAdapter = new MyTaskViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Todo");
                    break;
                case 1:
                    tab.setText("In Progress");
                    break;
                case 2:
                    tab.setText("Done");
                    break;
                case 3:
                    tab.setText("In Review");
                    break;
            }
        }).attach();

        myTaskViewModel.fetchMyTasks();
    }
}
