package com.example.permission;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import com.example.permission.databinding.ActivityMainBinding;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PHOTO_TAGGING_PERMISSIONS = 1;
    ActivityMainBinding binding;
    String[] permissions;
    String[] deniedPermissionsAmongPhotoTagging;
    private static final int CAMERA_REQUEST = 1888;
    Bitmap photo;
    Uri tempUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};

        WebSettings webSettings = binding.webView.getSettings();

        webSettings.setJavaScriptEnabled(true);
        binding.btClickHere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               //  openCamera();
               binding.webView.loadUrl("file:///android_asset/hello.html");

            }
        });
        binding.webView.addJavascriptInterface(new WebViewJavaScriptInterface(this), "app");





/*
        binding.btClickHere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        Snackbar.make(findViewById(android.R.id.content), "need a permisssion", Snackbar.LENGTH_INDEFINITE).setAction("Enable", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 101);
                            }
                        }).show();
                    } else {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 101);
                    }
                }
            }
        });*/
    }

    void openCamera() {
        if (checkWhetherAllPermissionsPresentForPhotoTagging()) {
            Toast.makeText(MainActivity.this, "camera permission granted", Toast.LENGTH_LONG).show();
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, CAMERA_REQUEST);
        } else {
            requestRunTimePermissions(MainActivity.this, permissions, MY_PHOTO_TAGGING_PERMISSIONS);
        }

    }


    protected void requestRunTimePermissions(final Activity activity, final String[] permissions, final int customPermissionConstant) {
        if (permissions.length > 1 && customPermissionConstant == MY_PHOTO_TAGGING_PERMISSIONS) {

            if (getDeniedPermissionsAmongPhototaggingPermissions().length == 1) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, deniedPermissionsAmongPhotoTagging[0])) {
                    Snackbar.make(findViewById(android.R.id.content), "App needs permission to work", Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ActivityCompat.requestPermissions(activity, deniedPermissionsAmongPhotoTagging, customPermissionConstant);
                                }
                            }).show();
                } else {
                    ActivityCompat.requestPermissions(activity, deniedPermissionsAmongPhotoTagging, customPermissionConstant);
                }
            } else if (getDeniedPermissionsAmongPhototaggingPermissions().length > 1) {
                if (isFirstTimeAskForPhotoTaggingPermission()) {
                    ActivityCompat.requestPermissions(activity, deniedPermissionsAmongPhotoTagging, customPermissionConstant);
                }
            } else {
                Snackbar.make(findViewById(android.R.id.content), "This functionality needs multiple app permissions", Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ActivityCompat.requestPermissions(activity, deniedPermissionsAmongPhotoTagging, customPermissionConstant);
                            }
                        }).show();
            }
        }
    }

    protected boolean isFirstTimeAskForPhotoTaggingPermission() {
        SharedPreferences sharedPreferences = getSharedPreferences("permissionasks", MODE_PRIVATE);
        boolean isFirstTime = sharedPreferences.getBoolean("PHOTO_FIRST_PERMISSION", true);
        if (isFirstTime) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("PHOTO_FIRST_PERMISSION", false);
            editor.commit();
        }
        return isFirstTime;
    }

    protected String[] getDeniedPermissionsAmongPhototaggingPermissions() {

        final List<String> deniedPermissions = new ArrayList<String>();
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
                deniedPermissions.add(permission);
            }
        }

        this.deniedPermissionsAmongPhotoTagging = deniedPermissions.toArray(new String[deniedPermissions.size()]);
        return deniedPermissionsAmongPhotoTagging;
    }

    protected boolean checkWhetherAllPermissionsPresentForPhotoTagging() {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PHOTO_TAGGING_PERMISSIONS) {
            if (checkWhetherAllPermissionsPresentForPhotoTagging()) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            } else {
                String permissionString = getDeniedPermissionsAmongPhototaggingPermissions().length == 1 ? "Permission" : "Permissions";
                Snackbar.make(findViewById(android.R.id.content), permissionString + " denied, photo tagging will not work. To enable now click here",
                        Snackbar.LENGTH_INDEFINITE).setAction("ENABLE", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ActivityCompat.requestPermissions(MainActivity.this, getDeniedPermissionsAmongPhototaggingPermissions(), MY_PHOTO_TAGGING_PERMISSIONS);
                    }
                }).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            photo = (Bitmap) data.getExtras().get("data");
            tempUri = getImageUri(getApplicationContext(), photo);
            binding.tvGetPath.setText(tempUri.getPath());
        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    Uri file() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            if (Build.VERSION.SDK_INT >= 23) {

                File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/images.jpg");
                if (dir.exists()) {
                    Log.d("path", dir.toString());
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    return Uri.fromFile(dir);
                }

            } else {
                File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/images.jpeg");
                if (dir.exists()) {
                    Log.d("path", dir.toString());
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;


                }
            }
        }
        return null;
    }

    public class WebViewJavaScriptInterface {

        private Context context;

        /*
         * Need a reference to the context in order to sent a post message
         */
        public WebViewJavaScriptInterface(Context context) {
            this.context = context;
        }

        /*
         * This method can be called from Android. @JavascriptInterface
         * required after SDK version 17.
         */
        @JavascriptInterface
        public String uri(String message) {
            openCamera();
            return tempUri.getPath();
        }
    }
}


