package com.fpt.myapplication.controller;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fpt.myapplication.R;
import com.fpt.myapplication.dto.ResponseError;
import com.fpt.myapplication.dto.response.TaskResponseDto;
import com.fpt.myapplication.model.TaskModel;
import com.fpt.myapplication.view.adapter.SearchTaskAdapter;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TaskCalendarActivity extends AppCompatActivity {

    private TextView tvMonthTitle, tvSelectedDateBar;
    private RecyclerView rvCalendarDays, rvTasksForDate;
    private ImageView btnPrevMonth, btnNextMonth;

    private final Calendar currentCal = Calendar.getInstance();
    private final List<DayItem> dayItems = new ArrayList<>();
    private DayGridAdapter dayAdapter;
    private TaskModel model;

    // DỮ LIỆU TASK
    private final List<TaskResponseDto> allTasks = new ArrayList<>();
    private final Map<Integer,Integer> dayTaskCount = new HashMap<>();

    // LIST ADAPTER (dùng SearchTaskAdapter)
    private SearchTaskAdapter taskAdapter;

    private GestureDetector gestureDetector;

    private static final DateTimeFormatter ISO_LOCAL = DateTimeFormatter.ISO_LOCAL_DATE; // yyyy-MM-dd
    private static final SimpleDateFormat MONTH_TITLE_FMT = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
    private static final SimpleDateFormat SELECTED_BAR_FMT = new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_calendar);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        model = new TaskModel(this);
        tvMonthTitle = findViewById(R.id.tvMonthTitle);
        tvSelectedDateBar = findViewById(R.id.tvSelectedDateBar);
        rvCalendarDays = findViewById(R.id.rvCalendarDays);
        rvTasksForDate = findViewById(R.id.rvTasksForDate);
        btnPrevMonth = findViewById(R.id.btnPrevMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);

        // Lưới lịch
        rvCalendarDays.setLayoutManager(new GridLayoutManager(this, 7));
        dayAdapter = new DayGridAdapter();
        rvCalendarDays.setAdapter(dayAdapter);



        // Danh sách task: dùng SearchTaskAdapter (grid 2 cột)
        taskAdapter = new SearchTaskAdapter(item -> {
            Intent i = new Intent(TaskCalendarActivity.this, TaskActivity.class);
            i.putExtra("task_id", item.getId());
            startActivity(i);
        });
        SearchTaskAdapter.setupAsTwoColumns(rvTasksForDate);
        rvTasksForDate.setAdapter(taskAdapter);

        // Điều hướng tháng
        btnPrevMonth.setOnClickListener(v -> changeMonth(-1));
        btnNextMonth.setOnClickListener(v -> changeMonth(1));
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int S = 120;
            @Override public boolean onFling(MotionEvent e1, MotionEvent e2, float vx, float vy) {
                float dx = e2.getX() - e1.getX();
                if (Math.abs(dx) > Math.abs(vy) && Math.abs(dx) > S && Math.abs(vx) > S) {
                    if (dx < 0) changeMonth(1); else changeMonth(-1);
                    return true;
                }
                return false;
            }
        });
        rvCalendarDays.setOnTouchListener((v, ev) -> gestureDetector.onTouchEvent(ev));

        // Render tháng + nạp dữ liệu
        buildMonthDays();
        loadAllMyTasks(); // => có mock data mẫu bên dưới
    }

    /* =========================================================
       LOAD DATA (hiện tạo MOCK để bạn thấy dot + filter)
       Thay thế bằng gọi API thật là xong.
       ========================================================= */
    private void loadAllMyTasks() {
        allTasks.clear();

        model.getMyTasks(new TaskModel.GetMyTasksCallBack() {
            @Override
            public void onSuccess(List<TaskResponseDto> data) {
                allTasks.clear();
                allTasks.addAll(data);
                afterDataLoaded();
            }

            @Override
            public void onError(ResponseError error) {
                allTasks.clear();
                afterDataLoaded();
                // TODO: Hiển thị thông báo lỗi nếu cần
            }

            @Override
            public void onLoading() {
                // TODO: Hiển thị loading nếu cần
            }
        });

        // Sau khi có data -> cập nhật dot và chọn ngày
        afterDataLoaded();
    }

    private void afterDataLoaded() {
        rebuildCountsForCurrentMonth();
        dayAdapter.notifyDataSetChanged();

        Calendar today = Calendar.getInstance();
        if (sameYearMonth(today, currentCal)) {
            int idx = firstDayOffsetOfMonth(currentCal) + (today.get(Calendar.DAY_OF_MONTH) - 1);
            dayAdapter.setSelectedPos(idx);
            updateSelectedDate(today.getTime());
        } else {
            Calendar c = (Calendar) currentCal.clone();
            c.set(Calendar.DAY_OF_MONTH, 1);
            updateSelectedDate(c.getTime());
        }
    }

    private void rebuildCountsForCurrentMonth() {
        dayTaskCount.clear();
        int y = currentCal.get(Calendar.YEAR);
        int m = currentCal.get(Calendar.MONTH) + 1;
        for (TaskResponseDto t : allTasks) {
            LocalDate d = parseIso(t.getDueDate());
            if (d != null && d.getYear() == y && d.getMonthValue() == m) {
                int dd = d.getDayOfMonth();
                dayTaskCount.put(dd, dayTaskCount.getOrDefault(dd, 0) + 1);
            }
        }
    }

    /* ======================= CALENDAR ======================= */

    private void buildMonthDays() {
        dayItems.clear();
        Calendar cal = (Calendar) currentCal.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int firstDow = cal.get(Calendar.DAY_OF_WEEK); // Sun=1..Sat=7
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Tail tháng trước
        Calendar prev = (Calendar) currentCal.clone(); prev.add(Calendar.MONTH, -1);
        int prevDays = prev.getActualMaximum(Calendar.DAY_OF_MONTH);
        int offset = Math.max(0, firstDow - Calendar.SUNDAY); // 0..6
        for (int i = 0; i < offset; i++) {
            int dayNum = prevDays - offset + 1 + i;
            Calendar dCal = (Calendar) prev.clone();
            dCal.set(Calendar.DAY_OF_MONTH, dayNum);
            dayItems.add(new DayItem(dayNum, dCal.getTime(), false));
        }

        // Ngày trong tháng
        for (int d = 1; d <= daysInMonth; d++) {
            cal.set(Calendar.DAY_OF_MONTH, d);
            dayItems.add(new DayItem(d, cal.getTime(), true));
        }

        // Head tháng sau cho đủ 42 ô
        Calendar next = (Calendar) currentCal.clone(); next.add(Calendar.MONTH, 1);
        int nextDay = 1;
        while (dayItems.size() < 42) {
            Calendar dCal = (Calendar) next.clone();
            dCal.set(Calendar.DAY_OF_MONTH, nextDay++);
            dayItems.add(new DayItem(dCal.get(Calendar.DAY_OF_MONTH), dCal.getTime(), false));
        }

        tvMonthTitle.setText(MONTH_TITLE_FMT.format(currentCal.getTime()));
        rebuildCountsForCurrentMonth();
        dayAdapter.setSelectedPos(-1);
        dayAdapter.notifyDataSetChanged();
    }

    private void changeMonth(int delta) {
        animateMonthFade();
        currentCal.add(Calendar.MONTH, delta);
        buildMonthDays();

        // vì mock theo tháng đang xem, nạp lại mock:
        loadAllMyTasks();
    }

    private void animateMonthFade() {
        ObjectAnimator a = ObjectAnimator.ofFloat(rvCalendarDays, "alpha", 1f, 0f);
        a.setDuration(120);
        ObjectAnimator b = ObjectAnimator.ofFloat(rvCalendarDays, "alpha", 0f, 1f);
        b.setDuration(180);
        AnimatorSet set = new AnimatorSet();
        set.playSequentially(a, b);
        set.start();
    }

    /* ===================== SELECT & FILTER ===================== */

    private void onDaySelected(Date date, int adapterPos) {
        updateSelectedDate(date);
        int old = dayAdapter.getSelectedPos();
        dayAdapter.setSelectedPos(adapterPos);
        if (old != -1) dayAdapter.notifyItemChanged(old);
        dayAdapter.notifyItemChanged(adapterPos);
    }

    private void updateSelectedDate(Date date) {
        tvSelectedDateBar.setText(SELECTED_BAR_FMT.format(date));

        Calendar c = Calendar.getInstance(); c.setTime(date);
        int y = c.get(Calendar.YEAR), m = c.get(Calendar.MONTH) + 1, d = c.get(Calendar.DAY_OF_MONTH);

        List<TaskResponseDto> filtered = new ArrayList<>();
        for (TaskResponseDto t : allTasks) {
            LocalDate due = parseIso(t.getDueDate());
            if (due != null && due.getYear()==y && due.getMonthValue()==m && due.getDayOfMonth()==d) {
                filtered.add(t);
            }
        }
        taskAdapter.submitList(filtered);
    }

    /* ======================== HELPERS ======================== */

    private LocalDate parseIso(String s) {
        if (s == null) return null;
        try { return LocalDate.parse(s.trim(), ISO_LOCAL); } catch (Exception e) { return null; }
    }

    private boolean sameYearMonth(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR)==b.get(Calendar.YEAR) && a.get(Calendar.MONTH)==b.get(Calendar.MONTH);
    }

    private int firstDayOffsetOfMonth(Calendar monthCal) {
        Calendar c = (Calendar) monthCal.clone();
        c.set(Calendar.DAY_OF_MONTH, 1);
        return Math.max(0, c.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY);
    }

    /* =================== MODELS & GRID ADAPTER =================== */

    private static class DayItem {
        final int dayNumber; final Date date; final boolean inCurrentMonth;
        DayItem(int dayNumber, Date date, boolean inCurrentMonth) { this.dayNumber = dayNumber; this.date = date; this.inCurrentMonth = inCurrentMonth; }
    }

    private class DayGridAdapter extends RecyclerView.Adapter<DayGridAdapter.DayVH> {
        private int selectedPos = -1;
        int getSelectedPos() { return selectedPos; }
        void setSelectedPos(int pos) { this.selectedPos = pos; }

        @NonNull @Override
        public DayVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_day, parent, false);
            return new DayVH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull DayVH h, int pos) {
            DayItem it = dayItems.get(pos);
            h.tv.setText(String.valueOf(it.dayNumber));
            h.tv.setEnabled(it.inCurrentMonth);

            if (!it.inCurrentMonth) {
                h.tv.setTextColor(Color.parseColor("#BDBDBD"));
                h.tv.setBackgroundResource(R.drawable.bg_day_unselected);
                h.dot.setVisibility(View.GONE);
            } else {
                if (pos == selectedPos) {
                    h.tv.setBackgroundResource(R.drawable.bg_day_selected);
                    h.tv.setTextColor(Color.WHITE);
                    h.dot.setVisibility(View.GONE);
                } else {
                    h.tv.setBackgroundResource(R.drawable.bg_day_unselected);
                    h.tv.setTextColor(Color.parseColor("#424242"));

                    Integer cnt = dayTaskCount.get(it.dayNumber);
                    if (cnt != null && cnt > 0) {
                        h.dot.setBackgroundResource(R.drawable.dot_dealine_indicator);
                        h.dot.setVisibility(View.VISIBLE);
                    } else if (isToday(it.date)) {
                        h.dot.setBackgroundResource(R.drawable.dot_today_indicator);
                        h.dot.setVisibility(View.VISIBLE);
                    } else {
                        h.dot.setVisibility(View.GONE);
                    }
                }
            }

            h.itemView.setOnClickListener(v -> {
                if (!it.inCurrentMonth) return;
                int p = h.getBindingAdapterPosition();
                if (p == RecyclerView.NO_POSITION) return;
                onDaySelected(it.date, p);
                h.tv.animate().scaleX(1.05f).scaleY(1.05f).setDuration(140)
                        .withEndAction(() -> h.tv.animate().scaleX(1f).scaleY(1f).setDuration(120));
            });
        }

        private boolean isToday(Date date) {
            Calendar c = Calendar.getInstance(); c.setTime(date);
            Calendar t = Calendar.getInstance();
            return c.get(Calendar.YEAR)==t.get(Calendar.YEAR) &&
                    c.get(Calendar.DAY_OF_YEAR)==t.get(Calendar.DAY_OF_YEAR);
        }

        @Override public int getItemCount() { return dayItems.size(); }

        class DayVH extends RecyclerView.ViewHolder {
            TextView tv; View dot;
            DayVH(@NonNull View v) { super(v); tv = v.findViewById(R.id.tvDayNumber); dot = v.findViewById(R.id.dotIndicator); }
        }
    }
}
