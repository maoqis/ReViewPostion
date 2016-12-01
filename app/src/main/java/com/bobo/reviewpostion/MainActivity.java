package com.bobo.reviewpostion;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fastaccess.permission.base.PermissionHelper;
import com.fastaccess.permission.base.callback.OnPermissionCallback;
import com.squareup.picasso.Picasso;


public class MainActivity extends AppCompatActivity implements OnPermissionCallback, View.OnClickListener {
    private static final String TAG = "MainActivity";
    Matrix matrix = new Matrix();
    private static final int PHOTO_REQUEST_GALLERY = 2;// 从相册中选择
    private static final int PHOTO_REQUEST_CUT = 3;// 结果
    private WindowManager wm;
    private WindowManager.LayoutParams params;
    private boolean isAdded;
    private ImageView btn_floatView;
    private int widthS;
    private PermissionHelper permissionHelper;
    private String SINGLE_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE;
    private Button button;
    private TextView textView;
    private RelativeLayout activity_main;

    boolean isShow = false;
    private TextView tv_head;
    private SeekBar seekBar3;
    private Button button2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();


        permissionHelper = PermissionHelper.getInstance(this);
        permissionHelper.request(SINGLE_PERMISSION);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PHOTO_REQUEST_GALLERY) {
            // 从相册返回的数据
            if (data != null) {
                // 得到图片的全路径
                Uri uri = data.getData();
                Picasso.with(this).load(uri).into(btn_floatView);
                matrix.reset();
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                Bitmap bitmap = BitmapFactory.decodeFile(Utils.getRealPathFromUri(this, uri), options);
                int width = options.outWidth;
                if (widthS == 0) {
                    widthS = 1080;
                }

                float v = widthS * 1.0f / width;

                matrix.postScale(v, v);
                btn_floatView.setImageMatrix(matrix);
                Toast.makeText(this, uri.toString(), Toast.LENGTH_LONG).show();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initView() {
        button = (Button) findViewById(R.id.button);
        textView = (TextView) findViewById(R.id.textView);
        activity_main = (RelativeLayout) findViewById(R.id.activity_main);

        button.setOnClickListener(this);

        createFloatView();
        setAlpha(0.5f);

        tv_head = (TextView) findViewById(R.id.tv_head);
        tv_head.setOnClickListener(this);
        seekBar3 = (SeekBar) findViewById(R.id.seekBar3);
        seekBar3.setOnClickListener(this);
        seekBar3.setProgress(50);
        button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(this);
    }

    private void createFloatView() {
        btn_floatView = new ImageView(getApplicationContext());
        btn_floatView.setScaleType(ImageView.ScaleType.MATRIX);


        wm = (WindowManager) getApplicationContext().getSystemService(
                Context.WINDOW_SERVICE);
        params = new WindowManager.LayoutParams();
        widthS = wm.getDefaultDisplay().getWidth();
        int height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = height;
        params.width = widthS;


        // 设置window type
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        /*
         * 如果设置为params.type = WindowManager.LayoutParams.TYPE_PHONE; 那么优先级会降低一些,
         * 即拉下通知栏不可见
         */

        params.format = PixelFormat.RGBA_8888; // 设置图片格式，效果为背景透明

        // 设置Window flag
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        /*
         * 下面的flags属性的效果形同“锁定”。 悬浮窗不可触摸，不接受任何事件,同时不影响后面的事件响应。
         * wmParams.flags=LayoutParams.FLAG_NOT_TOUCH_MODAL |
         * LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_NOT_TOUCHABLE;
         */


        // 设置悬浮窗的Touch监听


        setListener();
        wm.addView(btn_floatView, params);
        isAdded = true;


    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                gallery();
                break;
            case R.id.button2:
                setAlpha(seekBar3.getProgress()/100.f);
                break;
        }
    }

    private void showButton(boolean isShow) {
        if (isShow) {
            params.height = 100;
            params.width = 100;
            btn_floatView.scrollTo(0, 0);
        } else {
            btn_floatView.getLayoutParams().width = WindowManager.LayoutParams.MATCH_PARENT;
            btn_floatView.getLayoutParams().height = WindowManager.LayoutParams.WRAP_CONTENT;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                wm.updateViewLayout(btn_floatView, params);
            }
        });

    }

    private void gallery() {
        // 激活系统图库，选择一张图片
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
// 开启一个带有返回值的Activity，请求码为PHOTO_REQUEST_GALLERY
        startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
    }

    private void setAlpha(float alpha) {
        btn_floatView.setAlpha(alpha);
    }


    private void setListener() {
        btn_floatView.setOnTouchListener(new View.OnTouchListener() {
            float lastY;
            long lastUpTime = 0;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Log.d(TAG, "onTouch() called with: view = [" + view + "], motionEvent = [" + motionEvent + "]");
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastY = motionEvent.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float y = motionEvent.getY();
                        btn_floatView.scrollBy(0, (int) (lastY - y));
                        lastY = y;
                        break;
                    case MotionEvent.ACTION_UP:
                        this.lastY = 0;
                        if (lastUpTime == 0) {
                            lastUpTime = System.currentTimeMillis();
                        } else {
                            long timeMillis = System.currentTimeMillis();
                            if (timeMillis - lastUpTime < 300) {
                                //TODO:
                                isShow = !isShow;
                                showButton(isShow);
                                lastUpTime = 0;
                            } else {
                                lastUpTime = timeMillis;
                            }
                        }
                        break;
                }
                return false;
            }
        });
    }

    @Override
    protected void onDestroy() {

        removeView();
        super.onDestroy();
    }

    private void removeView() {
        if (btn_floatView != null) {
            wm.removeView(btn_floatView);
        }
    }

    @Override
    public void onPermissionGranted(@NonNull String[] permissionName) {

    }

    @Override
    public void onPermissionDeclined(@NonNull String[] permissionName) {

    }

    @Override
    public void onPermissionPreGranted(@NonNull String permissionsName) {

    }

    @Override
    public void onPermissionNeedExplanation(@NonNull String permissionName) {

    }

    @Override
    public void onPermissionReallyDeclined(@NonNull String permissionName) {

    }

    @Override
    public void onNoPermissionNeeded() {

    }
}
