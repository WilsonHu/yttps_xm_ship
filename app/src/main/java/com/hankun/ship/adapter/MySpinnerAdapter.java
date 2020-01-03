package com.hankun.ship.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.hankun.ship.R;

import java.util.List;

public class MySpinnerAdapter extends BaseAdapter implements SpinnerAdapter {

    private Context context;
    private List<String> list;

    public MySpinnerAdapter(Context context, List<String> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(context).inflate(R.layout.simple_list_item, null);
        TextView textView = (TextView) convertView.findViewById(R.id.spinner_text);
        textView.setTypeface(textView.getResources().getFont(R.font.comic));
        textView.setText(getItem(position).toString());
        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(context).inflate(R.layout.simple_list_item, null);
        TextView textView = (TextView) convertView.findViewById(R.id.spinner_text);
        textView.setText(getItem(position).toString());
        textView.setTypeface(textView.getResources().getFont(R.font.comic));
        return convertView;
    }
}
