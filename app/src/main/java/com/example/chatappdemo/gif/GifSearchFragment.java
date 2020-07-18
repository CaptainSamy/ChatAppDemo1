package com.example.chatappdemo.gif;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.chatappdemo.R;
import com.example.chatappdemo.fragment.EmoticonGIFKeyboardFragment;
import com.example.chatappdemo.gifs.Gif;
import com.example.chatappdemo.gifs.GifProviderProtocol;
import com.example.chatappdemo.gifs.GifSelectListener;

import java.util.ArrayList;
import java.util.List;

public final class GifSearchFragment extends Fragment implements GifSearchAdapter.ItemSelectListener {

    private ArrayList<Gif> mGifs;
    private GifSearchAdapter mGifGridAdapter;

    @Nullable
    private GifSelectListener mGifSelectListener;
    private GifProviderProtocol mGifProvider;
    private ViewFlipper mViewFlipper;
    private SearchGifTask mSearchTask;
    private TrendingGifTask mTrendingGifTask;
    private TextView mErrorTv;
    private RecyclerView mRecyclerView;
    private EditText mSearchEt;

    public GifSearchFragment() {
        // Required empty public constructor
    }

    public static GifSearchFragment getNewInstance() {
        return new GifSearchFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_gif_search, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mGifs = new ArrayList<>();
        mViewFlipper = view.findViewById(R.id.gif_search_view_pager);
        mErrorTv = view.findViewById(R.id.error_tv);

        //Set the list
        mGifGridAdapter = new GifSearchAdapter(getActivity(), mGifs, this);
        mRecyclerView = view.findViewById(R.id.gif_search_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(),
                LinearLayoutManager.HORIZONTAL, false));
        mRecyclerView.setAdapter(mGifGridAdapter);

        //Set the search interface
        mSearchEt = view.findViewById(R.id.search_box_et);
        mSearchEt.requestFocus();
        view.findViewById(R.id.search_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view1) {
                GifSearchFragment.this.searchGif(mSearchEt.getText().toString());
            }
        });
        mSearchEt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                GifSearchFragment.this.searchGif(mSearchEt.getText().toString());
                return true;
            }
        });

        //Set up button
        ImageButton backBtn = view.findViewById(R.id.up_arrow);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view12) {
                GifSearchFragment.this.hideKeyboard();

                mSearchEt.setText("");

                //Pop fragment from the back stack
                GifSearchFragment.this.getFragmentManager().popBackStackImmediate(EmoticonGIFKeyboardFragment.TAG_GIF_FRAGMENT, 0);
            }
        });

        showKeyboard();
    }

    @Override
    public void onResume() {
        super.onResume();
        mSearchEt.setText("");
        //Start loading trending GIFs
        if (mTrendingGifTask != null)
            mTrendingGifTask.cancel(true);
        mTrendingGifTask = new TrendingGifTask();
        mTrendingGifTask.execute();

    }

    private void showKeyboard() {
        //Show the keyboard
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.showSoftInput(mSearchEt, 0);
    }

    private void hideKeyboard() {
        //Hide the keyboard
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(mSearchEt.getWindowToken(), 0);
    }

    private void searchGif(@NonNull final String searchQuery) {
        if (searchQuery.length() > 0) {
            mTrendingGifTask.cancel(true);
            if (mSearchTask != null) mSearchTask.cancel(true);

            mSearchTask = new SearchGifTask();
            mSearchTask.execute(searchQuery);

            hideKeyboard();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSearchTask != null) mSearchTask.cancel(true);
    }

    @Override
    public void OnListItemSelected(@NonNull final Gif gif) {
        if (mGifSelectListener != null) mGifSelectListener.onGifSelected(gif);
    }

    @SuppressWarnings("ConstantConditions")
    public void setGifProvider(@NonNull final GifProviderProtocol gifProvider) {
        if (gifProvider == null) throw new RuntimeException("Set GIF loader.");
        mGifProvider = gifProvider;
    }

    public void setGifSelectListener(@Nullable final GifSelectListener gifSelectListener) {
        mGifSelectListener = gifSelectListener;
    }

    @SuppressLint("StaticFieldLeak")
    private class SearchGifTask extends AsyncTask<String, Void, List<Gif>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //Display loading view.
            mViewFlipper.setDisplayedChild(0);
        }

        @Override
        protected List<Gif> doInBackground(final String... strings) {
            if (mGifProvider == null) throw new RuntimeException("Set GIF provider.");
            return mGifProvider.searchGifs(20, strings[0]);
        }

        @Override
        protected void onPostExecute(@Nullable final List<Gif> gifs) {
            super.onPostExecute(gifs);

            if (gifs == null) { //Error occurred.
                mErrorTv.setText("Something went wrong");
                mViewFlipper.setDisplayedChild(2);
            } else if (gifs.isEmpty()) { //No result found.
                mErrorTv.setText("No search found");
                mViewFlipper.setDisplayedChild(2);
            } else {
                mViewFlipper.setDisplayedChild(1);

                //Load the tending gifs
                mGifs.clear();
                mGifs.addAll(gifs);
                mGifGridAdapter.notifyDataSetChanged();

                //Move to position 0.
                mRecyclerView.scrollToPosition(0);
            }

            mSearchTask = null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mSearchTask = null;
        }
    }

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
            return mGifProvider.getTrendingGifs(20);
        }

        @Override
        protected void onPostExecute(@Nullable final List<Gif> gifs) {
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

                //Move to position 0.
                mRecyclerView.scrollToPosition(0);
            }
        }
    }
}
