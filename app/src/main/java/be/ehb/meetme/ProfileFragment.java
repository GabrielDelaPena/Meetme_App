package be.ehb.meetme;

import static be.ehb.meetme.global.CurrentUser.currentEmail;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.fragment.NavHostFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import be.ehb.meetme.databinding.FragmentProfileBinding;
import be.ehb.meetme.models.Meetup;
import be.ehb.meetme.models.User;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    TextView name, email;
    User user;
    int meetupsLength;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        name = binding.tvProfileName;
        email = binding.tvProfileEmail;
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getUserDetails();
        getUserMeetups();

        binding.tvProfileMyreports.setOnClickListener(
                (View v) -> {
                    if (meetupsLength <= 0) {
                        replaceFragment(new EmptyFragment());
                    } else {
                        replaceFragment(new MeetupsFragment());
                    }
                }
        );

        binding.btnProfileLogout.setOnClickListener(
                (View v) -> {
                    currentEmail = "";
                    NavHostFragment.findNavController(this)
                            .navigate(R.id.action_startFragment_to_FirstFragment);
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

    public void getUserDetails() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient().newBuilder()
                            .build();
                    Request request = new Request.Builder()
                            .url("https://meetme-server.onrender.com/api/user/getUserByEmail/" + currentEmail)
                            .get()
                            .addHeader("Content-Type", "application/json")
                            .build();
                    String response = client.newCall(request).execute().body().string();
                    JSONObject userFetched = new JSONObject(response);
                    user = new User(
                            userFetched.getString("_id"),
                            userFetched.getString("role"),
                            userFetched.getString("firstname"),
                            userFetched.getString("lastname"),
                            userFetched.getString("email"),
                            userFetched.getString("password")
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
            name.setText(user.getFirstname() + " " + user.getLastname());
            email.setText(user.getEmail());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void getUserMeetups() {
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
                    String responseString = client.newCall(request).execute().body().string();
                    JSONArray meetupsRaw = new JSONArray(responseString);
                    int length = meetupsRaw.length();
                    meetupsLength = length;
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
}
