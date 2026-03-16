package com.yourname.wanderlust.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "posts")
public class Post {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String username;
    public String location;   // שם המקום — ישמש לסינון
    public String content;
    public String sentiment;  // "positive" / "negative" / "neutral"
    public long timestamp;

    public Post(String username, String location, String content,
                String sentiment, long timestamp) {
        this.username  = username;
        this.location  = location;
        this.content   = content;
        this.sentiment = sentiment;
        this.timestamp = timestamp;
    }
}