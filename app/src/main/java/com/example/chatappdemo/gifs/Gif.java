package com.example.chatappdemo.gifs;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

@SuppressWarnings("WeakerAccess")
public final class Gif implements Parcelable {

    public static final Creator<Gif> CREATOR = new Creator<Gif>() {
        @Override
        public Gif createFromParcel(Parcel in) {
            return new Gif(in);
        }

        @Override
        public Gif[] newArray(int size) {
            return new Gif[size];
        }
    };

    @NonNull
    private final String gifUrl;

    @Nullable
    private final String previewGifUrl;

    @Nullable
    private final String mp4Url;

    @SuppressWarnings("ConstantConditions")
    public Gif(@NonNull String gifUrl, @Nullable String previewGifUrl, @Nullable String mp4Url) {
        if (gifUrl == null) throw new IllegalArgumentException("GIF url cannot be null.");

        this.gifUrl = gifUrl;
        this.previewGifUrl = previewGifUrl;
        this.mp4Url = mp4Url;
    }

    @SuppressWarnings("ConstantConditions")
    public Gif(@NonNull String gifUrl) {
        if (gifUrl == null) throw new IllegalArgumentException("GIF url cannot be null.");

        this.gifUrl = gifUrl;
        this.previewGifUrl = null;
        this.mp4Url = null;
    }

    public Gif(Parcel in) {
        this.previewGifUrl = in.readString();
        this.gifUrl = in.readString();
        this.mp4Url = in.readString();

        //noinspection ConstantConditions
        if (gifUrl == null) throw new IllegalArgumentException("GIF url cannot be null.");
    }

    @NonNull
    public String getPreviewGifUrl() {
        return previewGifUrl == null ? gifUrl : previewGifUrl;
    }

    @NonNull
    public String getGifUrl() {
        return gifUrl;
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || (obj instanceof Gif && ((Gif) obj).gifUrl.equals(gifUrl));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(previewGifUrl);
        dest.writeString(gifUrl);
        dest.writeString(mp4Url);
    }

    @Override
    public int hashCode() {
        return gifUrl.hashCode();
    }
}
