package com.yourname.wanderlust.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.yourname.wanderlust.data.model.Post;
import java.util.List;

@Dao
public interface PostDao {

    @Insert
    void insert(Post post);

    @Query("SELECT * FROM posts ORDER BY timestamp DESC")
    LiveData<List<Post>> getAllPosts();

    // סינון לפי מיקום — ישמש במסך הפירוט
    @Query("SELECT * FROM posts WHERE location LIKE '%' || :location || '%' ORDER BY timestamp DESC")
    LiveData<List<Post>> getPostsByLocation(String location);
}