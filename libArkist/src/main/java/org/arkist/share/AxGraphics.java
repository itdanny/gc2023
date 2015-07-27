package org.arkist.share;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class AxGraphics {
	static final private String TAG = AxGraphics.class.getSimpleName();
	/*
	 * Other EFFECT.....
	 * http://www.shaikhhamadali.blogspot.ro/2013/06/highlightfocusshadow-image-in-imageview.html
	 */

	public AxGraphics() {

	}
	static public Bitmap getCreateBitmap(int width, int height, Config config){
		Bitmap bitmap;
		try {
			bitmap = Bitmap.createBitmap(width, height, config);
		} catch (Exception e){
			bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		}
		return bitmap;
	}
	static public Bitmap drawableToBitmap(Drawable drawable) {
	    if (drawable instanceof BitmapDrawable) {
	        return ((BitmapDrawable)drawable).getBitmap();
	    }
	    int width = drawable.getIntrinsicWidth(); // May return -1 if drawable is solid color;
	    int height = drawable.getIntrinsicHeight(); 
	    if (width==-1 || height==-1){
	    	Log.e(TAG,"Solid Color Drawable cannot change to bitmap");
	    	width=1;
	    	height=1;
	    }
	    Bitmap bitmap = getCreateBitmap(width, height, Config.ARGB_8888);
	    Canvas canvas = new Canvas(bitmap); 
	    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
	    drawable.draw(canvas);
	    return bitmap;		
	}
	static public int getGrayMaskColor(int color) {
		int b = color & 0xff;
		int g = (color & 0xff00) >> 8;
		int r = (color & 0xff0000) >> 16;
		int a = color >> 24;
		int gray = 50;
		float radio = 0.5f;
		b = hold(b * radio + gray);
		g = hold(g * radio + gray);
		r = hold(r * radio + gray);
		return (a << 24) | (r << 16) | (g << 8) | b;
	}
		static private int hold(float v) {
			int r = (int) v;
			return r > 255 ? 255 : r;
		}
	static public Bitmap getColorBitmap(Resources res, int drawableID, int colorID){
		return AxGraphics.getColorBitmap(BitmapFactory.decodeResource(res,drawableID),res.getColor(colorID));		
	}
	/*
	 * Below graphic function is powerful but it used a lot of CPU time to do it.
	 */
	static public Bitmap getColorBitmap(Bitmap src, int bitmapColor) {
		// original image size
	     int width = src.getWidth();
	     int height = src.getHeight();
	     // create output bitmap
	     Bitmap bmOut = getCreateBitmap(width, height, src.getConfig());
	     // color information
	     int A, R, G, B;
	     int pixel;	  
	     // scan through all pixels
	     for(int x = 0; x < width; ++x) {
	         for(int y = 0; y < height; ++y) {
	             // get pixel color
	             pixel = src.getPixel(x, y);
	             A = Color.alpha(pixel);
	             R = Color.red(bitmapColor);
	             G = Color.green(bitmapColor);
	             B = Color.blue(bitmapColor);	             
	               // apply new pixel color to output bitmap
	             bmOut.setPixel(x, y, Color.argb(A, R, G, B));
	         }
	     }	  
	     // return final image
	     return bmOut;
	}
	static public Bitmap getHighlight(Bitmap src) {				
        // create new bitmap, which will be painted and becomes result image
        Bitmap bmOut = getCreateBitmap(src.getWidth(),  src.getHeight(), src.getConfig());
        // setup canvas for painting
        Canvas canvas = new Canvas(bmOut);
        // setup default color
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        // create a blur paint for capturing alpha
        Paint ptBlur = new Paint();
        ptBlur.setMaskFilter(new BlurMaskFilter(25, Blur.NORMAL)); // Increate Number to make brighter
        int[] offsetXY = new int[2];
        // capture alpha into a bitmap
        Bitmap bmAlpha = src.extractAlpha(ptBlur, offsetXY);
        // create a color paint
        Paint ptAlphaColor = new Paint();
        ptAlphaColor.setColor(0xFFFFFFFF);
        // paint color for captured alpha region (bitmap)
        canvas.drawBitmap(bmAlpha, offsetXY[0], offsetXY[1], ptAlphaColor);
        // free memory
        bmAlpha.recycle();

        // paint the image source
        canvas.drawBitmap(src, 0, 0, null);

        // return out final image
        return bmOut;
    }
	static public Bitmap getGreyScale(Bitmap src) {
	    // constant factors
	    final double GS_RED = 0.299;
	    final double GS_GREEN = 0.587;
	    final double GS_BLUE = 0.114;
	 
	    // create output bitmap
	    Bitmap bmOut = getCreateBitmap(src.getWidth(), src.getHeight(), src.getConfig());	    
	    // pixel information
	    int A, R, G, B;
	    int pixel;
	 
	    // get image size
	    int width = src.getWidth();
	    int height = src.getHeight();
	 
	    // scan through every single pixel
	    for(int x = 0; x < width; ++x) {
	        for(int y = 0; y < height; ++y) {
	            // get one pixel color
	            pixel = src.getPixel(x, y);
	            // retrieve color of all channels
	            A = Color.alpha(pixel);
	            R = Color.red(pixel);
	            G = Color.green(pixel);
	            B = Color.blue(pixel);
	            // take conversion up to one single value
	            R = G = B = (int)(GS_RED * R + GS_GREEN * G + GS_BLUE * B);
	            // set new pixel color to output bitmap
	            bmOut.setPixel(x, y, Color.argb(A, R, G, B));
	        }
	    }
	 
	    // return final image
	    return bmOut;
	}
	static public  Bitmap getContrast(Bitmap src, double value) {
		// src image size
	    int width = src.getWidth();
	    int height = src.getHeight();
	    // create output bitmap with original size
	    Bitmap bmOut = getCreateBitmap(width, height, src.getConfig());	    	    
	    // color information
	    int A, R, G, B;
	    int pixel;
	    // get contrast value
	    double contrast = Math.pow((100 + value) / 100, 2);
	 
	    // scan through all pixels
	    for(int x = 0; x < width; ++x) {
	        for(int y = 0; y < height; ++y) {
	            // get pixel color
	            pixel = src.getPixel(x, y);
	            A = Color.alpha(pixel);
	            // apply filter contrast for every channel R, G, B
	            R = Color.red(pixel);
	            R = (int)(((((R / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
	            if(R < 0) { R = 0; }
	            else if(R > 255) { R = 255; }
	            
	            G = Color.red(pixel);
	            G = (int)(((((G / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
	            if(G < 0) { G = 0; }
	            else if(G > 255) { G = 255; }
	 
	            B = Color.red(pixel);
	            B = (int)(((((B / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
	            if(B < 0) { B = 0; }
	            else if(B > 255) { B = 255; }
	 
	            // set new pixel color to output bitmap
	            bmOut.setPixel(x, y, Color.argb(A, R, G, B));
	        }
	    }	  
	    // return final image
	    return bmOut;    
	}
	static public Bitmap getBrightness(Bitmap src, int value) { // +20,+40..-20,-40...
	     // original image size
	     int width = src.getWidth();
	     int height = src.getHeight();
	     // create output bitmap
	     Bitmap bmOut = getCreateBitmap(width, height, src.getConfig());		 
	     // color information
	     int A, R, G, B;
	     int pixel;
	  
	     // scan through all pixels
	     for(int x = 0; x < width; ++x) {
	         for(int y = 0; y < height; ++y) {
	             // get pixel color
	             pixel = src.getPixel(x, y);
	             A = Color.alpha(pixel);
	             R = Color.red(pixel);
	             G = Color.green(pixel);
	             B = Color.blue(pixel);
	  
	             // increase/decrease each channel
	             R += value;
	             if(R > 255) { R = 255; }
	             else if(R < 0) { R = 0; }
	  
	             G += value;
	             if(G > 255) { G = 255; }
	             else if(G < 0) { G = 0; }
	  
	             B += value;
	             if(B > 255) { B = 255; }
	             else if(B < 0) { B = 0; }
	  
	             // apply new pixel color to output bitmap
	             bmOut.setPixel(x, y, Color.argb(A, R, G, B));
	         }
	     }
	  
	     // return final image
	     return bmOut;
	 }
	 static public Bitmap getInvert(Bitmap src) {
	        // create new bitmap with the same attributes(width,height)
	         //as source bitmap
	        Bitmap bmOut = getCreateBitmap(src.getWidth(), src.getHeight(), src.getConfig());
	        // color info
	        int A, R, G, B;
	        int pixelColor;
	        // image size
	        int height = src.getHeight();
	        int width = src.getWidth();
	     
	        // scan through every pixel
	        for (int y = 0; y < height; y++)
	        {
	            for (int x = 0; x < width; x++)
	            {
	                // get one pixel
	                pixelColor = src.getPixel(x, y);
	                // saving alpha channel
	                A = Color.alpha(pixelColor);
	                // inverting byte for each R/G/B channel
	                R = 255 - Color.red(pixelColor);
	                G = 255 - Color.green(pixelColor);
	                B = 255 - Color.blue(pixelColor);
	                // set newly-inverted pixel to output image
	                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
	            }
	        }
	     
	        // return final bitmap
	        return bmOut;
	    }
	// Below is DCv73
	 
		// DCv76
		static public Bitmap getBitmapUponHalfDevicePixels(InputStream inputStream){
			int reqWidth=AxTools.getScreenWidth();//Screen Rotate may change this figures
	        int reqHeight=AxTools.getScreenHeight();
	        return getBitmapUponDevicePixels(inputStream, reqWidth, reqHeight, true);
		}
		static public Bitmap getBitmapUponDevicePixels( byte [] image){
			ByteArrayInputStream imageStream = new ByteArrayInputStream(image);
			Bitmap bitmap =  getBitmapUponDevicePixels(imageStream);
			try {imageStream.close();} catch (IOException e) {}
			return bitmap;
		}
		static public Bitmap getResizedBitmap(Bitmap bitmap, int reqWidth, int reqHeight, boolean isKeepRatio){
			int sourceWidth = bitmap.getWidth();
		    int sourceHeight = bitmap.getHeight();
	    	float scaleWidth;
		    float scaleHeight;
		    if (isKeepRatio){
		    	int newWidth;
			    int newHeight;
			    float newWidthScale;		    
			    float newHeightScale;
		    	if (reqWidth==-1){//follow height;
		    		scaleHeight = ((float) reqHeight) / sourceHeight;
		    		scaleWidth = scaleHeight;		
		    	} else if (reqHeight==-1){
		    		scaleWidth = ((float) reqWidth) / sourceWidth;
		    		scaleHeight = scaleWidth;		    		
		    	} else {
		    		newWidth = sourceWidth * reqHeight / sourceHeight;
				    newHeight = sourceHeight * reqWidth / sourceWidth;
				    newWidthScale = newWidth / sourceWidth;		    
				    newHeightScale = newHeight / sourceHeight;
				    scaleWidth  = Math.min(newWidthScale, newHeightScale);/*Use smaller scaling; Keep ratio*/
				    scaleHeight = scaleWidth;
		    	} 
		    } else {
		    	if (reqWidth==-1){
		    		scaleHeight = ((float) reqHeight) / sourceHeight;
		    		scaleWidth = scaleHeight;
		    	} else if (reqHeight==-1){
		    		scaleWidth = ((float) reqWidth) / sourceWidth;
		    		scaleHeight = scaleWidth;
		    	} else {
			    	scaleWidth = ((float) reqWidth) / sourceWidth;
				    scaleHeight = ((float) reqHeight) / sourceHeight;
		    	}
		    }
		    Matrix matrix = new Matrix();
		    matrix.postScale(scaleWidth, scaleHeight);
		    Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, sourceWidth, sourceHeight, matrix, false);
		    return resizedBitmap;						
		}
		static public Bitmap getBitmapUponDevicePixels(int drawableID){
			final InputStream inputStream = Resources.getSystem().openRawResource(drawableID);
			Bitmap bitmap = getBitmapUponDevicePixels(inputStream);
			try {
				if (bitmap==null){
					bitmap = getBitmapUponHalfDevicePixels(inputStream);
				}
				inputStream.close();
			} catch (IOException e) {
				
			}
			return bitmap;
		}
		static public Bitmap getBitmapUponDevicePixels(InputStream inputStream){			
			int reqWidth=AxTools.getScreenWidth();//Screen Rotate may change this figures
	        int reqHeight=AxTools.getScreenHeight();
	        return getBitmapUponDevicePixels(inputStream, reqWidth, reqHeight, false);	
		}
		/*
		  * For Bitmap Resource: Use .... getResources().openRawResource(imageId)
		  */
		static private class CopyInputStream{
			private InputStream inputStream;
			private ByteArrayOutputStream byteOutPutStream=new ByteArrayOutputStream();
			public CopyInputStream(InputStream is){
			    this.inputStream = is;
			    try{
			        int chunk = 0;
			        byte[] data = new byte[256];
			        while(-1 != (chunk = inputStream.read(data))){
			            byteOutPutStream.write(data, 0, chunk);
			        }
			    } catch (Exception e) {
			        e.printStackTrace();
			    }
			}
			public InputStream getIS(){return (InputStream)new ByteArrayInputStream(byteOutPutStream.toByteArray());}
		}
		static private Bitmap getBitmapUponDevicePixels(InputStream inputStream, int reqWidth, int reqHeight, boolean isHalfSize){
			return getBitmapUponDevicePixels(inputStream, reqWidth, reqHeight, isHalfSize, false);
		}
		static private Bitmap getBitmapUponDevicePixels(InputStream inputStream, int reqWidth, int reqHeight, boolean isHalfSize, boolean isChangeSize){	
				Bitmap bmp=null;
				try {
					if (!isChangeSize && !isHalfSize){
						try { 
							bmp = BitmapFactory.decodeStream(inputStream);
							if (bmp!=null){
								return bmp;
							}
							return bmp;
						} catch (Exception e){ // Use another method to get bitmap
							// Continue to decode by other method
						}
					}
					// DCv78 Read inputStream may cause read pointer to move, must reset.
					// However, since inputStream may not support mark/reset; Use BufferedInputStream		
					CopyInputStream copyStream = new CopyInputStream(inputStream);//After read, cannot use inputStream anymore.
					inputStream.close();
					
					//1st pass to get image size
			        BitmapFactory.Options options = new BitmapFactory.Options();
			        options.inJustDecodeBounds = true;
			        options.inDither = true;// optional
			        options.inPreferredConfig = Bitmap.Config.ARGB_8888;// MUST; Otherwise corrupt
			        InputStream testStream = copyStream.getIS();
			        BitmapFactory.decodeStream(testStream,null,options);// Can get options.outHeight & options.outWidth
			        if ((options.outWidth == -1) || (options.outHeight == -1)) return null;// Fatal Error on get options
			        
			        //2nd pass to get real image
			        options.inJustDecodeBounds = false;
			        // Calculate inSampleSize/scale (power of 2)
			        if (isHalfSize){
			        	options.inSampleSize = calculateInSampleHalfSize(options, reqWidth, reqHeight);
			        	//Log.e(TAG, "Size:"+options.inSampleSize);
			        } else {
			        	options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);	        	
			        }
			        //options.inPurgeable = true; // Tell to gc that whether it needs free memory, the Bitmap can be cleared
			        InputStream readStream = copyStream.getIS();
		        	bmp = BitmapFactory.decodeStream(readStream,null,options);			        
				} catch (Exception e) {
					e.printStackTrace();
					Log.e(TAG,"Decode error");
				}	
				return bmp;
			}
		// Copy from Android http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
		static private  int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		    // Raw height and width of image
		    final int height = options.outHeight;
		    final int width = options.outWidth;
		    int inSampleSize = 1;
		
		    if (height > reqHeight || width > reqWidth) {
		
		        final int halfHeight = height / 2;
		        final int halfWidth = width / 2;
		
		        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
		        // height and width larger than the requested height and width.
		        while ((halfHeight / inSampleSize) > reqHeight
		                && (halfWidth / inSampleSize) > reqWidth) {
		            inSampleSize *= 2;
		        }
		    }
		    return inSampleSize;
		}
		static private  int calculateInSampleHalfSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
			int fitDeviceSize = Math.max(reqWidth, reqHeight) / 2;
			int originalSize = (options.outHeight > options.outWidth) ? options.outHeight : options.outWidth;
			double ratio = (originalSize > fitDeviceSize) ? (originalSize / fitDeviceSize) : 1.0;
			int k = Integer.highestOneBit((int) Math.floor(ratio));
			if (k == 0)
				return 1;
			else
				return k;
		}
}
