/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zuoni.qrcode.zxing.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import com.google.zxing.ResultPoint;
import com.zuoni.qrcode.R;
import com.zuoni.qrcode.zxing.camera.CameraManager;
import java.util.Collection;
import java.util.HashSet;
/**
 * 二维码扫描动画界面
 * 中间横条移动
 */
public final class ViewfinderView extends View {

    private static final int[] SCANNER_ALPHA = {0, 64, 128, 192, 255, 192, 128, 64};
    private static final long ANIMATION_DELAY = 10L;
    private static final int OPAQUE = 0xFF;
    Bitmap line1;
    private Bitmap resultBitmap;
    private int scannerAlpha;
    private Collection<ResultPoint> possibleResultPoints;
    private Collection<ResultPoint> lastPossibleResultPoints;

    private Paint mPaint;


    private Bitmap bitmap_scan_line;//扫描的那个横线
    private int scan_line_y = 100;//横线的y坐标
    private int scan_line_height = 0;//扫描线的高度
    private int scan_linr_weight = 0;//扫描线的宽度


    /**
     * 构造方法
     * 这里初始化一些全局变量在onDraw不需要重新计算获得
     */
    public ViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);


        scannerAlpha = 0;
        possibleResultPoints = new HashSet<ResultPoint>(5);
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
//    mPaint.setAntiAlias(false);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(6);
//    mPaint.setAlpha(100);
        line1 = BitmapFactory.decodeResource(this.getContext().getResources(), R.drawable.scan_move_line);

    }

    /**
     * 绘制界面
     */
    @Override
    public void onDraw(Canvas canvas) {
        Rect frame = CameraManager.get().getFramingRect();
        if (frame == null) {
            return;
        }
        int height = canvas.getHeight();//得到当前画布的高度
        int width = canvas.getWidth();//获得当前画布的宽度

        if (scan_line_y < height * 3 / 10) {
            scan_line_y = height * 3 / 10;//当扫描线在扫描框的上方时候，将扫描线移动到扫描框最上方
        }

        int lineX1 = width / 6;
        int lineX2 = width * 5 / 6;
        if (bitmap_scan_line == null) {
            bitmap_scan_line = scaleBitmap(line1, width * 2 / 3, height / 100);
        }

        //绘制二维码扫描的横线
        canvas.drawBitmap(bitmap_scan_line, lineX1, scan_line_y, null);
        scan_line_y = scan_line_y + 5;
        if (scan_line_y > height * 5 / 7) {
            scan_line_y = height * 3 / 10;
        }

        if (resultBitmap != null) {

        } else {
            postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top, frame.right, frame.bottom);
        }
    }

    public void drawViewfinder() {
        resultBitmap = null;
        invalidate();
    }

    /**
     * Draw a bitmap with the result points highlighted instead of the live scanning display.
     *
     * @param barcode An image of the decoded barcode.
     */
    public void drawResultBitmap(Bitmap barcode) {
        resultBitmap = barcode;
        invalidate();
    }

    public void addPossibleResultPoint(ResultPoint point) {
        possibleResultPoints.add(point);
    }

    /**
     * 根据给定的宽和高进行拉伸
     *
     * @param origin    原图
     * @param newWidth  新图的宽
     * @param newHeight 新图的高
     * @return new Bitmap
     */
    private Bitmap scaleBitmap(Bitmap origin, int newWidth, int newHeight) {
        if (origin == null) {
            return null;
        }
        int height = origin.getHeight();
        int width = origin.getWidth();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);// 使用后乘
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (!origin.isRecycled()) {
            origin.recycle();
        }
        return newBM;
    }
}
