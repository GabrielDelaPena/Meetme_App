package be.ehb.meetme.viewmodels;

import static be.ehb.meetme.global.CurrentUser.currentEmail;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import be.ehb.meetme.models.Meetup;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MeetupViewModel extends AndroidViewModel {
    MutableLiveData<ArrayList<Meetup>> meetups;

    public MeetupViewModel(@NonNull Application application) {
        super(application);
        meetups = new MutableLiveData<>();
    }

    public MutableLiveData<ArrayList<Meetup>> getMeetups() {
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
                    int response = client.newCall(request).execute().code();
                    String responseString = client.newCall(request).execute().body().string();

                    if (response == 400) {
                        meetups = new MutableLiveData<>();
                    } else {
                        ArrayList<Meetup> items = new ArrayList<>();
                        JSONArray meetupsRaw = new JSONArray(responseString);
                        int length = meetupsRaw.length();

                        for (int i = 0; i < length; i++) {
                            JSONObject meetupRaw = meetupsRaw.getJSONObject(i);
                            Meetup meetupParsed = new Meetup(
                                    meetupRaw.getDouble("lat"),
                                    meetupRaw.getDouble("lon"),
                                    meetupRaw.getString("sender"),
                                    meetupRaw.getString("receiver"),
                                    meetupRaw.getString("_id"),
                                    meetupRaw.getString("date"),
                                    meetupRaw.getString("description"),
                                    meetupRaw.getString("location")
                            );
                            items.add(meetupParsed);
                        }
                        meetups.postValue(items);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        return meetups;
    }
}
