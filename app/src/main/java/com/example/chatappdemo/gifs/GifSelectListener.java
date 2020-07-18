package com.example.chatappdemo.gifs;

import androidx.annotation.NonNull;


public interface GifSelectListener {
    void onGifSelected(@NonNull Gif gif);

    void onBackSpace();
}
