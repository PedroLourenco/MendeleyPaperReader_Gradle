package com.mendeleypaperreader.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.mendeleypaperreader.R;

public class ListTitleAdapter extends BaseAdapter {

    Context context;
    String text;
    BaseAdapter parentAdapter;
    int listView;

    public ListTitleAdapter(Context c, String textToShow, int listLayout) {
        this(c, textToShow, null, listLayout);
    }

    public ListTitleAdapter(Context c, String textToShow, BaseAdapter dependentAdapter, int listLayout) {
        super();
        context = c;
        text = textToShow;
        listView = listLayout;

        if (dependentAdapter != null) {
            parentAdapter = dependentAdapter;
        }
    }

    public int getCount() {
        if (parentAdapter != null) {
            if (parentAdapter.getCount() == 0) {
                return 0;
            }
        }
        return 1;
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout layout = new LinearLayout(context);

        View v = convertView;

        LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v = vi.inflate(listView, null);
        TextView section = (TextView) v.findViewById(R.id.list_header_title);
        section.setText(text);
        LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f);
        section.setLayoutParams(params);
        layout.addView(section);


        return layout;
    }
}