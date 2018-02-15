package ru.roman.visiitcard;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * Created by Roman on 24.07.2017.
 */

public class QRcoderOld extends AsyncTask<String, BitMatrix, ImageView> {

    private Activity activity;
    private ProgressDialog dialog;

    private static final int BLACK = 0xFF000000;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int ID = 34646456;
    private ImageView imageView;
    private FrameLayout frameLayout;

    public QRcoderOld(Activity activity, FrameLayout frameLayout) {
        this.activity = activity;
        imageView = new ImageView(activity);

        this.frameLayout = frameLayout;
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
    protected ImageView doInBackground(String... params) {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width = displayMetrics.widthPixels;

        try {
            BitMatrix matrix = new QRCodeWriter().encode(params[0], BarcodeFormat.QR_CODE, width, width);

            publishProgress(matrix);
        } catch (WriterException e) {
            e.printStackTrace();
        }


        return imageView;
    }

    @Override
    protected void onProgressUpdate(BitMatrix... values) {
        super.onProgressUpdate(values);


        imageView.setImageBitmap(matrixToBitmap(values[0]));
        imageView.setTag(ID);
    }

    @Override
    protected void onPostExecute(ImageView imageView) {
        try {
            dialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }

        ImageView old = (ImageView) activity.findViewById(ID);
        if (old != null) {
            ((FrameLayout) old.getParent()).removeViewInLayout(old);
        }

        //LayoutInflater ltInflater = activity.getLayoutInflater();
        //не получилось
        //ltInflater.inflate((XmlPullParser) imageView,frameLayout,false);

        //activity.addContentView(imageView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        frameLayout.addView(imageView);
        frameLayout.setBackgroundColor(Color.BLUE);

        super.onPostExecute(imageView);
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
