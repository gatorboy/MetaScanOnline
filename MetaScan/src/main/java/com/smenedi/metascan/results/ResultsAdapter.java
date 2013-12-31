package com.smenedi.metascan.results;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.smenedi.metascan.ResultsFragment;
import com.smenedi.metascan.R;
import java.util.ArrayList;

/**
 * Created by smenedi on 12/21/13.
 */

public class ResultsAdapter extends ArrayAdapter<ResultsItem> {
    Context context;
    int layoutResourceId;
    ArrayList<ResultsItem> data = null;
    //HashMap<Integer, String>

    static class ResultsItemHolder {
        TextView FileName;
        TextView Result;
        TextView Status;
        ImageView overflowMenu;
    }

    public ResultsAdapter(Context context, int layoutResourceId, ArrayList<ResultsItem> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }


    public void setResultsItem(int position, ResultsItem resultsItem){
        data.set(position, resultsItem);
        notifyDataSetChanged();
    }

    /**
     * {@inheritDoc}
     *
     * @param position
     */
    @Override
    public ResultsItem getItem(int position) {
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
        ResultsItemHolder holder = null;
        final int pos = position;
        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new ResultsItemHolder();
            holder.FileName = (TextView) row.findViewById(R.id.file_name);
            holder.Result = (TextView) row.findViewById(R.id.scan_result);
            holder.Status = (TextView) row.findViewById(R.id.status);
            holder.overflowMenu = (ImageView) row.findViewById(R.id.expanded_menu);

            holder.overflowMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(context, "Click-" + String.valueOf(pos), Toast.LENGTH_SHORT).show();
                    //DirectoryFragment.getInstance().clickCheckBox((Integer) v.getTag());
                    PopupMenu popup = new PopupMenu(getContext(), v);
                    MenuInflater inflater = popup.getMenuInflater();
                    inflater.inflate(R.menu.overflow_results_menu, popup.getMenu());
                    final View view = v;
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            switch (menuItem.getItemId()) {
                                case R.id.OpenInBrowser:
                                    //Toast.makeText(getContext(), "Toched"+((Integer) view.getTag()).toString(), Toast.LENGTH_SHORT).show();
                                    ResultsFragment.getInstance().openBrowser((Integer) view.getTag());
                                    return true;
                                default:
                                    return false;
                            }
                        }
                    });
                    popup.show();
                }

            });
            row.setTag(holder);

        } else {
            holder = (ResultsItemHolder) row.getTag();
        }
        holder.overflowMenu.setTag(pos);
        ResultsItem resultsItem = data.get(position);
        holder.FileName.setText(resultsItem.fileName);
        holder.Result.setText(resultsItem.scanResult);
        holder.Status.setText(resultsItem.scanStatus);
        holder.overflowMenu.setImageResource(resultsItem.overflowMenu);

        return row;
    }
}

