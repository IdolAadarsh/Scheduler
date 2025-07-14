package com.idroid.scheduler;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class ProcessAdapter extends RecyclerView.Adapter<ProcessAdapter.ViewHolder> {
    private final ArrayList<ProcessModel> processList;

    public ProcessAdapter(ArrayList<ProcessModel> list) {
        this.processList = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txtName, txtArrival, txtBurst, txtPriority;
        public ImageButton deleteBtn;

        public ViewHolder(View view) {
            super(view);
            txtName = view.findViewById(R.id.txtName);
            txtArrival = view.findViewById(R.id.txtArrival);
            txtBurst = view.findViewById(R.id.txtBurst);
            txtPriority = view.findViewById(R.id.txtPriority);
            deleteBtn = view.findViewById(R.id.btnDelete);
        }
    }

    @NonNull
    @Override
    public ProcessAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_process, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ProcessModel p = processList.get(position);
        holder.txtName.setText(p.name);
        holder.txtArrival.setText("Arrival: " + p.arrival);
        holder.txtBurst.setText("Burst: " + p.burst);
        holder.txtPriority.setText("Priority: " + (p.priority == -1 ? "-" : p.priority));

        holder.deleteBtn.setOnClickListener(v -> {
            processList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, processList.size());
        });
    }

    @Override
    public int getItemCount() {
        return processList.size();
    }
}
