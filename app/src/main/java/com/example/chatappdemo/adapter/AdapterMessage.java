package com.example.chatappdemo.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.chatappdemo.R;
import com.example.chatappdemo.model.Messages;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;

public class AdapterMessage extends RecyclerView.Adapter<AdapterMessage.MessageViewHolder> {
    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    private List<Messages> userMessagesList;
    private FirebaseUser firebaseUser;
    private Context context;
    String imageUrl;
    private MediaPlayer mPlayer;
    private int lastProgress = 0;
    private Handler mHandler = new Handler();
    private boolean isPlaying = false;

    public AdapterMessage(List<Messages> userMessagesList, Context context, String imageUrl) {
        this.userMessagesList = userMessagesList;
        this.context = context;
        this.imageUrl = imageUrl;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        private TextView timeTv, isSeenTv;
        private EmojiconTextView MessageText;
        private CircleImageView ProfileImage;
        private RelativeLayout messageLayout;
        private ImageView message_image, iV_file;
        private CircleImageView imageViewPlay;
        private LinearLayout linearLayoutPlay;
        private SeekBar seekBar;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            MessageText = itemView.findViewById(R.id.message_text);
            ProfileImage = itemView.findViewById(R.id.message_profile_image);
            timeTv = itemView.findViewById(R.id.timeTv);
            isSeenTv = itemView.findViewById(R.id.isSeenTv);
            messageLayout = itemView.findViewById(R.id.messageLayout);
            message_image = itemView.findViewById(R.id.message_image);
            iV_file = itemView.findViewById(R.id.iV_file);
            imageViewPlay = itemView.findViewById(R.id.imageViewPlay);
            linearLayoutPlay = itemView.findViewById(R.id.linearLayoutPlay);
            seekBar = itemView.findViewById(R.id.seekBar);
        }
    }


    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if (i == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right, viewGroup, false);
            return new MessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left, viewGroup, false);
            return new MessageViewHolder(view);
        }

    }


    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, final int i) {
        String message = userMessagesList.get(i).getMessage();
        String timeStamp = userMessagesList.get(i).getTimeStamp();
        String type = userMessagesList.get(i).getType();
        String nameFile = userMessagesList.get(i).getNameFile();

        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(timeStamp));
        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        if (type.equals("image")) {
            messageViewHolder.iV_file.setVisibility(View.GONE);
            messageViewHolder.MessageText.setVisibility(View.GONE);
            messageViewHolder.message_image.setVisibility(View.VISIBLE);
            messageViewHolder.linearLayoutPlay.setVisibility(View.GONE);
            try {
                Glide.with(context)
                        .load(message)
                        .placeholder(R.drawable.image_iv)
                        .apply(new RequestOptions()
                                .transform(new RoundedCorners(50)))
                        .into(messageViewHolder.message_image);
            } catch (Exception e) {
                messageViewHolder.message_image.setImageResource(R.drawable.image_iv);
            }
        } else if (type.equals("text")) {
            messageViewHolder.iV_file.setVisibility(View.GONE);
            messageViewHolder.MessageText.setVisibility(View.VISIBLE);
            messageViewHolder.message_image.setVisibility(View.GONE);
            messageViewHolder.linearLayoutPlay.setVisibility(View.GONE);
            messageViewHolder.MessageText.setText(message);
        } else if (type.equals("file")) {
            messageViewHolder.MessageText.setVisibility(View.VISIBLE);
            messageViewHolder.MessageText.setText(nameFile);
            messageViewHolder.iV_file.setVisibility(View.VISIBLE);
            messageViewHolder.message_image.setVisibility(View.GONE);
            messageViewHolder.linearLayoutPlay.setVisibility(View.GONE);
        } else if (type.equals("image_gif")) {
            messageViewHolder.iV_file.setVisibility(View.GONE);
            messageViewHolder.MessageText.setVisibility(View.GONE);
            messageViewHolder.linearLayoutPlay.setVisibility(View.GONE);
            messageViewHolder.message_image.setVisibility(View.VISIBLE);
            try {
                Glide.with(context)
                        .asGif()
                        .load(message)
                        .apply(new RequestOptions()
                                .transform(new RoundedCorners(50)))
                        .into(messageViewHolder.message_image);
            } catch (Exception e) {
                messageViewHolder.message_image.setImageResource(R.drawable.image_iv);
            }
        } else if (type.equals("audio")) {
            messageViewHolder.iV_file.setVisibility(View.GONE);
            messageViewHolder.MessageText.setVisibility(View.GONE);
            messageViewHolder.message_image.setVisibility(View.GONE);
            messageViewHolder.linearLayoutPlay.setVisibility(View.VISIBLE);

            messageViewHolder.imageViewPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!isPlaying && message != null) {
                        isPlaying = true;
                        startPlaying();
                    } else {
                        isPlaying = false;
                        stopPlaying();
                    }
                }

                private void stopPlaying() {
                    try {
                        mPlayer.release();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mPlayer = null;
                    //showing the play button
                    messageViewHolder.imageViewPlay.setImageResource(R.drawable.play_circle);
                }

                private void startPlaying() {
                    mPlayer = null;
                    mPlayer = new MediaPlayer();
                    try {
                        mPlayer.setDataSource(message);
                        mPlayer.prepare();
                        mPlayer.start();
                    } catch (IOException e) {
                        Log.e("LOG_TAG", "prepare() failed");
                    }
                    //making the imageview pause button
                    messageViewHolder.imageViewPlay.setImageResource(R.drawable.pause_circle);

                    messageViewHolder.seekBar.setProgress(lastProgress);
                    mPlayer.seekTo(lastProgress);
                    messageViewHolder.seekBar.setMax(mPlayer.getDuration());
                    seekUpdation();


                    mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            messageViewHolder.imageViewPlay.setImageResource(R.drawable.play_circle);
                            mPlayer.reset();
                            isPlaying = false;
                        }
                    });


                    messageViewHolder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            if (mPlayer != null && fromUser) {
                                mPlayer.seekTo(progress);
                                messageViewHolder.seekBar.setProgress(progress);
                            }
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {

                        }
                    });
                }

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        seekUpdation();
                    }
                };

                private void seekUpdation() {
                    if (mPlayer != null) {
                        int mCurrentPosition = mPlayer.getCurrentPosition();
                        messageViewHolder.seekBar.setProgress(mCurrentPosition);
                        lastProgress = mCurrentPosition;
                    }
                    mHandler.postDelayed(runnable, 1000);
                }
            });
        }

            messageViewHolder.timeTv.setText(dateTime);
            try {
                Glide.with(context).load(imageUrl).placeholder(R.drawable.user_profile).into(messageViewHolder.ProfileImage);
            } catch (Exception e) {

            }
            // click to mess image
            messageViewHolder.message_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            //click to mess text
            messageViewHolder.MessageText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (messageViewHolder.timeTv.getVisibility() == View.GONE) {
                        messageViewHolder.timeTv.setVisibility(View.VISIBLE);
                    } else {
                        messageViewHolder.timeTv.setVisibility(View.GONE);
                    }
                }
            });

            // delete dialog
            messageViewHolder.MessageText.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Are you sure?")
                            .setContentText("You won't be able to recover this message!")
                            .setConfirmText("Delete!")
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    deleteMessage(i);
                                    sweetAlertDialog.dismiss();
                                }
                            })
                            .setCancelButton("Cancel", new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    sweetAlertDialog.dismiss();
                                }
                            })
                            .show();
                    return false;
                }
            });

            if (i == userMessagesList.size() - 1) {
                if (userMessagesList.get(i).isSeen()) {
                    messageViewHolder.isSeenTv.setText("Seen");
                } else {
                    messageViewHolder.isSeenTv.setText("Delivered");
                }
            } else {
                messageViewHolder.isSeenTv.setVisibility(View.GONE);
            }

    }

    private void deleteMessage(int i) {
        final String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String msgTimeStamp = userMessagesList.get(i).getTimeStamp();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        Query query = dbRef.orderByChild("timeStamp").equalTo(msgTimeStamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.child("from").getValue().equals(myUID)) {
                        //remove from chats
                        ds.getRef().removeValue();
                        Toasty.success(context, "Message deleted!", Toast.LENGTH_SHORT, true).show();
                    } else {
                        Toasty.error(context, "You can delete only your messages!", Toast.LENGTH_SHORT, true).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (userMessagesList.get(position).getFrom().equals(firebaseUser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }
}
