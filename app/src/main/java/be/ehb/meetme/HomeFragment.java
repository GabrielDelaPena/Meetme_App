package be.ehb.meetme;

import static be.ehb.meetme.global.CurrentUser.currentEmail;
import static be.ehb.meetme.global.CurrentUser.currentLocation;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import be.ehb.meetme.databinding.FragmentHomeBinding;
import be.ehb.meetme.models.Meetup;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private GoogleMap mGoogleMap;
    ArrayList<Meetup> meetups = new ArrayList<>();

    FusedLocationProviderClient fusedLocationClient;
    private static final int REQUEST_CODE = 101;

    private OnMapReadyCallback onMapReadyCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(@NonNull GoogleMap googleMap) {
            mGoogleMap = googleMap;
            mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            showMeetupsOnMap();
            getCurrentLocation();
        }
    };

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.mapView.getMapAsync(onMapReadyCallback);
        binding.mapView.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.mapView.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.mapView.onDestroy();
        binding = null;
    }

    // Own methods

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }

        Task<Location> task = fusedLocationClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    LatLng coorden = new LatLng(location.getLatitude(),location.getLongitude());
                    //LatLng coorden = new LatLng(50.8365808,4.308187);
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(coorden, 12);
                    mGoogleMap.animateCamera(cameraUpdate);
                } else {
                    Toast.makeText(getContext(), "Your current location is undefined, pls give meetme permission.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void drawAnnotations(double lat, double lng, String location, String date) {
        LatLng coordGroteMarkt = new LatLng(lat,lng);
        mGoogleMap.addMarker(new MarkerOptions()
                .position(coordGroteMarkt)
                .title(location)
                .snippet(date)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_locations_map))
        );
    }

    public void showMeetupsOnMap() {
        Log.d("EMAIL", currentEmail);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient().newBuilder()
                            .build();
                    Request request = new Request.Builder()
                            .url("https://meetme-server.onrender.com/api/invitation/getInvitationsByUser/" + currentEmail)
                            .get()
                            .build();
                    String response = client.newCall(request).execute().body().string();
                    ArrayList<Meetup> items = new ArrayList<>();
                    JSONArray meetupsRaw = new JSONArray(response);
                    int length = meetupsRaw.length();

                    for (int i = 0; i < length; i++) {
                        JSONObject meetupRaw = meetupsRaw.getJSONObject(i);
                        Meetup reportParsed = new Meetup(
                                meetupRaw.getDouble("lat"),
                                meetupRaw.getDouble("lon"),
                                meetupRaw.getString("sender"),
                                meetupRaw.getString("receiver"),
                                meetupRaw.getString("_id"),
                                meetupRaw.getString("date"),
                                meetupRaw.getString("description"),
                                meetupRaw.getString("location")
                        );
                        items.add(reportParsed);
                    }
                    meetups = items;
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();

        try {
            thread.join();
            for (int i = 0; i < meetups.size(); i++) {
                drawAnnotations(meetups.get(i).getLat(), meetups.get(i).getLon(), meetups.get(i).getLocation(), meetups.get(i).getDate());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
