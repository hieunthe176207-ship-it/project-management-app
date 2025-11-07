package com.fpt.myapplication.controller;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.fpt.myapplication.R;
import com.fpt.myapplication.view.adapter.CalendarTaskAdapter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import androidx.annotation.NonNull;
import android.graphics.Color;
import android.view.Window;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.animation.ObjectAnimator;
import android.animation.AnimatorSet;

public class TaskCalendarActivity extends AppCompatActivity {
    private TextView tvMonthTitle, tvSelectedDateBar;
    private RecyclerView rvCalendarDays, rvTasksForDate;
    private ImageView btnPrevMonth, btnNextMonth;
    private final Calendar currentCal = Calendar.getInstance();
    private final List<DayItem> dayItems = new ArrayList<>();
    private DayAdapter dayAdapter;
    private CalendarTaskAdapter taskAdapter;
    private Date selectedDate;
    private static final Map<Integer,Integer> dayTaskCount = new HashMap<>();
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_calendar);
        tvMonthTitle = findViewById(R.id.tvMonthTitle);
        tvSelectedDateBar = findViewById(R.id.tvSelectedDateBar);
        rvCalendarDays = findViewById(R.id.rvCalendarDays);
        rvTasksForDate = findViewById(R.id.rvTasksForDate);
        btnPrevMonth = findViewById(R.id.btnPrevMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);
        if (getSupportActionBar() != null) getSupportActionBar().hide(); // ẩn thanh action bar
        setupCalendar();
        setupTasksList();
        btnPrevMonth.setOnClickListener(v -> changeMonth(-1));
        btnNextMonth.setOnClickListener(v -> changeMonth(1));
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener(){
            private static final int SWIPE_THRESHOLD = 120;
            private static final int SWIPE_VELOCITY_THRESHOLD = 120;
            @Override public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(e2.getY()-e1.getY())) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX < 0) changeMonth(1); else changeMonth(-1);
                        return true;
                    }
                }
                return false;
            }
        });
        rvCalendarDays.setOnTouchListener((v, ev) -> gestureDetector.onTouchEvent(ev));
    }

    private void setupCalendar() {
        rvCalendarDays.setLayoutManager(new GridLayoutManager(this, 7));
        dayAdapter = new DayAdapter(dayItems, this::onDaySelected);
        rvCalendarDays.setAdapter(dayAdapter);
        buildMonthDays();
    }

    private void setupTasksList() {
        rvTasksForDate.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new CalendarTaskAdapter();
        rvTasksForDate.setAdapter(taskAdapter);
        updateSelectedDate(new Date());
    }

    private void changeMonth(int delta) {
        animateMonthFade();
        currentCal.add(Calendar.MONTH, delta);
        buildMonthDays();
    }

    private void animateMonthFade() {
        if (rvCalendarDays == null) return;
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(rvCalendarDays, "alpha", 1f, 0f);
        fadeOut.setDuration(120);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(rvCalendarDays, "alpha", 0f, 1f);
        fadeIn.setDuration(180);
        AnimatorSet set = new AnimatorSet();
        set.playSequentially(fadeOut, fadeIn);
        set.start();
    }

    private void buildMonthDays() {
        dayItems.clear();
        Calendar cal = (Calendar) currentCal.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK); // Sunday=1
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        // Previous month tail
        Calendar prev = (Calendar) currentCal.clone();
        prev.add(Calendar.MONTH, -1);
        int prevMonthDays = prev.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int i = 1; i < firstDayOfWeek; i++) {
            int dayNum = prevMonthDays - (firstDayOfWeek - i) + 1;
            Calendar dCal = (Calendar) prev.clone();
            dCal.set(Calendar.DAY_OF_MONTH, dayNum);
            dayItems.add(new DayItem(dayNum, dCal.getTime(), false));
        }
        // Current month
        for (int d = 1; d <= daysInMonth; d++) {
            cal.set(Calendar.DAY_OF_MONTH, d);
            dayItems.add(new DayItem(d, cal.getTime(), true));
        }
        // Next month head to fill 6 rows if needed
        Calendar next = (Calendar) currentCal.clone();
        next.add(Calendar.MONTH, 1);
        int totalCells = dayItems.size();
        int rows = (int) Math.ceil(totalCells / 7f);
        int targetCells = rows < 6 ? 42 : rows * 7; // ensure 6 rows
        int nextDay = 1;
        while (dayItems.size() < targetCells) {
            Calendar dCal = (Calendar) next.clone();
            dCal.set(Calendar.DAY_OF_MONTH, nextDay);
            dayItems.add(new DayItem(nextDay, dCal.getTime(), false));
            nextDay++;
        }
        generateMockCounts(daysInMonth); // TODO real counts
        // Auto select today
        Calendar today = Calendar.getInstance();
        if (today.get(Calendar.YEAR) == currentCal.get(Calendar.YEAR) && today.get(Calendar.MONTH) == currentCal.get(Calendar.MONTH)) {
            int todayDay = today.get(Calendar.DAY_OF_MONTH);
            // index after previous month tail
            int index = 0;
            // previous tail length = firstDayOfWeek-1
            index = (firstDayOfWeek - 1) + (todayDay - 1);
            if (index >= 0 && index < dayItems.size()) {
                dayAdapter.setSelectedPos(index);
                selectedDate = today.getTime();
            }
        } else {
            dayAdapter.setSelectedPos(-1);
            selectedDate = null;
        }
        dayAdapter.notifyDataSetChanged();
        SimpleDateFormat fmt = new SimpleDateFormat("MMMM yyyy", Locale.US);
        tvMonthTitle.setText(fmt.format(currentCal.getTime()));
    }

    private void generateMockCounts(int daysInMonth) {
        dayTaskCount.clear();
        for (int d=1; d<=daysInMonth; d++) {
            int c = 0;
            if (d % 5 == 0) c = 3; else if (d % 3 == 0) c = 1; else if (d % 7 == 0) c = 2; // mô phỏng
            if (c>0) dayTaskCount.put(d, c);
        }
    }

    private void onDaySelected(Date date) {
        updateSelectedDate(date);
    }

    private void updateSelectedDate(Date date) {
        selectedDate = date; // TODO Backend
        SimpleDateFormat barFmt = new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.US);
        tvSelectedDateBar.setText(barFmt.format(date));
        List<CalendarTaskAdapter.CalendarTaskData> tasks = new ArrayList<>();
        tasks.add(new CalendarTaskAdapter.CalendarTaskData("Design Wireframe", "IN_PROGRESS", "10/10", "1 assigned"));
        tasks.add(new CalendarTaskAdapter.CalendarTaskData("API Contract", "TODO", "2/5", "2 assigned"));
        tasks.add(new CalendarTaskAdapter.CalendarTaskData("Retrospective", "DONE", "5/5", "3 assigned"));
        taskAdapter.setItems(tasks);
    }

    // Data models
    private static class DayItem {
        final int dayNumber; final Date date; final boolean inCurrentMonth;
        DayItem(int dayNumber, Date date, boolean inCurrentMonth) { this.dayNumber = dayNumber; this.date = date; this.inCurrentMonth = inCurrentMonth; }
    }

    private static class DayAdapter extends RecyclerView.Adapter<DayAdapter.DayVH> {
        interface OnDayClick { void onClick(Date date); }
        private final List<DayItem> items; private final OnDayClick listener;
        private int selectedPos = -1;
        DayAdapter(List<DayItem> items, OnDayClick l){ this.items=items; this.listener=l; }
        void setSelectedPos(int pos){ this.selectedPos = pos; }
        @NonNull @Override public DayVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_day, parent, false);
            return new DayVH(v);
        }
        @Override public void onBindViewHolder(@NonNull DayVH h, int pos) {
            DayItem it = items.get(pos);
            if (it.date == null) {
                h.tv.setText(""); h.tv.setBackgroundResource(R.drawable.bg_day_unselected); h.tv.setEnabled(false); h.dot.setVisibility(View.GONE); return;
            }
            h.tv.setEnabled(it.inCurrentMonth);
            h.tv.setText(String.valueOf(it.dayNumber));
            if (!it.inCurrentMonth) {
                h.tv.setTextColor(Color.parseColor("#BDBDBD"));
                h.tv.setBackgroundResource(R.drawable.bg_day_unselected);
                h.dot.setVisibility(View.GONE);
            } else {
                if (pos == selectedPos) {
                    h.tv.setBackgroundResource(R.drawable.bg_day_selected);
                    h.tv.setTextColor(Color.WHITE);
                } else {
                    h.tv.setBackgroundResource(R.drawable.bg_day_unselected);
                    h.tv.setTextColor(Color.parseColor("#424242"));
                }
                // Task indicator dot
                Integer count = dayTaskCount.get(it.dayNumber);
                if (count != null && count > 0 && pos != selectedPos) {
                    h.dot.setBackgroundResource(R.drawable.bg);
                    h.dot.setVisibility(View.VISIBLE);
                } else if (isToday(it.date) && pos != selectedPos) {
                    h.dot.setBackgroundResource(R.drawable.dot_today_indicator);
                    h.dot.setVisibility(View.VISIBLE);
                } else {
                    h.dot.setVisibility(View.GONE);
                }
            }
            h.itemView.setOnClickListener(v -> {
                if (!it.inCurrentMonth) return; // ignore outside month
                int adapterPos = h.getBindingAdapterPosition();
                if (adapterPos == RecyclerView.NO_POSITION) return;
                DayItem clicked = items.get(adapterPos);
                if (clicked.date != null) listener.onClick(clicked.date);
                int old = selectedPos; selectedPos = adapterPos;
                if (old != -1) notifyItemChanged(old);
                notifyItemChanged(selectedPos);
                h.tv.animate().scaleX(1.05f).scaleY(1.05f).setDuration(150)
                        .withEndAction(() -> h.tv.animate().scaleX(1f).scaleY(1f).setDuration(120));
            });
        }
        private boolean isToday(Date date) {
            Calendar c = Calendar.getInstance(); c.setTime(date);
            Calendar t = Calendar.getInstance();
            return c.get(Calendar.YEAR)==t.get(Calendar.YEAR) && c.get(Calendar.DAY_OF_YEAR)==t.get(Calendar.DAY_OF_YEAR);
        }
        @Override public int getItemCount() { return items.size(); }
        class DayVH extends RecyclerView.ViewHolder { TextView tv; View dot; DayVH(View v){ super(v); tv=v.findViewById(R.id.tvDayNumber); dot=v.findViewById(R.id.dotIndicator);} }
    }
}
