package com.example.chatappdemo.adapter;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.chatappdemo.R;
import com.example.chatappdemo.activity.ViewImageChatActivity;
import com.example.chatappdemo.model.GroupChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;

public class AdapterGroupChat extends RecyclerView.Adapter<AdapterGroupChat.HolderGroupChat> {
    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;

    private Context context;
    private ArrayList<GroupChat> modelGroupChatList;

    private FirebaseAuth firebaseAuth;

    private MediaPlayer mPlayer;
    private int lastProgress = 0;
    private Handler mHandler = new Handler();
    private boolean isPlaying = false;

    public AdapterGroupChat(Context context, ArrayList<GroupChat> modelGroupChatList) {
        this.context = context;
        this.modelGroupChatList = modelGroupChatList;

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderGroupChat onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_groupchat_right, parent, false);
            return new HolderGroupChat(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_groupchat_left, parent, false);
            return new HolderGroupChat(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull HolderGroupChat holder, int position) {
        GroupChat model = modelGroupChatList.get(position);
        String timestamp = model.getTimestamp();
        String message = model.getMessage();
        String senderUid = model.getSender();
        String messageType = model.getType();
        String nameFile = model.getNameFile();

        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        if (messageType.equals("text")) {
            holder.messageIv.setVisibility(View.GONE);
            holder.linearLayoutPlay.setVisibility(View.GONE);
            holder.messageTv.setVisibility(View.VISIBLE);
            holder.messageTv.setText(message);
            //click text message
            holder.messageTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.timeTv.getVisibility() == View.GONE) {
                        holder.timeTv.setVisibility(View.VISIBLE);
                    } else {
                        holder.timeTv.setVisibility(View.GONE);
                    }
                }
            });
        } else if (messageType.equals("file")) {
            holder.messageTv.setVisibility(View.VISIBLE);
            holder.messageTv.setText(nameFile);
            holder.linearLayoutPlay.setVisibility(View.GONE);
            holder.iV_file.setVisibility(View.VISIBLE);
            holder.messageIv.setVisibility(View.GONE);
            holder.civ_download_file.setVisibility(View.VISIBLE);
            //click download file
            holder.civ_download_file.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        if (context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                                PackageManager.PERMISSION_DENIED){
                            Toasty.error(context, "Permission Denied!", Toast.LENGTH_SHORT, true).show();
                        }else {
                            startDownloading(message, nameFile);
                        }
                    }else {
                        startDownloading(message, nameFile);
                    }
                }
            });
        } else if (messageType.equals("image")){
            holder.messageIv.setVisibility(View.VISIBLE);
            holder.messageTv.setVisibility(View.GONE);
            holder.linearLayoutPlay.setVisibility(View.GONE);
            try {
                Glide.with(context)
                        .load(message)
                        .placeholder(R.drawable.image_iv)
                        .apply(new RequestOptions()
                                .transform(new RoundedCorners(50)))
                        .into(holder.messageIv);
            } catch (Exception e) {
                holder.messageIv.setImageResource(R.drawable.image_iv);
            }
            //click image
            holder.messageIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, ViewImageChatActivity.class);
                    intent.putExtra("url_img", message);
                    context.startActivity(intent);
                }
            });
        } else if (messageType.equals("image_gif")) {
            holder.messageTv.setVisibility(View.GONE);
            holder.messageIv.setVisibility(View.VISIBLE);
            holder.linearLayoutPlay.setVisibility(View.GONE);
            try {
                Glide.with(context)
                        .asGif()
                        .load(message)
                        .apply(new RequestOptions()
                                .transform(new RoundedCorners(50)))
                        .into(holder.messageIv);
            }catch (Exception e){
                holder.messageIv.setImageResource(R.drawable.image_iv);
            }
        } else if (messageType.equals("audio")) {
            holder.messageTv.setVisibility(View.GONE);
            holder.messageIv.setVisibility(View.GONE);
            holder.linearLayoutPlay.setVisibility(View.VISIBLE);

            holder.imageViewPlay.setOnClickListener(new View.OnClickListener() {
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
                    holder.imageViewPlay.setImageResource(R.drawable.play_circle);
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
                    holder.imageViewPlay.setImageResource(R.drawable.pause_circle);

                    holder.seekBar.setProgress(lastProgress);
                    mPlayer.seekTo(lastProgress);
                    holder.seekBar.setMax(mPlayer.getDuration());
                    seekUpdation();


                    mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            holder.imageViewPlay.setImageResource(R.drawable.play_circle);
                            mPlayer.reset();
                            isPlaying = false;
                        }
                    });


                    holder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            if (mPlayer != null && fromUser) {
                                mPlayer.seekTo(progress);
                                holder.seekBar.setProgress(progress);
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
                        holder.seekBar.setProgress(mCurrentPosition);
                        lastProgress = mCurrentPosition;
                    }
                    mHandler.postDelayed(runnable, 1000);
                }
            });
        }


        holder.timeTv.setText(dateTime);
        setUserName(model, holder);

    }

    private void startDownloading(String url, String nameFile) {
        new SweetAlertDialog(context, SweetAlertDialog.NORMAL_TYPE)
                .setTitleText("Downloading!")
                .setConfirmText("OK!")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                    }
                })
                .show();
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setTitle(nameFile);
        request.setDescription("Downloading file...");
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, ""+ System.currentTimeMillis());

        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }

    private void setUserName(GroupChat model, HolderGroupChat holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(model.getSender())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String name = "" + ds.child("name").getValue();
                            String imgDD = "" + ds.child("imgAnhDD").getValue();

                            holder.nameTv.setText(name);
                            try {
                                Glide.with(context)
                                        .load(imgDD)
                                        .placeholder(R.drawable.user_profile)
                                        .apply(new RequestOptions()
                                                .transform(new RoundedCorners(50))
                                                .error(R.drawable.image_iv)
                                                .skipMemoryCache(true)
                                                .diskCacheStrategy(DiskCacheStrategy.NONE))
                                        .into(holder.message_profile_image);
                            }catch (Exception e){

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return modelGroupChatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (modelGroupChatList.get(position).getSender().equals(firebaseAuth.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

    class HolderGroupChat extends RecyclerView.ViewHolder {
        private CircleImageView message_profile_image;
        private TextView nameTv, timeTv;
        private EmojiconTextView messageTv;
        private ImageView messageIv, iV_file;
        private CircleImageView imageViewPlay, civ_download_file;
        private LinearLayout linearLayoutPlay;
        private SeekBar seekBar;

        public HolderGroupChat(@NonNull View itemView) {
            super(itemView);
            message_profile_image = itemView.findViewById(R.id.message_profile_image);
            nameTv = itemView.findViewById(R.id.nameTv);
            messageTv = itemView.findViewById(R.id.messageTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            messageIv = itemView.findViewById(R.id.messageIv);
            iV_file = itemView.findViewById(R.id.iV_file);
            imageViewPlay = itemView.findViewById(R.id.imageViewPlay);
            linearLayoutPlay = itemView.findViewById(R.id.linearLayoutPlay);
            seekBar = itemView.findViewById(R.id.seekBar);
            civ_download_file = itemView.findViewById(R.id.civ_download_file);
        }
    }
}
