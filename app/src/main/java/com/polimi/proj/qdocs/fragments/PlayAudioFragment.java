package com.polimi.proj.qdocs.fragments;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.polimi.proj.qdocs.R;
import com.polimi.proj.qdocs.activities.ShowFileFragmentActivity;

import java.io.IOException;
import java.util.Objects;

import at.markushi.ui.CircleButton;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PlayAudioFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PlayAudioFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlayAudioFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match

    private static final String TAG = "AUDIO FRAGMENT";

    //used for manage the audio file
    private MediaPlayer mediaPlayer = null;

    private double startTime = 0;

    private String audioName;

    private SeekBar seekbar;

    //used to managed the position of the seekbar
    private Handler myHandler = new Handler();

    //Runnable object launched by the andler
    private UpdateSongTime updateSongTime;

    private OnFragmentInteractionListener mListener;

    private final Object updateState= new Object();

    private boolean onlyOne = true;

    public PlayAudioFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PlayAudioFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PlayAudioFragment newInstance() {
        PlayAudioFragment fragment = new PlayAudioFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        //prepare the media player object
        Uri myUri = ((ShowFileFragmentActivity) Objects.requireNonNull(getActivity())).getAudioUri();
        Log.d(TAG, "audio uri: "+myUri.getPath());
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(getApplicationContext(), myUri);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        audioName = ((ShowFileFragmentActivity) getActivity()).getFileName();

        Log.d(TAG, "audio name: "+audioName);
        Log.d(TAG, "audio duration: "+mediaPlayer.getDuration());


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d(TAG, "On create view");
        View view = inflater.inflate(R.layout.fragment_play_audio, container, false);
        final CircleButton playButton = view.findViewById(R.id.btn_play);
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

        seekbar = view.findViewById(R.id.seekBar);
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

        TextView audioNameTextView = view.findViewById(R.id.audio_name);
        audioNameTextView.setText(audioName);

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onPlayAudioFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onPlayAudioFragmentInteraction(Uri uri);
    }


    /**
     * This class implement Runnable interface. The run method update the position of the seek bar
     * every 100 millis
     *
     * call stop for stop the run method
     */
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
