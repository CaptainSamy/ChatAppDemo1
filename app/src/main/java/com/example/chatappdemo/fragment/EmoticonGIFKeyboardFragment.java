package com.example.chatappdemo.fragment;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringDef;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;


import com.example.chatappdemo.R;
import com.example.chatappdemo.gif.GifFragment;
import com.example.chatappdemo.gif.GifSearchFragment;
import com.example.chatappdemo.gifs.GifProviderProtocol;
import com.example.chatappdemo.gifs.GifSelectListener;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


public final class EmoticonGIFKeyboardFragment extends Fragment implements FragmentManager.OnBackStackChangedListener {

    public static final String TAG_GIF_FRAGMENT = "tag_gif_fragment";
    private static final String TAG_GIF_SEARCH_FRAGMENT = "tag_gif_search_fragment";

    private static final String KEY_CURRENT_FRAGMENT = "current_fragment";

    @NonNull
    private final GifFragment mGifFragment;
    @NonNull
    private final GifSearchFragment mGifSearchFragment;

    @Nullable
    private GifSelectListener gifSelectListener;

    private View mBottomViewContainer;
    //private View mGifTabBtn;
    private View mBackSpaceBtn;
    private View mRootView;

    private boolean mIsGIFsEnable;

    private boolean mIsOpen = true;

    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    public EmoticonGIFKeyboardFragment() {
        mGifFragment = GifFragment.getNewInstance();
        mGifSearchFragment = GifSearchFragment.getNewInstance();
    }

    @SuppressWarnings("deprecation")
    public static EmoticonGIFKeyboardFragment getNewInstance(@NonNull View container,
                                                             @Nullable GIFConfig gifConfig) {
        //Validate inputs
        if (gifConfig == null)
            throw new IllegalStateException("At least one of emoticon or GIF should be active.");

        //Set the layout parameter for container
        container.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        container.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;

        //Initialize the fragment
        EmoticonGIFKeyboardFragment emoticonGIFKeyboardFragment = new EmoticonGIFKeyboardFragment();
        emoticonGIFKeyboardFragment.setRetainInstance(true);

        //Set GIFs
        if (gifConfig != null) {
            emoticonGIFKeyboardFragment.mIsGIFsEnable = true;
            emoticonGIFKeyboardFragment.setGifProvider(gifConfig.mGifProviderProtocol);
            emoticonGIFKeyboardFragment.setGifSelectListener(gifConfig.mGifSelectListener);
        }

        return emoticonGIFKeyboardFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        //Add back stack change listener to maintain views state according to fragment
        getChildFragmentManager().addOnBackStackChangedListener(this);

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_emoticon_gif_keyboard, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRootView = view.findViewById(R.id.root_view);
        mBottomViewContainer = view.findViewById(R.id.bottom_container);

        //Set backspace button
        mBackSpaceBtn = view.findViewById(R.id.emojis_backspace);
        mBackSpaceBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View view1) {
                if (gifSelectListener != null) gifSelectListener.onBackSpace();

                //dispatch back space event
                final KeyEvent event = new KeyEvent(0, 0, 0,
                        KeyEvent.KEYCODE_DEL, 0, 0, 0, 0,
                        KeyEvent.KEYCODE_ENDCALL);
                EmoticonGIFKeyboardFragment.this.getActivity().dispatchKeyEvent(event);
            }
        });

        //Setup the search button.
        View searchBtn = view.findViewById(R.id.search_btn);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view14) {
                EmoticonGIFKeyboardFragment.this.replaceFragment(mGifSearchFragment, TAG_GIF_SEARCH_FRAGMENT);
            }
        });

        if (savedInstanceState != null) {
            //noinspection ConstantConditions
            switch (savedInstanceState.getString(KEY_CURRENT_FRAGMENT)) {
                case TAG_GIF_FRAGMENT:
                    replaceFragment(mGifFragment, TAG_GIF_FRAGMENT);
                    break;
                case TAG_GIF_SEARCH_FRAGMENT:
                    replaceFragment(mGifSearchFragment, TAG_GIF_SEARCH_FRAGMENT);
                    break;
            }
        } else {
            if (isGIFsEnable())
                replaceFragment(mGifFragment, TAG_GIF_FRAGMENT);
            else
                throw new IllegalStateException("At least one of emoticon or GIF should be active.");
        }
    }

    private void replaceFragment(@NonNull Fragment fragment,
                                 @FragmentBackStackTags String tag) {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.keyboard_fragment_container, fragment)
                .addToBackStack(tag)
                .commit();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //Save current fragment
        outState.putString(KEY_CURRENT_FRAGMENT, getChildFragmentManager()
                .getBackStackEntryAt(getChildFragmentManager().getBackStackEntryCount() - 1)
                .getName());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackStackChanged() {
        int index = getChildFragmentManager().getBackStackEntryCount() - 1;
        if (index < 0) return;

        //noinspection WrongConstant
        changeLayoutFromTag(getChildFragmentManager().getBackStackEntryAt(index).getName());
    }

    public boolean handleBackPressed() {
        //Close the emoticon fragment
        if (isOpen()) {
            toggle();
            return true;
        }
        return false;
    }

    private void changeLayoutFromTag(@FragmentBackStackTags String tag) {
        switch (tag) {
            case TAG_GIF_FRAGMENT:
                //Display bottom bar of not displayed.
                if (mBottomViewContainer != null) mBottomViewContainer.setVisibility(View.VISIBLE);
                //mGifTabBtn.setSelected(true);
                mBackSpaceBtn.setVisibility(View.GONE);
                break;
            case TAG_GIF_SEARCH_FRAGMENT:
                //Display bottom bar of not displayed.
                if (mBottomViewContainer != null) mBottomViewContainer.setVisibility(View.GONE);
                break;
            default:
                //Do nothing
        }
    }

    private void setGifProvider(@NonNull GifProviderProtocol gifProvider) {
        mGifFragment.setGifProvider(gifProvider);
        mGifSearchFragment.setGifProvider(gifProvider);
    }


    public void setGifSelectListener(@Nullable GifSelectListener gifSelectListener) {
        mGifFragment.setGifSelectListener(gifSelectListener);
        mGifSearchFragment.setGifSelectListener(gifSelectListener);
    }

    public boolean isGIFsEnable() {
        return mIsGIFsEnable;
    }

    public synchronized void open() {
        if (mRootView != null) mRootView.setVisibility(View.VISIBLE);
    }

    public synchronized void close() {
        if (mRootView != null) mRootView.setVisibility(View.GONE);
        replaceFragment(mGifFragment, TAG_GIF_FRAGMENT);
    }


    public void toggle() {
        if (isOpen()) close();
        else open();
    }

    public boolean isOpen() {
        return mRootView.getVisibility() == View.VISIBLE;
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({TAG_GIF_FRAGMENT, TAG_GIF_SEARCH_FRAGMENT})
    @interface FragmentBackStackTags {
    }

    public final static class GIFConfig {
        @NonNull
        private GifProviderProtocol mGifProviderProtocol;

        @Nullable
        private GifSelectListener mGifSelectListener;

        public GIFConfig(@NonNull GifProviderProtocol gifProviderProtocol) {
            mGifProviderProtocol = gifProviderProtocol;
        }

        @NonNull
        public GIFConfig setGifSelectListener(@Nullable GifSelectListener gifSelectListener) {
            mGifSelectListener = gifSelectListener;
            return this;
        }
    }
}
