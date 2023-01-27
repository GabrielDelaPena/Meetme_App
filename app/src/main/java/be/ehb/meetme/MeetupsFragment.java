package be.ehb.meetme;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import be.ehb.meetme.databinding.FragmentMeetupsBinding;
import be.ehb.meetme.models.Meetup;
import be.ehb.meetme.util.MeetupAdapter;
import be.ehb.meetme.viewmodels.MeetupViewModel;

public class MeetupsFragment extends Fragment {
    private FragmentMeetupsBinding binding;
    // Testing
    private ArrayList<Meetup> meetups = new ArrayList<>();

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentMeetupsBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MeetupAdapter meetupAdapter = new MeetupAdapter(meetups, getActivity());
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);

        binding.rvMeetups.setAdapter(meetupAdapter);
        binding.rvMeetups.setLayoutManager(layoutManager);

        MeetupViewModel model = new ViewModelProvider(getActivity()).get(MeetupViewModel.class);
        model.getMeetups().observe(getViewLifecycleOwner(), new Observer<ArrayList<Meetup>>() {
            @Override
            public void onChanged(ArrayList<Meetup> meetups) {
                meetupAdapter.setItems(meetups);
            }
        });


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
