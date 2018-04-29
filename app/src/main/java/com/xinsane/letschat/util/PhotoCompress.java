package com.xinsane.letschat.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.OutputStream;

/**
 * 用于发送图片前的压缩图片
 */
public class PhotoCompress {
    public static void compress(String srcPath, OutputStream outputStream) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(srcPath, opts);
        opts.inJustDecodeBounds = false;

        int w = opts.outWidth;
        int h = opts.outHeight;
        float standardW = 480f;
        float standardH = 800f;
        int zoomRatio = 1;
        if (w > h && w > standardW) {
            zoomRatio = (int) (w / standardW);
        } else if (w < h && h > standardH) {
            zoomRatio = (int) (h / standardH);
        }
        if (zoomRatio <= 0)
            zoomRatio = 1;
        opts.inSampleSize = zoomRatio;

        bmp = BitmapFactory.decodeFile(srcPath, opts);
        bmp.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
    }
}
