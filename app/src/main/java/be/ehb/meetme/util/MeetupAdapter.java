package be.ehb.meetme.util;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import be.ehb.meetme.DetailsFragment;
import be.ehb.meetme.R;
import be.ehb.meetme.models.Meetup;

public class MeetupAdapter extends RecyclerView.Adapter<MeetupAdapter.MeetupViewHolder> {
    FragmentActivity activity;
    private ArrayList<Meetup> items;

    // Meetup row configurations
    protected class MeetupViewHolder extends RecyclerView.ViewHolder {
        TextView location, date, details;

        public MeetupViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.tv_row_date);
            location = itemView.findViewById(R.id.tv_row_street);
            details = itemView.findViewById(R.id.tv_row_navdetails);
        }
    }

    public MeetupAdapter(ArrayList<Meetup> meetups, FragmentActivity r) {
        this.activity = r;
        this.items = meetups;
    }

    public void setItems(ArrayList<Meetup> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MeetupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_meetup_row, parent, false);
        return new MeetupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MeetupViewHolder holder, int position) {
        Meetup current = items.get(position);
        Bundle bundle = new Bundle();
        bundle.putString("key", current.getId());
        holder.location.setText(current.getLocation());
        holder.date.setText(current.getDate());
        holder.details.setOnClickListener(
                (View v) -> {
                    replaceFragment(new DetailsFragment(), bundle);
                }
        );
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private void replaceFragment(Fragment fragment, Bundle bundle) {
        fragment.setArguments(bundle);
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
}
