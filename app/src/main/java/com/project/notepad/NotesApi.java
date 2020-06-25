package com.project.notepad;

import com.project.notepad.Model.RemoteNote;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface NotesApi {

    @FormUrlEncoded
    @Headers(value = {"charset:utf-8"})
    @POST(" ")
    Call<ResponseBody> saveNote(@Field(value = "notes") RemoteNote notes);

    @GET()
    Call<String> getAllNotes(@Path(value = "email") String email);
}
