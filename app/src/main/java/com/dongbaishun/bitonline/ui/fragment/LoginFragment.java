package com.dongbaishun.bitonline.ui.fragment;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.dongbaishun.bitonline.NetUrl.NetState;
import com.dongbaishun.bitonline.NetUrl.NetUrl;
import com.dongbaishun.bitonline.NetUrl.PingTest;
import com.dongbaishun.bitonline.R;
import com.dongbaishun.bitonline.Util.MLog;
import com.dongbaishun.bitonline.Util.MyToast;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by DongBaishun on 2016/7/26.
 */
public class LoginFragment extends Fragment {
  EditText editAccountText;
  EditText editPassText;
  String editAccount = "";
  String editPass = "";
  Button log_post;
  CheckBox checkBoxSaveMe;
  CheckBox checkBoxAutoLogin;

  boolean isPingSucceed = false;

  private boolean checkSaveMe = NetState.checkSaveMe;
  private boolean checkAutoLogin = NetState.checkAutoLogin;

  final static String TAG = "networkTaggggggg";

  OkHttpClient client = new OkHttpClient();

  SharedPreferences pref;

  private Handler mainhandler;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstaceState) {

    View view = inflater.inflate(R.layout.loginfragment, container, false);

    pref = this.getActivity().getSharedPreferences("myNetworkLoginInfo", 0);

    editAccountText = (EditText) view.findViewById(R.id.network_account);
    editPassText = (EditText) view.findViewById(R.id.network_editPass);
    log_post = (Button) view.findViewById(R.id.button_network);
    checkBoxSaveMe = (CheckBox) view.findViewById(R.id.network_save_me);
    checkBoxAutoLogin = (CheckBox) view.findViewById(R.id.network_auto_login);

    boolean isAutoLogin = pref.getBoolean("isAutoLogin", false);
    if (isAutoLogin) {
      loginNetworkbyAuto();
      NetState.loginSucceedbyAuto = true;
      loginNetwork();
    } else {
      //loginNetworkbyEdit();
    /*getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (PingTest.ping()) {
          MLog.iLog(TAG, "ping succeed");
          MyToast.SToast(getActivity(), "网络可用");
          isPingSucceed = true;
        } else {
          isPingSucceed = false;
          MyToast.SToast(getActivity(), "网络不可用");
        }
      }
    });*/
      boolean isSaveMe = pref.getBoolean("isSaveMe", false);
      if (isSaveMe) {
        String setAccountContent = "";
        String setPasswordContent = "";
        setAccountContent = pref.getString("networkAccount", "");
        setPasswordContent = pref.getString("networkPassword", "");
        editAccountText.setText(setAccountContent);
        editPassText.setText(setPasswordContent);
      }

      loginNetworkbyEdit();
    }

    mainhandler = new Handler() {
      public void handleMessage(Message msg) {
        MyToast.SToast(getActivity(), msg.obj.toString());
        switch (msg.what) {
          case 1:
            NetState.Network_login_state = 1;
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            Fragment fragment = null;
            if (NetState.loginSucceedbyAuto) {
              fragment = new OnlineAutoFragment();
            } else {
              fragment = new OnlineFragment();
            }
            ft.replace(R.id.container, fragment);
            ft.commit();
            break;
          case -1:
            // TODO 测试是否能显示“请重试”
            MyToast.SToast(getActivity(), "请重试"); //可能显示不出来~测试下
            NetState.Network_login_state = 0;
            loginNetworkbyEdit();
            break;
          default:
            MyToast.SToast(getActivity(), "嘿嘿嘿");
        }
      }
    };

    return view;//null
  }

  private void loginNetworkbyAuto() {
    editAccount = pref.getString("networkAccount", "");
    editPass = pref.getString("networkPassword", "");
  }

  private void loginNetworkbyEdit() {
    log_post.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        editAccount = editAccountText.getText().toString();
        editPass = editPassText.getText().toString();
        checkSaveMe = checkBoxSaveMe.isChecked();
        checkAutoLogin = checkBoxAutoLogin.isChecked();
        MLog.iLog(TAG, editAccount + "Edit");
        MLog.iLog(TAG, editPass + "Edit");
//        checkAutoLogin = checkBoxAutoLogin.isChecked();

        SharedPreferences.Editor editor = pref.edit();
        if (checkSaveMe) {
          editor.putString("networkAccount", editAccount);
          editor.putString("networkPassword", editPass);
          editor.putBoolean("isSaveMe", true);
          if (checkAutoLogin) {
            NetState.checkAutoLogin = true;
            NetState.checkSaveMe = true;
            editor.putBoolean("isAutoLogin", true);
          } else {
            editor.putBoolean("isAutoLogin", false);
          }
        } else {
          editor.putBoolean("isSaveMe", false);
        }
        editor.apply();  //editor.commit();
        MLog.iLog(TAG, "adjust Toast");
        if (editAccount.equals("") || editPass.equals("")) {
          MLog.iLog(TAG, "null");
          Toast.makeText(getActivity(), "输入不能为空", Toast.LENGTH_SHORT).show();
//          MyToast.SToast(getActivity(), "输入不能不为空");
        } else {
          MLog.iLog(TAG, "noToast");
          loginNetwork();
        }
      }
    });
  }

  private String isConnectOk(String str) {
    String result = "";
    String isOK = str.substring(0, 8);
    if (isOK.equals("login_ok")) {
      result = "登录成功";
    } else if (str.substring(0, 5).equals("E2553")) {
      //NetState.Network_login_state = 0;
      result = "密码错误";
    } else if (str.substring(0, 5).equals("E2531")) {
      //NetState.Network_login_state = 0;
      result = "用户不存在";
    }
    return result;
  }

  private void loginNetwork() {
    new Thread(new Runnable() {
      @Override
      public void run() {
        Log.e("当前线程：", "" + Thread.currentThread().getName());

        Headers headers = new Headers.Builder()
                .add("Accept", "*/*")
                .add("Content-Type", "application/x-www-form-urlencoded")
                .add("Accept-Language", "zh-CN,zh;q=0.8")
                .build();

        FormBody body = new FormBody.Builder()
                .add("ac_id", "1")
                .add("action", "login")
                .add("ajax", "1")
                .add("password", editPass)
                .add("save_me", "0")
                .add("username", editAccount)
                .build();

        final Request request = new Request.Builder()
                .url(NetUrl.NETWORK_LOGIN)
                .headers(headers)
                .post(body)
                .build();

        Response response = null;
        try {
          response = client.newCall(request).execute();

          if (response.isSuccessful()) {
            String responseBody = response.body().string();
            MLog.iLog(TAG, responseBody);
            String ToastResult = isConnectOk(responseBody);
            MLog.iLog(TAG, ToastResult);
            Message msg = new Message();
            if (ToastResult.equals("登录成功")) {
              msg.obj = "登录成功";
              msg.what = 1;
            } else {
              msg.obj = "登录失败";
              msg.what = -1;
            }
            mainhandler.sendMessage(msg);
          } else {
            MLog.iLog(TAG, response.body().string());
            MLog.iLog(TAG, "网络请求失败");
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }).start();
  }
}
