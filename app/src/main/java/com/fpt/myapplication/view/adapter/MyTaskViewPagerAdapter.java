package com.fpt.myapplication.view.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.fpt.myapplication.constant.TaskStatus;
import com.fpt.myapplication.view.fragment.TaskPageFragment;
public class MyTaskViewPagerAdapter extends FragmentStateAdapter{
    private static final int NUM_TABS = 4;

    public MyTaskViewPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public TaskPageFragment createFragment(int position) {

        switch (position) {
            case 0:
                return TaskPageFragment.newInstance(TaskStatus.TODO.name());
            case 1:
                return TaskPageFragment.newInstance(TaskStatus.IN_PROGRESS.name());
            case 2:
                return TaskPageFragment.newInstance(TaskStatus.DONE.name());
            case 3:
                return TaskPageFragment.newInstance(TaskStatus.IN_REVIEW.name());
            default:
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return NUM_TABS; // 4 tab
    }

}
