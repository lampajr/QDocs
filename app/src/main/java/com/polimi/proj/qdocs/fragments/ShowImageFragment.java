package com.polimi.proj.qdocs.fragments;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ortiz.touchview.TouchImageView;
import com.polimi.proj.qdocs.R;
import com.polimi.proj.qdocs.activities.ShowFileFragmentActivity;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ShowImageFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ShowImageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShowImageFragment extends Fragment {

    private static final String TAG = "IMAGE FRAGMENT";
    private BitmapDrawable bitmapDrowalbe = null;

    public ShowImageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ShowImageFragment.
     */
    public static ShowImageFragment newInstance() {
        return new ShowImageFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Take the bitmap from the activity
        bitmapDrowalbe = ((ShowFileFragmentActivity) Objects.requireNonNull(getActivity())).getImageResult();
        checkbitmap(bitmapDrowalbe);
        Log.d(TAG, "bitmap received");


    }

    private void checkbitmap(BitmapDrawable b) {
        if(b == null) {
            Log.d(TAG,"Bitmap is null");
            Objects.requireNonNull(getActivity()).getSupportFragmentManager().popBackStack();
        }


    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView method");

        View view = inflater.inflate(R.layout.fragment_show_image, container, false);
        TouchImageView tuchImageView = view.findViewById(R.id.touch_image);
        tuchImageView.setImageDrawable(bitmapDrowalbe);
        
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (!(context instanceof OnFragmentInteractionListener)) {
            throw new RuntimeException(context.toString()
                    + " must implement OnScannerInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
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

        void onShowImageFragmentInteraction(Uri uri);
    }
}
