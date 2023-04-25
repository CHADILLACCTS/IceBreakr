package com.example.message;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class ChatSearching extends AppCompatActivity {

    ImageButton cancelBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_popup_search);

        TextView timer = findViewById(R.id.chat_popup_search_progress_timer);
        new CountDownTimer(60000,1000){
            public void onTick(long millisUntilFinished) {
                timer.setText(""+ millisUntilFinished/1000);
            }
            public void onFinish() {
                timer.setText("done!");
            }
        }.start();

        cancelBtn = findViewById(R.id.chat_popup_search_cancel_btn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }
}
