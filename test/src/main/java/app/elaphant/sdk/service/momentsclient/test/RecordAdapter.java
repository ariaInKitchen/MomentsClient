package app.elaphant.sdk.service.momentsclient.test;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class RecordAdapter extends ArrayAdapter<Record> {
    public RecordAdapter(Context context, int resource, List<Record> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View view, ViewGroup group) {

        RecordAdapter.ViewHolder holder;
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.record_item, null);
            holder = new RecordAdapter.ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (RecordAdapter.ViewHolder)view.getTag();
        }

        Record item = getItem(position);
        holder.mDid.setText(item.did);
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(item.time);
        holder.mTime.setText(format.format(date));
        holder.mContent.setText(item.content);
        return view;
    }

    class ViewHolder {
        TextView mDid;
        TextView mTime;
        TextView mContent;

        ViewHolder(View parent) {
            mDid = parent.findViewById(R.id.record_did);
            mTime = parent.findViewById(R.id.record_time);
            mContent = parent.findViewById(R.id.record_content);
        }
    }
}
