package com.example.chatappdemo.gif;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import com.example.chatappdemo.R;
import com.example.chatappdemo.gifs.Gif;
import com.example.chatappdemo.gifs.GifProviderProtocol;
import com.example.chatappdemo.gifs.GifSelectListener;

import java.util.ArrayList;
import java.util.List;


public final class GifFragment extends Fragment implements GifGridAdapter.ItemSelectListener {
    private static final String OUT_STATE_GIFS = "out_state_gifs";

    private Context mContext;
    private ArrayList<Gif> mGifs;
    private GifGridAdapter mGifGridAdapter;
    private ViewFlipper mViewFlipper;
    private GifProviderProtocol mGifProvider;
    private TextView mErrorTv;
    private AsyncTask<Void, Void, List<Gif>> mTrendingGifTask;

    @Nullable
    private GifSelectListener mGifSelectListener;


    public GifFragment() {
        // Required empty public constructor
    }

    public static GifFragment getNewInstance() {
        return new GifFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            mGifs = new ArrayList<>();
        } else {
            mGifs = savedInstanceState.getParcelableArrayList(OUT_STATE_GIFS);
        }

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_gif, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @SuppressWarnings("DanglingJavadoc")
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewFlipper = view.findViewById(R.id.gif_view_flipper);
        mErrorTv = view.findViewById(R.id.error_textview);

        //Set the grid view
        mGifGridAdapter = new GifGridAdapter(mContext, mGifs, this);

        GridView gridView = view.findViewById(R.id.gif_gridView);
        gridView.setNumColumns(getResources().getInteger(R.integer.gif_recycler_view_span_size));
        gridView.setAdapter(mGifGridAdapter);

        //Load the list of trending GIFs.
        if (mGifs.isEmpty()) {
            if (mTrendingGifTask != null) mTrendingGifTask.cancel(true);
            mTrendingGifTask = new TrendingGifTask();
            mTrendingGifTask.execute();
            mViewFlipper.setDisplayedChild(0);
        } else {
            mViewFlipper.setDisplayedChild(1);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(OUT_STATE_GIFS, mGifs);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //Cancel trending GIF.
        if (mTrendingGifTask != null) mTrendingGifTask.cancel(true);
    }

    @SuppressWarnings("ConstantConditions")
    public void setGifProvider(@NonNull GifProviderProtocol gifProvider) {
        mGifProvider = gifProvider;
    }

    public void setGifSelectListener(@Nullable GifSelectListener gifSelectListener) {
        mGifSelectListener = gifSelectListener;
    }

    @Override
    public void OnListItemSelected(@NonNull Gif gif) {
        if (mGifSelectListener != null) mGifSelectListener.onGifSelected(gif);
    }


    /**
     * Async task to load the list of trending GIFs.
     */
    @SuppressLint("StaticFieldLeak")
    private class TrendingGifTask extends AsyncTask<Void, Void, List<Gif>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //Display loading view.
            mViewFlipper.setDisplayedChild(0);
        }

        @Override
        protected List<Gif> doInBackground(Void... voids) {
            if (mGifProvider == null) throw new RuntimeException("Set GIF provider.");
            return mGifProvider.getTrendingGifs(16);
        }

        @Override
        protected void onPostExecute(@Nullable List<Gif> gifs) {
            super.onPostExecute(gifs);

            if (gifs == null) { //Error occurred.
                mErrorTv.setText("Something went wrong");
                mViewFlipper.setDisplayedChild(2);
            } else if (gifs.isEmpty()) { //No result found.
                mErrorTv.setText("No result found");
                mViewFlipper.setDisplayedChild(2);
            } else {
                mViewFlipper.setDisplayedChild(1);

                //Load the tending gifs
                mGifs.clear();
                mGifs.addAll(gifs);
                mGifGridAdapter.notifyDataSetChanged();
            }
        }
    }
}
