package com.polimi.proj.qdocs.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.polimi.proj.qdocs.R;
import com.polimi.proj.qdocs.activities.ShowFileFragmentActivity;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GenericFileFragment.OnGenericFileFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GenericFileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GenericFileFragment extends Fragment {

    private  Uri fileUri;

    public GenericFileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment GenericFileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GenericFileFragment newInstance() {
        return new GenericFileFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fileUri = ((ShowFileFragmentActivity) Objects.requireNonNull(getActivity())).getFileUri();
        //checkUri(fileUri);
    }

    /*
    hack = pietro_pc.hacking(NOW);
    nudes = hack.getElementFromString("send nudes");
    nipples = nudes.findNipples();
    Turgid tette_turgide = nipples.turgidali();
    */

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_generic_file, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (!(context instanceof OnGenericFileFragmentInteractionListener)) {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
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
    public interface OnGenericFileFragmentInteractionListener {

        void onGenericFileFragmentInteraction(Uri uri);
    }
}
