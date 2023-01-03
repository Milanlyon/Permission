package com.example.permission;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.webkit.WebSettings;

import com.example.permission.databinding.ActivityLoadingUriPageBinding;

public class LoadingUrlPage extends AppCompatActivity {
    ActivityLoadingUriPageBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_loading_uri_page);
        WebSettings webSettings = binding.webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        Intent intent = getIntent();
        String uri = intent.getExtras().get("url").toString();
        binding.webView.loadUrl(uri);
    }
}