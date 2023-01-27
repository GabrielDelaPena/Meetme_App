package be.ehb.meetme;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import be.ehb.meetme.databinding.FragmentDetailsBinding;
import be.ehb.meetme.models.Meetup;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DetailsFragment extends Fragment {
    private FragmentDetailsBinding binding;
    int status;
    TextView sender, receiver, location, date, description;
    Meetup meetup;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentDetailsBinding.inflate(inflater, container, false);
        sender = binding.tvDetailsSender;
        receiver = binding.tvDetailsReceiver;
        location = binding.tvDetailsLocation;
        date = binding.tvDetailsDate;
        description = binding.tvDetailsDescription;
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        getMeetupDetails(bundle.getString("key"));

        binding.btnDetailsBack.setOnClickListener(
                (View v) -> {
                    replaceFragment(new MeetupsFragment());
                }
        );

        binding.btnDetailsRemove.setOnClickListener(
                (View v) -> {
                    deleteMeetup(bundle.getString("key"));
                }
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Own methods

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    public void getMeetupDetails(String meetupID) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient().newBuilder()
                            .build();
                    Request request = new Request.Builder()
                            .url("https://meetme-server.onrender.com/api/invitation/getInvitationByID/" + meetupID)
                            .get()
                            .build();
                    String response = client.newCall(request).execute().body().string();
                    JSONObject meetupFetched = new JSONObject(response);
                    meetup = new Meetup(
                        meetupFetched.getDouble("lat"),
                        meetupFetched.getDouble("lon"),
                        meetupFetched.getString("sender"),
                        meetupFetched.getString("receiver"),
                        meetupFetched.getString("_id"),
                        meetupFetched.getString("date"),
                        meetupFetched.getString("description"),
                        meetupFetched.getString("location")
                    );
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
            sender.setText(meetup.getSender());
            receiver.setText(meetup.getReceiver());
            location.setText(meetup.getLocation());
            date.setText(meetup.getDate());
            description.setText(meetup.getDescription());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void deleteMeetup(String id) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient().newBuilder()
                            .build();
                    MediaType mediaType = MediaType.parse("text/plain");
                    RequestBody body = RequestBody.create(mediaType, "");
                    Request request = new Request.Builder()
                            .url("https://meetme-server.onrender.com/api/invitation/deleteInvitation/" + id)
                            .method("POST", body)
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
                Toast.makeText(getActivity(), "Meetup deleted.", Toast.LENGTH_SHORT).show();
                replaceFragment(new MeetupsFragment());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
