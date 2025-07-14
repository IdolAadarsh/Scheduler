// GanttChartView.java
package com.idroid.scheduler;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;

public class GanttChartView extends View {

    private final ArrayList<GanttBlock> blocks = new ArrayList<>();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final HashMap<String, Integer> colorMap = new HashMap<>();
    private int[] colors = {0xFFE57373, 0xFF64B5F6, 0xFF81C784, 0xFFFFB74D, 0xFFBA68C8};
    private int maxTime = 1;
    private float animationProgress = 0;

    public GanttChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setBlocks(ArrayList<GanttBlock> newBlocks) {
        blocks.clear();
        blocks.addAll(newBlocks);
        assignColors();
        animateChart();
    }

    private void assignColors() {
        colorMap.clear();
        int index = 0;
        for (GanttBlock block : blocks) {
            if (!colorMap.containsKey(block.name)) {
                colorMap.put(block.name, colors[index % colors.length]);
                index++;
            }
        }
        maxTime = 0;
        for (GanttBlock block : blocks) {
            if (block.end > maxTime) maxTime = block.end;
        }
    }

    private void animateChart() {
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1f);
        animator.setDuration(1000);
        animator.addUpdateListener(animation -> {
            animationProgress = (float) animation.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float widthPerUnit = getWidth() / (float) maxTime;
        float height = getHeight();

        for (GanttBlock block : blocks) {
            float left = block.start * widthPerUnit;
            float right = block.end * widthPerUnit * animationProgress;
            paint.setColor(colorMap.get(block.name));
            canvas.drawRect(left, 0, right, height, paint);
            paint.setColor(0xFFFFFFFF);
            paint.setTextSize(height / 3);
            canvas.drawText(block.name, left + 10, height / 2, paint);
        }
    }



    public static class GanttBlock implements android.os.Parcelable {
        public String name;
        public int start, end;

        public GanttBlock(String name, int start, int end) {
            this.name = name;
            this.start = start;
            this.end = end;
        }

        protected GanttBlock(android.os.Parcel in) {
            name = in.readString();
            start = in.readInt();
            end = in.readInt();
        }

        public static final Creator<GanttBlock> CREATOR = new Creator<GanttBlock>() {
            @Override
            public GanttBlock createFromParcel(android.os.Parcel in) {
                return new GanttBlock(in);
            }

            @Override
            public GanttBlock[] newArray(int size) {
                return new GanttBlock[size];
            }
        };

        @Override
        public void writeToParcel(android.os.Parcel dest, int flags) {
            dest.writeString(name);
            dest.writeInt(start);
            dest.writeInt(end);
        }

        @Override
        public int describeContents() {
            return 0;
        }
    }

}

