package cn.ucloud.httpdns.demo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Created by joshua
 * On 2021/12/14 21:26
 * E-mail: joshua.yin@ucloud.cn
 * Description:
 */
public class RecordAdapter extends RecyclerView.Adapter<RecordViewHolder> {
    private Context context;
    private LayoutInflater inflater;
    private List<Record> records;

    public RecordAdapter(Context context, List<Record> records) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.records = records;
    }

    @NonNull
    @Override
    public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecordViewHolder(inflater.inflate(R.layout.item_record, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
        if (records == null) return;
        final Record record = records.get(position);
        if (record == null) return;

        holder.bindData(record);
    }

    @Override
    public int getItemCount() {
        return records == null ? 0 : records.size();
    }

}
