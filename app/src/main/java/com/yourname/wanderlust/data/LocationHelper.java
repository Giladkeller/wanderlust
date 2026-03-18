package com.yourname.wanderlust.data;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import java.util.List;
import java.util.Locale;

public class LocationHelper {

    public interface LocationCallback {
        void onLocationResult(String country, String city, boolean isIsrael);
        void onError(String error);
    }

    private final Context context;
    private final FusedLocationProviderClient fusedClient;

    public LocationHelper(Context context) {
        this.context = context;
        this.fusedClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public boolean hasPermission() {
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    public void getLocation(LocationCallback callback) {
        if (!hasPermission()) {
            callback.onError("אין הרשאת מיקום");
            return;
        }

        fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        resolveCountry(location, callback);
                    } else {
                        callback.onError("לא ניתן לקבל מיקום");
                    }
                })
                .addOnFailureListener(e ->
                        callback.onError("שגיאת מיקום: " + e.getMessage()));
    }

    private void resolveCountry(Location location, LocationCallback callback) {
        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(
                    location.getLatitude(), location.getLongitude(), 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String country = address.getCountryName();
                String city    = address.getLocality();
                boolean isIsrael = "Israel".equalsIgnoreCase(address.getCountryCode()) ||
                        "IL".equalsIgnoreCase(address.getCountryCode());
                callback.onLocationResult(country, city, isIsrael);
            } else {
                callback.onError("לא ניתן לזהות מיקום");
            }
        } catch (Exception e) {
            callback.onError("שגיאה: " + e.getMessage());
        }
    }
}