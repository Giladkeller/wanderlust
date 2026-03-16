package com.yourname.wanderlust.data.api;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.yourname.wanderlust.data.model.GroqResponse;
import com.yourname.wanderlust.data.model.RouteRequest;
import com.yourname.wanderlust.data.model.TripResult;
import com.yourname.wanderlust.viewmodel.DetailViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Response;

public class TripRepository {

    public interface TripCallback {
        void onSuccess(List<TripResult> results, String summary,
                       String flightTip, String hotelTip);
        void onError(String error);
    }

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public void fetchRoutes(RouteRequest request, TripCallback callback) {
        executor.execute(() -> {
            try {
                // בנה הודעה
                List<Map<String, String>> messages = new ArrayList<>();
                Map<String, String> userMessage = new HashMap<>();
                userMessage.put("role", "user");
                userMessage.put("content", request.toPrompt());
                messages.add(userMessage);

                // בנה body
                Map<String, Object> body = new HashMap<>();
                body.put("model", "llama-3.3-70b-versatile");
                body.put("messages", messages);
                body.put("max_tokens", 1024);
                body.put("temperature", 0.7);

                // שלח בקשה
                Call<GroqResponse> call = ApiClient.getInstance().sendMessage(body);
                Response<GroqResponse> response = call.execute();

                if (response.isSuccessful() && response.body() != null) {
                    String text = response.body().getText();
                    parseAndReturn(text, callback);
                } else {
                    String err = response.errorBody() != null ?
                            response.errorBody().string() : "שגיאה לא ידועה";
                    mainHandler.post(() -> callback.onError("שגיאת שרת: " + err));
                }

            } catch (Exception e) {
                Log.e("TripRepository", "Error: " + e.getMessage());
                mainHandler.post(() -> callback.onError("שגיאה: " + e.getMessage()));
            }
        });
    }

    private void parseAndReturn(String jsonText, TripCallback callback) {
        try {
            String clean = jsonText
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            Gson gson = new Gson();
            JsonObject obj = gson.fromJson(clean, JsonObject.class);

            List<TripResult> results = new ArrayList<>();
            obj.getAsJsonArray("routes").forEach(el -> {
                TripResult r = gson.fromJson(el, TripResult.class);
                results.add(r);
            });

            String summary    = obj.has("summary")    ? obj.get("summary").getAsString()    : "";
            String flightTip  = obj.has("flightTip")  ? obj.get("flightTip").getAsString()  : "";
            String hotelTip   = obj.has("hotelTip")   ? obj.get("hotelTip").getAsString()   : "";

            mainHandler.post(() -> callback.onSuccess(results, summary, flightTip, hotelTip));

        } catch (Exception e) {
            Log.e("TripRepository", "Parse error: " + e.getMessage());
            mainHandler.post(() -> callback.onError("שגיאה בניתוח התשובה"));
        }
    }

    public interface DetailsCallback {
        void onSuccess(DetailViewModel.RouteDetails details);
        void onError(String error);
    }

    public void fetchRouteDetails(String title, String destination, DetailsCallback callback) {
        executor.execute(() -> {
            try {
                String prompt = "אתה מומחה טיולים. תן מידע מפורט על המסלול: " + title +
                        " ביעד: " + destination + "\n\n" +
                        "החזר JSON בלבד בפורמט:\n" +
                        "{\n" +
                        "  \"prices\": \"תיאור תעריפים ועלויות כולל כניסות, אוכל, תחבורה\",\n" +
                        "  \"tips\": \"טיפים חשובים למבקרים\",\n" +
                        "  \"bestTime\": \"מתי הכי כדאי לבקר\",\n" +
                        "  \"reviews\": \"סיכום ביקורות וחוויות של מבקרים\"\n" +
                        "}";

                List<Map<String, String>> messages = new ArrayList<>();
                Map<String, String> userMessage = new HashMap<>();
                userMessage.put("role", "user");
                userMessage.put("content", prompt);
                messages.add(userMessage);

                Map<String, Object> body = new HashMap<>();
                body.put("model", "llama-3.3-70b-versatile");
                body.put("messages", messages);
                body.put("max_tokens", 1024);

                Call<GroqResponse> call = ApiClient.getInstance().sendMessage(body);
                Response<GroqResponse> response = call.execute();

                if (response.isSuccessful() && response.body() != null) {
                    String text = response.body().getText();
                    String clean = text.replaceAll("```json", "").replaceAll("```", "").trim();

                    Gson gson = new Gson();
                    JsonObject obj = gson.fromJson(clean, JsonObject.class);

                    DetailViewModel.RouteDetails d = new DetailViewModel.RouteDetails();
                    d.prices   = obj.has("prices")   ? obj.get("prices").getAsString()   : "";
                    d.tips     = obj.has("tips")      ? obj.get("tips").getAsString()     : "";
                    d.bestTime = obj.has("bestTime")  ? obj.get("bestTime").getAsString() : "";
                    d.reviews  = obj.has("reviews")   ? obj.get("reviews").getAsString()  : "";

                    mainHandler.post(() -> callback.onSuccess(d));
                } else {
                    mainHandler.post(() -> callback.onError("שגיאת שרת"));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("שגיאה: " + e.getMessage()));
            }
        });
    }
}