package com.healthy.util;

import java.lang.ref.SoftReference;
import org.jivesoftware.smack.XMPPException;

import com.healthy.logic.HealthyApplication;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class AsyncImageDownLoader {

	public Bitmap loadImage(final String imageUrl, final ImageCallback callback) {
		Bitmap bitmap = null;
		if (HealthyApplication.imageCache.containsKey(imageUrl)) {
			Log.i("tag", "cache�Ѿ�����------>"+imageUrl);
			SoftReference<Bitmap> softReference = HealthyApplication.imageCache.get(imageUrl);
			bitmap = softReference.get();
			if (bitmap != null) {
				return bitmap;
			}else {// �����ͼƬ�Ѿ����ͷţ��򽫸�path��Ӧ�ļ�ֵ�Դ�map���Ƴ�
				HealthyApplication.imageCache.remove(imageUrl);
				Log.i("tag", "���ͷ�");
			}
		}

		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				Log.i("tag", "msg");
				callback.imageLoaded((Bitmap) msg.obj);
			}
		};

		new Thread() {
			public void run() {
				Bitmap bitmap = null;
				try {
					Log.i("tag", "ִ�е�����" + imageUrl);
					bitmap = HealthyUtil.getInstance().getUserAvatar(imageUrl);
				} catch (XMPPException e) {
					e.printStackTrace();
				} catch (HealthyException e) {
					e.printStackTrace();
				}
				HealthyApplication.imageCache.put(imageUrl, new SoftReference<Bitmap>(bitmap));
				handler.sendMessage(handler.obtainMessage(0, bitmap));
				Log.i("tag", "sendhandler");
			};
		}.start();

		return null;
	}

	public interface ImageCallback {
		public void imageLoaded(Bitmap bitmap);
	}

	/**
	 * ����ͼƬ��Ҫ�Ŀ�Ⱥ͸߶�
	 * 
	 * @param f
	 *            ���õĿ�� height ���õĸ߶�
	 */
	public static Bitmap changeBitmapWH(Bitmap bitmap, float f, float g) {
		int originalWidth = 0, originalHeight = 0;
		if (bitmap.getWidth() == 0 || bitmap.getHeight() == 0) {

		} else {
			originalWidth = bitmap.getWidth();
			originalHeight = bitmap.getHeight();
			// ����ѹ����
			float scaleWidth = (float) f / originalWidth;
			float scaleHeight = (float) g / originalHeight;
			Matrix matrix = new Matrix();
			matrix.postScale(scaleWidth, scaleHeight);
			bitmap = Bitmap.createBitmap(bitmap, 0, 0, originalWidth,
					originalHeight, matrix, true);
		}
		return bitmap;
	}

	/**
	 * 
	 * @param bitmap
	 *            Ҫת��ΪԲ�ǵ�ͼƬbitmap
	 * @param pixels
	 *            Բ�ǵĴ�С���� ��ֵԽ��Բ��Խ��,����Ϊ4
	 * @return ������Բ��ͼƬ
	 */
	public static Bitmap toRoundCorner(Bitmap bitmap, int pixels) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		final float roundPx = pixels;
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
		return output;
	}

	/**
	 * ת��ͼƬΪԲ��
	 * 
	 * @param Bitmap
	 *            ��Ҫ�����ͼƬ �����ΪԲ�� bitmap
	 * 
	 */
	public static Bitmap toRoundBitmap(Bitmap bitmap, int width, int height) {
		Bitmap changeBitmap = changeBitmapWH(bitmap, width, height);
		Log.i("tag", "ͼƬ�Ŀ�ߣ�"+width+"--"+height);
		float roundPx;
		float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
		roundPx = width / 2;
		top = 0;
		bottom = width;
		left = 0;
		right = width;
		height = width;
		dst_left = 0;
		dst_top = 0;
		dst_right = width;
		dst_bottom = width;
		Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect src = new Rect((int) left, (int) top, (int) right,
				(int) bottom);
		final Rect dst = new Rect((int) dst_left, (int) dst_top,
				(int) dst_right, (int) dst_bottom);
		final RectF rectF = new RectF(dst);
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(changeBitmap, src, dst, paint);
		return output;
	}

	/**
	 * �ϳ�����bitmap����
	 * 
	 * @param background
	 *            ������ʽ
	 * @param foreground
	 *            ǰ�渲�ǵ���ʽ
	 * @return �ϳɽ��bitmap
	 */
	public static Bitmap combineDrawable(Bitmap background, Bitmap foreground) {
		if (background == null) {
			return null;
		}
		int bgWidth = background.getWidth();
		int bgHeight = background.getHeight();
		int fgWidth = foreground.getWidth();
		int fgHeight = foreground.getHeight();
		Bitmap newmap = Bitmap
				.createBitmap(bgWidth, bgHeight, Config.ARGB_8888);
		Canvas canvas = new Canvas(newmap);
		canvas.drawBitmap(background, 0, 0, null);
		canvas.drawBitmap(foreground, (bgWidth - fgWidth) / 2,
				(bgHeight - fgHeight) / 2 - 5*HealthyApplication.phoneScale, null);
		canvas.save(Canvas.ALL_SAVE_FLAG);
		canvas.restore();
		return newmap;
	}
}
