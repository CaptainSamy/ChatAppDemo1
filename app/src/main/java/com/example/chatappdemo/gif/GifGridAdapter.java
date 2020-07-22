package com.example.chatappdemo.gif;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.chatappdemo.R;
import com.example.chatappdemo.gifs.Gif;

import java.util.List;


final class GifGridAdapter extends ArrayAdapter<Gif> {
    @NonNull
    private final Context mContext;
    @NonNull
    private final ItemSelectListener mListener;
    @NonNull
    private final List<Gif> mData;

    GifGridAdapter(@NonNull final Context context,
                   @NonNull final List<Gif> data,
                   @NonNull final ItemSelectListener listener) {
        super(context, R.layout.item_emojicon, data);
        //noinspection ConstantConditions
        if (context == null || data == null || listener == null)
            throw new IllegalArgumentException("Null parameters not allowed.");

        mContext = context;
        mData = data;
        mListener = listener;
    }

    @Override
    public Gif getItem(int position) {
        return mData.get(position);
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        View v = convertView;
        ViewHolder holder;
        if (v == null) {
            v = LayoutInflater.from(mContext).inflate(R.layout.item_gif, parent, false);

            holder = new ViewHolder();
            holder.gifIv = v.findViewById(R.id.gif_iv);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        final Gif gif = getItem(position);
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
        return v;
    }


    interface ItemSelectListener {
        void OnListItemSelected(@NonNull Gif gif);
    }

    private class ViewHolder {
        private ImageView gifIv;
    }
}
