package com.dongbaishun.bitonline.NetUrl;

import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;

import java.io.IOException;

/**
 * Created by DongBaishun on 2016/8/2.
 */
public class PingTest {

  private boolean isNetworkAvailable(Context context) {
    // 得到网络连接信息
    ConnectivityManager manager = (ConnectivityManager)
            context.getSystemService(Context.CONNECTIVITY_SERVICE);
    // 去进行判断网络是否连接
    if (manager.getActiveNetworkInfo() != null) {
      return manager.getActiveNetworkInfo().isAvailable();
    }
    return false;
  }

  public static final boolean ping() {
    String result = null;
    try {
      String ip = "http://www.baidu.com";// ping 的地址，可以换成任何一种可靠的外网
      Process p = Runtime.getRuntime().exec("ping -c 5 -w. 10 " + ip);// ping网址5次
      // 读取ping的内容，可以不加
      /*InputStream input = p.getInputStream();
      BufferedReader in = new BufferedReader(new InputStreamReader(input));
      StringBuffer stringBuffer = new StringBuffer();
      String content = "";
      while ((content = in.readLine()) != null) {
        stringBuffer.append(content);
      }
      Log.d("------ping-----", "result content : " + stringBuffer.toString());*/

      // ping的状态
      int status = p.waitFor();
      if (status == 0) {
        result = "success";
        return true;
      } else {
        result = "failed";
      }
    } catch (IOException e) {
      result = "IOException";
    } catch (InterruptedException e) {
      result = "InterruptedException";
    } finally {
      Log.d("----result---", "result = " + result);
    }
    return false;
  }

  public static String pingIpAddr() {
    String mPingIpAddrResult = null;
    try {
      // This is hardcoded IP addr. This is for testing purposes.
      // We would need to get rid of this before release.
      String ipAddress = "10.0.0.55";
      Process p = Runtime.getRuntime().exec("ping -c 1 " + ipAddress);
      int status = p.waitFor();
      if (status == 0) {
        mPingIpAddrResult = "Pass";
      } else {
        mPingIpAddrResult = "Fail: IP addr not reachable";
      }
    } catch (IOException e) {
      mPingIpAddrResult = "Fail: IOException";
    } catch (InterruptedException e) {
      mPingIpAddrResult = "Fail: InterruptedException";
    }
    return mPingIpAddrResult;
  }
}
