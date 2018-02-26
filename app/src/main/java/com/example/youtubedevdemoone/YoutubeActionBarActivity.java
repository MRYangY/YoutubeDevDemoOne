package com.example.youtubedevdemoone;

import android.app.ActionBar;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Created by yangyu on 2018/2/26.
 */

public class YoutubeActionBarActivity extends YouTubeBaseActivity implements YouTubePlayer.OnFullscreenListener {

    public static final String DEVELOPER_KEY = "AIzaSyBx7v0YOb140fDO7EbfMx4l87raxezDWFw";

    private static final int FORWARD_REWIND_STEP = 300;
    private static final long FORWARD_REWIND_FIRST_TIME_INTERVAL = 500L;
    private static final long FORWARD_REWIND_TIME_INTERVAL = 100L;


    private ActionBarPaddedFrameLayout mContainer;
    private YouTubePlayerView mPlayView;
    private YouTubePlayer mPlayer;
    private ActionBar mActionBar;
    private TextView mCurTimeText;
    private TextView mDurationText;

    private String videoId = "9c6W4CCU9M4";

    private int mCurDuration;
    private int mCurPosition;
    private int mForwardRwindOff;
    private int mForwardRwindPos = -1;

    private Handler mPlayerHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_youtube_layout);
        mContainer = findViewById(R.id.youtube_play_container);
        mPlayView = findViewById(R.id.player_view);
        mPlayView.initialize(DEVELOPER_KEY, mInitializedListener);
        mPlayView.setFocusable(false);
        mContainer = findViewById(R.id.youtube_play_container);
        mActionBar = getActionBar();
        if (mActionBar != null) {
            mContainer.setActionBar(mActionBar);
            mActionBar.setBackgroundDrawable(new ColorDrawable(0xAA000000));
            mActionBar.setDisplayShowHomeEnabled(false);
            mActionBar.setDisplayShowCustomEnabled(true);
            mActionBar.setCustomView(R.layout.youtube_activity_actionbar_layout_show_time);
            mCurTimeText = mActionBar.getCustomView().findViewById(R.id.youtube_cur_time);
            mDurationText = mActionBar.getCustomView().findViewById(R.id.youtube_duration);
            mActionBar.hide();
        }
    }

    private YouTubePlayer.OnInitializedListener mInitializedListener = new YouTubePlayer.OnInitializedListener() {
        @Override
        public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
            if (youTubePlayer == null) return;
            mPlayer = youTubePlayer;
            mPlayer.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);
            mPlayer.setOnFullscreenListener(YoutubeActionBarActivity.this);
            mPlayer.setShowFullscreenButton(false);
            mPlayer.setFullscreen(true);
//                    mPlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
            if (!b) {
                mPlayer.cueVideo(videoId);
            }
            mPlayer.setPlaybackEventListener(mPlayBackEventListener);
            mPlayer.setPlayerStateChangeListener(mPlayStateChangeListener);
        }

        @Override
        public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

        }
    };

    private YouTubePlayer.PlayerStateChangeListener mPlayStateChangeListener = new YouTubePlayer.PlayerStateChangeListener() {
        @Override
        public void onLoading() {

        }

        @Override
        public void onLoaded(String s) {
            playOrPause();
            mCurDuration = mPlayer.getDurationMillis();
            mCurPosition = mPlayer.getCurrentTimeMillis();
        }

        @Override
        public void onAdStarted() {

        }

        @Override
        public void onVideoStarted() {

        }

        @Override
        public void onVideoEnded() {

        }

        @Override
        public void onError(YouTubePlayer.ErrorReason errorReason) {

        }
    };

    private YouTubePlayer.PlaybackEventListener mPlayBackEventListener = new YouTubePlayer.PlaybackEventListener() {
        @Override
        public void onPlaying() {

        }

        @Override
        public void onPaused() {

        }

        @Override
        public void onStopped() {

        }

        @Override
        public void onBuffering(boolean b) {

        }

        @Override
        public void onSeekTo(int i) {

        }
    };

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    startRewind();
                    return true;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    startForward();
                    return true;
//                case KeyEvent.KEYCODE_DPAD_UP:
//                    //播放上一级
//                    mPlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
//                    doPreVideo();
//                    showControl();
//                    hideControl();
//                    return true;
//                case KeyEvent.KEYCODE_DPAD_DOWN:
//                    //播放下一级
//                    mPlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
//                    doNextVideo();
//                    showControl();
//                    hideControl();
//                    return true;
                case KeyEvent.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_ENTER:
                    //暂停or播放
                    mPlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
                    playOrPause();
                    return true;
                case KeyEvent.KEYCODE_BACK:
                    finish();
                    return true;
                case KeyEvent.KEYCODE_MENU:
                    return true;

            }

        }
        if (event.getAction() == KeyEvent.ACTION_UP) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                case KeyEvent.KEYCODE_MEDIA_REWIND:
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    stopForwardRewind();
                    return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private void startForward() {
        if (mForwardRwindPos >= 0) return;
        mActionBar.show();
        mCurPosition = mPlayer.getCurrentTimeMillis();
        mPlayerHandler.removeCallbacks(mUpdatePlayTime);
        mPlayerHandler.removeCallbacks(mHideProgressRunnable);
        mForwardRwindOff = mCurDuration / FORWARD_REWIND_STEP;
        mForwardRwindPos = mCurPosition + mForwardRwindOff;

        mPlayerHandler.removeCallbacks(mForwardRewindRunnable);
        mPlayerHandler.postDelayed(mForwardRewindRunnable, FORWARD_REWIND_FIRST_TIME_INTERVAL);


    }

    private void startRewind() {
//        if (!mIsStart || !mIsPause) return;
        if (mForwardRwindPos >= 0) return;
        mActionBar.show();
        mCurPosition = mPlayer.getCurrentTimeMillis();
        mPlayerHandler.removeCallbacks(mUpdatePlayTime);
        mPlayerHandler.removeCallbacks(mHideProgressRunnable);
        mForwardRwindOff = -mCurDuration / FORWARD_REWIND_STEP;
        mForwardRwindPos = mCurPosition + mForwardRwindOff;

        mPlayerHandler.removeCallbacks(mForwardRewindRunnable);
        mPlayerHandler.postDelayed(mForwardRewindRunnable, FORWARD_REWIND_FIRST_TIME_INTERVAL);
    }

    private void stopForwardRewind() {
        mPlayerHandler.removeCallbacks(mHideProgressRunnable);
        mPlayerHandler.postDelayed(mHideProgressRunnable, 2000);
        mPlayerHandler.removeCallbacks(mForwardRewindRunnable);
        mPlayerHandler.post(mUpdatePlayTime);
        doSeek(mForwardRwindPos);
    }

    private Runnable mHideProgressRunnable = new Runnable() {
        @Override
        public void run() {
            mActionBar.hide();
        }
    };

    private Runnable mForwardRewindRunnable = new Runnable() {
        @Override
        public void run() {
            mPlayerHandler.removeCallbacks(mForwardRewindRunnable);
            mForwardRwindPos += mForwardRwindOff;
            if (mForwardRwindPos < 0) {
                mForwardRwindPos = 0;
                mPlayerHandler.post(mUpdatePlayTime);
                return;
            }
            if (mForwardRwindPos > mCurDuration) {
                mForwardRwindPos = mCurDuration;
                mPlayerHandler.post(mUpdatePlayTime);
                return;
            }

            mPlayerHandler.post(mUpdatePlayTime);
            mPlayerHandler.postDelayed(mForwardRewindRunnable, FORWARD_REWIND_TIME_INTERVAL);
        }
    };

    private Runnable mUpdatePlayTime = new Runnable() {
        @Override
        public void run() {
            int position = mForwardRwindPos >= 0 ? mForwardRwindPos : mCurPosition;
            int duration = mCurDuration; // mMediaPlayer.getDuration();
            if (position < 0) position = 0;
            if (position > duration) position = duration;

            mCurPosition = position;
            mCurTimeText.setText(Utils.formatTimeMilliseconds(position));
            mDurationText.setText("/" + Utils.formatTimeMilliseconds(duration));
        }
    };

    private void doSeek(int p) {
        if (mPlayer != null) {
            mCurPosition = p;
            mPlayer.seekToMillis(p);
            mForwardRwindPos = -1;
        }
    }


    private void playOrPause() {
        if (mPlayer != null) {
            if (mPlayer.isPlaying()) {
                mPlayer.pause();
            } else {
                mPlayer.play();
                mPlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
            }
        }
    }


    @Override
    public void onFullscreen(boolean b) {
        mContainer.setEnablePadding(!b);
        if (b) {
            ViewGroup.LayoutParams playerParams = mPlayView.getLayoutParams();
            playerParams.width = MATCH_PARENT;
            playerParams.height = MATCH_PARENT;
        }
    }
}
