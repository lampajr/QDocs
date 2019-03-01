package com.polimi.proj.qdocs.activities;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.polimi.proj.qdocs.R;
import com.polimi.proj.qdocs.services.DownloadFileService;

import java.io.IOException;

import at.markushi.ui.CircleButton;

public class PlayAudioActivity extends AppCompatActivity {

    private static final String TAG = "AUDIO FRAGMENT";
    private MediaPlayer mediaPlayer = null; //used for manage the audio file
    private double startTime = 0;
    private String fileName;
    private SeekBar seekbar;
    private Handler myHandler = new Handler();    //used to managed the position of the seekbar
    private PlayAudioActivity.UpdateSongTime updateSongTime; //Runnable object launched by the andler
    private final Object updateState= new Object();
    private boolean onlyOne = true;
    private Uri fileUri;
    private String mimeType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_audio);

        Bundle bundle = getIntent().getExtras();
        readParameter(bundle);

        checkParameter();
        setMediaPlayer();
        setViewElement();

    }

    private void setViewElement() {
        final CircleButton playButton = findViewById(R.id.btn_play);
        playButton.setOnClickListener(new View.OnClickListener() {
            /**
             *  If the music is stopped then starts play
             *  If the musiu is played then stop it
             * @param v
             */
            @Override
            public void onClick(View v) {
                Log.d(TAG,"Button play clicked");
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                    Log.d(TAG,"music stop");
                    playButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                }
                else{
                    mediaPlayer.start();
                    Log.d(TAG,"music start");
                    playButton.setImageResource(R.drawable.ic_pause_black_24dp);
                    startTime = mediaPlayer.getCurrentPosition();
                    seekbar.setProgress((int) startTime);

                    //the first time an audio is played an handler that manage the seekbar run
                    // updateSong time
                    if(onlyOne) {
                        updateSongTime = new UpdateSongTime();
                        myHandler.postDelayed(updateSongTime, 100);
                        onlyOne = false;
                    }

                }
            }
        });

        seekbar = findViewById(R.id.seekBar);
        seekbar.setClickable(false);

        double finalTime = mediaPlayer.getDuration();
        seekbar.setMax((int) finalTime);

        Log.d(TAG, "seekbar start: "+ startTime);
        Log.d(TAG, "seekbar end: "+ seekbar.getMax());

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                startTime = 0;
                seekbar.setProgress((int) startTime);
            }
        });

        TextView audioNameTextView = findViewById(R.id.audio_name);
        audioNameTextView.setText(fileName);

    }

    private void setMediaPlayer() {
        Log.d(TAG, "audio uri: "+fileUri.getPath());
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(getApplicationContext(), fileUri);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "audio duration: "+mediaPlayer.getDuration());
    }

    private void readParameter(Bundle bundle) {
        if (bundle != null) {
            fileUri = (Uri) bundle.get(DownloadFileService.RESULT_KEY_URI);
            mimeType = bundle.getString(DownloadFileService.RESULT_KEY_EXTENSION);
            fileName = bundle.getString(DownloadFileService.RESULT_KEY_FILENAME);
        }

        Log.d(TAG, "file uri: " + fileUri);
        Log.d(TAG, "mimeType: " + mimeType);
    }

    private void checkParameter() {
        if(fileUri != null && mimeType != null && fileName!= null){
            Log.d(TAG, "file URI recived: " + fileUri);
            Log.d(TAG, "mime type recived: " + mimeType);
            Log.d(TAG, "filename type recived: " + fileName);
        }
        else {

            if (fileUri == null) {
                Log.e(TAG, "URI recived is NULL");
            }
            if (mimeType == null) {
                Log.e(TAG, "mime type recived is NULL");
            }
            if (fileName == null){
                Log.d(TAG, "filename recived is NULL ");
            }

            //TODO: handle error

            finish();
        }
    }

    @Override
    public void onStop(){
        Log.d(TAG, "OnStop");
        super.onStop();
    }

    @Override
    public void onPause(){
        Log.d(TAG, "OnPause");
        super.onPause();
        if(updateSongTime!=null) {

            //the stop method of UpdateSongTime class take care also to stop mediaPlayer
            updateSongTime.stop();
            Log.d(TAG, "stopping the runnable updateSongTime");
        }
        seekbar.setProgress(0);
    }

    private class UpdateSongTime implements Runnable{

        private boolean stop =false;

        @Override
        public void run() {
            if(!stop) {
                if(startTime % 200 == 0)
                    Log.d(TAG, "updating song time...");
                synchronized (updateState) {
                    if(mediaPlayer != null) {
                        startTime = mediaPlayer.getCurrentPosition();
                        if (mediaPlayer.isPlaying())
                            seekbar.setProgress((int) startTime);
                    }
                }
                myHandler.postDelayed(this, 100);
            }
            else {
                Log.d(TAG, "update song time stopped");
            }
        }

        /**
         * stop the run and release the mediaPlayer object
         */
        void stop(){
            synchronized (updateState) {
                this.stop = true;
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        }
    }
}
