package com.fpt.myapplication.view.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.fpt.myapplication.constant.TaskStatus; // Import enum của bạn
import com.fpt.myapplication.view.fragment.TaskPageFragment;
public class MyTaskViewPagerAdapter extends FragmentStateAdapter{
    // Chúng ta sẽ dùng 4 tab dựa trên enum
    private static final int NUM_TABS = 4;

    public MyTaskViewPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public TaskPageFragment createFragment(int position) {
        // "Nhét" Fragment con vào đúng vị trí
        switch (position) {
            case 0:
                // Dùng enum để truyền status (an toàn)
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
