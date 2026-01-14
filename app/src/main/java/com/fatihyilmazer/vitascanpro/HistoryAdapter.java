package com.fatihyilmazer.vitascanpro;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    List<ScanHistoryItem> historyList;

    public HistoryAdapter(List<ScanHistoryItem> historyList) {
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        ScanHistoryItem item = historyList.get(position);
        holder.tvBarcode.setText(item.barcode);
        holder.tvDate.setText(item.date);
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvBarcode, tvDate;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBarcode = itemView.findViewById(R.id.tvHistoryBarcode);
            tvDate = itemView.findViewById(R.id.tvHistoryDate);
        }
    }
}