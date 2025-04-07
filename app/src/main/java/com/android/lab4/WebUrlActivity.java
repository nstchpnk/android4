package com.android.lab4;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;
import android.widget.Button;


import androidx.appcompat.app.AppCompatActivity;

public class WebUrlActivity extends AppCompatActivity {
    private EditText editTextUrl;
    private RadioButton radioAudio;
    private RadioButton radioVideo;
    private Button btnLoad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_url);

        editTextUrl = findViewById(R.id.edit_text_url);
        radioAudio = findViewById(R.id.radio_audio);
        radioVideo = findViewById(R.id.radio_video);
        btnLoad = findViewById(R.id.btn_load);

        Button btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        btnLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = editTextUrl.getText().toString().trim();
                if (url.isEmpty()) {
                    Toast.makeText(WebUrlActivity.this, "Введіть URL", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent playerIntent;
                if (radioAudio.isChecked()) {
                    playerIntent = new Intent(WebUrlActivity.this, AudioPlayerActivity.class);
                } else {
                    playerIntent = new Intent(WebUrlActivity.this, VideoPlayerActivity.class);
                }

                playerIntent.putExtra("fileUri", url);
                playerIntent.putExtra("isWebUrl", true);
                startActivity(playerIntent);
            }
        });
    }

}