package com.yourname.wanderlust.ui.social;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.yourname.wanderlust.R;
import com.yourname.wanderlust.databinding.FragmentSocialBinding;
import com.yourname.wanderlust.ui.auth.AuthActivity;
import com.yourname.wanderlust.viewmodel.SocialViewModel;

import java.util.ArrayList;
import java.util.List;

public class SocialFragment extends Fragment {

    private FragmentSocialBinding binding;
    private SocialViewModel viewModel;
    private PostAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSocialBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(SocialViewModel.class);

        // בדוק אם מחובר
        FirebaseUser user = viewModel.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(requireContext(), AuthActivity.class));
            return;
        }

        setupToolbar();
        setupRecycler(user.getUid());
        setupRatingBar();
        setupSearchedLocations();
        setupPostButton();
        observePosts();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding == null) return;
        // רענן מיקומים בכל כניסה לטאב
        viewModel.loadSearchedLocations();
    }

    // ─── Toolbar ───────────────────────────────────────────────

    private void setupToolbar() {
        binding.toolbarSocial.inflateMenu(R.menu.social_menu);

        FirebaseUser user = viewModel.getCurrentUser();
        if (user != null) {
            String name = user.getDisplayName() != null ?
                    user.getDisplayName() : user.getEmail();
            binding.toolbarSocial.setSubtitle(name);
            binding.toolbarSocial.setSubtitleTextColor(
                    android.graphics.Color.parseColor("#1D9E75"));
        }

        binding.toolbarSocial.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == R.id.action_logout) {
                logout();
                return true;
            } else if (id == R.id.action_switch_user) {
                logout();
                return true;
            }
            return false;
        });
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(requireContext(), AuthActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_NEW_TASK   |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    // ─── RecyclerView ──────────────────────────────────────────

    private void setupRecycler(String uid) {
        adapter = new PostAdapter();
        adapter.setCurrentUserId(uid);
        adapter.setOnPostActionListener((postId, isLike) ->
                viewModel.toggleLike(postId, isLike));

        binding.recyclerPosts.setLayoutManager(
                new LinearLayoutManager(requireContext()));
        binding.recyclerPosts.setAdapter(adapter);
    }

    // ─── Rating Bar ────────────────────────────────────────────

    private void setupRatingBar() {
        binding.ratingBar.setIsIndicator(false);
        binding.ratingBar.setRating(0);
        binding.ratingBar.setOnRatingBarChangeListener((bar, rating, fromUser) -> {
            if (!fromUser) return;
            int stars = (int) rating;
            String[] labels = {
                    "בחר דירוג",
                    "גרוע 😞",
                    "סביר 😐",
                    "טוב 🙂",
                    "מצוין 😊",
                    "מושלם 🤩"
            };
            binding.tvRatingValue.setText(
                    stars > 0 ? stars + "★ " + labels[stars] : labels[0]);
        });
    }

    // ─── מיקומים שחיפשת ────────────────────────────────────────

    private void setupSearchedLocations() {
        // placeholder בזמן טעינה
        List<String> placeholder = new ArrayList<>();
        placeholder.add("טוען מיקומים...");
        binding.spinnerLocation.setAdapter(new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                placeholder));

        viewModel.loadSearchedLocations();

        viewModel.getSearchedLocations().observe(
                getViewLifecycleOwner(), locations -> {
                    if (binding == null) return;

                    List<String> list = (locations != null && !locations.isEmpty())
                            ? new ArrayList<>(locations)
                            : new ArrayList<>();

                    if (list.isEmpty()) list.add("חפש מסלול קודם");

                    binding.spinnerLocation.setAdapter(new ArrayAdapter<>(
                            requireContext(),
                            android.R.layout.simple_dropdown_item_1line,
                            list));
                    binding.spinnerLocation.setText(list.get(0), false);
                });
    }

    // ─── כפתור פרסום ──────────────────────────────────────────

    private void setupPostButton() {

        // פתח טופס
        binding.btnShowPost.setOnClickListener(v -> {
            binding.layoutPostForm.setVisibility(View.VISIBLE);
            binding.btnShowPost.setVisibility(View.GONE);
        });

        // ביטול
        binding.btnCancel.setOnClickListener(v -> closeForm());

        // פרסם
        binding.btnPost.setOnClickListener(v -> {
            String location = binding.spinnerLocation
                    .getText().toString().trim();
            String content  = binding.editPostContent
                    .getText().toString().trim();
            float  rating   = binding.ratingBar.getRating();

            if (location.isEmpty() ||
                    location.equals("חפש מסלול קודם") ||
                    location.equals("טוען מיקומים...")) {
                Toast.makeText(requireContext(),
                        "חפש מסלול קודם כדי לפרסם עליו",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (content.isEmpty()) {
                binding.editPostContent.setError("נא לכתוב משהו");
                return;
            }
            if (rating == 0) {
                Toast.makeText(requireContext(),
                        "נא לדרג את המקום",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            binding.tvAnalyzing.setVisibility(View.VISIBLE);
            binding.btnPost.setEnabled(false);
            binding.btnCancel.setEnabled(false);

            viewModel.addPost(location, content, rating);
        });
    }

    private void closeForm() {
        if (binding == null) return;
        binding.layoutPostForm.setVisibility(View.GONE);
        binding.btnShowPost.setVisibility(View.VISIBLE);
        binding.editPostContent.setText("");
        binding.ratingBar.setRating(0);
        binding.tvRatingValue.setText("בחר דירוג");
        binding.tvAnalyzing.setVisibility(View.GONE);
        binding.btnPost.setEnabled(true);
        binding.btnCancel.setEnabled(true);
    }

    // ─── Observers ─────────────────────────────────────────────

    private void observePosts() {
        // פוסטים
        viewModel.getPosts().observe(getViewLifecycleOwner(), posts -> {
            if (posts != null) adapter.setItems(posts);
        });

        // סיום פרסום
        viewModel.getPosting().observe(getViewLifecycleOwner(), isPosting -> {
            if (Boolean.FALSE.equals(isPosting) && binding != null) {
                closeForm();
                Toast.makeText(requireContext(),
                        "הפוסט פורסם ✓", Toast.LENGTH_SHORT).show();
            }
        });

        // שגיאות
        viewModel.getError().observe(getViewLifecycleOwner(), err -> {
            if (err != null && binding != null) {
                Toast.makeText(requireContext(),
                        err, Toast.LENGTH_LONG).show();
            }
        });
    }

    // ─── Lifecycle ─────────────────────────────────────────────

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}