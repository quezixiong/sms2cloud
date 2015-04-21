package com.cumulitech.sms2cloud;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.Hashtable;

/**
 * Created by harley on 15/4/12.
 */
public class Utility {

    private Context context;

    private final static int QR_WIDTH = 512;
    private final static int QR_HEIGHT = 512;

    public Utility(Context context){
        this.context = context;
    }

    public Boolean hasInternet(){
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    public static Bitmap createQRImage(String url)
    {
        if (url == null || "".equals(url) || url.length() < 1)
        {
            return null;
        }

//        Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
//        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");

        try {
            BitMatrix bitMatrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT);
            Bitmap bmp = Bitmap.createBitmap(QR_WIDTH, QR_HEIGHT, Bitmap.Config.RGB_565);
            for (int x = 0; x < QR_WIDTH; x++) {
                for (int y = 0; y < QR_HEIGHT; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bmp;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    public class ErrorCode{
        public static final int OK = 0;
        public static final int ICCID_NOT_VALID = 1;
        public static final int ICCID_ALREADY_BIND = 2;
        public static final int IDENTIFIER_NOT_VALID = 3;
        public static final int NOT_AUTHORIZED = 4;
    }
}
