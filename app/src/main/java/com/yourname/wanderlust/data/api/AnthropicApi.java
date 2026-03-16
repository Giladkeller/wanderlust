package com.yourname.wanderlust.data.api;

import com.yourname.wanderlust.data.model.GroqResponse;

import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface AnthropicApi {

    @Headers({
            "anthropic-version: 2023-06-01",
            "content-type: application/json"
    })
    @POST("v1/messages")
    Call<GroqResponse> sendMessage(@Body Map<String, Object> body);
}