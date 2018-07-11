package com.akado.flowableview.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.akado.flowableview.FlowableView;

public class MainActivity extends AppCompatActivity {

    FlowableView flowableView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        flowableView = findViewById(R.id.flowableView);
        flowableView
                .scale(1.1f)
                .translateXs(50, -50)
                .frames(R.raw.img1, R.raw.img2, R.raw.img3, R.raw.img4)
                .flow();
    }

    @Override
    protected void onResume() {
        super.onResume();
        flowableView.flow();
    }

    @Override
    protected void onPause() {
        flowableView.pause();
        super.onPause();
    }
}
