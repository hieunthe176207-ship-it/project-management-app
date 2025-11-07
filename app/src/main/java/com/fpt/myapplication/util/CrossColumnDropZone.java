package com.fpt.myapplication.util;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.DragEvent;
import android.view.View;
import androidx.recyclerview.widget.RecyclerView;

public class CrossColumnDropZone {

    private Paint dropZonePaint;
    private boolean isActive = false;

    public CrossColumnDropZone(Context context) {
        dropZonePaint = new Paint();
        dropZonePaint.setColor(Color.BLUE);
        dropZonePaint.setAlpha(100);
        dropZonePaint.setStyle(Paint.Style.FILL);
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public void drawDropZone(Canvas canvas, RecyclerView recyclerView) {
        if (isActive) {
            Rect bounds = new Rect();
            recyclerView.getGlobalVisibleRect(bounds);
            canvas.drawRect(bounds, dropZonePaint);
        }
    }

    public static class DropZoneOverlay extends View {
        private CrossColumnDropZone dropZone;

        public DropZoneOverlay(Context context, CrossColumnDropZone dropZone) {
            super(context);
            this.dropZone = dropZone;
            setVisibility(GONE);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (dropZone.isActive) {
                canvas.drawRect(0, 0, getWidth(), getHeight(), dropZone.dropZonePaint);
            }
        }

        public void showDropZone() {
            dropZone.setActive(true);
            setVisibility(VISIBLE);
            invalidate();
        }

        public void hideDropZone() {
            dropZone.setActive(false);
            setVisibility(GONE);
        }
    }
}