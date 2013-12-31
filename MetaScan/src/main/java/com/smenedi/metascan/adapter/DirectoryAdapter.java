package com.smenedi.metascan.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.smenedi.metascan.DirectoryFragment;
import com.smenedi.metascan.R;
import java.util.ArrayList;


/**
 * Created by smenedi on 12/19/13.
 */
public class DirectoryAdapter extends ArrayAdapter<DirectoryItem> {
    Context context;
    int layoutResourceId;
    ArrayList<DirectoryItem> data = null;

    static class DirectoryItemHolder {
        ImageView checkBoxIcon;
        ImageView imgIcon;
        TextView txtTitle;
    }

    public DirectoryAdapter(Context context, int layoutResourceId, ArrayList<DirectoryItem> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    /**
     * {@inheritDoc}
     *
     * @param position
     */
    @Override
    public DirectoryItem getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return data.size();
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        DirectoryItemHolder holder = null;
        final int pos = position;
        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new DirectoryItemHolder();
            holder.checkBoxIcon = (ImageView) row.findViewById(R.id.cb_item_select);
            holder.imgIcon = (ImageView) row.findViewById(R.id.thumbnail);
            holder.txtTitle = (TextView) row.findViewById(R.id.item_name);


            holder.checkBoxIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(context, "Click-" + String.valueOf(pos), Toast.LENGTH_SHORT).show();
                    DirectoryFragment.getInstance().clickCheckBox((Integer)v.getTag());
                }
            });
            row.setTag(holder);

        } else {
            holder = (DirectoryItemHolder) row.getTag();
        }
        holder.checkBoxIcon.setTag(pos);
        DirectoryItem directoryItem = data.get(position);
        holder.checkBoxIcon.setImageResource(directoryItem.checkBoxIcon);
        holder.txtTitle.setText(directoryItem.title);
        holder.imgIcon.setImageResource(directoryItem.icon);

        return row;
    }
}

