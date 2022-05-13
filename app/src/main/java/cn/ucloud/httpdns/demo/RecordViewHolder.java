package cn.ucloud.httpdns.demo;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by joshua
 * On 2021/12/14 21:34
 * E-mail: joshua.yin@ucloud.cn
 * Description:
 */
public class RecordViewHolder extends RecyclerView.ViewHolder {

    private TextView txt_record_time;
    private TextView txt_record_content;
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public RecordViewHolder(@NonNull View itemView) {
        super(itemView);
        txt_record_time = itemView.findViewById(R.id.txt_record_time);
        txt_record_content = itemView.findViewById(R.id.txt_record_content);
    }

    public void bindData(Record record) {
        txt_record_time.setText(format.format(new Date(record.timestamp)));
        txt_record_content.setText(record.content);
        if (record.type.equals(Record.RecordType.ERROR)) {
            txt_record_content.setTextColor(Color.RED);
        } else {
            txt_record_content.setTextColor(Color.BLACK);
        }
    }
}