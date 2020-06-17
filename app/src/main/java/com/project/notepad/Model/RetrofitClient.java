package com.project.notepad.Model;

import com.project.notepad.NotesApi;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "http://192.168.43.29:8080/NoteBackup/webapi/notes/";
    private static Retrofit mRetrofit;
    private static RetrofitClient mRetrofitClient;
    private RetrofitClient(){
        mRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static synchronized RetrofitClient  getInstance(){
        if (mRetrofitClient == null) {
            mRetrofitClient = new RetrofitClient();
        }
        return mRetrofitClient;
    }

    public NotesApi getNotesApi(){
        return mRetrofit.create(NotesApi.class);
    }
}
