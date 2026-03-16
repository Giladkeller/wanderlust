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
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.yourname.wanderlust.R;
import com.yourname.wanderlust.databinding.FragmentResultsBinding;
import com.yourname.wanderlust.data.model.RouteRequest;
import com.yourname.wanderlust.viewmodel.SearchViewModel;
import java.util.ArrayList;


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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // כפתור חזרה
        binding.toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(requireView()).navigateUp());

        setupRecycler();
        setupViewModel();
        fetchData();
    }

    private void setupRecycler() {
        // קודם צור את ה-adapter
        adapter = new TripResultAdapter();

        // אחר כך הגדר את ה-listener
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

        // ואז חבר ל-RecyclerView
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
            adapter.setItems(results);
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

        RouteRequest request = new RouteRequest(
                args.getString("destination", ""),
                args.getString("duration", ""),
                args.getString("age", ""),
                args.getString("pax", "2"),
                args.getString("dateFrom", ""),
                args.getString("dateTo", ""),
                args.getString("budget", ""),
                args.getString("origin", "תל אביב"),
                args.getStringArrayList("tripTypes") != null ?
                        args.getStringArrayList("tripTypes") : new ArrayList<>(),
                args.getStringArrayList("prefs") != null ?
                        args.getStringArrayList("prefs") : new ArrayList<>()
        );

        viewModel.fetchRoutes(request);
        setupExternalLinks(args.getString("destination", ""),
                args.getString("origin", "תל אביב"),
                args.getString("dateFrom", ""),
                args.getString("dateTo", ""));
    }

    private void setupExternalLinks(String destination, String origin,
                                    String dateFrom, String dateTo) {
        // Skyscanner
        binding.btnSkyscanner.setOnClickListener(v -> {
            String url = "https://www.skyscanner.co.il/transport/flights/" +
                    Uri.encode(origin) + "/" +
                    Uri.encode(destination) + "/";
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        });

        // Booking
        binding.btnBooking.setOnClickListener(v -> {
            String url = "https://www.booking.com/searchresults.he.html?ss=" +
                    Uri.encode(destination);
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}