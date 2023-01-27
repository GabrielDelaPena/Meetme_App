package be.ehb.meetme;

import static be.ehb.meetme.global.CurrentUser.currentEmail;
import static be.ehb.meetme.global.CurrentUser.currentLocation;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import be.ehb.meetme.databinding.FragmentAddBinding;
import be.ehb.meetme.models.Meetup;
import be.ehb.meetme.models.User;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddFragment extends Fragment {
    private FragmentAddBinding binding;
    int status;
    EditText date, description;
    TextView location;
    String selectedEmail;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentAddBinding.inflate(inflater, container, false);
        date = binding.etAddDate;
        description = binding.etAddDescription;
        location = binding.tvAddLocation;
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getUsersNames();
        onGetLocation();

        binding.btnAddSubmit.setOnClickListener(
                (View v) -> {
                    addMeetup();
                }
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Own methods

    public void getUsersNames() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient().newBuilder()
                            .build();
                    Request request = new Request.Builder()
                            .url("https://meetme-server.onrender.com/api/user")
                            .get()
                            .addHeader("Content-Type", "application/json")
                            .build();
                    String responseString = client.newCall(request).execute().body().string();
                    JSONArray usersRaw = new JSONArray(responseString);
                    int length = usersRaw.length();
                    ArrayList<String> items = new ArrayList<>();

                    for (int i = 0; i < length; i++) {
                        JSONObject userRaw = usersRaw.getJSONObject(i);
                        if (!userRaw.getString("email").equals(currentEmail)) {
                            items.add(userRaw.getString("email"));
                        }
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Dropdown list friends.
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, items);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            binding.ddFriendsList.setAdapter(adapter);
                            binding.ddFriendsList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    String selectedItem = parent.getItemAtPosition(position).toString();
                                    selectedEmail = selectedItem;
                                    Toast.makeText(getContext(), selectedEmail, Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {
                                    Toast.makeText(getContext(), "Please select one friend.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public void addMeetup() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient().newBuilder()
                            .build();
                    MediaType mediaType = MediaType.parse("application/json");
                    RequestBody body = RequestBody.create(mediaType, "{\r\n    \"sender\": \"" + currentEmail +
                            "\",\r\n    \"receiver\": \"" + selectedEmail + "\",\r\n    \"lat\": " + currentLocation.getLatitude() +
                            ",\r\n    \"lon\": " + currentLocation.getLongitude() + ",\r\n    \"date\": \"" + date.getText().toString() +
                            "\",\r\n    \"description\": \"" + description.getText().toString() +
                            "\",\r\n    \"location\": \"" + location.getText().toString() + "\"\r\n}");
                    Request request = new Request.Builder()
                            .url("https://meetme-server.onrender.com/api/invitation/createInvitation")
                            .method("POST", body)
                            .addHeader("Content-Type", "application/json")
                            .build();
                    int response = client.newCall(request).execute().code();
                    status = response;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();

        try {
            thread.join();
            if (status == 200) {
                replaceFragment(new HomeFragment());
                Toast.makeText(getContext(), "New meetup added.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Some fields are invalid, pls try again.", Toast.LENGTH_SHORT).show();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    public void onGetLocation() {
        try {
            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1);

            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                location.setText(address.getAddressLine(0));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
