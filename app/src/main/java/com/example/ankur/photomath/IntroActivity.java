package com.example.ankur.photomath;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import com.gospelware.liquidbutton.LiquidButton;
import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;

public class IntroActivity extends AppCompatActivity {

    LiquidButton liquidButton;
    ShimmerTextView tv;
    Shimmer shimmer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_intro);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(this.getResources().getColor(android.R.color.transparent));
        }
        tv = (ShimmerTextView) findViewById(R.id.shimmer_tv);
        shimmer = new Shimmer();
        tv.setTextColor(Color.RED);
        shimmer.setDirection(Shimmer.ANIMATION_DIRECTION_LTR);
        shimmer.start(tv);
        liquidButton = (LiquidButton) findViewById(R.id.liquidbtn);
        liquidButton.startPour();
        liquidButton.setEnabled(false);
        liquidButton.setFillAfter(true);
        liquidButton.setAutoPlay(true);
        liquidButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        liquidButton.setPourFinishListener(new LiquidButton.PourFinishListener() {
            @Override
            public void onPourFinish() {
                Intent main = new Intent(getApplicationContext(),MainActivity.class);
                shimmer.cancel();
                startActivity(main);
                finish();
            }

            @Override
            public void onProgressUpdate(float progress) {
            }
        });
    }

}
