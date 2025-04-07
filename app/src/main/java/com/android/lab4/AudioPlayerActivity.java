package com.android.lab4;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.*;
import android.content.Intent;
import android.database.Cursor;
import android.provider.OpenableColumns;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class AudioPlayerActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private ImageButton btnPlay, btnPause, btnStop;
    private SeekBar seekBar;
    private TextView tvCurrentTime, tvTotalTime, tvSongTitle;
    private Handler handler;
    private Runnable runnable;
    private Button btnBack, btnSelectFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);

        btnPlay = findViewById(R.id.btn_play);
        btnPause = findViewById(R.id.btn_pause);
        btnStop = findViewById(R.id.btn_stop);
        btnBack = findViewById(R.id.btn_back);
        btnSelectFile = findViewById(R.id.btn_select_file);
        seekBar = findViewById(R.id.seek_bar);
        tvCurrentTime = findViewById(R.id.tv_current_time);
        tvTotalTime = findViewById(R.id.tv_total_time);
        tvSongTitle = findViewById(R.id.tv_song_title);

        handler = new Handler();

        btnPlay.setVisibility(View.VISIBLE);
        btnPause.setVisibility(View.GONE);

        btnBack.setOnClickListener(v -> onBackPressed());
        btnSelectFile.setOnClickListener(v -> pickAudioFile());
        btnPlay.setOnClickListener(v -> playAudio());
        btnPause.setOnClickListener(v -> pauseAudio());
        btnStop.setOnClickListener(v -> stopAudio());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                    tvCurrentTime.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    handler.removeCallbacks(runnable);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    updateSeekBar();
                }
            }
        });

        String fileUriString = getIntent().getStringExtra("fileUri");
        if (fileUriString != null) {
            initializeMediaPlayer(fileUriString);
        } else {
            tvCurrentTime.setText(formatTime(0));
            tvTotalTime.setText(formatTime(0));
            tvSongTitle.setText("Виберіть аудіофайл");
        }
    }


    private void updateSeekBar() {
        if (mediaPlayer != null) {
            runnable = new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        int currentPosition = mediaPlayer.getCurrentPosition();
                        seekBar.setProgress(currentPosition);
                        tvCurrentTime.setText(formatTime(currentPosition));
                        handler.postDelayed(this, 1000);
                    }
                }
            };
            handler.postDelayed(runnable, 1000);
        }
    }

    private void initializeMediaPlayer(String fileUriString) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
            );

            mediaPlayer.setDataSource(getApplicationContext(), Uri.parse(fileUriString));

            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> {
                seekBar.setMax(mediaPlayer.getDuration());
                tvTotalTime.setText(formatTime(mediaPlayer.getDuration()));
                updateSeekBar();
                playAudio();

                String songTitle = getSongTitle(fileUriString);
                tvSongTitle.setText(songTitle);
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                btnPlay.setVisibility(View.VISIBLE);
                btnPause.setVisibility(View.GONE);
                seekBar.setProgress(0);
                tvCurrentTime.setText(formatTime(0));
                handler.removeCallbacks(runnable);
            });

        } catch (IOException e) {
            Toast.makeText(this, "Помилка відтворення: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private String getSongTitle(String fileUriString) {
        String songName = "Без назви";

        // Перевіряємо, чи є URI локальним чи віддаленим
        if (fileUriString != null && fileUriString.startsWith("http")) {
            // Якщо це віддалений файл, намагаємося отримати ім'я з URL
            songName = fileUriString.substring(fileUriString.lastIndexOf("/") + 1);

            // Якщо ім'я файлу містить розширення, видаляємо його
            if (songName.contains(".")) {
                songName = songName.substring(0, songName.lastIndexOf('.'));
            }
        } else {
            // Якщо це локальний файл, використовуємо попередній метод для отримання назви
            Uri uri = Uri.parse(fileUriString);
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        songName = cursor.getString(nameIndex);
                        if (songName.contains(".")) {
                            songName = songName.substring(0, songName.lastIndexOf('.'));
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return songName;
    }


    private void pickAudioFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("audio/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 100);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectedUri = data.getData();
                if (selectedUri != null) {
                    initializeMediaPlayer(selectedUri.toString());
                }
            }
        }
    }

    private void playAudio() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            btnPlay.setVisibility(View.GONE);
            btnPause.setVisibility(View.VISIBLE);
            updateSeekBar();
        }
    }

    private void pauseAudio() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            btnPlay.setVisibility(View.VISIBLE);
            btnPause.setVisibility(View.GONE);
            handler.removeCallbacks(runnable);
        }
    }

    private void stopAudio() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            try {
                mediaPlayer.prepare();
                mediaPlayer.seekTo(0);
                tvCurrentTime.setText(formatTime(0));
                seekBar.setProgress(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
            btnPlay.setVisibility(View.VISIBLE);
            btnPause.setVisibility(View.GONE);
            handler.removeCallbacks(runnable);
        }
    }

    private String formatTime(long milliseconds) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(milliseconds),
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacks(runnable);
    }
}
