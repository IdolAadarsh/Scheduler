package com.idroid.scheduler;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Parcelable;
import android.os.Parcel;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;

public class GanttChartView extends View {
    private final ArrayList<GanttBlock> blocks = new ArrayList<>();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final HashMap<String, Integer> colorMap = new HashMap<>();
    private final int[] colors = {0xFFE57373, 0xFF64B5F6, 0xFF81C784, 0xFFFFB74D, 0xFFBA68C8};

    private int maxTime = 1;
    private float animationProgress = 0;
    private boolean isPlaying = false;
    private int currentStep = 0;

    private final Handler handler = new Handler();
    private Runnable animationRunnable;

    public GanttChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setBlocks(ArrayList<GanttBlock> newBlocks) {
        blocks.clear();
        blocks.addAll(newBlocks);
        assignColors();
        maxTime = blocks.isEmpty() ? 1 : blocks.get(blocks.size() - 1).end;
        resetAnimation();
        invalidate();
    }

    public void resetAnimation() {
        animationProgress = 0;
        currentStep = 0;
        isPlaying = false;
        invalidate();
    }

    public void playAnimation() {
        if (isPlaying) return;
        isPlaying = true;

        animationRunnable = new Runnable() {
            @Override
            public void run() {
                if (currentStep < blocks.size()) {
                    currentStep++;
                    invalidate();
                    handler.postDelayed(this, 1000);
                } else {
                    isPlaying = false;
                }
            }
        };
        handler.post(animationRunnable);
    }

    public void pauseAnimation() {
        isPlaying = false;
        handler.removeCallbacks(animationRunnable);
    }

    public void stepForward() {
        if (currentStep < blocks.size()) {
            currentStep++;
            invalidate();
        }
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
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float widthPerUnit = getWidth() / (float) maxTime;
        float height = getHeight();

        for (int i = 0; i < currentStep && i < blocks.size(); i++) {
            GanttBlock block = blocks.get(i);
            float left = block.start * widthPerUnit;
            float right = block.end * widthPerUnit;
            paint.setColor(colorMap.get(block.name));
            canvas.drawRect(left, 0, right, height, paint);

            paint.setColor(0xFFFFFFFF);
            paint.setTextSize(height / 3);
            canvas.drawText(block.name, left + 10, height / 2, paint);
        }
    }

    public static class GanttBlock implements Parcelable {
        public String name;
        public int start, end;

        public GanttBlock(String name, int start, int end) {
            this.name = name;
            this.start = start;
            this.end = end;
        }

        // Parcelable Constructor
        protected GanttBlock(Parcel in) {
            name = in.readString();
            start = in.readInt();
            end = in.readInt();
        }

        public static final Creator<GanttBlock> CREATOR = new Creator<GanttBlock>() {
            @Override
            public GanttBlock createFromParcel(Parcel in) {
                return new GanttBlock(in);
            }

            @Override
            public GanttBlock[] newArray(int size) {
                return new GanttBlock[size];
            }
        };

        @Override
        public void writeToParcel(Parcel dest, int flags) {
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