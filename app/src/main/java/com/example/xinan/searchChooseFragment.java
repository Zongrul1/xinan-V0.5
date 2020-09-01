package com.example.xinan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.android.tu.loadingdialog.LoadingDailog;
import com.example.xinan.db.Content;
import com.example.xinan.util.HttpUtil;
import com.example.xinan.util.Utility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class searchChooseFragment extends Fragment {
    private ListView listView;
    private ContentAdapter adapter;
    List<Content> cons = new ArrayList<>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.choose_area, container, false);
        listView = (ListView) view.findViewById(R.id.list_view);
        adapter = new ContentAdapter(getContext(), R.layout.search_content, cons);
        listView.setAdapter(adapter);
        return view;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        //loading
        LoadingDailog.Builder loadBuilder=new LoadingDailog.Builder(getActivity())
                .setMessage("加载中...")
                .setCancelable(false)
                .setCancelOutside(false);
        final LoadingDailog dialog=loadBuilder.create();
        dialog.show();
        final Timer t = new Timer();
        t.schedule(new TimerTask() {
            public void run() {
                dialog.dismiss();
                t.cancel();
            }
        }, 2000);
        //request
        String indexString = prefs.getString("search", null);
        if(indexString == null) {
            requestSearch();
        }
        else{
            Utility.handleContentResponse(cons,indexString);
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Content m = cons.get(position);
                Intent intent =new Intent(getActivity(),ShowActivity.class);
                Bundle bundle=new Bundle();
                bundle.putString("id", String.valueOf(m.getId()));
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    public void requestSearch() {
        String Url = "https://xnxz.top/wc/getCard?type=1&page=1&search=";
        HttpUtil.sendOkHttpRequest(Url, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                Utility.handleContentResponse(cons,responseText);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
//                        editor.putString("search", responseText);
//                        editor.apply();
                        adapter.notifyDataSetChanged();
                        listView.setSelection(0);
                    }
                });
            }
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //closeProgressDialog();
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        //loadBingPic();
    }
}
