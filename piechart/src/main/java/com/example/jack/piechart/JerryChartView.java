package com.example.jack.piechart;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.List;

/**
 * <b>饼状统计图</b>
 * <h2>JerryChartView.java</h2>
 *
 * @author Jerry VeryEast
 * @since 2016年3月30日
 */
public class JerryChartView extends View {

    private static final String TAG = "JerryChartView";
    private List<ChartData> datas;
    private Context context;

    /*
     * 默认文本颜色
     */
    private int DEFAULT_TEXTCOLOR = 0xff000000;
    /*
     * 默认文本大小
     */
    private int textSize = 24;

    /*
     * 默认间隔线颜色
     */
    private int lineColor = 0xffffffff;
    /*
     * 默认间隔线宽度
     */
    private int lineWidth = 2;

    /*
     * 默认起始绘制角度 3点方向为0，默认12点方向
     */
    private float startAngle = -90;
    /*
     * 默认饼状图样式——扇形
     */
    private ChartStyle chartStyle = ChartStyle.FANSHAPE;
    /*
     * 1.突出部分距离间隔线的垂直距离
     */
    // private int distance = 30;
    /*
     * 2.突出部分圆心距离原位置的圆心
	 */
    private int distance = 30;

    private Paint paint;// 扇形画笔
    private Paint linePaint;// 分割线画笔，圆画笔
    private Paint textPaint;// 文本画笔
    private RectF rectf, rectf3;

    /**
     * 矩形边长
     */
    private int rectfSize = 500;

    private int width, height;

    /**
     * 文字是否在view中间，仅对 {@link ChartStyle#ANNULAR}有用
     */
    private boolean isTextInCenter = false;

    private int textColor = DEFAULT_TEXTCOLOR;

    public JerryChartView(Context context) {
        this(context, null);

    }

    public JerryChartView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public JerryChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.JerryChartView);
        lineColor = a.getColor(R.styleable.JerryChartView_lineColor, lineColor);
        lineWidth = a.getDimensionPixelOffset(R.styleable.JerryChartView_lineWidth, lineWidth);
        startAngle = a.getFloat(R.styleable.JerryChartView_startAngle, startAngle);
        distance = a.getDimensionPixelOffset(R.styleable.JerryChartView_distance, distance);
        textSize = a.getDimensionPixelSize(R.styleable.JerryChartView_android_textSize, textSize);
        int cs = a.getInt(R.styleable.JerryChartView_chartStyle, 0);
        if (cs == 0) {
            chartStyle = ChartStyle.FANSHAPE;
        } else {
            chartStyle = ChartStyle.ANNULAR;
        }
        a.recycle();
        init(context);
    }

    /**
     * 初始化
     *
     * @param context
     */
    private void init(Context context) {
        this.context = context;
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setColor(lineColor);
        linePaint.setStrokeWidth(lineWidth);

        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(textSize);
        linePaint.setStyle(Paint.Style.STROKE);

    }

    /**
     * 设置数据
     *
     * @param datas
     */
    public void setData(List<ChartData> datas) {
        this.setData(datas, null);
    }

    /**
     * 设置数据
     *
     * @param datas
     * @param chartStyle 扇形or环形 ChartStyle.FANSHAPE|ChartStyle.ANNULAR
     */
    public void setData(List<ChartData> datas, ChartStyle chartStyle) {
        if (datas == null || datas.size() == 0) {
            Log.e("TAG", "datas is null or datas.size()==0");
            return;
        }
        this.datas = datas;
        if (chartStyle != null) {
            this.chartStyle = chartStyle;
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (datas == null || datas.size() == 0) {
            return;
        }
        /**
         * 文本位置，主要区分扇形、环形；至于扇形突出部分文本位置待定 扇形：文本应位于扇形中间 环形：文本应位于环形中间
         */
        float textLocation = 0f;
        switch (chartStyle) {
            case FANSHAPE:// 扇形
                textLocation = rectfSize * 7f / 24f;
                break;
            case ANNULAR:// 环形
                textLocation = rectfSize * 5f / 12f;
                break;
            default:
                break;
        }

        float sweepAngle = 0;
        for (int i = 0; i < datas.size(); i++) {
            ChartData chartData = datas.get(i);
            paint.setColor(chartData.getColor());
            sweepAngle = 360 * chartData.getProgress();

            RectF rectf2 = null;
            //Log.e(TAG, "startAngle=" + startAngle + ",sweepAngle=" + sweepAngle);
            float value = (startAngle + sweepAngle / 2) % 360;
            // Log.e(TAG, "value=" + value);
            if (value <= 0) {
                value = value + 360;
            }
            // Log.e(TAG, "value=" + value);
            float x = (float) (Math.abs(Math.cos(Math.toRadians(value))));
            float y = (float) (Math.abs(Math.sin(Math.toRadians(value))));

            /**
             * 1.见distance注释
             */
            // float a = (float) (distance /
            // (Math.abs(Math.sin(Math.toRadians(sweepAngle / 2)))));

            /**
             * 2.见distance注释
             */
            float a = distance;

            /**
             * 若扇形突出，绘制扇形时所需矩形的位置left，top，right，bottom
             */
            float left = 0;
            float top = 0;
            float right = 0;
            float bottom = 0;

            /**
             * 文本位置，textX,textY
             */
            float textX = 0;
            float textY = 0;

            /**
             * 文本 ，文本宽度
             */
            String text = (int) (chartData.getProgress() * 100) + "%";

            float textWidth = textPaint.measureText(text);

            if (value > 0 && value <= 90) {
                left = (width - rectfSize) / 2 + x * a;
                top = (height - rectfSize) / 2 + y * a;
                right = (width + rectfSize) / 2 + x * a;
                bottom = (height + rectfSize) / 2 + y * a;

                textX = width / 2 + x * textLocation - textWidth / 2;
                textY = height / 2 + y * textLocation + textSize / 2;
            } else if (value > 90 && value <= 180) {
                left = (width - rectfSize) / 2 - x * a;
                top = (height - rectfSize) / 2 + y * a;
                right = (width + rectfSize) / 2 - x * a;
                bottom = (height + rectfSize) / 2 + y * a;

                textX = width / 2 - x * textLocation - textWidth / 2;
                textY = height / 2 + y * textLocation + textSize / 2;
            } else if (value > 180 && value <= 270) {
                left = (width - rectfSize) / 2 - x * a;
                top = (height - rectfSize) / 2 - y * a;
                right = (width + rectfSize) / 2 - x * a;
                bottom = (height + rectfSize) / 2 - y * a;

                textX = width / 2 - x * textLocation - textWidth / 2;
                textY = height / 2 - y * textLocation + textSize / 2;
            } else if (value > 270 && value <= 360) {
                left = (width - rectfSize) / 2 + x * a;
                top = (height - rectfSize) / 2 - y * a;
                right = (width + rectfSize) / 2 + x * a;
                bottom = (height + rectfSize) / 2 - y * a;

                textX = width / 2 + x * textLocation - textWidth / 2;
                textY = height / 2 - y * textLocation + textSize / 2;
            }
            textPaint.setColor(chartData.getTextColor());
            /**
             * 只有扇形的情况下才支持突出显示
             */
            if (chartStyle == ChartStyle.FANSHAPE && chartData.isFloat()) {
                rectf2 = new RectF(left, top, right, bottom);
                canvas.drawArc(rectf2, startAngle, sweepAngle, true, paint);
                /**
                 * 待定 文本位置
                 */
                canvas.drawText(text, textX, textY, textPaint);

            } else {
                canvas.drawArc(rectf, startAngle, sweepAngle, true, paint);
                canvas.drawText(text, textX, textY, textPaint);
            }

            // Log.e(TAG, value + "," + textX + "," + textX + "," + text);
            /**
             * 绘制分割线
             */
            canvas.drawArc(rectf3, startAngle, sweepAngle, true, linePaint);

            startAngle = startAngle + sweepAngle;

        }
        switch (chartStyle) {
            case FANSHAPE:// 扇形

                break;
            case ANNULAR:// 环形
                paint.setColor(lineColor);
                canvas.drawCircle(width / 2, height / 2, rectfSize * 1 / 3, paint);
                break;

            default:
                break;
        }
    }

    /**
     * 以布局中设置的宽度为准，高度数据舍弃 矩形的边长为：宽度——2*distance
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        width = measureWidth(widthMeasureSpec);
        height = measureHeight(widthMeasureSpec);
        // 设置宽高
        setMeasuredDimension(width, height);
        rectfSize = width - distance * 2;
        rectf = new RectF((width - rectfSize) / 2, (height - rectfSize) / 2, (width + rectfSize) / 2,
                (height + rectfSize) / 2);
        rectf3 = new RectF(0, 0, width, width);
    }

    // 根据xml的设定获取宽度
    private int measureWidth(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        // wrap_content
        if (specMode == MeasureSpec.AT_MOST) {

        }
        // fill_parent或者精确值
        else if (specMode == MeasureSpec.EXACTLY) {

        }
        return specSize;
    }

    // 根据xml的设定获取高度
    private int measureHeight(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        // wrap_content
        if (specMode == MeasureSpec.AT_MOST) {

        }
        // fill_parent或者精确值
        else if (specMode == MeasureSpec.EXACTLY) {

        }
        return specSize;
    }
}
