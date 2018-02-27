package com.huang.cn.horizontalprogressbar;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import java.lang.ref.WeakReference;

/**
 * 自定义进度条
 * @author huangchaobo
 * create at 2018/2/6 10:33
 */
public class MixTextProgressBar extends ProgressBar {
    /**
     * 注意：容易理解错误的地方：
     * src表示的下一个准备绘制像素(或图形)。
     * dsc表示的是画布上已有的像素(或图形)，也就是准备绘制在画布上的目标像素。
     */
    private static final PorterDuffXfermode PORTER_DUFF_XFERMODE = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);  //只绘制交集部分。颜色去目标颜色
    private String mText = "";
    private Paint mTextPaint;
    private Paint mMixPaint;
    private int mMixTextColor = Color.WHITE;
    private int mTextColor = 0xFF3b3b3b;
    private float mTextSize = 36;
    private int gravity = 0;  //0:left  1:center  2:right
    private float padding = 0;  //相对于gravity的间距，当gravity = center时没有意义
    private int mProgress;//当前进度
    private MyHandler myHandler;
    /**目标进度*/
    private int targetProgress;

    private class MyHandler extends Handler {

        private WeakReference<Context> reference;

        MyHandler(Context context) {
            reference = new WeakReference<>(context);
        }

        @Override
        public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        if (mProgress <= targetProgress) {
                            setProgress(mProgress);
                            setText(mProgress/10+"/"+30);
                            myHandler.sendEmptyMessageDelayed(0, 0);
                            mProgress+=5;
                        }
                        break;

                    default:
                        break;
                }
        }

    }
    public MixTextProgressBar(Context context) {
        super(context);
        init(context, null);
    }

    public MixTextProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MixTextProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MixTextProgressBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }


    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //新建一个图层
        int drawableSaveCount = canvas.saveLayer(0, 0, getWidth(), getHeight(), null, Canvas.ALL_SAVE_FLAG);
        drawText(canvas, mTextPaint);
        if (!isIndeterminate() && !isInEditMode()) {
            drawMix(canvas, mMixPaint);
        }
        canvas.restoreToCount(drawableSaveCount);  //将新建的图层绘制到默认图层上
    }

    protected void drawMix(Canvas canvas, Paint paint) {
        if (TextUtils.isEmpty(mText)) {
            return;
        }
        int progress = getProgress();
        if (progress == 0) {
            return;
        }
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int progressWidth = getWidth() - paddingLeft - paddingRight;
        if (progressWidth <= 0) {
            return;
        }
        int left = paddingLeft;
        int right = (int) (paddingLeft + progressWidth * ((float) progress / getMax()));
        if (right - left <= 0) {
            return;
        }
        canvas.drawRect(left, 0, right, getHeight(), paint);
    }

    protected void drawText(Canvas canvas, Paint paint) {
        if (TextUtils.isEmpty(mText)) {
            return;
        }
        Rect rect = new Rect();
        paint.getTextBounds(this.mText, 0, this.mText.length(), rect);
        float y = (getHeight() / 2) - rect.centerY();
        float x;
        switch (gravity) {
            case 0:  //left
                x = getPaddingLeft() + padding;
                break;
            case 1:  //center
                x = (getWidth() / 2) - rect.centerX();
                break;
            default:  //right
                x = getWidth() - rect.width() - padding;
                break;
        }
        canvas.drawText(this.mText, x, y, paint);
    }

    @Override
    public void setProgress(int progress) {
        super.setProgress(progress);
    }
    /**设置进度，带动效*/
    public void setDynamicProgress(int progress) {
        mProgress=0;
        this.targetProgress=progress;
        myHandler.sendEmptyMessageDelayed(0, 0);
    }

    private void init(Context context, AttributeSet attrs) {
        //init attrs
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MixTextProgressBar);
            mMixTextColor = a.getColor(R.styleable.MixTextProgressBar_mixTextColor, Color.WHITE);
            mTextColor = a.getColor(R.styleable.MixTextProgressBar_textColor, 0xFF3b3b3b);
            mTextSize = a.getDimension(R.styleable.MixTextProgressBar_textSize, 36);
            gravity = a.getInt(R.styleable.MixTextProgressBar_textGravity, 0);
            padding = a.getDimension(R.styleable.MixTextProgressBar_textPadding,10);
            a.recycle();
        }

        //init TextPaint
        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(mTextSize);

        //init MixPaint
        mMixPaint = new Paint();
        mMixPaint.setXfermode(PORTER_DUFF_XFERMODE);
        mMixPaint.setAntiAlias(true);
        mMixPaint.setColor(mMixTextColor);
        mMixPaint.setFilterBitmap(false);

        myHandler = new MyHandler(context);
    }

    public void setTextColor(int color) {
        if (mTextColor != color) {
            mTextColor = color;
            mTextPaint.setColor(mTextColor);
            invalidate();
        }
    }

    public void setTextSize(int size) {
        this.mTextSize = size;
        mTextPaint.setTextSize(mTextSize);
        invalidate();
    }

    public void setMixTextColor(int color) {
        if (mMixTextColor != color) {
            mMixTextColor = color;
            mMixPaint.setColor(mMixTextColor);
            invalidate();
        }
    }

    public void setText(String text) {
        if (this.mText == null && text == null) {
            return;
        }
        if (!TextUtils.isEmpty(this.mText) && this.mText.equals(text)) {
            return;
        }
        this.mText = text;
        postInvalidate();
    }

}
