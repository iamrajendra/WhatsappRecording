package com.iamrajendra.whatsapprecording;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.iamrajendra.recordingbutton.customui.AudioRecordingButton;
import com.iamrajendra.recordingbutton.customui.RecordingListener;

public class MainActivity extends AppCompatActivity implements RecordingListener {
    AudioRecordingButton audioRecordingButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        audioRecordingButton = findViewById(R.id.dd);
        audioRecordingButton.setRecordingListener(this);
    }

    @Override
    public void onRecordingStarted() {
        show("recording started");
    }

    private void show(String recording_started) {
        Toast.makeText(getApplicationContext(),recording_started,Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRecordingLocked() {
        show("recording locked");

    }

    @Override
    public void onRecordingCompleted() {
        show("recording completed");

    }

    @Override
    public void onRecordingCanceled() {
show("recording cancel");
    }
}
