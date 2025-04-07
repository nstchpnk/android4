package com.android.lab4;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_AUDIO_PERMISSION = 101;
    private static final int REQUEST_CODE_VIDEO_PERMISSION = 102;
    private static final int PICK_AUDIO_REQUEST = 1;
    private static final int PICK_VIDEO_REQUEST = 2;

    private Button btnSelectAudio;
    private Button btnSelectVideo;
    private Button btnLoadFromWeb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSelectAudio = findViewById(R.id.btn_select_audio);
        btnSelectVideo = findViewById(R.id.btn_select_video);
        btnLoadFromWeb = findViewById(R.id.btn_load_from_web);

        btnSelectAudio.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AudioPlayerActivity.class);
            startActivity(intent);
        });

        btnSelectVideo.setOnClickListener(v -> {
            checkPermissionAndPickFile(PICK_VIDEO_REQUEST);
        });

        btnLoadFromWeb.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, WebUrlActivity.class);
            startActivity(intent);
        });
    }

    private void checkPermissionAndPickFile(int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API 33+
            if (requestCode == PICK_AUDIO_REQUEST) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_AUDIO}, REQUEST_CODE_AUDIO_PERMISSION);
                } else {
                    pickFile(requestCode);
                }
            } else if (requestCode == PICK_VIDEO_REQUEST) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_VIDEO}, REQUEST_CODE_VIDEO_PERMISSION);
                } else {
                    pickFile(requestCode);
                }
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        requestCode == PICK_AUDIO_REQUEST ? REQUEST_CODE_AUDIO_PERMISSION : REQUEST_CODE_VIDEO_PERMISSION);
            } else {
                pickFile(requestCode);
            }
        }
    }

    private void pickFile(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        if (requestCode == PICK_AUDIO_REQUEST) {
            intent.setType("audio/*");
        } else {
            intent.setType("video/*");
        }
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickFile(PICK_AUDIO_REQUEST);
            } else {
                Toast.makeText(this, "Дозвіл на доступ до аудіофайлів відхилено", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_CODE_VIDEO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickFile(PICK_VIDEO_REQUEST);
            } else {
                Toast.makeText(this, "Дозвіл на доступ до відеофайлів відхилено", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            Uri selectedFileUri = data.getData();
            Intent playerIntent;

            if (requestCode == PICK_AUDIO_REQUEST) {
                playerIntent = new Intent(MainActivity.this, AudioPlayerActivity.class);
            } else {
                playerIntent = new Intent(MainActivity.this, VideoPlayerActivity.class);
            }

            playerIntent.putExtra("fileUri", selectedFileUri.toString());
            startActivity(playerIntent);
        }
    }
}