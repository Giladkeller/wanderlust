package com.yourname.wanderlust.ui.results;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.yourname.wanderlust.R;
import com.yourname.wanderlust.data.api.FirebaseRepository;
import com.yourname.wanderlust.data.model.RouteRequest;
import com.yourname.wanderlust.databinding.FragmentResultsBinding;
import com.yourname.wanderlust.viewmodel.SearchViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ResultsFragment extends Fragment {

    private FragmentResultsBinding binding;
    private SearchViewModel viewModel;
    private TripResultAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentResultsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(requireView()).navigateUp());

        setupRecycler();
        setupViewModel();
        fetchData();
    }

    private void setupRecycler() {
        adapter = new TripResultAdapter();

        adapter.setOnItemClickListener(item -> {
            Bundle args = new Bundle();
            args.putString("title",       item.title);
            args.putString("description", item.description);
            args.putString("duration",    item.duration);
            args.putString("difficulty",  item.difficulty);
            args.putString("rating",      item.rating);
            args.putString("destination",
                    getArguments() != null ?
                            getArguments().getString("destination", "") : "");

            Navigation.findNavController(requireView())
                    .navigate(R.id.detailFragment, args);
        });

        binding.recyclerResults.setLayoutManager(
                new LinearLayoutManager(requireContext()));
        binding.recyclerResults.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);

        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.layoutLoading.setVisibility(
                    isLoading ? View.VISIBLE : View.GONE);
            binding.recyclerResults.setVisibility(
                    isLoading ? View.GONE : View.VISIBLE);
        });

        viewModel.getResults().observe(getViewLifecycleOwner(), results -> {
            if (results != null) adapter.setItems(results);
        });

        viewModel.getSummary().observe(getViewLifecycleOwner(), summary -> {
            if (summary != null && !summary.isEmpty()) {
                binding.tvSummary.setText(summary);
                binding.layoutSummary.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), err -> {
            if (err != null) {
                binding.tvError.setText(err);
                binding.tvError.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getFlightTip().observe(getViewLifecycleOwner(), tip -> {
            if (tip != null && !tip.isEmpty()) {
                binding.tvFlightTip.setText("✈ " + tip);
                binding.layoutFlightHotel.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getHotelTip().observe(getViewLifecycleOwner(), tip -> {
            if (tip != null && !tip.isEmpty()) {
                binding.tvHotelTip.setText("🏨 " + tip);
            }
        });
    }

    private void fetchData() {
        Bundle args = getArguments();
        if (args == null) return;

        boolean isFlightMode = args.getBoolean("isFlightMode", false);
        RouteRequest request;

        if (isFlightMode) {
            request = new RouteRequest(
                    args.getString("destination", ""),
                    args.getString("dateFrom", ""),
                    args.getString("dateTo", ""),
                    args.getString("pax", "2"),
                    args.getString("budget", ""),
                    args.getStringArrayList("luggage") != null ?
                            args.getStringArrayList("luggage") : new ArrayList<>()
            );
        } else {
            request = new RouteRequest(
                    args.getString("area", ""),
                    args.getString("pax", "2"),
                    args.getString("budget", ""),
                    args.getString("difficulty", "קל"),
                    args.getString("dateFrom", ""),
                    args.getString("dateTo", ""),
                    args.getStringArrayList("accommodation") != null ?
                            args.getStringArrayList("accommodation") : new ArrayList<>()
            );
        }

        // שמור מיקום ב-Firestore לפי המשתמש המחובר
        String dest = isFlightMode ?
                args.getString("destination", "") :
                args.getString("area", "");

        if (!dest.isEmpty()) {
            FirebaseRepository firebaseRepo = new FirebaseRepository();
            firebaseRepo.saveSearchedLocation(dest,
                    new FirebaseRepository.ActionCallback() {
                        @Override
                        public void onSuccess() {
                            Log.d("Results", "✓ מיקום נשמר: " + dest);
                        }
                        @Override
                        public void onError(String err) {
                            Log.e("Results", "שגיאה בשמירת מיקום: " + err);
                        }
                    });
        }

        // הגדר כפתורי טיסות רק במצב טיסה
        if (isFlightMode) {
            setupExternalLinks(
                    args.getString("destination", ""),
                    args.getString("origin", "תל אביב"),
                    args.getString("dateFrom", ""),
                    args.getString("dateTo", ""),
                    args.getString("pax", "1")
            );
        } else {
            // הסתר כפתורי טיסות במצב מקומי
            binding.layoutFlightHotel.setVisibility(View.GONE);
        }

        viewModel.fetchRoutes(request);
    }

    private void setupExternalLinks(String destination, String origin,
                                    String dateFrom, String dateTo,
                                    String pax) {
        int adults = 1;
        try { adults = Integer.parseInt(pax); } catch (Exception ignored) {}
        int finalAdults = adults;

        String ymd1 = convertDate(dateFrom, "yyyy-MM-dd");
        String ymd2 = convertDate(dateTo,   "yyyy-MM-dd");

        // הצג פרטי טיסה
        binding.tvFlightDetails.setText(
                "✈ " + origin + " → " + destination +
                        "  |  יציאה: " + dateFrom +
                        "  |  חזרה: " + dateTo +
                        "  |  " + finalAdults + " נוסעים\n" +
                        "הכנס פרטים אלה באתר החיפוש");
        binding.tvFlightDetails.setVisibility(View.VISIBLE);

        // Google Flights
        binding.btnSkyscanner.setText("✈ Google Flights");
        binding.btnSkyscanner.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.parseColor("#4285F4")));
        binding.btnSkyscanner.setOnClickListener(v ->
                openUrl("https://www.google.com/travel/flights?hl=iw"));

        // Booking
        binding.btnBooking.setOnClickListener(v ->
                openUrl("https://www.booking.com/searchresults.he.html" +
                        "?ss=" + Uri.encode(destination) +
                        "&checkin=" + ymd1 +
                        "&checkout=" + ymd2 +
                        "&group_adults=" + finalAdults +
                        "&no_rooms=1"));

        // הוסף Kayak ו-Kiwi רק פעם אחת
        if (binding.layoutFlightHotel
                .findViewWithTag("extraButtons") != null) return;

        binding.layoutFlightHotel.post(() -> {
            if (binding == null) return;

            com.google.android.material.button.MaterialButton btnKayak =
                    new com.google.android.material.button.MaterialButton(
                            requireContext());
            btnKayak.setText("✈ Kayak");
            btnKayak.setTextSize(12);
            btnKayak.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            android.graphics.Color.parseColor("#FF690F")));
            LinearLayout.LayoutParams p1 = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            p1.setMarginEnd(6);
            btnKayak.setLayoutParams(p1);
            btnKayak.setOnClickListener(v ->
                    openUrl("https://www.kayak.co.il"));

            com.google.android.material.button.MaterialButton btnKiwi =
                    new com.google.android.material.button.MaterialButton(
                            requireContext());
            btnKiwi.setText("✈ Kiwi");
            btnKiwi.setTextSize(12);
            btnKiwi.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            android.graphics.Color.parseColor("#00B2A9")));
            LinearLayout.LayoutParams p2 = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            btnKiwi.setLayoutParams(p2);
            btnKiwi.setOnClickListener(v ->
                    openUrl("https://www.kiwi.com"));

            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setTag("extraButtons");
            LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            rp.setMargins(0, 8, 0, 0);
            row.setLayoutParams(rp);
            row.addView(btnKayak);
            row.addView(btnKiwi);
            binding.layoutFlightHotel.addView(row);
        });
    }

    private String convertDate(String date, String toFormat) {
        try {
            if (date == null || date.isEmpty()) return "";
            SimpleDateFormat from = new SimpleDateFormat(
                    "dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat to = new SimpleDateFormat(
                    toFormat, Locale.getDefault());
            Date parsed = from.parse(date);
            if (parsed == null) return "";
            return to.format(parsed);
        } catch (Exception e) {
            Log.e("CONVERT_DATE", "Error: " + e.getMessage());
            return "";
        }
    }

    private void openUrl(String url) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}