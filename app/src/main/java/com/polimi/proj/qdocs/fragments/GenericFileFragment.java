package com.polimi.proj.qdocs.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.polimi.proj.qdocs.R;
import com.polimi.proj.qdocs.activities.ShowFileFragmentActivity;

import org.apache.commons.io.FileUtils;

import java.io.File;
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
    private static final int GENERIC = 100;
    private final String TAG="GENERIC_FILE_FRAGMENT";
    private final String AUTHORITY = "com.polimi.proj.qdocs.fileprovider";
    private  Uri fileUri;
    private Uri providerUri;
    private String mimeType;

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
        mimeType = ((ShowFileFragmentActivity) Objects.requireNonNull(getActivity())).getFileType();
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
        Log.d(TAG, "file URI: "+fileUri);
        Log.d(TAG, "mimeType: "+mimeType);
        /*
        if(fileUri.getPath().contains("cache")){
            createProviderUri();
        }*/
        createProviderUri();
        Log.d(TAG, "provider URI: "+providerUri);

        //finalUri = fileUri.getPath().contains("cache") ? providerUri : fileUri;


        Intent objIntent = new Intent(Intent.ACTION_VIEW);
        objIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        objIntent.setDataAndType(providerUri, mimeType);

        startActivityForResult(objIntent, GENERIC);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_generic_file, container, false);
    }

    private void createProviderUri() {
        // in a rare case we received file:// in currentUri, we need to:
        // 1. create new File variable from currentUri that looks like "file:///storage/emulated/0/download/50044382b.jpg"
        // 2. generate a proper content:// Uri for it
        File currentFile = new File(fileUri.getPath());
        providerUri = FileProvider.getUriForFile(getActivity(), AUTHORITY, currentFile);

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GENERIC)
            getActivity().finish();
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
