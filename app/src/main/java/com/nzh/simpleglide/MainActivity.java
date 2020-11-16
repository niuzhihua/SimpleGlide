package com.nzh.simpleglide;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.nzh.simpleglide.glide.GlideTestActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void testGlide(View view) {
        Intent i = new Intent(this, GlideTestActivity.class);
        startActivity(i);
    }
}