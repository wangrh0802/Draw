package com.example.blair.draw;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

/**
 * Created by wangrh on 2016/11/19.
 */
public class DrawPage extends Activity implements View.OnClickListener{

    private String photoPath;
    private DrawView drawView;
    private Button undoButton;
    private Button saveButton;
    private Button clearButton;
    private Button whiteButton;
    private Button blackButton;
    private Button blueButton;
    private Button redButton;
    private Button greenButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.draw_page);
        photoPath = getIntent().getStringExtra("photoPath");
        Bitmap bitmap = rotateBitmapByDegree(BitmapFactory.decodeFile(photoPath), getBitmapDegree(photoPath));
        drawView = (DrawView)findViewById(R.id.draw_view);
        undoButton = (Button)findViewById(R.id.undo);
        undoButton.setOnClickListener(this);
        saveButton = (Button)findViewById(R.id.save);
        saveButton.setOnClickListener(this);
        clearButton = (Button)findViewById(R.id.clear);
        clearButton.setOnClickListener(this);

        whiteButton = (Button)findViewById(R.id.white);
        whiteButton.setOnClickListener(this);
        blackButton = (Button)findViewById(R.id.black);
        blackButton.setOnClickListener(this);
        blueButton = (Button)findViewById(R.id.blue);
        blueButton.setOnClickListener(this);
        redButton = (Button)findViewById(R.id.red);
        redButton.setOnClickListener(this);
        greenButton = (Button)findViewById(R.id.green);
        greenButton.setOnClickListener(this);

        drawView.init(bitmap);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.undo:
                drawView.undo();
                break;

            case R.id.clear:
                drawView.clear();
                break;

            case R.id.save:
                Toast.makeText(this,"saving...please wait a moment",Toast.LENGTH_SHORT).show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        drawView.save();
                        postOnMainThread();
                    }
                }).start();

                break;

            case R.id.white:
                drawView.setPaintColor(Color.WHITE);
                break;

            case R.id.red:
                drawView.setPaintColor(Color.RED);
                break;

            case R.id.black:
                drawView.setPaintColor(Color.BLACK);
                break;

            case R.id.blue:
                drawView.setPaintColor(Color.BLUE);
                break;

            case R.id.green:
                drawView.setPaintColor(Color.GREEN);
                break;
        }
    }

    private void postOnMainThread(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(DrawPage.this,"save successfully",Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private int getBitmapDegree(String path) {
        int degree = 0;
        try {
            // 从指定路径下读取图片，并获取其EXIF信息
            ExifInterface exifInterface = new ExifInterface(path);
            // 获取图片的旋转信息
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    public static Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
        Bitmap returnBm = null;
        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
        }
        if (returnBm == null) {
            returnBm = bm;
        }
        if (bm != returnBm) {
            bm.recycle();
        }
        return returnBm;
    }
}
