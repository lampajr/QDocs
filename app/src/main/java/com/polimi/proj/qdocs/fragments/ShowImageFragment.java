package com.polimi.proj.qdocs.fragments;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ortiz.touchview.TouchImageView;
import com.polimi.proj.qdocs.R;
import com.polimi.proj.qdocs.activities.ShowFileFragmentActivity;

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

    private OnFragmentInteractionListener mListener;

    public ShowImageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ShowImageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ShowImageFragment newInstance() {
        ShowImageFragment fragment = new ShowImageFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bitmapDrowalbe = ((ShowFileFragmentActivity) getActivity()).getImageResult();
        checkbitmap(bitmapDrowalbe);
        Log.d(TAG, "bitmap received");


    }

    private void checkbitmap(BitmapDrawable b) {
        if(b == null) {
            Log.d(TAG,"Bitmap is null");
            getActivity().getSupportFragmentManager().popBackStack();
        }


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView method");

        View view = inflater.inflate(R.layout.fragment_show_image, container, false);
        TouchImageView tuchImageView = view.findViewById(R.id.touch_image);
        tuchImageView.setImageDrawable(bitmapDrowalbe);
        
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onShowImageFragmentInteraction(uri);
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
        void onShowImageFragmentInteraction(Uri uri);
    }
}
