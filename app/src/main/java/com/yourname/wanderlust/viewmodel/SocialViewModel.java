package com.yourname.wanderlust.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.auth.FirebaseUser;
import com.yourname.wanderlust.data.api.FirebaseRepository;
import com.yourname.wanderlust.data.api.TripRepository;
import com.yourname.wanderlust.data.model.FirebasePost;
import java.util.List;

public class SocialViewModel extends AndroidViewModel {

    private final FirebaseRepository firebaseRepo = new FirebaseRepository();
    private final TripRepository tripRepo;
    private final MutableLiveData<List<FirebasePost>> posts = new MutableLiveData<>();
    private final MutableLiveData<Boolean> posting = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<List<String>> searchedLocations =
            new MutableLiveData<>();

    public SocialViewModel(@NonNull Application application) {
        super(application);
        tripRepo = new TripRepository();
        loadPosts();
    }

    public LiveData<List<FirebasePost>> getPosts() { return posts; }
    public LiveData<Boolean> getPosting() { return posting; }
    public LiveData<String> getError() { return error; }


    public FirebaseUser getCurrentUser() {
        return firebaseRepo.getCurrentUser();
    }
    public LiveData<List<String>> getSearchedLocations() {
        return searchedLocations;
    }

    public void loadSearchedLocations() {
        firebaseRepo.getSearchedLocations(locations ->
                searchedLocations.postValue(locations));
    }

    private void loadPosts() {
        firebaseRepo.listenToPosts(new FirebaseRepository.PostsCallback() {
            @Override
            public void onSuccess(List<FirebasePost> list) {
                posts.postValue(list);
            }
            @Override
            public void onError(String err) {
                error.postValue(err);
            }
        });
    }

    public void addPost(String location, String content, float rating) {
        FirebaseUser user = getCurrentUser();
        if (user == null) {
            Log.e("SOCIAL", "משתמש לא מחובר!");
            return;
        }

        Log.d("SOCIAL", "מתחיל פרסום: " + location + " | " + content);
        posting.setValue(true);

        tripRepo.analyzeSentiment(content, new TripRepository.SentimentCallback() {
            @Override
            public void onSuccess(String sentiment) {
                Log.d("SOCIAL", "סנטימנט: " + sentiment);

                String username = user.getDisplayName() != null ?
                        user.getDisplayName() : user.getEmail();

                FirebasePost post = new FirebasePost(
                        user.getUid(), username,
                        location, content, sentiment, rating);

                firebaseRepo.addPost(post,
                        new FirebaseRepository.ActionCallback() {
                            @Override
                            public void onSuccess() {
                                Log.d("SOCIAL", "פוסט נשמר בהצלחה!");
                                posting.postValue(false);
                            }
                            @Override
                            public void onError(String err) {
                                Log.e("SOCIAL", "שגיאה בשמירה: " + err);
                                posting.postValue(false);
                            }
                        });
            }

            @Override
            public void onError(String err) {
                Log.e("SOCIAL", "שגיאת סנטימנט: " + err);
                posting.postValue(false);
            }
        });
    }

    public void toggleLike(String postId, boolean isLike) {
        firebaseRepo.toggleLike(postId, isLike,
                new FirebaseRepository.ActionCallback() {
                    @Override public void onSuccess() {}
                    @Override public void onError(String err) {
                        error.postValue(err);
                    }
                });
    }
}