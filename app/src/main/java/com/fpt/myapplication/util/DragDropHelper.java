package com.fpt.myapplication.util;



import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.fpt.myapplication.R;

public class DragDropHelper extends ItemTouchHelper.Callback {

    private final DragDropListener dragDropListener;
    private boolean isDragging = false;

    public interface DragDropListener {
        void onItemMoved(RecyclerView.ViewHolder viewHolder, int fromPosition, int toPosition);
        void onItemStartDrag(RecyclerView.ViewHolder viewHolder);
        void onItemEndDrag(RecyclerView.ViewHolder viewHolder);
        boolean canDropOver(RecyclerView source, RecyclerView.ViewHolder current, RecyclerView target);
    }

    public DragDropHelper(DragDropListener listener) {
        this.dragDropListener = listener;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return false; // Chúng ta sẽ start drag manually
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return false;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlags = 0;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        int fromPosition = viewHolder.getAdapterPosition();
        int toPosition = target.getAdapterPosition();

        if (dragDropListener != null) {
            dragDropListener.onItemMoved(viewHolder, fromPosition, toPosition);
        }
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        // Không sử dụng swipe
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            isDragging = true;
            if (viewHolder != null && dragDropListener != null) {
                dragDropListener.onItemStartDrag(viewHolder);
                // Tạo hiệu ứng visual cho item đang được drag
                viewHolder.itemView.setScaleX(1.1f);
                viewHolder.itemView.setScaleY(1.1f);
                viewHolder.itemView.setAlpha(0.8f);
            }
        }
        super.onSelectedChanged(viewHolder, actionState);
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        if (isDragging) {
            isDragging = false;
            if (dragDropListener != null) {
                dragDropListener.onItemEndDrag(viewHolder);
            }
            // Reset visual effects
            viewHolder.itemView.setScaleX(1.0f);
            viewHolder.itemView.setScaleY(1.0f);
            viewHolder.itemView.setAlpha(1.0f);
        }
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            // Vẽ shadow hoặc highlight khi drag
            View itemView = viewHolder.itemView;
            Drawable background = new ColorDrawable(Color.LTGRAY);
            background.setBounds(itemView.getLeft(), itemView.getTop(),
                    itemView.getRight(), itemView.getBottom());
            background.draw(c);
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
}
