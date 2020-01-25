package com.iamrajendra.recordingbutton.customui;

import android.animation.Animator;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.iamrajendra.recordingbutton.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import io.supercharge.shimmerlayout.ShimmerLayout;

import static android.content.ContentValues.TAG;


public class AudioRecordingButton extends RelativeLayout {
    private SimpleDateFormat timeFormatter = new SimpleDateFormat("m:ss", Locale.getDefault());

// this is a basic two buttons
    private View audioButton,stopButton;

// this is a lock view
    private View lock,arrow,locklayout;

// this is a animation for lock,mic and arrow
    private Animation animJump,animBlink, animJumpFast;

//    this is slide layout contains arrow and text
    private View layoutSlideCancel;


// this is dustbin cover,dustbin and mic which is used when recording is cancelled and killed
    private  View dustin_cover, dustbin,imageViewMic;


    private UserBehaviour userBehaviour = UserBehaviour.NONE;

    private boolean isLocked = false;

    private RecordingBehaviour recordingBehaviour = RecordingBehaviour.CANCELED;


    private float directionOffset,lockOffset, cancelOffset;

    private float lastX, lastY;
    private float firstX, firstY;




    private float dp = 0;



    private TextView timeText;
    private TimerTask timerTask;
    private Timer audioTimer;
    private int audioTotalTime;



    private Handler handler;

    private RecordingListener recordingListener;

    public void setRecordingListener(RecordingListener recordingListener) {
        this.recordingListener = recordingListener;
    }

    public AudioRecordingButton(Context context) {
        super(context);
        init();
    }



    public AudioRecordingButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AudioRecordingButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public AudioRecordingButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }


    private void init() {
        View view  = LayoutInflater.from(getContext()).inflate(R.layout.audio_recording_button,null,false);
        addView(view);

        dp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());



        audioButton  = findViewById(R.id.audio_button);
        stopButton = findViewById(R.id.stop_button);

        lock = findViewById(R.id.imageViewLock);
        arrow = findViewById(R.id.imageViewLockArrow);
        locklayout = findViewById(R.id.layoutLock);

        imageViewMic  = findViewById(R.id.imageViewMic);
        timeText = findViewById(R.id.textViewTime);

        dustin_cover = findViewById(R.id.dustin_cover);
        dustbin = findViewById(R.id.dustin);

        layoutSlideCancel = findViewById(R.id.layoutSlideCancel);

        ( (ShimmerLayout)layoutSlideCancel).setAnimationReversed(true);
        stopButton.setOnClickListener(stopButtonClickListener);
        audioButton.setOnTouchListener(audioButtonOnTouchListener);




        handler = new Handler(Looper.getMainLooper());



        animJump = AnimationUtils.loadAnimation(getContext(),
                R.anim.jump);
        animBlink = AnimationUtils.loadAnimation(getContext(),
                R.anim.blink);
        animJumpFast = AnimationUtils.loadAnimation(getContext(),R.anim.jump_fast);


    }

    private OnClickListener stopButtonClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(recordingBehaviour == RecordingBehaviour.LOCKED || recordingBehaviour == RecordingBehaviour.RELEASED ){
                isLocked= false;
                stopRecording(RecordingBehaviour.LOCK_DONE);
            }
        }
    };

    private OnTouchListener audioButtonOnTouchListener =  new OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {



            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                Log.e(TAG, "onTouch: ACTION DOWN" );
                lockOffset = (float) (audioButton.getX() / 2.5);
                cancelOffset = (float) (audioButton.getX() / 2.8);

                if (firstX == 0) {
                    firstX = motionEvent.getRawX();
                }

                if (firstY == 0) {
                    firstY = motionEvent.getRawY();
                }

           startRecording();


            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP
                    || motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {


                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    stopRecording(RecordingBehaviour.RELEASED);


                }

            } else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {

                float motionX = Math.abs(firstX - motionEvent.getRawX());
                float motionY = Math.abs(firstY - motionEvent.getRawY());
               UserBehaviour direction = UserBehaviour.NONE;




                if (motionX > directionOffset &&
                        motionX > directionOffset &&
                        lastX < firstX && lastY < firstY) {

                    if (motionX > motionY && lastX < firstX) {
                        direction = UserBehaviour.CANCELING;

                    } else if (motionY > motionX && lastY < firstY) {
                        direction = UserBehaviour.LOCKING;
                    }

                } else if (motionX > motionY && motionX > directionOffset && lastX < firstX) {
                    direction = UserBehaviour.CANCELING;
                } else if (motionY > motionX && motionY > directionOffset && lastY < firstY) {
                    direction = UserBehaviour.LOCKING;
                }

                if (direction == UserBehaviour.CANCELING) {
                    if (userBehaviour == UserBehaviour.NONE || motionEvent.getRawY() + audioButton.getWidth() / 2 > firstY) {
                        userBehaviour = UserBehaviour.CANCELING;
                    }

                    if (userBehaviour == UserBehaviour.CANCELING) {
                        translateX(-(firstX - motionEvent.getRawX()));
                    }
                } else if (direction == UserBehaviour.LOCKING) {
                    if (userBehaviour == UserBehaviour.NONE || motionEvent.getRawX() + audioButton.getWidth() / 2 > firstX) {
                        userBehaviour = UserBehaviour.LOCKING;
                    }

                    if (userBehaviour == UserBehaviour.LOCKING) {
                        translateY(-(firstY - motionEvent.getRawY()));
                    }
                }


                lastX = motionEvent.getRawX();
                lastY = motionEvent.getRawY();


            }
            return view.onTouchEvent(motionEvent);

        }
    };


    private void startRecording() {

        layoutSlideCancel.setVisibility(View.VISIBLE);
        ((ShimmerLayout)layoutSlideCancel).startShimmerAnimation();
        imageViewMic.setVisibility(View.VISIBLE);
        timeText.setVisibility(View.VISIBLE);




//        lock start animation

        locklayout.setVisibility(View.VISIBLE);

        lock.clearAnimation();
        lock.startAnimation(animJump);

        arrow.clearAnimation();
        arrow.startAnimation(animJumpFast);

        imageViewMic.clearAnimation();
        imageViewMic.startAnimation(animBlink);
        if (recordingListener!=null)
            recordingListener.onRecordingStarted();

        audioButton.animate().scaleXBy(1f).scaleYBy(1f).setDuration(200).setInterpolator(new OvershootInterpolator()).start();

        if (audioTimer == null) {
            audioTimer = new Timer();
            timeFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        }
        timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        timeText.setText(timeFormatter.format(new Date(audioTotalTime * 1000)));
                        audioTotalTime++;
                    }
                });
            }
        };

        audioTotalTime = 0;
        audioTimer.schedule(timerTask, 0, 1000);
    }


    private void stopRecording(RecordingBehaviour recordingBehaviour) {
        this.recordingBehaviour = recordingBehaviour;
//        stopTrackingAction = true;
        firstX = 0;
        firstY = 0;
        lastX = 0;
        lastY = 0;

        userBehaviour = UserBehaviour.NONE;

        audioButton.animate().scaleX(1f).scaleY(1f).translationX(0).translationY(0).setDuration(100).setInterpolator(new LinearInterpolator()).start();
        layoutSlideCancel.setTranslationX(0);
        layoutSlideCancel.setVisibility(View.GONE);
        ((ShimmerLayout)layoutSlideCancel).stopShimmerAnimation();


        locklayout.setVisibility(View.GONE);
        locklayout.setTranslationY(0);
        arrow.clearAnimation();
        lock.clearAnimation();

        if (isLocked) {
            return;
        }

        if (recordingBehaviour == RecordingBehaviour.LOCKED) {
            Log.e(TAG, "stopRecording: "+recordingBehaviour );
//            imageViewStop.setVisibility(View.VISIBLE);

            stopButton.setVisibility(View.VISIBLE);
            audioButton.setVisibility(View.GONE);

            if (recordingListener != null)
                recordingListener.onRecordingLocked();

        } else if (recordingBehaviour == RecordingBehaviour.CANCELED) {
            timeText.clearAnimation();
            timeText.setVisibility(View.INVISIBLE);
            imageViewMic.setVisibility(View.INVISIBLE);
            imageViewMic.clearAnimation();
//            imageViewStop.setVisibility(View.GONE);

            timerTask.cancel();
            delete();
//
            if (recordingListener != null)
                recordingListener.onRecordingCanceled();

        } else if (recordingBehaviour == RecordingBehaviour.RELEASED || recordingBehaviour == RecordingBehaviour.LOCK_DONE) {
            timeText.clearAnimation();
            timeText.setVisibility(View.INVISIBLE);
            imageViewMic.setVisibility(View.INVISIBLE);
            imageViewMic.clearAnimation();
            stopButton.setVisibility(View.GONE);
            audioButton.setVisibility(View.VISIBLE);


            timerTask.cancel();

            if (recordingListener != null)
                recordingListener.onRecordingCompleted();
        }
    }

    private void delete() {
        imageViewMic.setVisibility(View.VISIBLE);
        imageViewMic.setRotation(0);
        audioButton.setEnabled(false);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                audioButton.setEnabled(true);
            }
        }, 1250);

        imageViewMic.animate().translationY(-dp * 150).rotation(180).scaleXBy(0.6f).scaleYBy(0.6f).setDuration(500).setInterpolator(new DecelerateInterpolator()).setListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                dustbin.setTranslationX(-dp * 40);
                dustin_cover.setTranslationX(-dp * 40);

                dustin_cover.animate().translationX(0).rotation(-120).setDuration(350).setInterpolator(new DecelerateInterpolator()).start();

                dustbin.animate().translationX(0).setDuration(350).setInterpolator(new DecelerateInterpolator()).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        dustbin.setVisibility(View.VISIBLE);
                        dustin_cover.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {

                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                imageViewMic.animate().translationY(0).scaleX(1).scaleY(1).setDuration(350).setInterpolator(new LinearInterpolator()).setListener(
                        new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                imageViewMic.setVisibility(View.INVISIBLE);
                                imageViewMic.setRotation(0);
                                imageViewMic.clearAnimation();

                                dustin_cover.animate().rotation(0).setDuration(150).setStartDelay(50).start();
                                dustbin.animate().translationX(-dp * 40).setDuration(200).setStartDelay(250).setInterpolator(new DecelerateInterpolator()).start();
                                dustin_cover.animate().translationX(-dp * 40).setDuration(200).setStartDelay(250).setInterpolator(new DecelerateInterpolator()).setListener(new Animator.AnimatorListener() {
                                    @Override
                                    public void onAnimationStart(Animator animation) {


                                    }

                                    @Override
                                    public void onAnimationEnd(Animator animation) {
//                                        layoutMessage.setVisibility(View.VISIBLE);
                                    }

                                    @Override
                                    public void onAnimationCancel(Animator animation) {

                                    }

                                    @Override
                                    public void onAnimationRepeat(Animator animation) {

                                    }
                                }).start();
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        }
                ).start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).start();
    }



    private void canceled() {
        stopRecording(RecordingBehaviour.CANCELED);
    }


    private void locked() {
        stopRecording(RecordingBehaviour.LOCKED);
        isLocked = true;
    }


    private void translateX(float x) {
        if (x < -cancelOffset) {
            canceled();
            audioButton.setTranslationX(0);
            layoutSlideCancel.setTranslationX(0);
            return;
        }

        audioButton.setTranslationX(x);
        layoutSlideCancel.setTranslationX(x);
        locklayout.setTranslationY(0);
        audioButton.setTranslationY(0);

        if (Math.abs(x) < imageViewMic.getWidth() / 2) {
            if (locklayout.getVisibility() != View.VISIBLE) {
                locklayout.setVisibility(View.VISIBLE);
            }
        } else {
            if (locklayout.getVisibility() != View.GONE) {
                locklayout.setVisibility(View.GONE);
            }
        }
    }


    private void translateY(float y) {

        if (y < -lockOffset) {
            locked();
            audioButton.setTranslationY(0);
            return;
        }
        if (locklayout.getVisibility() != View.VISIBLE) {
            locklayout.setVisibility(View.VISIBLE);
        }
        audioButton.setTranslationY(y);
        locklayout.setTranslationY(y / 2);
        audioButton.setTranslationX(0);
    }
    }



