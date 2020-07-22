package com.example.chatappdemo.gif;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatappdemo.R;
import com.example.chatappdemo.gifs.Gif;

import java.util.List;


final class GifSearchAdapter extends RecyclerView.Adapter<GifSearchAdapter.GifViewHolder> {
    @NonNull
    private final Context mContext;
    @NonNull
    private final List<Gif> mData;
    @NonNull
    private final ItemSelectListener mListener;

    GifSearchAdapter(@NonNull final Context context,
                     @NonNull final List<Gif> data,
                     @NonNull final ItemSelectListener listener) {
        //noinspection ConstantConditions
        if (context == null || data == null || listener == null)
            throw new IllegalArgumentException("Null parameters not allowed.");

        mContext = context;
        mData = data;
        mListener = listener;
    }

    @Override
    public GifViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new GifViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_gif_search, parent, false));
    }

    @Override
    public void onBindViewHolder(GifViewHolder holder, int position) {
        final Gif gif = mData.get(position);
        if (gif != null) {
            Glide.with(mContext)
                    .asGif()
                    .load(gif.getPreviewGifUrl())
                    .centerCrop()
                    .into(holder.gifIv);

            holder.gifIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.OnListItemSelected(gif);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    interface ItemSelectListener {
        void OnListItemSelected(@NonNull Gif gif);
    }

    class GifViewHolder extends RecyclerView.ViewHolder {
        ImageView gifIv;

        GifViewHolder(View itemView) {
            super(itemView);
            gifIv = itemView.findViewById(R.id.gif_iv);
        }
    }
}
