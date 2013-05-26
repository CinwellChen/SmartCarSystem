package com.guet.Reader.Utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;

public class ColorPickerDialog extends Dialog {
	private Context mContext;
	private String mTitle;
	private int mInitialColor;
	private OnColorChangedListener mListener;

	public ColorPickerDialog(Context context, String title,
			OnColorChangedListener l) {
		this(context, title, Color.GRAY, l);

	}

	public ColorPickerDialog(Context context, String title, int initalColor,
			OnColorChangedListener l) {
		super(context);
		mContext = context;
		mTitle = title;
		mListener = l;
		mInitialColor = initalColor;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(new LandscapeColorPickerView(mContext, mListener));

		setTitle(mTitle);
	}

	public interface OnColorChangedListener {
		void colorChanged(int color);
	}

	protected abstract class ColorPickerView extends View {
		protected OnColorChangedListener mListener;
		protected Paint mCirclePaint;// 渐变色环画笔
		protected Paint mCenterPaint;// 中间圆画笔
		protected Paint mLinePaint;// 分隔线画笔
		protected Paint mRectPaint;// 渐变方块画笔

		protected Shader mRectShader;// 渐变方块渐变图像
		protected float mRectLeft;// 渐变方块左顶点x坐标
		protected float mRectTop;// 渐变方块左顶点y坐标
		protected float mRectRight;// 渐变方块右底点x坐标
		protected float mRectBottom;// 渐变方块右底点y坐标

		protected int mHeight;// View高
		protected int mWidth;// View宽
		protected float mCircleRadius;// 色环半径(paint中部)
		protected float mCenterRadius;// 中心圆半径

		protected boolean mDownInCircle = true;// 按在渐变环上
		protected boolean mDownInRect;// 按在渐变方块上
		protected boolean mHighlightCenter;// 高亮
		protected boolean mlittleLightCenter;// 微亮

		protected final int[] mCircleColors;// 渐变色环颜色
		protected final int[] mRectColors;// 渐变方块颜色

		public ColorPickerView(Context context, OnColorChangedListener l) {
			super(context);
			this.mListener = l;
			mCircleColors = new int[] { 0xFFFF0000, 0xFFFF00FF, 0xFF0000FF,
					0xFF00FFFF, 0xFF00FF00, 0xFFFFFF00, 0xFFFF0000 };
			mRectColors = new int[] { 0xFF000000, mInitialColor, 0xFFFFFFFF };
		}

		protected int ave(int s, int d, float p) {
			return s + Math.round(p * (d - s));
		}

		protected boolean inColorCircle(float x, float y, float outRadius,
				float inRadius) {
			double outCircle = Math.PI * outRadius * outRadius;
			double inCircle = Math.PI * inRadius * inRadius;
			double fingerCircle = Math.PI * (x * x + y * y);
			return (fingerCircle < outCircle && fingerCircle > inCircle);
		}

		protected boolean inCenter(float x, float y, float centerRadius) {
			double centerCircle = Math.PI * centerRadius * centerRadius;
			double fingerCircle = Math.PI * (x * x + y * y);
			return (fingerCircle < centerCircle);
		}

		protected boolean inRect(float x, float y) {
			return (x <= mRectRight && x >= mRectLeft && y <= mRectBottom && y >= mRectTop);
		}

		protected int interpCircleColor(int colors[], float unit) {
			if (unit <= 0) {
				return colors[0];
			}
			if (unit >= 1) {
				return colors[colors.length - 1];
			}

			float p = unit * (colors.length - 1);
			int i = (int) p;
			p -= i;

			int c0 = colors[i];
			int c1 = colors[i + 1];
			int a = ave(Color.alpha(c0), Color.alpha(c1), p);
			int r = ave(Color.red(c0), Color.red(c1), p);
			int g = ave(Color.green(c0), Color.green(c1), p);
			int b = ave(Color.blue(c0), Color.blue(c1), p);

			return Color.argb(a, r, g, b);
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			super.onMeasure(mWidth, mHeight);
		}

		protected void onActionDown(boolean inCircle, boolean inCenter,
				boolean inRect) {
			mDownInCircle = inCircle;
			mDownInRect = inRect;
			mHighlightCenter = inCenter;
		}

		abstract int interpRectColor(int[] colors, float x, float y);

		protected void onActionMove(float x, float y, boolean inCircle,
				boolean inCenter, boolean inRect) {
			if (mDownInCircle && inCircle) {
				final float angle = (float) Math.atan2(y, x);
				float unit = (float) (angle / (2 * Math.PI));
				if (unit < 0) {
					unit += 1;
				}
				mCenterPaint.setColor(interpCircleColor(mCircleColors, unit));
			} else if (mDownInRect && inRect) {
				mCenterPaint.setColor(interpRectColor(mRectColors, x, y));
			}
			if ((mHighlightCenter && inCenter)
					|| (mlittleLightCenter && inCenter)) {
				mHighlightCenter = true;
				mlittleLightCenter = false;
			} else if (mHighlightCenter || mlittleLightCenter) {
				mHighlightCenter = false;
				mlittleLightCenter = true;
			} else {
				mHighlightCenter = false;
				mlittleLightCenter = false;
			}
			invalidate();
		}

		protected void onActionUp(boolean inCenter) {
			if (mHighlightCenter && inCenter) {// 点击在中心圆, 且当前启动在中心圆
				if (this.mListener != null) {
					this.mListener.colorChanged(mCenterPaint.getColor());
					ColorPickerDialog.this.dismiss();
				}
			}
			if (mDownInCircle) {
				mDownInCircle = false;
			}
			if (mDownInRect) {
				mDownInRect = false;
			}
			if (mHighlightCenter) {
				mHighlightCenter = false;
			}
			if (mlittleLightCenter) {
				mlittleLightCenter = false;
			}
			invalidate();
		}
	}

	private class LandscapeColorPickerView extends ColorPickerView {

		public LandscapeColorPickerView(Context context,
				OnColorChangedListener l) {
			super(context, l);
			Display display = ColorPickerDialog.this.getWindow()
					.getWindowManager().getDefaultDisplay();
			int height = (int) (display.getHeight() * 0.5f);
			int width = (int) (display.getWidth() * 0.5f);
			this.mHeight = height;
			this.mWidth = width;
			setMinimumHeight(height);
			setMinimumWidth(width);
			Shader s = new SweepGradient(0, 0, mCircleColors, null);
			mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mCirclePaint.setShader(s);
			mCirclePaint.setStyle(Paint.Style.STROKE);
			mCirclePaint.setStrokeWidth(50);
			mCircleRadius = mHeight / 2 * 0.7f - mCirclePaint.getStrokeWidth()
					* 0.5f;
			mCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mCenterPaint.setColor(mInitialColor);
			mCenterPaint.setStrokeWidth(5);
			mCenterRadius = (mCircleRadius - mCirclePaint.getStrokeWidth() / 2) * 0.7f;
			mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mLinePaint.setColor(Color.parseColor("#72A1D1"));
			mLinePaint.setStrokeWidth(4);
			mRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mRectPaint.setStrokeWidth(5);
			mRectLeft = mCircleRadius + mCirclePaint.getStrokeWidth() * 0.5f
					+ mLinePaint.getStrokeMiter() * 0.5f + 15;
			mRectTop = -mCircleRadius - mCirclePaint.getStrokeWidth() * 0.5f;
			mRectRight = mRectLeft + 50;
			mRectBottom = mCircleRadius + mCirclePaint.getStrokeWidth() * 0.5f;
		}

		@Override
		protected void onDraw(Canvas canvas) {
			canvas.translate(mWidth / 2 - 50, mHeight / 2);
			canvas.drawCircle(0, 0, mCenterRadius, mCenterPaint);
			if (mHighlightCenter || mlittleLightCenter) {
				int c = mCenterPaint.getColor();
				mCenterPaint.setStyle(Paint.Style.STROKE);
				if (mHighlightCenter) {
					mCenterPaint.setAlpha(0xFF);
				} else if (mlittleLightCenter) {
					mCenterPaint.setAlpha(0x90);
				}
				canvas.drawCircle(0, 0,
						mCenterRadius + mCenterPaint.getStrokeWidth(),
						mCenterPaint);

				mCenterPaint.setStyle(Paint.Style.FILL);
				mCenterPaint.setColor(c);
			}
			canvas.drawOval(new RectF(-mCircleRadius, -mCircleRadius,
					mCircleRadius, mCircleRadius), mCirclePaint);
			if (mDownInCircle) {
				mRectColors[1] = mCenterPaint.getColor();
			}
			mRectShader = new LinearGradient(0, mRectTop, 0, mRectBottom,
					mRectColors, null, Shader.TileMode.MIRROR);
			mRectPaint.setShader(mRectShader);
			canvas.drawRect(mRectLeft, mRectTop, mRectRight, mRectBottom,
					mRectPaint);
			float offset = mLinePaint.getStrokeWidth() / 2;
			canvas.drawLine(mRectLeft - offset, mRectTop - offset * 2,
					mRectLeft - offset, mRectBottom + offset * 2, mLinePaint);// 左
			canvas.drawLine(mRectLeft - offset * 2, mRectTop - offset,
					mRectRight + offset * 2, mRectTop - offset, mLinePaint);// 上
			canvas.drawLine(mRectRight + offset, mRectTop - offset * 2,
					mRectRight + offset, mRectBottom + offset * 2, mLinePaint);// 右
			canvas.drawLine(mRectLeft - offset * 2, mRectBottom + offset,
					mRectRight + offset * 2, mRectBottom + offset, mLinePaint);// 下
			super.onDraw(canvas);
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			float x = event.getX() - mWidth / 2 + 50;
			float y = event.getY() - mHeight / 2;
			boolean inCircle = inColorCircle(x, y,
					mCircleRadius + mCirclePaint.getStrokeWidth() / 2,
					mCircleRadius - mCirclePaint.getStrokeWidth() / 2);
			boolean inCenter = inCenter(x, y, mCenterRadius);
			boolean inRect = inRect(x, y);
			System.out.println(x + "..." + y);
			System.out.println(mRectLeft + "..." + mRectRight + "..."
					+ mRectTop + "..." + mRectBottom);
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				onActionDown(inCircle, inCenter, inRect);
			case MotionEvent.ACTION_MOVE:
				onActionMove(x, y, inCircle, inCenter, inRect);
				break;
			case MotionEvent.ACTION_UP:
				onActionUp(inCenter);
				break;
			}
			return true;
		}

		protected int interpRectColor(int colors[], float x, float y) {
			int a, r, g, b, c0, c1;
			float p;
			float referLine = mRectBottom;
			if (y < 0) {
				c0 = colors[0];
				c1 = colors[1];
				p = (y + referLine) / referLine;
			} else {
				c0 = colors[1];
				c1 = colors[2];
				p = y / referLine;
			}
			a = ave(Color.alpha(c0), Color.alpha(c1), p);
			r = ave(Color.red(c0), Color.red(c1), p);
			g = ave(Color.green(c0), Color.green(c1), p);
			b = ave(Color.blue(c0), Color.blue(c1), p);
			return Color.argb(a, r, g, b);
		}
	}

}
