package com.example.sushiyu.smartshot;
  
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;  
import java.io.IOException;  
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;  
import java.net.URL;
import java.util.HashMap;
  
import android.app.AlertDialog;  
import android.app.Dialog;  
import android.app.AlertDialog.Builder;  
import android.content.Context;  
import android.content.DialogInterface;  
import android.content.Intent;  
import android.content.DialogInterface.OnClickListener;  
import android.content.pm.PackageManager.NameNotFoundException;  
import android.net.Uri;  
import android.os.Environment;  
import android.os.Handler;  
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;  
import android.widget.ProgressBar;  
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

public class UpdateManager  
{  
    /* 下载中 */  
    private static final int DOWNLOAD = 1;  
    /* 下载结束 */  
    private static final int DOWNLOAD_FINISH = 2;  
    /* 保存解析的XML信息 */  
    HashMap<String, String> mHashMap;  
    /* 下载保存路径 */  
    private String mSavePath;  
    /* 记录进度条数量 */  
    private int progress;  
    /* 是否取消更新 */  
    private boolean cancelUpdate = false;  
  
    private Context mContext;  
    /* 更新进度条 */  
    private ProgressBar mProgress;  
    private Dialog mDownloadDialog;  
  
    private Handler mHandler = new Handler()  
    {  
        public void handleMessage(Message msg)  
        {  
            switch (msg.what)  
            {  
            // 正在下载  
            case DOWNLOAD:  
                // 设置进度条位置  
                mProgress.setProgress(progress);  
                break;  
            case DOWNLOAD_FINISH:  
                // 安装文件  
                installApk();  
                break;  
            default:  
                break;  
            }  
        };  
    };  
  
    public UpdateManager(Context context)  
    {  
        this.mContext = context;  
    }  
  
    /** 
     * 检测软件更新 
     */  
    public void checkUpdate(String url)
    {  
        if (isUpdate(url))
        {  
            // 显示提示对话框  
            showNoticeDialog();  
        } else  
        {  
            Toast.makeText(mContext, R.string.soft_update_no, Toast.LENGTH_LONG).show();  
        }  
    }  
  
    /** 
     * 检查软件是否有更新版本 
     *  
     * @return 
     */

    private void doGet(String s) {
        //1、获得需要访问的server地址、方法、及参数键值
        final String serverAddress = s;//"https://pan.baidu.com/s/10wO7rqhuEGk3El2JcEfLrA";
        //访问网络，开启一个线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.e("TAG", "baidu ");
                    //2、创建URL对象，将上述访问地址传入；
                    URL url = new URL(serverAddress);
                    //3、调用URL.openConnection()方法返回HttpURLConnection对象
                    HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
                    //4、调用HttpURLConnection.connect()方法连接server

                    //needs Base64 encoder, apache.commons.codec

                    httpUrlConnection.connect();
                    //5、调用HttpURLConnection.respondCode()方法，根据返回码判断server返回是否正确
                    int code = httpUrlConnection.getResponseCode();
                    Log.e("TAG", "code "+code);
                    if (code == 200) {
                        Log.e("TAG", "connect to server! " + code);
                        //6、调用HttpURLConnection.getInputStream()方法读取server返回的内容
                        InputStream is = httpUrlConnection.getInputStream();
                        //包装流：字节流->转换字符流（处理流）->缓冲字符流（处理流）

                        BufferedReader br = new BufferedReader(new InputStreamReader(is));
                        StringBuffer sb = new StringBuffer();
                        String readLine = "";
                        while ((readLine = br.readLine()) != null) {
                            sb.append(readLine);

                        }
                        //7、关闭流、调用HttpURLConnection.disconnect()方法断开连接
                        is.close();
                        br.close();

                        httpUrlConnection.disconnect();
                        //Log日志中显示返回结果

                        Log.e("TAG", sb.toString());


                    } else {

                        Log.e("TAG", "failed to connect server! " + code);
                    }


                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();

                }
            }
        }).start();


    }

    private boolean isUpdate(String url)
    {  
        // 获取当前软件版本


        int save_code;
        save_code = 0;
        Log.e("soft_update","AAA");
        //doGet("https://pan.baidu.com/s/10wO7rqhuEGk3El2JcEfLrA");
        doGet(url);

        int versionCode = getVersionCode(mContext);
        Log.e("soft_update", "versionCode = "+versionCode);
        Log.e("soft_update", "save_code = "+save_code);
        // 把version.xml放到网络上，然后获取文件信息  
        InputStream inStream = ParseXmlService.class.getClassLoader().getResourceAsStream("version.xml");
        // 解析XML文件。 由于XML文件比较小，因此使用DOM方式进行解析  
        ParseXmlService service = new ParseXmlService();  
        try  
        {
            mHashMap = service.parseXml(inStream);
        } catch (Exception e)
        {
            e.printStackTrace();  
        }  
        if (null != mHashMap)  
        {
            Log.e("soft_update", "null != mHashMap");
            int serviceCode = Integer.valueOf(mHashMap.get("version"));  
            // 版本判断  
            if (serviceCode > versionCode)  
            {
                Log.e("soft_update", "true");
                return true;  
            }  
        }
        Log.e("soft_update", "false");
        return false;  
    }  
  
/** 
 * 获取软件版本号 
 *  
 * @param context 
 * @return 
 */  
private int getVersionCode(Context context)  
{  
    int versionCode = 0;  
    try  
    {  
        // 获取软件版本号，对应AndroidManifest.xml下android:versionCode  
        versionCode = context.getPackageManager().getPackageInfo("com.example.sushiyu.smartshot", 0).versionCode;
    } catch (NameNotFoundException e)  
    {  
        e.printStackTrace();  
    }  
    return versionCode;  
}  
  
    /** 
     * 显示软件更新对话框 
     */  
    private void showNoticeDialog()  
    {  
        // 构造对话框  
        AlertDialog.Builder builder = new Builder(mContext);  
        builder.setTitle(R.string.soft_update_title);  
        builder.setMessage(R.string.soft_update_info);  
        // 更新  
        builder.setPositiveButton(R.string.soft_update_updatebtn, new OnClickListener()  
        {  
            @Override  
            public void onClick(DialogInterface dialog, int which)  
            {  
                dialog.dismiss();  
                // 显示下载对话框  
                showDownloadDialog();  
            }  
        });  
        // 稍后更新  
        builder.setNegativeButton(R.string.soft_update_later, new OnClickListener()  
        {  
            @Override  
            public void onClick(DialogInterface dialog, int which)  
            {  
                dialog.dismiss();  
            }  
        });  
        Dialog noticeDialog = builder.create();  
        noticeDialog.show();  
    }  
  
    /** 
     * 显示软件下载对话框 
     */  
    private void showDownloadDialog()  
    {  
        // 构造软件下载对话框  
        AlertDialog.Builder builder = new Builder(mContext);  
        builder.setTitle(R.string.soft_updating);  
        // 给下载对话框增加进度条  
        final LayoutInflater inflater = LayoutInflater.from(mContext);  
        View v = inflater.inflate(R.layout.softupdate_progress, null);  
        mProgress = (ProgressBar) v.findViewById(R.id.update_progress);  
        builder.setView(v);  
        // 取消更新  
        builder.setNegativeButton(R.string.soft_update_cancel, new OnClickListener()  
        {  
            @Override  
            public void onClick(DialogInterface dialog, int which)  
            {  
                dialog.dismiss();  
                // 设置取消状态  
                cancelUpdate = true;  
            }  
        });  
        mDownloadDialog = builder.create();  
        mDownloadDialog.show();  
        // 现在文件  
        downloadApk();  
    }  
  
    /** 
     * 下载apk文件 
     */  
    private void downloadApk()  
    {  
        // 启动新线程下载软件  
        new downloadApkThread().start();  
    }  
  
    /** 
     * 下载文件线程 
     *  
     * @author coolszy 
     *@date 2012-4-26 
     *@blog http://blog.92coding.com 
     */  
    private class downloadApkThread extends Thread  
    {  
        @Override  
        public void run()  
        {  
            try  
            {  
                // 判断SD卡是否存在，并且是否具有读写权限  
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))  
                {  
                    // 获得存储卡的路径  
                    String sdpath = Environment.getExternalStorageDirectory() + "/";  
                    mSavePath = sdpath + "download";  
                    URL url = new URL(mHashMap.get("url"));  
                    // 创建连接  
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();  
                    conn.connect();  
                    // 获取文件大小  
                    int length = conn.getContentLength();  
                    // 创建输入流  
                    InputStream is = conn.getInputStream();  
  
                    File file = new File(mSavePath);  
                    // 判断文件目录是否存在  
                    if (!file.exists())  
                    {  
                        file.mkdir();  
                    }  
                    File apkFile = new File(mSavePath, mHashMap.get("name"));  
                    FileOutputStream fos = new FileOutputStream(apkFile);  
                    int count = 0;  
                    // 缓存  
                    byte buf[] = new byte[1024];  
                    // 写入到文件中  
                    do  
                    {  
                        int numread = is.read(buf);  
                        count += numread;  
                        // 计算进度条位置  
                        progress = (int) (((float) count / length) * 100);  
                        // 更新进度  
                        mHandler.sendEmptyMessage(DOWNLOAD);  
                        if (numread <= 0)  
                        {  
                            // 下载完成  
                            mHandler.sendEmptyMessage(DOWNLOAD_FINISH);  
                            break;  
                        }  
                        // 写入文件  
                        fos.write(buf, 0, numread);  
                    } while (!cancelUpdate);// 点击取消就停止下载.  
                    fos.close();  
                    is.close();  
                }  
            } catch (MalformedURLException e)  
            {  
                e.printStackTrace();  
            } catch (IOException e)  
            {  
                e.printStackTrace();  
            }  
            // 取消下载对话框显示  
            mDownloadDialog.dismiss();  
        }  
    };  
  
    /** 
     * 安装APK文件 
     */  
    private void installApk()  
    {  
        File apkfile = new File(mSavePath, mHashMap.get("name"));  
        if (!apkfile.exists())  
        {  
            return;  
        }  
        // 通过Intent安装APK文件  
        Intent i = new Intent(Intent.ACTION_VIEW);  
        i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");  
        mContext.startActivity(i);  
    }  
}
/*
try {
            /*
            URL myURL = new URL("https://github.com/mcfire2018/SmartShoot/blob/master/version.xml");
            // 创建HttpsURLConnection对象，并设置其SSLSocketFactory对象
            HttpsURLConnection httpsConn = (HttpsURLConnection) myURL
                    .openConnection();
            // 取得该连接的输入流，以读取响应内容
            InputStreamReader insr = new InputStreamReader(httpsConn
                    .getInputStream());
            httpsConn.setConnectTimeout(10000);
            // 读取服务器的响应内容并显示
            int respInt = insr.read();
            while (respInt != -1) {
                Log.e("soft_update","respInt "+respInt);
                System.out.print((char) respInt);
                respInt = insr.read();
            }
            Log.e("soft_update","respInt end "+respInt);
            */

        //SSLContext sslcontext = SSLContext.getInstance("TLSv1");
        //sslcontext.init(null, null, null);

        //SSLSocketFactory NoSSLv3Factory = new NoSSLv3SocketFactory(sslcontext.getSocketFactory());

        //HttpsURLConnection.setDefaultSSLSocketFactory(NoSSLv3Factory);

        /*URL url=new URL("https://pan.baidu.com/s/10wO7rqhuEGk3El2JcEfLrA");
        Log.e("soft_update","BBB");
        HttpsURLConnection connection= (HttpsURLConnection) url.openConnection();
        Log.e("soft_update","CCC");
        //设置请求方式‘
        connection.setRequestMethod("GET");
        Log.e("soft_update","DDD");
        //设置请求连接超时的时间（优化）
        connection.setConnectTimeout(5000);
        Log.e("soft_update","EEE");
        //获取结果码
        int code=connection.getResponseCode();
        Log.e("soft_update","FFF code");
        Log.e("soft_update",""+code);
        save_code = code;
        if(code==200){
        //获取服务器返回过来的结果
        InputStream is=connection.getInputStream();*/
        //打印（读）--》测试
//                    BufferedReader br=new BufferedReader(new InputStreamReader(is));
//                    String str=null;
//                    while((str=br.readLine())!=null){
//                        Log.i("test",str);
//                    }
        //解析XML
        //01.使用DOM解析
//                    DocumentBuilderFactory documentBuilderFactory=DocumentBuilderFactory.newInstance();
//                    DocumentBuilder documentBuilder=documentBuilderFactory.newDocumentBuilder();
//                    Document document=documentBuilder.parse(is);
//                    //获取跟标签
//                    Element root=document.getDocumentElement();
//                    Log.i("test","跟标签："+root.getNodeName());
//
//                    //获取<persons>下面的所有的子标签<person>
//                    NodeList nodeList=root.getElementsByTagName("person");
//                    for (int i = 0; i <nodeList.getLength() ; i++) {
//                        //获取单个
//                        //Node
//                        //Element
//                        Element personElement= (Element) nodeList.item(i);
//                        //获取<person>属性id的值
//                        String id=personElement.getAttribute("id");
//                        Log.i("test",id);
//
//                        //获取<person>下面的子标签<name><age><image>的值
//                        Element nameElement= (Element) personElement.getElementsByTagName("name").item(0);
//                        String name=nameElement.getTextContent();
//                        Element ageElement= (Element) personElement.getElementsByTagName("age").item(0);
//                        String age=ageElement.getTextContent();
//                        Element imageElement= (Element) personElement.getElementsByTagName("image").item(0);
//                        String image=imageElement.getTextContent();
//
//                        Log.i("test",name+" "+age+" "+image);
//                    }

        //02.SAX(边读边解析，基于事件（方法）驱动方式)

//                    SAXParserFactory saxParserFactory=SAXParserFactory.newInstance();
//                    SAXParser saxParser=saxParserFactory.newSAXParser();
//
//                    saxParser.parse(is,new DefaultHandler(){
//                        @Override
//                        public void startDocument() throws SAXException {
//                            super.startDocument();
//                        }
//
//                        @Override
//                        public void endDocument() throws SAXException {
//                            super.endDocument();
//                        }
//
//                        @Override
//                        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
//                            super.startElement(uri, localName, qName, attributes);
//                            cuurentTag=localName;
//                            //获取开始标签的名字
//                            if("person".equals(localName)){
//                                //取属性的值
//                                String id=attributes.getValue(0);
//                                Log.i("test",id);
//                            }
//
//
//                        }
//
//                        @Override
//                        public void endElement(String uri, String localName, String qName) throws SAXException {
//                            super.endElement(uri, localName, qName);
//                            cuurentTag=null;
//                        }
//
//                        @Override
//                        public void characters(char[] ch, int start, int length) throws SAXException {
//                            super.characters(ch, start, length);
//                            if("name".equals(cuurentTag)){
//                                //获取<name>的值
//                                String name=new String(ch,start,length);
//                                Log.i("test", "   "+name);
//                            }else if("age".equals(cuurentTag)){
//                                //获取<name>的值
//                                String age=new String(ch,start,length);
//                                Log.i("test", "   "+age);
//                            }else if("image".equals(cuurentTag)){
//                                //获取<name>的值
//                                String image=new String(ch,start,length);
//                                Log.i("test", "   "+image);
//                            }
//                        }
//                    });

        //03.使用PULL解析（类似SAX）
        /*XmlPullParser xmlPullParser= Xml.newPullParser();
        xmlPullParser.setInput(is,"UTF-8");
        Log.e("soft_update","fuck https");
        //获取解析的标签的类型
        int type=xmlPullParser.getEventType();
        while(type!=XmlPullParser.END_DOCUMENT){
        switch (type) {
        case XmlPullParser.START_TAG:
        //获取开始标签名字
        String starttagName=xmlPullParser.getName();
        if("version".equals(starttagName)){
        //获取id的值
        String id=xmlPullParser.getAttributeValue(0);
        Log.e("soft_update",id);
        }else if("name".equals(starttagName)){
        String name=xmlPullParser.nextText();
        Log.e("soft_update",name);
        }else if("url".equals(starttagName)){
        String age=xmlPullParser.nextText();
        Log.e("soft_update",age);
        }else if("image".equals(starttagName)){
        String image=xmlPullParser.nextText();
        Log.e("soft_update",image);
        }
        break;
        case XmlPullParser.END_TAG:
        break;
        }
        //细节：
        type=xmlPullParser.next();
        }



        }
        //结果码（状态）
        //成功：200
        //
        //未修改：304
        } catch (MalformedURLException e) {
        e.printStackTrace();
        } catch (IOException e) {
        e.printStackTrace();
        } catch (XmlPullParserException e) {
        e.printStackTrace();
        }*/