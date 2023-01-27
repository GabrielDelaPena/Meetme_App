package be.ehb.meetme;

import static be.ehb.meetme.global.CurrentUser.currentEmail;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.location.FusedLocationProviderClient;

import java.io.IOException;

import be.ehb.meetme.databinding.FragmentLoginBinding;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    EditText email, password;
    int status;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentLoginBinding.inflate(inflater, container, false);
        email = binding.etLoginEmail;
        password = binding.etLoginPassword;
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.tvNavRegister.setOnClickListener(
                (View v) -> {
                    NavHostFragment.findNavController(LoginFragment.this)
                            .navigate(R.id.action_FirstFragment_to_registerFragment);
                }
        );

        binding.btnLoginSubmit.setOnClickListener(
                (View v) -> {
                    loggedinUser(email.getText().toString(), password.getText().toString());
                }
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void loggedinUser(String email, String password) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient().newBuilder()
                            .build();
                    MediaType mediaType = MediaType.parse("application/json");
                    RequestBody body = RequestBody.create(mediaType, "{\r\n    \"email\": \"" + email + "\",\r\n    \"password\": \"" + password + "\"\r\n}");
                    Request request = new Request.Builder()
                            .url("https://meetme-server.onrender.com/api/user/login")
                            .method("POST", body)
                            .addHeader("Content-Type", "application/json")
                            .build();
                    String responseString = client.newCall(request).execute().body().string();
                    int response = client.newCall(request).execute().code();
                    currentEmail = responseString;
                    status = response;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();

        try {
            thread.join();
            if (status == 400) {
                Toast.makeText(this.getContext(), "Email or Password is invalid.", Toast.LENGTH_SHORT).show();
            } else if (status == 500) {
                Toast.makeText(this.getContext(), "An error occurred in the server.", Toast.LENGTH_SHORT).show();
            } else if (status == 200) {
                Toast.makeText(this.getContext(), "User successfully logged in.", Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(LoginFragment.this)
                        .navigate(R.id.action_FirstFragment_to_startFragment);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}