package com.example.sushiyu.smartshot;

import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by sushiyu on 2018/6/13.
 */

public class Update {

    public static void software_update() throws IOException {
        Log.e("TAG", "software_update");
        //String github_url = "https://github.com/mcfire2018/SmartShoot/blob/master/version.xml";
        String github_url = "https://www.baidu.com";
        byte[] bytes = HttpsUtil.doGet(github_url);
        byte[] bytes1 = {0x1,0x2,0x3};
        Log.e("TAG", Arrays.toString(bytes1));
        Log.e("TAG", Arrays.toString(bytes));
        //FileOutputStream fos = new FileOutputStream("D:/bing.picture-of-day.jpg");
        //fos.write(bytes);
        //fos.close();
        System.out.println("done!");
    }
}
