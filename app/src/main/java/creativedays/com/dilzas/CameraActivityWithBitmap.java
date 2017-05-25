package creativedays.com.dilzas;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.jameskelso.android.widget.PinchToZoomImageView;

import org.apache.commons.lang3.builder.CompareToBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import custom_views.CameraPreview;
import objects.CameraResolution;

public class CameraActivityWithBitmap extends AppCompatActivity  {


    Bitmap picture;
    private Camera mCamera;
    private CameraPreview mPreview;
    PinchToZoomImageView curtain;
    FrameLayout preview;
    ImageView capture;
    ImageView capturedImage;

    LinearLayout savePanel;
    ImageView screenshot;
    ImageView cancel;
    ImageView back;

    FrameLayout root;

    Bitmap mBitmap;

    byte[] byteArray;

    private Camera.PictureCallback mPicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera);
        curtain=(PinchToZoomImageView)findViewById(R.id.curtain);
        capture=(ImageView)findViewById(R.id.capture);
        cancel=(ImageView)findViewById(R.id.cancel);
        savePanel=(LinearLayout)findViewById(R.id.save_panel);
        screenshot=(ImageView)findViewById(R.id.screenshot);
        capturedImage=(ImageView)findViewById(R.id.captured_image);
        back=(ImageView)findViewById(R.id.back);

        root=(FrameLayout)findViewById(R.id.root);


        picture = getImageBitmap(this,"image","png");

        curtain.setImageBitmap(picture);

        /*Glide.with(this)
                .load(picture)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .fitCenter()
                .dontAnimate()
                .into(curtain);*/

        preview = (FrameLayout) findViewById(R.id.camera_view);

        //mCamera.setDisplayOrientation(90);

        initPictureCallback();
        startCamera();

        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.takePicture(null,null,mPicture);
            }
        });

        screenshot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeScreenshot();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.startPreview();
                capture.setVisibility(View.VISIBLE);
                savePanel.setVisibility(View.GONE);
                capturedImage.setImageBitmap(null);
                capturedImage.setVisibility(View.GONE);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        checkStorage();
    }

    public Bitmap getImageBitmap(Context context, String name, String extension){
        name=name+"."+extension;
        try{
            FileInputStream fis = context.openFileInput(name);
            Bitmap b = BitmapFactory.decodeStream(fis);
            fis.close();
            return b;
        }
        catch(Exception e){
        }
        return null;
    }

    public void checkStorage () {
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            if (ContextCompat.checkSelfPermission(CameraActivityWithBitmap.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(CameraActivityWithBitmap.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(CameraActivityWithBitmap.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            2);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            }
        }
    }

    public void initPictureCallback () {
        mPicture = new Camera.PictureCallback() {

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                capturedImage.setImageBitmap(bmp);
                capturedImage.setVisibility(View.VISIBLE);
                capture.setVisibility(View.GONE);
                savePanel.setVisibility(View.VISIBLE);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getScreenOrientation()==Configuration.ORIENTATION_LANDSCAPE) {
            mCamera.setDisplayOrientation(0);
        }
        else {
            mCamera.setDisplayOrientation(90);
        }
    }


    public void startCamera () {
        mCamera = getCameraInstance();

        Camera.Parameters params = mCamera.getParameters();
        List<String> focusModes = params.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }

        List<Camera.Size> resolutions = params.getSupportedPictureSizes();
        Camera.Size optimal= getOptimalResolution(resolutions);
        params.setPictureSize(optimal.width,optimal.height);

        mCamera.setParameters(params);



        mPreview = new CameraPreview(this, mCamera);
        root.addView(mPreview,0);

    }

    public void takeScreenshot(){    //THIS METHOD TAKES A SCREENSHOT AND SAVES IT AS .jpg
        Random num = new Random();
        long nu= Calendar.getInstance().getTimeInMillis(); //PRODUCING A RANDOM NUMBER FOR FILE NAME
        Bitmap bmp = Bitmap.createBitmap(preview.getWidth() , preview.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        preview.draw(c);

        //preview.layout(0, 0, preview.getLayoutParams().width, preview.getLayoutParams().height);
        preview.draw(c);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        byte[] bitmapdata = bos.toByteArray();
        ByteArrayInputStream fis = new ByteArrayInputStream(bitmapdata);

        String picId="Thema_"+String.valueOf(nu);
        String myfile=picId+".jpeg";

        File dir_image = new  File(Environment.getExternalStorageDirectory()+//<---
                File.separator+"Thema Pictures");          //<---
        dir_image.mkdirs();                                                  //<---
        //^IN THESE 3 LINES YOU SET THE FOLDER PATH/NAME . HERE I CHOOSE TO SAVE
        //THE FILE IN THE SD CARD IN THE FOLDER "Ultimate Entity Detector"

        try {
            File tmpFile = new File(dir_image,myfile);
            FileOutputStream fos = new FileOutputStream(tmpFile);

            byte[] buf = new byte[1024];
            int len;
            while ((len = fis.read(buf)) > 0) {
                fos.write(buf, 0, len);
            }
            fis.close();
            fos.close();
            Toast.makeText(getApplicationContext(),
                    "The file is saved at :SD/Thema Pictures",Toast.LENGTH_LONG).show();
            capture.setVisibility(View.VISIBLE);
            savePanel.setVisibility(View.GONE);
            capturedImage.setImageBitmap(null);
            capturedImage.setVisibility(View.GONE);
            mCamera.startPreview();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static Bitmap rotate(Bitmap bitmap, int degree) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix mtx = new Matrix();
        //       mtx.postRotate(degree);
        mtx.setRotate(degree);

        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }

    public Camera.Size getOptimalResolution (List<Camera.Size> resolutions) {

        //if there is only one resolution
        if (resolutions.size()==1) {
            return resolutions.get(0);
        }

        ArrayList<CameraResolution> megapixels=new ArrayList<>();
        for (int i=0; i<resolutions.size(); i++) {
            CameraResolution tmp=new CameraResolution(resolutions.get(i),(int)((resolutions.get(i).width * resolutions.get(i).height) / 1024000));
            megapixels.add(tmp);
        }

        Collections.sort(megapixels, new Comparator<CameraResolution>() {
            @Override
            public int compare(CameraResolution l1, CameraResolution l2) {
                return new CompareToBuilder().append(l2.getMegapixels(), l1.getMegapixels()).toComparison();
            }
        });

        for (int i=0; i<megapixels.size(); i++) {
            System.out.println("megapixels are " +megapixels.get(i).getMegapixels());
        }

        if (megapixels.get(0).getMegapixels()<6) {
            return megapixels.get(0).getSize();
        }
        else {
            int mid=megapixels.size()/2;
            return megapixels.get(mid).getSize();
        }

    }



    public int getScreenOrientation()
    {
        Display getOrient = getWindowManager().getDefaultDisplay();
        int orientation = Configuration.ORIENTATION_UNDEFINED;
        if(getOrient.getWidth()==getOrient.getHeight()){
            orientation = Configuration.ORIENTATION_SQUARE;
        } else{
            if(getOrient.getWidth() < getOrient.getHeight()){
                orientation = Configuration.ORIENTATION_PORTRAIT;
            }else {
                orientation = Configuration.ORIENTATION_LANDSCAPE;
            }
        }
        return orientation;
    }

    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }




}
