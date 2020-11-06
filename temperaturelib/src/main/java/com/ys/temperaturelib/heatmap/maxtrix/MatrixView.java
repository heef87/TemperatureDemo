package com.ys.temperaturelib.heatmap.maxtrix;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.HandlerThread;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.ys.temperaturelib.R;
import com.ys.temperaturelib.temperature.TemperatureEntity;

import java.util.ArrayList;
import java.util.List;

public class MatrixView extends View {

    private int marginVertical = 1;          //每个小方块垂直间隔
    private int marginHorizontal = 1;        //水平间隔
    private int row;                     //行
    private int column;                  //列
    private int defaultWidth;           //默认的宽度
    private int defaultHeight;          //默认的高度
    private Paint textPaint;                //text paint
    private Paint mPaint;                //填充画笔
    private RectF rectF;                            //小矩形框
    private int defaultSize = 26;    //矩形框尺寸

    public MatrixView(Context context) {
        this(context, null);
    }

    public MatrixView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MatrixView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.matrix_view);
        int value = array.getInt(R.styleable.matrix_view_view_size, -1);
        if (value > 0) defaultSize = value;
        array.recycle();
        //初始化画笔
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(1);
        //初始化矩形框
        rectF = new RectF(0, 0, defaultSize, defaultSize);

        textPaint = new Paint();
        textPaint.setColor(getResources().getColor(R.color.white));
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(7.5f);
        textPaint.setStrokeWidth(1);
        HandlerThread handlerThread = new HandlerThread("matrix_draw");
        handlerThread.start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //计算自定义view的实际宽高
        int width = getRealWidth(widthMeasureSpec);
        int height = getRealHeight(heightMeasureSpec);
        //长度是小方块的长度*个数+ 分割线的长度,宽度
        if (width > 0 && height > 0 && row > 0 && column > 0) {
            int w = width / row;
            int h = height / column;
            int min = Math.min(w, h);
            defaultSize = Math.min(defaultSize, min);
        }
        setMeasuredDimension(width, height);
    }

    private int getRealWidth(int widthMeasureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(widthMeasureSpec);
        int specSize = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            defaultWidth = row * defaultSize + row * marginHorizontal;
            result = Math.min(defaultWidth, specSize);
        }
        return result;
    }

    private int getRealHeight(int heightMeasureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(heightMeasureSpec);
        int specSize = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        //match_parent就使用得到的数值
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            //计算
            defaultHeight = column * defaultSize + column * marginVertical;
            result = Math.min(defaultHeight, specSize);
        }
        return result;
    }

    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int startY = 0;
        int startX = 0;
        //列
        for (int i = 0; i < column; i++) {
            startX = 0;
            //行
            for (int j = 0; j < row; j++) {
                rectF.set(startX, startY, startX + defaultSize, startY + defaultSize);
                DrawableInfo drawableInfo = mDrawableInfos.get(i * (row - 1) + j);
                mPaint.setColor(drawableInfo.color);
                canvas.drawRoundRect(rectF, 0, 0, mPaint);
                Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
                float top = fontMetrics.top;//为基线到字体上边框的距离,即上图中的top
                float bottom = fontMetrics.bottom;//为基线到字体下边框的距离,即上图中的bottom
                int baseLineY = (int) (rectF.centerY() - top / 2 - bottom / 2);//基线中间点的y轴计算公式
                canvas.drawText(drawableInfo.temp + "", startX + defaultSize / 8, baseLineY, textPaint);
                startX = startX + marginHorizontal + defaultSize;
            }
            startY = startY + marginVertical + defaultSize;
        }
    }

    private List<DrawableInfo> mDrawableInfos = new ArrayList<>();

    /**
     * 设置原始数据
     *
     * @param entity 温度数据集合
     * @param row    行数
     * @param column 列数
     */
    public void setDataResource(TemperatureEntity entity, int row, int column) {
        if (entity == null || entity.tempList == null || entity.tempList.isEmpty()) return;
        mDrawableInfos.clear();
        for (int i = 0; i < entity.tempList.size(); i++) {
            DrawableInfo drawableInfo = new DrawableInfo();
            drawableInfo.temp = entity.tempList.get(i);
            drawableInfo.color = getColor(drawableInfo.temp, entity.max, entity.min);
            mDrawableInfos.add(drawableInfo);
        }
        this.row = row;
        this.column = column;
        requestLayout();
        invalidate();
    }

    private int getColor(float temp, float max, float min) {
        int color = 0;
        float sum = 0;
        if (max < 36f || max > 42) {
            sum = max - 36f;
        }
        if (temp < 32.5f + sum) {
            color = getResources().getColor(R.color.blue);
        } else if (temp >= 32.5f + sum && temp < 34.5f + sum) {
            color = getResources().getColor(R.color.greens);
        } else if (temp >= 34.5f + sum && temp < 36f + sum) {
            color = getResources().getColor(R.color.green);
        } else if (temp >= 36f + sum && temp < 36.6f + sum) {
            color = getResources().getColor(R.color.red1);
        } else if (temp >= 36.6f + sum && temp < 37.3f + sum) {
            color = getResources().getColor(R.color.red2);
        } else if (temp >= 37.3f + sum && temp <= 42f + sum) {
            color = getResources().getColor(R.color.red3);
        } else {
            color = getResources().getColor(R.color.red4);
        }
        return color;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.dispatchTouchEvent(event);
    }

    private class DrawableInfo {
        transient public int color;
        public float temp;
    }
}
