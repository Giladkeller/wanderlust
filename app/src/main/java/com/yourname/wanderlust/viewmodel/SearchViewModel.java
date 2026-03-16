package com.yourname.wanderlust.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.yourname.wanderlust.data.api.TripRepository;
import com.yourname.wanderlust.data.model.RouteRequest;
import com.yourname.wanderlust.data.model.TripResult;
import java.util.List;

public class SearchViewModel extends ViewModel {

    private final TripRepository repository = new TripRepository();

    private final MutableLiveData<List<TripResult>> results = new MutableLiveData<>();
    private final MutableLiveData<String> summary = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    private final MutableLiveData<String> flightTip = new MutableLiveData<>();
    private final MutableLiveData<String> hotelTip  = new MutableLiveData<>();

    public LiveData<String> getFlightTip() { return flightTip; }
    public LiveData<String> getHotelTip()  { return hotelTip; }

    public LiveData<List<TripResult>> getResults() { return results; }
    public LiveData<String> getSummary() { return summary; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }

    public void fetchRoutes(RouteRequest request) {
        loading.setValue(true);
        error.setValue(null);

        repository.fetchRoutes(request, new TripRepository.TripCallback() {
            @Override
            public void onSuccess(List<TripResult> tripResults, String sum,
                                  String fTip, String hTip) {
                loading.postValue(false);
                results.postValue(tripResults);
                summary.postValue(sum);
                flightTip.postValue(fTip);
                hotelTip.postValue(hTip);
            }

            @Override
            public void onError(String err) {
                loading.postValue(false);
                error.postValue(err);
            }
        });
    }
}