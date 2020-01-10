package app.elaphant.sdk.service.momentsclient.test;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class Adapter extends ArrayAdapter<MomentsItem> {


    public Adapter(Context context, int resource, List<MomentsItem> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View view, ViewGroup group) {

        ViewHolder holder;
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.moments_item, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder)view.getTag();
        }

        MomentsItem item = getItem(position);
        holder.mDid.setText(item.mDid);
        holder.mStatus.setText(item.mStatus);
        return view;
    }

    class ViewHolder {
        TextView mDid;
        TextView mStatus;

        ViewHolder(View parent) {
            mDid = parent.findViewById(R.id.moments_id);
            mStatus = parent.findViewById(R.id.moments_status);
        }
    }
}
