package com.yourname.wanderlust.data.api;

import com.yourname.wanderlust.data.model.GroqResponse;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface GroqApi {

    @POST("chat/completions")
    Call<GroqResponse> sendMessage(@Body Map<String, Object> body);
}