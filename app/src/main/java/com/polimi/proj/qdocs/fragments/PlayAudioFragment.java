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

    private MediaPlayer mediaPlayer = null;

    private double startTime = 0;
    private double finalTime = 0;
    private String audioName;

    private SeekBar seekbar;

    private Handler myHandler = new Handler();

    public static int oneTimeOnly = 0;


    private OnFragmentInteractionListener mListener;

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
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }


        Uri myUri = ((ShowFileFragmentActivity) getActivity()).getAudioUri();
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


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d(TAG, "On create view");
        View view = inflater.inflate(R.layout.fragment_play_audio, container, false);
        final Button playButton = view.findViewById(R.id.btn_play);
        playButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d(TAG,"Button play clicked");
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                    Log.d(TAG,"music stop");
                    playButton.setText(getString(R.string.play_string));
                }
                else{
                    mediaPlayer.start();
                    Log.d(TAG,"music start");
                    playButton.setText(getString(R.string.pause_string));

                    finalTime = mediaPlayer.getDuration();
                    startTime = mediaPlayer.getCurrentPosition();

                    if (oneTimeOnly == 0) {
                        seekbar.setMax((int) finalTime);
                        oneTimeOnly = 1;
                    }

                    seekbar.setProgress((int)startTime);
                    myHandler.postDelayed(UpdateSongTime,100);
                }
            }
        });

        seekbar = view.findViewById(R.id.seekBar);
        seekbar.setClickable(false);

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playButton.setText(getString(R.string.play_string));
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

    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            startTime = mediaPlayer.getCurrentPosition();
            if(mediaPlayer.isPlaying()) {
                seekbar.setProgress((int) startTime);
            }
            myHandler.postDelayed(this, 100);
        }
    };
}
