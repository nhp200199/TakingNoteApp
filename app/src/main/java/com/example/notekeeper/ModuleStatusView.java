package com.example.notekeeper;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.customview.widget.ExploreByTouchHelper;

import java.util.List;

/**
 * TODO: document your custom view class.
 */
public class ModuleStatusView extends View {
    public static final int EDIT_MODE_MODULE_COUNT = 7;
    public static final String TAG = "TAG";
    public static final int INVALID_INDEX = -1;
    public static final int SHAPE_CIRCLE = 0;
    public static final int DEFAULT_OUTLINE_WIDTH_DP = 2;
    private String mExampleString; // TODO: use a default from R.string...
    private int mExampleColor = Color.RED; // TODO: use a default from R.color...
    private float mExampleDimension = 0; // TODO: use a default from R.dimen...
    private Drawable mExampleDrawable;

    private boolean[] mModuleStatus;
    private float mOutlineWidth;
    private float mShapeSize;
    private float mSpacing;
    private Rect[] mModuleRectangles;
    private int mOutlineColor;
    private Paint mPaintOutline;
    private int mFillColor;
    private Paint mPaintFill;
    private float mRadius;
    private int mMaxHorizontalModules;
    private int mShape;
    private ModuleStatusAccessibilityHelper mAccessibilityHelper;

    public ModuleStatusView(Context context) {
        super(context);
        init(null, 0);
    }

    public ModuleStatusView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ModuleStatusView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {

        setFocusable(true);
        mAccessibilityHelper = new ModuleStatusAccessibilityHelper(this);
        ViewCompat.setAccessibilityDelegate(this, mAccessibilityHelper);

        // Load attributes
        // TypedArray giúp ta có thể sử dụng trực tiếp resource, còn AttributeSet sẽ trả về raw
        // Ví dụ: resource là một nguồn reference thì nó lấy cái reference đó chứ không lấy thẳng giá trị
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.ModuleStatusView, defStyle, 0);

        DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
        float displayDensity = dm.density;
        float defaultOutlineWidthPixels = displayDensity * DEFAULT_OUTLINE_WIDTH_DP;

        mOutlineColor = a.getColor(R.styleable.ModuleStatusView_outlineColor, Color.BLACK);
        mShape = a.getInt(R.styleable.ModuleStatusView_shape, SHAPE_CIRCLE);
        mOutlineWidth = a.getDimension(R.styleable.ModuleStatusView_outlineWidth, defaultOutlineWidthPixels);
        a.recycle();

        if(isInEditMode()){
            setupEditModeValues();
        }

        // ở đây cũng nên chuyển các số đo về dạng dp, không nên để dưới dạng pixel vì sẽ làm hiển thị
        // không đúng ở các màn hình khác nhau
        mShapeSize = 144f;
        mSpacing = 30f;
        mRadius = (mShapeSize - mOutlineWidth) / 2;

        mPaintOutline = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintOutline.setStyle(Paint.Style.STROKE);
        mPaintOutline.setStrokeWidth(mOutlineWidth);
        mPaintOutline.setColor(mOutlineColor);

        mFillColor = getContext().getResources().getColor(R.color.pluralsight_orange);
        mPaintFill = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintFill.setStyle(Paint.Style. FILL);
        mPaintFill.setColor(mFillColor);
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, @Nullable Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        mAccessibilityHelper.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return mAccessibilityHelper.dispatchKeyEvent(event) || super.dispatchKeyEvent(event);
    }

    @Override
    protected boolean dispatchHoverEvent(MotionEvent event) {
        return mAccessibilityHelper.dispatchHoverEvent(event) || super.dispatchHoverEvent(event);
    }

    private void setupEditModeValues() {
        boolean[] exampleModuleValues = new boolean[EDIT_MODE_MODULE_COUNT];
        int middle = EDIT_MODE_MODULE_COUNT / 2;
        for(int i=0; i < middle; i++){
            exampleModuleValues[i] = true;
        }
        setModuleStatus(exampleModuleValues);
    }

    private void setupModuleRectangles(int width) {
        int availableWidth = width - getPaddingRight() - getPaddingLeft();
        int horizontalModulesThatCanFit = (int) (availableWidth / (mShapeSize + mSpacing));
        int maxHorizontalModules = Math.min(horizontalModulesThatCanFit, mModuleStatus.length);

        mModuleRectangles = new Rect[mModuleStatus.length];
        for(int moduleIndex=0; moduleIndex < mModuleRectangles.length; moduleIndex++){
            int coloumn = moduleIndex % maxHorizontalModules;
            int row = moduleIndex / maxHorizontalModules;
            int x = getPaddingLeft() + (int) ((mShapeSize + mSpacing) * coloumn);
            int y = getPaddingTop() + (int) ((mShapeSize + mSpacing) * row);
            mModuleRectangles[moduleIndex] = new Rect(x, y, x + (int) mShapeSize, y + (int) mShapeSize);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //tính kích thước cơ bản cho view (dùng cho việc wrapContent)
        //trong hàm này cũng nên làm gọn để tăng hiệu suất vì nó có thể được gọi thường xuyên
        //vì vậy trong hàm này chỉ tính width và height cho nguyên một cái custom view, việc đặt các
        //view cụ thể sẽ được hàm onSizeChanged thực hiện
        int desireWidth = 0;
        int desireHeight = 0;

        int specWidth = MeasureSpec.getSize(widthMeasureSpec);
        int availableWidth = specWidth - getPaddingRight() - getPaddingLeft();
        int horizontalModulesThatCanFit = (int) (availableWidth / (mShapeSize + mSpacing));
        mMaxHorizontalModules = Math.min(horizontalModulesThatCanFit, mModuleStatus.length);

        int rows = (mModuleStatus.length - 1) / mMaxHorizontalModules + 1;
        //Có thể dùng phép tính round up để tính số row cần
        //int rows = Math.(mModuleStatus.length / mMaxHorizontalModules);

        desireWidth = (int) (((mShapeSize + mSpacing) * mMaxHorizontalModules) - mSpacing);
        desireWidth += getPaddingLeft() + getPaddingRight();

        desireHeight = (int) ((mShapeSize + mSpacing) * rows - mSpacing);
        desireHeight += getPaddingTop() + getPaddingBottom();

        int width = resolveSizeAndState(desireWidth, widthMeasureSpec, 0 );
        int height = resolveSizeAndState(desireHeight, heightMeasureSpec, 0 );
        Log.d(TAG, String.valueOf(width));
        Log.d(TAG, "onMeasure");

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        setupModuleRectangles(w);
        Log.d(TAG, "size changed");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int moduleIndex=0; moduleIndex < mModuleRectangles.length; moduleIndex++) {
            if (mShape == SHAPE_CIRCLE) {
                //find the center point of the circle
                float x = mModuleRectangles[moduleIndex].centerX();
                float y = mModuleRectangles[moduleIndex].centerY();
                //the radius is the same in every circle, we shouldn't recalculate it (remember to simplify the word in onDraw)
                //float radius = (mShapeSize - mOutlineWidth) / 2;

                //draw the circle
                if (mModuleStatus[moduleIndex]) {
                    canvas.drawCircle(x, y, mRadius, mPaintFill);
                }
                canvas.drawCircle(x, y, mRadius, mPaintOutline);
            }
            else{
                //draw square
                drawSquare(canvas, moduleIndex);
            }
        }
    }

    private void drawSquare(Canvas canvas, int moduleIndex) {
        if(mModuleStatus[moduleIndex])
            canvas.drawRect(mModuleRectangles[moduleIndex], mPaintFill);
        canvas.drawRect(mModuleRectangles[moduleIndex], mPaintOutline);
    }

    @Override
    public boolean onTouchEvent (MotionEvent event){
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_UP:
                int moduleIndex = findItemAtPoint(event.getX(), event.getY());
                onModuleSelected(moduleIndex);
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void onModuleSelected(int moduleIndex) {
        if(moduleIndex == INVALID_INDEX)
            return;

        mModuleStatus[moduleIndex] = !mModuleStatus[moduleIndex];
        invalidate(); // đọc doc của cái này nha

        mAccessibilityHelper.invalidateVirtualView(moduleIndex);
        mAccessibilityHelper.sendEventForVirtualView(moduleIndex, AccessibilityEvent.TYPE_VIEW_CLICKED);
    }

    private int findItemAtPoint(float x, float y) {
        int moduleIndex = INVALID_INDEX;
        for(int i = 0; i < mModuleRectangles.length; i++){
            if(mModuleRectangles[i].contains((int)x, (int)y)) {
                moduleIndex = i;
                break;
            }
        }
        return moduleIndex;
    }

    public boolean[] getModuleStatus() {
        return mModuleStatus;
    }

    public void setModuleStatus(boolean[] moduleStatus) {
        mModuleStatus = moduleStatus;
    }

    private class ModuleStatusAccessibilityHelper extends ExploreByTouchHelper{

        /**
         * Constructs a new helper that can expose a virtual view hierarchy for the
         * specified host view.
         *
         * @param host view whose virtual view hierarchy is exposed by this helper
         */
        public ModuleStatusAccessibilityHelper(@NonNull View host) {
            super(host);
        }

        @Override
        protected int getVirtualViewAt(float x, float y) {
            int moduleIndex = findItemAtPoint(x, y);
            return moduleIndex == INVALID_INDEX? ExploreByTouchHelper.INVALID_ID : moduleIndex;
        }

        @Override
        protected void getVisibleVirtualViews(List<Integer> virtualViewIds) {
            if(mModuleRectangles == null)
                return;
            for(int moduleIndex = 0; moduleIndex < mModuleRectangles.length; moduleIndex++){
                virtualViewIds.add(moduleIndex);
            }

        }

        @Override
        protected void onPopulateNodeForVirtualView(int virtualViewId, @NonNull AccessibilityNodeInfoCompat node) {
            node.setFocusable(true);
            node.setBoundsInParent(mModuleRectangles[virtualViewId]);
            node.setContentDescription("Module" + virtualViewId);

            node.setCheckable(true);
            node.setChecked(mModuleStatus[virtualViewId]);

            node.addAction(AccessibilityEvent.TYPE_VIEW_CLICKED);
        }

        @Override
        protected boolean onPerformActionForVirtualView(int virtualViewId, int action, @Nullable Bundle arguments) {
            switch (action){
                case AccessibilityEvent.TYPE_VIEW_CLICKED:
                    onModuleSelected(virtualViewId);
                    return true;
            }
            return false;
        }
    }
}
