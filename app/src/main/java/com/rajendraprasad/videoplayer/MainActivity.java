package com.rajendraprasad.videoplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import com.rajendraprasad.videoplayer.databinding.ActivityMainBinding;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private AllVideosAdapter mAdapter;
    private ActivityMainBinding mBinding;
    private LinearLayoutManager mLayoutManager;
    private MediaController mediaController;
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 100;
    private boolean isVideoOpened = false;
    private ArrayList<String> allVideosList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        //Set MediaController  to enable play, pause, forward, etc options.
        mediaController = new MediaController(this);
        mediaController.setAnchorView(mBinding.videoview);

        checkStoragePermission();

        /* selection of video from list of videos*/
        mBinding.recyclerviewVideolist.addOnItemTouchListener(new RecyclerItemClickListener(MainActivity.this,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        isVideoOpened=true;
                        mBinding.tvVideos.setText("VIDEOS : " + (position + 1) + " / " + allVideosList.size());

                        int orientation = getResources().getConfiguration().orientation;
                        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                            // code for portrait mode
                            screenCalculationfor35Percentage();
                        } else {
                            // code for landscape mode
                            screenCalculationfor100Percentage();
                        }

                        mBinding.videoview.setVisibility(View.VISIBLE);

                        Uri uri = Uri.parse(allVideosList.get(position));
                        playVideo(uri);
                    }
                }));
    }

    private void setListsAdapter() {
        allVideosList.clear();
        allVideosList.addAll(getAllVideoPath(MainActivity.this));
        // setting adapter
        mLayoutManager = new LinearLayoutManager(MainActivity.this);
        mBinding.recyclerviewVideolist.setLayoutManager(mLayoutManager);
        mAdapter = new AllVideosAdapter(MainActivity.this, allVideosList);
        mBinding.recyclerviewVideolist.setAdapter(mAdapter);

        mBinding.tvVideos.setText("VIDEOS : " + allVideosList.size());


        /*Sample video if videos are not there in media storage*/
        if (allVideosList.size()== 0) {

            screenCalculationfor35Percentage();
            mBinding.videoview.setVisibility(View.VISIBLE);

            //Location of Media File
            Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.video);
            //Starting VideView By Setting MediaController and URI
            playVideo(uri);
        }
    }


    private ArrayList<String> getAllVideoPath(Context context) {
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Video.VideoColumns.DATA};

        /* this code is for to sort newest videos first*/
        String orderBy = android.provider.MediaStore.Video.Media.DATE_TAKEN;

        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, orderBy + " DESC");
        ArrayList<String> pathArrList = new ArrayList<String>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                pathArrList.add(cursor.getString(0));
            }
            cursor.close();
        }
        return pathArrList;
    }

    private void checkStoragePermission() {
        int MyVersion = Build.VERSION.SDK_INT;
        if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (!checkIfAlreadyHaveStoragePermission()) {
                requestForCameraPermission();
            } else {
                setListsAdapter();
            }
        } else {
            setListsAdapter();
        }
    }

    private boolean checkIfAlreadyHaveStoragePermission() {
        int result = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestForCameraPermission() {
        ActivityCompat.requestPermissions((MainActivity.this), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST_CODE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case STORAGE_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setListsAdapter();
                    mBinding.tvPermissionDenied.setVisibility(View.GONE);
                    mBinding.totalLayout.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(MainActivity.this, R.string.permission_denied_string, Toast.LENGTH_SHORT).show();
                    mBinding.totalLayout.setVisibility(View.GONE);
                    mBinding.tvPermissionDenied.setVisibility(View.VISIBLE);
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void playVideo(Uri uri) {
        mBinding.videoview.setMediaController(mediaController);
        mBinding.videoview.setVideoURI(uri);
        mBinding.videoview.requestFocus();
        mBinding.videoview.start();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            screenCalculationfor100Percentage();
            if(isVideoOpened)
                mBinding.recyclerviewVideolist.setVisibility(View.GONE);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            screenCalculationfor35Percentage();
            mBinding.recyclerviewVideolist.setVisibility(View.VISIBLE);
        }
    }

    private void screenCalculationfor35Percentage(){
        /* calculated screen 35 percent of height*/
        android.view.Display display = ((android.view.WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, (int)(display.getHeight()*0.35));
        mBinding.videoview.setLayoutParams(layoutParams);
    }
     private void screenCalculationfor100Percentage(){
        /* calculated screen 100 percent of height*/
         android.view.Display display = ((android.view.WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
         mBinding.videoview.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, (int)(display.getHeight()*1)));
    }

    @Override
    protected void onResume() {
        mBinding.videoview.resume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mBinding.videoview.suspend();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mBinding.videoview.stopPlayback();
        super.onDestroy();
    }
}
