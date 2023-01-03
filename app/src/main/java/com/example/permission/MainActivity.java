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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
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
    private static final int PERMISSIONS_CODE = 101;
    private ActivityMainBinding binding;
    private String[] permissions;
    private String[] getPermissionsAmongPermission;
    private static final int CAMERA_REQUEST = 1888;
    private Bitmap photo;
    private Uri tempUri;
    private int permissionsCount;
    private String permissionString;


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
                binding.webView.loadUrl("file:///android_asset/appzillon.html");

            }
        });
        binding.webView.addJavascriptInterface(new WebViewJavaScriptInterface(this), "Android");

    }

    void openCamera() {
        if (checkWhetherAllPermissionsPresentForPhotoTagging()) {
            Toast.makeText(MainActivity.this, "camera permission granted", Toast.LENGTH_SHORT).show();
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, CAMERA_REQUEST);

        } else {
            requestRunTimePermissions();
        }

    }



    private void requestRunTimePermissions() {

        if (getPermissionsAmongPermission().length > 0) {
            if (permissionsCount < 2) {
                ActivityCompat.requestPermissions(MainActivity.this, getPermissionsAmongPermission, 101);
            } else {
                showAppDialog();
                Log.d("pk", "call from requestRunTimePermissions");
            }
        }


    }


    private String[] getPermissionsAmongPermission() {

        final List<String> getPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
                getPermissions.add(permission);
            }
        }
        permissionsCount++;
        this.getPermissionsAmongPermission = getPermissions.toArray(new String[getPermissions.size()]);
        return getPermissionsAmongPermission;
    }

    private boolean checkWhetherAllPermissionsPresentForPhotoTagging() {
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

        if (requestCode == PERMISSIONS_CODE) {

            if (checkWhetherAllPermissionsPresentForPhotoTagging()) {

                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);

            } else if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, getPermissionsAmongPermission[0])) {
                showAppDialog();
                Log.d("pk", "from onRequestPermissionsResult 1 ");
            } else {
                showAppDialog();
                Log.d("pk", "from onRequestPermissionsResult  2");

            }
        }
    }

    private void showAppDialog() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, getPermissionsAmongPermission[0])) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("alert").setMessage("app needs permissions");
            builder.setCancelable(false);
            builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, getPermissionsAmongPermission[0])) {
                        ActivityCompat.requestPermissions(MainActivity.this, getPermissionsAmongPermission, 101);
                    }

                }
            });
            builder.setNegativeButton("No,Exit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
        } else {
            permissionString = getPermissionsAmongPermission().length == 1 ? "Permission" : "Permissions";
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("alert").setMessage("please provide " + permissionString + ",go to setting and give required permission");
            builder.setCancelable(false);
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                }
            });
            builder.setNegativeButton("No,Exit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            builder.create().show();
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            photo = (Bitmap) data.getExtras().get("data");
            tempUri = getImageUri(getApplicationContext(), photo);
            String uriString = tempUri.toString();
            binding.webView.evaluateJavascript("javascript:updateURI(\"" + uriString + "\")", null);
        } else if (resultCode == Activity.RESULT_CANCELED){
            binding.webView.evaluateJavascript("javascript:updateURI('camera didnt capture the img')", null);

        }

    }


    private Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, " " + System.currentTimeMillis(), null);
        return Uri.parse(path);
    }


    public class WebViewJavaScriptInterface {

        private Context context;

        public WebViewJavaScriptInterface(Context context) {
            this.context = context;
        }

        @JavascriptInterface
        public void uri() {
            openCamera();
        }
        @JavascriptInterface
        public void openURL(String url){
            openLoadingUrlPage(url);
        }
    }

    private void openLoadingUrlPage(String url) {
        Intent intent = new Intent(MainActivity.this,LoadingUrlPage.class);
        intent.putExtra("url",url);
        startActivity(intent);
    }


}


