package com.yourname.wanderlust.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.yourname.wanderlust.data.api.TripRepository;

public class DetailViewModel extends ViewModel {

    public static class RouteDetails {
        public String prices;
        public String tips;
        public String bestTime;
        public String reviews;
    }

    private final MutableLiveData<RouteDetails> details = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public LiveData<RouteDetails> getDetails() { return details; }
    public LiveData<String> getError() { return error; }

    public void fetchDetails(String title, String destination) {
        TripRepository repository = new TripRepository();
        repository.fetchRouteDetails(title, destination, new TripRepository.DetailsCallback() {
            @Override
            public void onSuccess(RouteDetails d) {
                details.postValue(d);
            }
            @Override
            public void onError(String err) {
                error.postValue(err);
            }
        });
    }
}