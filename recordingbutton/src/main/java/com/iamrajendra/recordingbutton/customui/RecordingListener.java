package com.iamrajendra.recordingbutton.customui;

public interface RecordingListener {

    void onRecordingStarted();

    void onRecordingLocked();

    void onRecordingCompleted();

    void onRecordingCanceled();

}
