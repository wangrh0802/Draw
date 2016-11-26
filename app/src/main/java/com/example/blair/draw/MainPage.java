package com.example.blair.draw;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import java.io.File;

public class MainPage extends Activity implements View.OnClickListener{

    private static final int TAKE_PHOTO = 100;
    private static final int CHECK_PERMISSION = 101;

    protected String photoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page);
        checkUserPermission();
        findViewById(R.id.take_photo).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.take_photo){
            takePhoto();
        }
    }

    protected void takePhoto(){
        long timeStamp = System.currentTimeMillis();
        photoPath = Environment.getExternalStorageDirectory() + "/BlairDraw/"+timeStamp+".jpg";
        File photoFile = new File(photoPath);
        if (!photoFile.getParentFile().exists()) {
            photoFile.getParentFile().mkdirs();
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri uri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, TAKE_PHOTO);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == TAKE_PHOTO){
                Intent intent = new Intent(this, DrawPage.class);
                intent.putExtra("photoPath", photoPath);
                startActivity(intent);
            }
        }
    }

    private void checkUserPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS},
                    CHECK_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 99){
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED ||
                    grantResults[1] != PackageManager.PERMISSION_GRANTED ||
                    grantResults[2] != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"Cannot get permission, app will be close in 3s",Toast.LENGTH_SHORT).show();
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }finally {
                    finish();
                }
            }
        }
    }
}
