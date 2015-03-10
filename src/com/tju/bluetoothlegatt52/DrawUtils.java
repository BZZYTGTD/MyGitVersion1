package com.tju.bluetoothlegatt52;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.view.SurfaceHolder;

public class DrawUtils {
	private int mHeight;
	private int mWidth;
	private int mTop;
	private Paint mPaint;
	private int nBaseLine;
	private int nYNum;
	private int nXNum;
	private int nDelta;
	float fStartX = 100.0f;
	float fEndX = 0.0f;
	private SurfaceHolder mSurfaceHolder;
	
	public void setValue(int Height,int Width, int Top,SurfaceHolder surfaceHolder,Paint Paint){
		mHeight = Height;
		mWidth = Width;
		mTop = Top;
		mSurfaceHolder = surfaceHolder;
		mPaint = Paint;
	}
	
	public void draw(){
		mPaint = new Paint();
 		mPaint.setStyle(Style.STROKE);
 		mPaint.setAntiAlias(true);
 		mPaint.setColor(Color.WHITE);
 		mPaint.setStrokeWidth(2);

 		nBaseLine = mHeight / 2 + mTop;
 		nYNum = 6;                               ///////
 		nDelta = mHeight /nYNum / 5;    ///////
 		nXNum = mWidth / (nDelta * 5);    ///////
 		fStartX = ((float) mWidth - (nXNum * 5 * nDelta)) / 2.0f;    ////////
 		fEndX = mWidth - fStartX;           //////

    	Canvas canvas = mSurfaceHolder.lockCanvas(
				new Rect(0, 0, mWidth, mHeight));// 关键:获取画布
			canvas.drawColor(Color.BLACK);// 清除背景
			// TODO: handle exception
		
		mPaint.setStrokeWidth(1);
		mPaint.setStyle(Style.STROKE);
		mPaint.setColor(Color.argb(255, 248, 195, 175));
//		mPaint.setColor(Color.argb(255, 50, 50, 50));

		for (int yy = 0; yy <= nYNum * 5; yy++) {
			canvas.drawLine(fStartX, nBaseLine + (yy - 15) * (nDelta),
					fEndX, nBaseLine + (yy - 15) * (nDelta), mPaint);
		}
		for (int xx = 0; xx <= nXNum * 5; xx++) {
			canvas.drawLine(fStartX + xx * nDelta, nBaseLine - 3
					* (nDelta * 5), fStartX + xx * nDelta, nBaseLine + 3
					* (nDelta * 5), mPaint);
		}
	
		mPaint.setColor(Color.argb(255, 240, 152, 116));
//		mPaint.setColor(Color.argb(255, 70, 50, 50));

		for (int yy = 0; yy <= nYNum; yy++) {
			canvas.drawLine(fStartX, nBaseLine + (yy - 3) * (nDelta * 5),
					fEndX, nBaseLine + (yy - 3) * (nDelta * 5), mPaint);
		}
		for (int xx = 0; xx <= nXNum * 5; xx++) {
			canvas.drawLine(fStartX + xx * 5 * nDelta, nBaseLine - 3
					* (nDelta * 5), fStartX + xx * 5 * nDelta, nBaseLine
					+ 3 * (nDelta * 5), mPaint);
		}
        mSurfaceHolder.unlockCanvasAndPost(canvas);// 解锁画布，提交画好的图像   
	}
	
}

