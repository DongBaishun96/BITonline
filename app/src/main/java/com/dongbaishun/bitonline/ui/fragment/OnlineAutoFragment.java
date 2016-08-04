package com.dongbaishun.bitonline.ui.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.dongbaishun.bitonline.NetUrl.NetState;
import com.dongbaishun.bitonline.NetUrl.NetUrl;
import com.dongbaishun.bitonline.R;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by DongBaishun on 2016/8/4.
 */
public class OnlineAutoFragment extends Fragment {

  Button bt_logout;
  Button bt_cancel_auto;
  TextView tv;

  private Handler mainhandler;

  final static String TAG = "OnlineTaggggggg";

  OkHttpClient client = new OkHttpClient();

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstaceState) {

    View view = inflater.inflate(R.layout.onlineautofragment, container, false);
    bt_logout = (Button) view.findViewById(R.id.button_logout);
    bt_cancel_auto = (Button) view.findViewById(R.id.button_cancel_auto);
    tv = (TextView) view.findViewById(R.id.textHint);

    bt_logout.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        logout();
      }
    });

    bt_cancel_auto.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        SharedPreferences pref = getActivity().getSharedPreferences("myNetworkLoginInfo", 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("isAutoLogin", false);
        editor.apply();
        Message msg = new Message();
        msg.obj = "取消自动登录";
        msg.what = -2;
        mainhandler.sendMessage(msg);
      }
    });

    mainhandler = new Handler() {
      public void handleMessage(Message msg) {
        switch (msg.what) {
          case 1:
            tv.setText("注销成功");
            bt_logout.setVisibility(View.INVISIBLE);
            NetState.Network_login_state = 0;
            break;
          case -2:
            tv.setText("取消自动登录成功");
            bt_cancel_auto.setVisibility(View.INVISIBLE);
          default:
            tv.setText("注销错误");
        }
      }
    };
    return view;//null
  }

  private void logout() {

    new Thread(new Runnable() {
      @Override
      public void run() {
        Log.e("当前线程：" + TAG, "" + Thread.currentThread().getName());
        Headers headers = new Headers.Builder()
                .add("Upgrade-Insecure-Requests", "1")
                .add("Content-Type", "application/x-www-form-urlencoded")
                .add("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                .add("Accept-Encoding", "gzip, deflate")
                .add("Accept-Language", "zh-CN,zh;q=0.8")
                .build();

        FormBody body = new FormBody.Builder()
                .add("action", "auto_logout")
                .build();

        Request request = new Request.Builder()
                .url(NetUrl.NETWORK_LOGIN)
                .headers(headers)
                .post(body)
                .build();

        Response response = null;

        try {
          response = client.newCall(request).execute();
          if (response.isSuccessful()) {
            Message msg = new Message();
            msg.obj = "注销成功";
            msg.what = 1;
            mainhandler.sendMessage(msg);
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }).start();
  }
}
