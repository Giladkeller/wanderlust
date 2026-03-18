package com.yourname.wanderlust.data.model;

import com.google.firebase.firestore.DocumentId;
import java.util.List;

public class FirebasePost {

    @DocumentId
    public String id;
    public String userId;
    public String username;
    public String location;
    public String content;
    public String sentiment;
    public float rating;        // 1-5 כוכבים
    public long timestamp;
    public int likes;
    public int dislikes;
    public List<String> likedBy;
    public List<String> dislikedBy;
    public List<String> searchedLocations; // מקומות שחיפש

    public FirebasePost() {} // נדרש ל-Firestore

    public FirebasePost(String userId, String username, String location,
                        String content, String sentiment, float rating) {
        this.userId    = userId;
        this.username  = username;
        this.location  = location;
        this.content   = content;
        this.sentiment = sentiment;
        this.rating    = rating;
        this.timestamp = System.currentTimeMillis();
        this.likes     = 0;
        this.dislikes  = 0;
    }
}