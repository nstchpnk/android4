package com.android.lab4;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.TimeUnit;

public class VideoPlayerActivity extends AppCompatActivity {
    private VideoView videoView;
    private ImageButton btnPlay;
    private ImageButton btnPause;
    private ImageButton btnStop;
    private SeekBar seekBar;
    private TextView tvCurrentTime;
    private TextView tvTotalTime;
    private Handler handler;
    private Runnable runnable;
    private Button btnBack, btnSelectFile;
    private boolean isWebUrl = false;

    private final ActivityResultLauncher<String> pickVideo = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    if (videoView.isPlaying()) {
                        videoView.stopPlayback();
                    }

                    try {
                        videoView.setVideoURI(uri);
                        videoView.requestFocus();
                        btnPlay.setVisibility(View.VISIBLE);
                        btnPause.setVisibility(View.GONE);
                        seekBar.setProgress(0);
                        updateCurrentTime();
                    } catch (Exception e) {
                        Toast.makeText(VideoPlayerActivity.this,
                                "Помилка завантаження відео: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        videoView = findViewById(R.id.video_view);
        btnPlay = findViewById(R.id.btn_play);
        btnPause = findViewById(R.id.btn_pause);
        btnStop = findViewById(R.id.btn_stop);
        seekBar = findViewById(R.id.seek_bar);
        tvCurrentTime = findViewById(R.id.tv_current_time);
        tvTotalTime = findViewById(R.id.tv_total_time);
        btnSelectFile = findViewById(R.id.btn_select_file);
        btnBack = findViewById(R.id.btn_back);

        btnSelectFile.setOnClickListener(v -> pickVideoFile());

        handler = new Handler();

        String fileUriString = getIntent().getStringExtra("fileUri");
        isWebUrl = getIntent().getBooleanExtra("isWebUrl", false);

        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        if (fileUriString != null && !fileUriString.isEmpty()) {
            try {
                Uri videoUri = Uri.parse(fileUriString);
                videoView.setVideoURI(videoUri);
            } catch (Exception e) {
                Toast.makeText(this, "Помилка завантаження відео: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        btnBack.setOnClickListener(v -> finish());

        videoView.setOnPreparedListener(mp -> {
            seekBar.setMax(videoView.getDuration());
            tvTotalTime.setText(formatTime(videoView.getDuration()));
            updateSeekBar();
            playVideo();
        });

        videoView.setOnCompletionListener(mp -> {
            btnPlay.setVisibility(View.VISIBLE);
            btnPause.setVisibility(View.GONE);
            handler.removeCallbacks(runnable);
        });

        btnPlay.setOnClickListener(v -> playVideo());

        btnPause.setOnClickListener(v -> pauseVideo());

        btnStop.setOnClickListener(v -> stopVideo());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    videoView.seekTo(progress);
                }
                updateCurrentTime();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void pickVideoFile() {
        pickVideo.launch("video/*");
    }

    private void playVideo() {
        if (!videoView.isPlaying()) {
            videoView.start();
            btnPlay.setVisibility(View.GONE);
            btnPause.setVisibility(View.VISIBLE);
            updateSeekBar();
        }
    }

    private void pauseVideo() {
        if (videoView.isPlaying()) {
            videoView.pause();
            btnPlay.setVisibility(View.VISIBLE);
            btnPause.setVisibility(View.GONE);
        }
    }

    private void stopVideo() {
        videoView.stopPlayback();
        btnPlay.setVisibility(View.VISIBLE);
        btnPause.setVisibility(View.GONE);
        handler.removeCallbacks(runnable);
        seekBar.setProgress(0);
        updateCurrentTime();
    }

    private void updateSeekBar() {
        seekBar.setProgress(videoView.getCurrentPosition());
        updateCurrentTime();

        if (videoView.isPlaying()) {
            runnable = () -> updateSeekBar();
            handler.postDelayed(runnable, 1000);
        }
    }

    private void updateCurrentTime() {
        tvCurrentTime.setText(formatTime(videoView.getCurrentPosition()));
    }

    private String formatTime(long milliseconds) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(milliseconds),
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds))
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }
}