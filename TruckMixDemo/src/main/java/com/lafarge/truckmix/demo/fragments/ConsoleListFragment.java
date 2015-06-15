package com.lafarge.truckmix.demo.fragments;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ArrayAdapter;
import com.lafarge.truckmix.demo.R;

public class ConsoleListFragment extends ListFragment {

    private static final String TAG = "ConsoleListFragment";

    private ArrayAdapter<String> mAdapter;

    public ConsoleListFragment() {
        // Required empty public constructor
    }

    public static ConsoleListFragment newInstance() {
        ConsoleListFragment fragment = new ConsoleListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item);
        mAdapter.setNotifyOnChange(true);
        setListAdapter(mAdapter);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getListView().setDivider(null);
    }

    public void clear() {
        mAdapter.clear();
    }

    public void addLog(String log) {
        mAdapter.add(log);

        getListView().post(new Runnable() {
            @Override
            public void run() {
                getListView().setSelection(mAdapter.getCount() - 1);
            }
        });
    }
}
