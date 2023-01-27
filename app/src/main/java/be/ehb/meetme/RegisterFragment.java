package be.ehb.meetme;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.io.IOException;

import be.ehb.meetme.databinding.FragmentRegisterBinding;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    EditText firstname, lastname, email, password;
    int status;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        firstname = binding.etRegisterFirstname;
        lastname = binding.etRegisterLastname;
        email = binding.etRegisterEmail;
        password = binding.etRegisterPassword;
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.tvNavRegisterLogin.setOnClickListener(
                (View v) -> {
                    NavHostFragment.findNavController(RegisterFragment.this)
                            .navigate(R.id.action_registerFragment_to_FirstFragment);
                }
        );

        binding.btnRegisterSubmit.setOnClickListener(
                (View v) -> {
                    postRegister();
                }
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Own methods

    public void postRegister() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient().newBuilder()
                            .build();
                    MediaType mediaType = MediaType.parse("application/json");
                    RequestBody body = RequestBody.create(mediaType, "{\r\n    \"email\": \"" + email.getText().toString() +
                            "\",\r\n    \"password\": \"" + password.getText().toString() +
                            "\",\r\n    \"firstname\": \"" + firstname.getText().toString() +
                            "\",\r\n    \"lastname\": \"" + lastname.getText().toString() + "\"\r\n}");
                    Request request = new Request.Builder()
                            .url("https://meetme-server.onrender.com/api/user/register")
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
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_registerFragment_to_FirstFragment);
                Toast.makeText(getActivity(), "Registered Successfully.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Some fields are invalid, pls try again.", Toast.LENGTH_SHORT).show();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}