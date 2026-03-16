package com.yourname.wanderlust.ui.results;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.yourname.wanderlust.databinding.FragmentDetailBinding;
import com.yourname.wanderlust.viewmodel.DetailViewModel;

public class DetailFragment extends Fragment {

    private FragmentDetailBinding binding;
    private DetailViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(DetailViewModel.class);

        Bundle args = getArguments();
        if (args == null) return;

        String title       = args.getString("title", "");
        String description = args.getString("description", "");
        String duration    = args.getString("duration", "");
        String difficulty  = args.getString("difficulty", "");
        String rating      = args.getString("rating", "");
        String destination = args.getString("destination", "");

        setupUI(title, description, duration, difficulty, rating, destination);
        setupButtons(title, destination);
        loadAiDetails(title, destination);
    }

    private void setupUI(String title, String description,
                         String duration, String difficulty,
                         String rating, String destination) {
        binding.toolbar.setTitle(title);
        binding.toolbar.setNavigationOnClickListener(v ->
                requireActivity().onBackPressed());

        binding.tvTitle.setText(title);
        binding.tvDescription.setText(description);
        binding.tvDuration.setText("⏱  " + duration);
        binding.tvDifficulty.setText("🥾  " + difficulty);
        binding.tvRating.setText("★  " + rating);
        binding.tvDestination.setText("📍  " + destination);
    }

    private void setupButtons(String title, String destination) {

        // ניווט ל-Google Maps
        binding.btnGoogleMaps.setOnClickListener(v -> {
            Uri uri = Uri.parse("geo:0,0?q=" + Uri.encode(destination + " " + title));
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setPackage("com.google.android.apps.maps");
            if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivity(intent);
            } else {
                // אם אין Google Maps — פתח בדפדפן
                Uri webUri = Uri.parse("https://maps.google.com/?q=" +
                        Uri.encode(destination + " " + title));
                startActivity(new Intent(Intent.ACTION_VIEW, webUri));
            }
        });

        // ניווט ל-Waze
        binding.btnWaze.setOnClickListener(v -> {
            Uri uri = Uri.parse("waze://?q=" + Uri.encode(destination + " " + title));
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Uri webUri = Uri.parse("https://waze.com/ul?q=" +
                        Uri.encode(destination + " " + title));
                startActivity(new Intent(Intent.ACTION_VIEW, webUri));
            }
        });

        // שיתוף
        binding.btnShare.setOnClickListener(v -> {
            String shareText = "בדוק את המסלול הזה: " + title +
                    "\nב-" + destination +
                    "\nנמצא דרך Wanderlust 🌍";
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, shareText);
            startActivity(Intent.createChooser(intent, "שתף מסלול"));
        });
    }

    private void loadAiDetails(String title, String destination) {
        binding.layoutAiLoading.setVisibility(View.VISIBLE);
        binding.layoutAiContent.setVisibility(View.GONE);

        viewModel.fetchDetails(title, destination);

        viewModel.getDetails().observe(getViewLifecycleOwner(), details -> {
            binding.layoutAiLoading.setVisibility(View.GONE);
            binding.layoutAiContent.setVisibility(View.VISIBLE);
            binding.tvPrices.setText(details.prices);
            binding.tvTips.setText(details.tips);
            binding.tvBestTime.setText(details.bestTime);
            binding.tvReviews.setText(details.reviews);
        });

        viewModel.getError().observe(getViewLifecycleOwner(), err -> {
            binding.layoutAiLoading.setVisibility(View.GONE);
            binding.tvAiError.setVisibility(View.VISIBLE);
            binding.tvAiError.setText(err);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}