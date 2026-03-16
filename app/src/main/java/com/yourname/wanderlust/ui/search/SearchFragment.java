package com.yourname.wanderlust.ui.search;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.yourname.wanderlust.R;
import com.yourname.wanderlust.databinding.FragmentSearchBinding;
import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupDatePickers();
        setupDurationDropdown();
        setupAgeDropdown();
        setupSearchButton();
    }

    private void setupDatePickers() {
        binding.editDateFrom.setOnClickListener(v -> showDatePicker(true));
        binding.editDateTo.setOnClickListener(v -> showDatePicker(false));
    }

    private void showDatePicker(boolean isFromDate) {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder
                .datePicker()
                .setTitleText(isFromDate ? "תאריך יציאה" : "תאריך חזרה")
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            java.text.SimpleDateFormat sdf =
                    new java.text.SimpleDateFormat("dd/MM/yyyy",
                            java.util.Locale.getDefault());
            String date = sdf.format(new java.util.Date(selection));
            if (isFromDate) {
                binding.editDateFrom.setText(date);
            } else {
                binding.editDateTo.setText(date);
            }
        });

        picker.show(getParentFragmentManager(), "date_picker");
    }

    private void setupDurationDropdown() {
        String[] durations = {"יום אחד", "סוף שבוע", "שבוע", "שבועיים", "חודש+"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                durations);
        binding.spinnerDuration.setAdapter(adapter);
        binding.spinnerDuration.setText(durations[2], false);
    }

    private void setupAgeDropdown() {
        String[] ages = {"ילדים (3-12)", "צעירים (18-35)", "מבוגרים (35-60)", "קשישים (60+)", "מעורב"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                ages);
        binding.spinnerAge.setAdapter(adapter);
        binding.spinnerAge.setText(ages[1], false);
    }

    private void setupSearchButton() {
        binding.btnSearch.setOnClickListener(v -> {

            String destination = binding.editDestination.getText().toString().trim();
            if (destination.isEmpty()) {
                binding.editDestination.setError("נא להזין יעד");
                return;
            }

            String duration  = binding.spinnerDuration.getText().toString();
            String age       = binding.spinnerAge.getText().toString();
            String pax       = binding.editPax.getText().toString();
            String dateFrom  = binding.editDateFrom.getText().toString();
            String dateTo    = binding.editDateTo.getText().toString();
            String budget    = binding.editBudget.getText().toString();
            String origin    = binding.editOrigin.getText().toString();

            List<String> tripTypes = getSelectedChips();
            List<String> prefs     = getSelectedPrefs();

            Bundle args = new Bundle();
            args.putString("destination", destination);
            args.putString("duration",    duration);
            args.putString("age",         age);
            args.putString("pax",         pax);
            args.putString("dateFrom",    dateFrom);
            args.putString("dateTo",      dateTo);
            args.putString("budget",      budget);
            args.putString("origin",      origin);
            args.putStringArrayList("tripTypes", new ArrayList<>(tripTypes));
            args.putStringArrayList("prefs",     new ArrayList<>(prefs));

            Navigation.findNavController(v).navigate(R.id.resultsFragment, args);
        });
    }

    private List<String> getSelectedChips() {
        List<String> selected = new ArrayList<>();
        if (binding.chipHiking.isChecked()) selected.add("הייקינג");
        if (binding.chipCity.isChecked()) selected.add("עיר ותרבות");
        if (binding.chipBeach.isChecked()) selected.add("חוף ים");
        if (binding.chipShopping.isChecked()) selected.add("שופינג");
        if (binding.chipAdventure.isChecked()) selected.add("הרפתקאות");
        if (binding.chipFood.isChecked()) selected.add("אוכל ושתייה");
        if (binding.chipHistory.isChecked()) selected.add("היסטוריה");
        if (binding.chipFamily.isChecked()) selected.add("משפחה");
        return selected;
    }

    private List<String> getSelectedPrefs() {
        List<String> selected = new ArrayList<>();
        if (binding.chipAccessible.isChecked()) selected.add("נגיש");
        if (binding.chipBudget.isChecked()) selected.add("תקציב נמוך");
        if (binding.chipMid.isChecked()) selected.add("תקציב בינוני");
        if (binding.chipLuxury.isChecked()) selected.add("יוקרה");
        if (binding.chipPet.isChecked()) selected.add("ידידותי לחיות");
        return selected;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}