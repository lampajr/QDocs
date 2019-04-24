package com.polimi.proj.qdocs.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.polimi.proj.qdocs.R;
import com.polimi.proj.qdocs.dialogs.ConfirmDialog;
import com.polimi.proj.qdocs.services.DownloadFileService;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import at.markushi.ui.CircleButton;

public class PlayAudioActivity extends AppCompatActivity {

    static final int DELETE_CODE = 100;
    private final String TAG = "AUDIO FRAGMENT";
    private MediaPlayer mediaPlayer = null; //used for manage the audio file
    private double startTime = 0;
    private String fileName;
    private SeekBar seekbar;
    private Handler myHandler = new Handler();    //used to managed the position of the seekbar
    private PlayAudioActivity.UpdateSongTime updateSongTime; //Runnable object launched by the handler
    private final Object updateState= new Object();
    private boolean onlyOne = true;
    private Uri fileUri;
    private String mimeType;
    private TextView start_time_text;
    private TextView end_time_text;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_audio);

        Bundle bundle = getIntent().getExtras();
        readParameter(bundle);

        setupToolbar();
        checkParameter();
        setMediaPlayer();
        setViewElement();

    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_widget);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void setViewElement() {
        final CircleButton playButton = findViewById(R.id.btn_play);
        start_time_text = findViewById(R.id.start_time_text);
        end_time_text = findViewById(R.id.end_time_text);
        updateLabelTime();
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

                updateLabelTime();


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
            mimeType = bundle.getString(DownloadFileService.RESULT_KEY_MIME_TYPE);
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

    private void updateLabelTime(){
        start_time_text.setText(String.format("%d:%d",
                TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                startTime)))
        );

        double finalTime = mediaPlayer.getDuration();
        end_time_text.setText(String.format("%d:%d",
                TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
                TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                finalTime)))
        );
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        switch(id)
        {
            case R.id.logout_menu:
                LoginActivity.logout();
                startLoginActivity();
                break;


            case R.id.delete_option:
                ConfirmDialog d = new ConfirmDialog(this, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent data = new Intent();
                        data.putExtra("fileName", fileName);
                        setResult(DELETE_CODE,data);
                        finish();

                    }
                });
        }
        return false;
    }

    /**
     * start the login Activity
     */
    private void startLoginActivity() {
        Intent loginIntent = new Intent(this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }

    private class UpdateSongTime implements Runnable{

        private boolean stop =false;

        @Override
        public void run() {
            if(!stop) {
                synchronized (updateState) {
                    if(mediaPlayer != null) {
                        startTime = mediaPlayer.getCurrentPosition();
                        if (mediaPlayer.isPlaying())
                            seekbar.setProgress((int) startTime);
                            updateLabelTime();
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
