package com.yourname.wanderlust.ui.search;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.yourname.wanderlust.R;
import com.yourname.wanderlust.data.LocationHelper;
import com.yourname.wanderlust.databinding.FragmentSearchBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private LocationHelper locationHelper;
    private boolean isFlightMode = false;

    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    granted -> {
                        String dest = binding.editWantedDestination
                                .getText().toString().trim();
                        if (granted) {
                            detectLocation(dest);
                        } else {
                            binding.editDestination.setText(dest);
                            showManualModeSelection();
                        }
                    });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        locationHelper = new LocationHelper(requireContext());
        setupDatePickers();
        requestLocationAndDetect();
        setupSearchButton();
    }

    private void requestLocationAndDetect() {
        // חכה שהמשתמש יכתוב יעד לפני זיהוי מיקום
        binding.editWantedDestination.setOnEditorActionListener((v, actionId, event) -> {
            String dest = binding.editWantedDestination.getText().toString().trim();
            if (!dest.isEmpty()) {
                startLocationDetection(dest);
            }
            return false;
        });

        // גם כשמאבד פוקוס
        binding.editWantedDestination.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String dest = binding.editWantedDestination.getText().toString().trim();
                if (!dest.isEmpty()) {
                    startLocationDetection(dest);
                }
            }
        });
    }

    private void startLocationDetection(String wantedDestination) {
        binding.layoutLocationDetecting.setVisibility(View.VISIBLE);
        binding.progressLocation.setVisibility(View.VISIBLE);
        binding.tvLocationStatus.setText("מזהה מיקום...");

        if (locationHelper.hasPermission()) {
            detectLocation(wantedDestination);
        } else {
            locationPermissionLauncher.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void detectLocation(String wantedDestination) {
        locationHelper.getLocation(new LocationHelper.LocationCallback() {
            @Override
            public void onLocationResult(String country, String city,
                                         boolean isIsrael) {
                if (!isAdded() || binding == null) return; // ← הוסף
                requireActivity().runOnUiThread(() -> {
                    if (binding == null) return; // ← הוסף
                    binding.progressLocation.setVisibility(View.GONE);

                    boolean wantLocal = isIsrael &&
                            isLocalDestination(wantedDestination);

                    if (wantLocal) {
                        binding.tvLocationStatus.setText(
                                "📍 טיול מקומי — " + wantedDestination);
                        fillLocalArea(wantedDestination);
                        showLocalMode();
                    } else {
                        binding.tvLocationStatus.setText(
                                "✈ טיסה מ" + city + " ל" + wantedDestination);
                        binding.editDestination.setText(wantedDestination);
                        showFlightMode();
                    }
                });
            }

            @Override
            public void onError(String error) {
                if (!isAdded() || binding == null) return; // ← הוסף
                requireActivity().runOnUiThread(() -> {
                    if (binding == null) return; // ← הוסף
                    binding.progressLocation.setVisibility(View.GONE);
                    binding.tvLocationStatus.setText(
                            "לא ניתן לזהות מיקום — בחר סוג טיול:");
                    binding.editDestination.setText(wantedDestination);
                    showManualModeSelection();
                });
            }
        });
    }

    private boolean isLocalDestination(String dest) {
        String d = dest.toLowerCase();
        return d.contains("ישראל") || d.contains("israel") ||
                d.contains("צפון") || d.contains("דרום") ||
                d.contains("ירושלים") || d.contains("jerusalem") ||
                d.contains("תל אביב") || d.contains("tel aviv") ||
                d.contains("אילת") || d.contains("eilat") ||
                d.contains("חיפה") || d.contains("haifa") ||
                d.contains("ים המלח") || d.contains("dead sea") ||
                d.contains("נגב") || d.contains("גליל") ||
                d.contains("כנרת") || d.contains("הר");
    }

    private void fillLocalArea(String dest) {
        String d = dest.toLowerCase();
        if (d.contains("צפון") || d.contains("חיפה") ||
                d.contains("גליל") || d.contains("כנרת")) {
            binding.chipNorth.setChecked(true);
        } else if (d.contains("דרום") || d.contains("נגב") ||
                d.contains("אילת")) {
            binding.chipSouth.setChecked(true);
        } else if (d.contains("ירושלים")) {
            binding.chipJerusalem.setChecked(true);
        } else if (d.contains("ים המלח")) {
            binding.chipDeadSea.setChecked(true);
        } else if (d.contains("תל אביב") || d.contains("מרכז")) {
            binding.chipCenter.setChecked(true);
        }
    }

    private void showFlightMode() {
        isFlightMode = true;
        binding.layoutTripMode.setVisibility(View.VISIBLE);
        binding.layoutFlight.setVisibility(View.VISIBLE);
        binding.layoutLocal.setVisibility(View.GONE);
    }

    private void showLocalMode() {
        isFlightMode = false;
        binding.layoutTripMode.setVisibility(View.VISIBLE);
        binding.layoutLocal.setVisibility(View.VISIBLE);
        binding.layoutFlight.setVisibility(View.GONE);
    }

    private void showManualModeSelection() {
        binding.progressLocation.setVisibility(View.GONE);
        binding.tvLocationStatus.setText("בחר סוג טיול:");
        binding.layoutTripMode.setVisibility(View.VISIBLE);
        binding.layoutFlight.setVisibility(View.VISIBLE);
        binding.layoutLocal.setVisibility(View.VISIBLE);
    }

    private void setupDatePickers() {
        binding.editDateFrom.setOnClickListener(v -> showDatePicker(true, false));
        binding.editDateTo.setOnClickListener(v -> showDatePicker(false, false));
        binding.editLocalDateFrom.setOnClickListener(v -> showDatePicker(true, true));
        binding.editLocalDateTo.setOnClickListener(v -> showDatePicker(false, true));
    }

    private void showDatePicker(boolean isFrom, boolean isLocal) {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder
                .datePicker()
                .setTitleText(isFrom ? "תאריך יציאה" : "תאריך חזרה")
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat sdf = new SimpleDateFormat(
                    "dd/MM/yyyy", Locale.getDefault());
            String date = sdf.format(new Date(selection));
            if (isLocal) {
                if (isFrom) binding.editLocalDateFrom.setText(date);
                else binding.editLocalDateTo.setText(date);
            } else {
                if (isFrom) binding.editDateFrom.setText(date);
                else binding.editDateTo.setText(date);
            }
        });

        picker.show(getParentFragmentManager(), "date_picker");
    }

    private void setupSearchButton() {
        binding.btnSearch.setOnClickListener(v -> {

            if (isFlightMode) {
                // בדיקות לטיסה
                String destination = binding.editDestination
                        .getText().toString().trim();
                String dateFrom = binding.editDateFrom
                        .getText().toString().trim();
                String dateTo = binding.editDateTo
                        .getText().toString().trim();
                String pax = binding.editPax
                        .getText().toString().trim();
                String budget = binding.editBudget
                        .getText().toString().trim();

                if (destination.isEmpty()) {
                    binding.editDestination.setError("נא להזין יעד");
                    binding.editDestination.requestFocus();
                    return;
                }
                if (dateFrom.isEmpty()) {
                    Toast.makeText(requireContext(),
                            "נא לבחור תאריך יציאה",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (dateTo.isEmpty()) {
                    Toast.makeText(requireContext(),
                            "נא לבחור תאריך חזרה",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (pax.isEmpty()) {
                    binding.editPax.setError("נא להזין מספר נוסעים");
                    binding.editPax.requestFocus();
                    return;
                }
                if (budget.isEmpty()) {
                    binding.editBudget.setError("נא להזין תקציב");
                    binding.editBudget.requestFocus();
                    return;
                }
                if (getSelectedLuggage().isEmpty()) {
                    Toast.makeText(requireContext(),
                            "נא לבחור סוג כבודה",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

            } else {
                // בדיקות לטיול מקומי
                if (getSelectedArea().equals("כללי")) {
                    Toast.makeText(requireContext(),
                            "נא לבחור אזור",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                String pax = binding.editLocalPax
                        .getText().toString().trim();
                String budget = binding.editLocalBudget
                        .getText().toString().trim();
                String dateFrom = binding.editLocalDateFrom
                        .getText().toString().trim();
                String dateTo = binding.editLocalDateTo
                        .getText().toString().trim();

                if (pax.isEmpty()) {
                    binding.editLocalPax.setError("נא להזין מספר אנשים");
                    binding.editLocalPax.requestFocus();
                    return;
                }
                if (budget.isEmpty()) {
                    binding.editLocalBudget.setError("נא להזין תקציב");
                    binding.editLocalBudget.requestFocus();
                    return;
                }
                if (dateFrom.isEmpty()) {
                    Toast.makeText(requireContext(),
                            "נא לבחור תאריך יציאה",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (dateTo.isEmpty()) {
                    Toast.makeText(requireContext(),
                            "נא לבחור תאריך חזרה",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (getSelectedAccommodation().isEmpty()) {
                    Toast.makeText(requireContext(),
                            "נא לבחור סוג לינה",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // הכל תקין — בנה args ונווט
            Bundle args = new Bundle();
            args.putBoolean("isFlightMode", isFlightMode);

            if (isFlightMode) {
                buildFlightArgs(args);
            } else {
                buildLocalArgs(args);
            }

            Navigation.findNavController(v)
                    .navigate(R.id.resultsFragment, args);
        });
    }

    private void buildFlightArgs(Bundle args) {
        args.putString("destination",
                binding.editDestination.getText().toString().trim());
        args.putString("dateFrom",
                binding.editDateFrom.getText().toString());
        args.putString("dateTo",
                binding.editDateTo.getText().toString());
        args.putString("pax",
                binding.editPax.getText().toString());
        args.putString("budget",
                binding.editBudget.getText().toString());
        args.putStringArrayList("luggage", getSelectedLuggage());
    }

    private void buildLocalArgs(Bundle args) {
        args.putString("area", getSelectedArea());
        args.putString("pax", binding.editLocalPax.getText().toString());
        args.putString("budget", binding.editLocalBudget.getText().toString());
        args.putString("difficulty", getSelectedDifficulty());
        args.putString("dateFrom", binding.editLocalDateFrom.getText().toString());
        args.putString("dateTo", binding.editLocalDateTo.getText().toString());
        args.putStringArrayList("accommodation", getSelectedAccommodation());
    }

    private ArrayList<String> getSelectedLuggage() {
        ArrayList<String> list = new ArrayList<>();
        if (binding.chipHandbag.isChecked()) list.add("תיק יד");
        if (binding.chipTrolley.isChecked()) list.add("טרולי קאבין");
        if (binding.chipSuitcase.isChecked()) list.add("מזוודה גדולה");
        if (binding.chipBackpack.isChecked()) list.add("תיק גב");
        return list;
    }

    private String getSelectedArea() {
        if (binding.chipNorth.isChecked()) return "צפון";
        if (binding.chipSouth.isChecked()) return "דרום";
        if (binding.chipCenter.isChecked()) return "מרכז";
        if (binding.chipJerusalem.isChecked()) return "ירושלים";
        if (binding.chipDeadSea.isChecked()) return "ים המלח";
        if (binding.chipEilat.isChecked()) return "אילת";
        return "כללי";
    }

    private String getSelectedDifficulty() {
        if (binding.chipEasy.isChecked()) return "קל";
        if (binding.chipMedium.isChecked()) return "בינוני";
        if (binding.chipHard.isChecked()) return "קשה";
        return "קל";
    }

    private ArrayList<String> getSelectedAccommodation() {
        ArrayList<String> list = new ArrayList<>();
        if (binding.chipCamping.isChecked()) list.add("קמפינג");
        if (binding.chipZimmer.isChecked()) list.add("צימר");
        if (binding.chipHotel.isChecked()) list.add("מלון");
        if (binding.chipHostel.isChecked()) list.add("האוסטל");
        return list;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}