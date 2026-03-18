package com.yourname.wanderlust.data.api;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.yourname.wanderlust.data.model.FirebasePost;
import java.util.ArrayList;
import java.util.List;

public class FirebaseRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth    = FirebaseAuth.getInstance();
    private static final String POSTS  = "posts";

    public interface PostsCallback {
        void onSuccess(List<FirebasePost> posts);
        void onError(String error);
    }

    public interface ActionCallback {
        void onSuccess();
        void onError(String error);
    }

    // שמור מיקום שחיפשנו
    // שמור מיקום שחיפשנו
    public void saveSearchedLocation(String location, ActionCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        db.collection("users").document(user.getUid())
                .update("searchedLocations",
                        FieldValue.arrayUnion(location))
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e ->
                        // אם הדוקומנט לא קיים — צור אותו
                        db.collection("users").document(user.getUid())
                                .set(new java.util.HashMap<String, Object>() {{
                                    put("searchedLocations",
                                            new ArrayList<String>() {{
                                                add(location);
                                            }});
                                }})
                                .addOnSuccessListener(v -> callback.onSuccess())
                                .addOnFailureListener(err ->
                                        callback.onError(err.getMessage())));
    }

    // קבל מיקומים
    public void getSearchedLocations(
            java.util.function.Consumer<List<String>> callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.accept(new ArrayList<>());
            return;
        }

        db.collection("users").document(user.getUid())
                .addSnapshotListener((doc, e) -> {
                    if (e != null || doc == null) {
                        callback.accept(new ArrayList<>());
                        return;
                    }
                    List<String> locations = (List<String>)
                            doc.get("searchedLocations");
                    callback.accept(locations != null ?
                            locations : new ArrayList<>());
                });
    }

    // פרסם פוסט
    public void addPost(FirebasePost post, ActionCallback callback) {
        db.collection(POSTS)
                .add(post)
                .addOnSuccessListener(ref -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // קבל כל הפוסטים
    public void getPosts(PostsCallback callback) {
        db.collection(POSTS)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<FirebasePost> posts = new ArrayList<>();
                    snapshot.forEach(doc -> {
                        FirebasePost post = doc.toObject(FirebasePost.class);
                        post.id = doc.getId();
                        posts.add(post);
                    });
                    callback.onSuccess(posts);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // האזן לפוסטים בזמן אמת
    public void listenToPosts(PostsCallback callback) {
        db.collection(POSTS)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null || snapshot == null) return;
                    List<FirebasePost> posts = new ArrayList<>();
                    snapshot.forEach(doc -> {
                        FirebasePost post = doc.toObject(FirebasePost.class);
                        post.id = doc.getId();
                        posts.add(post);
                    });
                    callback.onSuccess(posts);
                });
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public void toggleLike(String postId, boolean isLike, ActionCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();
        String addField    = isLike ? "likedBy"    : "dislikedBy";
        String removeField = isLike ? "dislikedBy" : "likedBy";
        String addCount    = isLike ? "likes"       : "dislikes";
        String removeCount = isLike ? "dislikes"    : "likes";

        db.collection(POSTS).document(postId).get()
                .addOnSuccessListener(doc -> {
                    List<String> addList = (List<String>) doc.get(addField);
                    List<String> removeList = (List<String>) doc.get(removeField);

                    boolean alreadyDid = addList != null && addList.contains(uid);

                    java.util.Map<String, Object> updates = new java.util.HashMap<>();

                    if (alreadyDid) {
                        // בטל
                        updates.put(addField, FieldValue.arrayRemove(uid));
                        updates.put(addCount, FieldValue.increment(-1));
                    } else {
                        // הוסף
                        updates.put(addField, FieldValue.arrayUnion(uid));
                        updates.put(addCount, FieldValue.increment(1));

                        // הסר מהצד השני אם קיים
                        if (removeList != null && removeList.contains(uid)) {
                            updates.put(removeField, FieldValue.arrayRemove(uid));
                            updates.put(removeCount, FieldValue.increment(-1));
                        }
                    }

                    db.collection(POSTS).document(postId)
                            .update(updates)
                            .addOnSuccessListener(v -> callback.onSuccess())
                            .addOnFailureListener(e -> callback.onError(e.getMessage()));
                });
    }
}