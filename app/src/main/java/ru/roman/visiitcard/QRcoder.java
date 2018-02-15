package ru.roman.visiitcard;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * Created by Roman on 24.07.2017. upd:16.10.2017
 */

public class QRcoder extends AsyncTask<String, Void, Bitmap> {

    private Activity activity;
    private ProgressDialog dialog;

    private static final int BLACK = 0xFF000000;
    private static final int WHITE = 0xFFFFFFFF;
    private ImageView imageView;
    private Bitmap bitmap;

    public QRcoder(Activity activity, ImageView imageView) {
        this.activity = activity;
        this.imageView = imageView;


    }


    @Override
    protected void onPreExecute() {
        dialog = new ProgressDialog(activity);
        dialog.setTitle("Генерация QR кода");
        dialog.setMessage("Пожалуйста подождите");
        dialog.setCancelable(false);
        dialog.show();
        super.onPreExecute();
    }


    @Override
    protected Bitmap doInBackground(String... params) {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width = displayMetrics.widthPixels;

        try {
            BitMatrix matrix = new QRCodeWriter().encode(params[0], BarcodeFormat.QR_CODE, width, width);
            bitmap = matrixToBitmap(matrix);
        } catch (WriterException e) {
            e.printStackTrace();
        }


        return bitmap;
    }

   @Override
    protected void onPostExecute(Bitmap bitmap) {
        try {
            dialog.dismiss();
            imageView.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }


        super.onPostExecute(bitmap);
    }

    private Bitmap matrixToBitmap(BitMatrix matrix) {

        int width = matrix.getWidth();
        int height = matrix.getHeight();
        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        for (int x = 0; x < width; x++){
            for (int y=0;y<height;y++){
                image.setPixel(x, y, matrix.get(x, y) ? BLACK : WHITE);
            }
        }
            return image;
    }


}
