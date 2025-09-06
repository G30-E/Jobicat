package com.jobicat.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jobicat.app.R;
import com.jobicat.app.model.Task;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.VH> {

    public interface Listener {
        void onClick(Task task);
        void onLongClick(Task task);
    }

    private final Listener listener;
    private final List<Task> data = new ArrayList<>();

    public TaskAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<Task> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Task t = data.get(position);
        h.tvTitle.setText(t.title);
        h.tvDesc.setText(t.description == null ? "" : t.description);
        h.tvDifficulty.setText("Dificultad: " + (t.difficulty == null ? "" : t.difficulty));
        h.tvTime.setText(t.timeHHmm == null ? "--:--" : t.timeHHmm);
        h.itemView.setOnClickListener(v -> listener.onClick(t));
        h.itemView.setOnLongClickListener(v -> { listener.onLongClick(t); return true; });
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDesc, tvDifficulty, tvTime;
        VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDesc = itemView.findViewById(R.id.tvDesc);
            tvDifficulty = itemView.findViewById(R.id.tvDifficulty);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}