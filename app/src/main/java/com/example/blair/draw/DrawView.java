package com.example.blair.draw;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangrh on 2016/11/23.
 */
public class DrawView extends View {

    private Bitmap picBitmap;
    private Bitmap bitmap;
    private Bitmap longPressBitmap;
    private Bitmap doubleClickBitmap;
    private Canvas mCanvas;
    private List<Object> pathList = new ArrayList<>();
    private List<Integer> pathColorList = new ArrayList<>();
    private List<Integer> drawTypeList = new ArrayList<>();
    private Paint mBitmapPaint;
    private Paint mPaint;
    private Matrix matrix;
    private boolean isInit = false;
    private long clickStartTime;

    class DrawType{
        public static final int MOVE = 0;
        public static final int LONG_PRESS = 1;
        public static final int DOUBLE_CLICK = 2;
    }

    class Location{
        float x;
        float y;

        public float getX() {
            return x;
        }

        public void setX(float x) {
            this.x = x;
        }

        public float getY() {
            return y;
        }

        public void setY(float y) {
            this.y = y;
        }
    }



    public DrawView(Context context) {
        this(context, null);
    }

    public DrawView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DrawView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        longPressBitmap = BitmapFactory.decodeResource(context.getResources(),R.mipmap.longpress);
        doubleClickBitmap = BitmapFactory.decodeResource(context.getResources(),R.mipmap.doubleclick);
    }

    public void init(Bitmap bitmap){
        this.bitmap = bitmap;
        WindowManager wm = (WindowManager) getContext()
                .getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();
        float scaleX = (float)width/bitmap.getWidth();
        float scaleY = (float)height/bitmap.getHeight();
        matrix = new Matrix();
        matrix.setScale(scaleX, scaleY);

        picBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true);
        mCanvas = new Canvas(picBitmap);
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(5);

        isInit = true;
    }


    public void undo(){
        if(pathList.size()>0){
            pathList.remove(pathList.size()-1);
            pathColorList.remove(pathColorList.size() - 1);
            drawTypeList.remove(drawTypeList.size() - 1);
            picBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                    bitmap.getHeight(), matrix, true);
            mCanvas = new Canvas(picBitmap);
            for(int i=0;i<drawTypeList.size();i++){

                if(drawTypeList.get(i) == DrawType.DOUBLE_CLICK){
                    Location location = (Location)pathList.get(i);
                    mCanvas.drawBitmap(doubleClickBitmap, location.getX(),
                            location.getY(),mBitmapPaint);
                }else if(drawTypeList.get(i) == DrawType.LONG_PRESS){
                    Location location = (Location)pathList.get(i);
                    mCanvas.drawBitmap(longPressBitmap, location.getX(),
                            location.getY(),mBitmapPaint);
                }else {
                    List<Path> paths = (List<Path>)pathList.get(i);
                    mPaint.setColor(pathColorList.get(i));
                    for(int j=0;j<paths.size();j++){
                        mCanvas.drawPath(paths.get(j),mPaint);
                    }
                }
            }
            invalidate();
        }
    }

    public void clear(){
        pathList.clear();
        pathColorList.clear();
        drawTypeList.clear();
        picBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true);
        mCanvas = new Canvas(picBitmap);
        invalidate();
    }

    public void setPaintColor(int color){
        mPaint.setColor(color);
    }

    public void save(){
        File f = new File(Environment.getExternalStorageDirectory() + "/BlairDraw/"+System.currentTimeMillis()+".jpg");
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            picBitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(isInit){
            canvas.drawColor(0xFFAAAAAA);
            canvas.drawBitmap(picBitmap, 0, 0, mBitmapPaint);
        }
    }

    List<Path> paths = new ArrayList<>();
    List<Float> preXList = new ArrayList<>();
    List<Float> preYList = new ArrayList<>();
    private boolean isMove = false;

        @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (mCanvas != null) {
            // 获得屏幕触点数量
            int pointerCount = event.getPointerCount();

            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:// 手势按下
                    if(System.currentTimeMillis() - clickStartTime<500){
                        float x = event.getX(pointerCount - 1)-doubleClickBitmap.getWidth()/2;
                        float y = event.getY(pointerCount - 1)-doubleClickBitmap.getHeight()/2;
                        mCanvas.drawBitmap(doubleClickBitmap, x, y, mBitmapPaint);
                        Location location = new Location();
                        location.setX(x);
                        location.setY(y);
                        pathList.add(location);
                        pathColorList.add(null);
                        drawTypeList.add(DrawType.DOUBLE_CLICK);
                        return true;
                    }
                    clickStartTime = System.currentTimeMillis();

                case MotionEvent.ACTION_POINTER_DOWN:
                    float x1 = event.getX(pointerCount-1);
                    float y1 = event.getY(pointerCount-1);
                    Path path = new Path();
                    path.moveTo(x1, y1);
                    preXList.add(x1);
                    preYList.add(y1);
                    paths.add(path);
                    break;
                case MotionEvent.ACTION_MOVE:
                    try {
                        for (int i = 0; i < paths.size(); i++) {
                            float x = event.getX(i);
                            float y = event.getY(i);
                            float dx = Math.abs(x - preXList.get(i));
                            float dy = Math.abs(y - preYList.get(i));
                            if (dx > 10 || dy > 10) {// 用户要移动超过5像素才算是画图，免得手滑、手抖现象
                                isMove = true;
                                paths.get(i).quadTo(preXList.get(i), preYList.get(i), (x + preXList.get(i)) / 2, (y + preYList.get(i)) / 2);
                                preXList.set(i, x);
                                preYList.set(i, y);
                                mCanvas.drawPath(paths.get(i), mPaint);// 绘制路径
                            }
                        }
                    }catch (Exception e){

                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if(System.currentTimeMillis()-clickStartTime>1000){
                        float x = event.getX(pointerCount-1)- longPressBitmap.getWidth()/2;
                        float y = event.getY(pointerCount - 1)-longPressBitmap.getHeight()/2;
                        mCanvas.drawBitmap(longPressBitmap, x, y,mBitmapPaint);
                        Location location = new Location();
                        location.setX(x);
                        location.setY(y);
                        pathList.add(location);
                        pathColorList.add(null);
                        drawTypeList.add(DrawType.LONG_PRESS);
                        preXList.clear();
                        preYList.clear();
                        paths.clear();
                        return true;
                    }
                    if(isMove) {
                        List<Path> pathTemp = new ArrayList<>();
                        pathTemp.addAll(paths);
                        pathList.add(pathTemp);
                        drawTypeList.add(DrawType.MOVE);
                        pathColorList.add(mPaint.getColor());
                    }
                    isMove = false;
                    preXList.clear();
                    preYList.clear();
                    paths.clear();
                    break;
            }
            invalidate();
        }
        return true;
    }
}
