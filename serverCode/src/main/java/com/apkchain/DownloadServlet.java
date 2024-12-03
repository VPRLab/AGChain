package com.apkchain;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.io.FileOutputStream;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.RandomStringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import io.ipfs.api.IPFS;
import io.ipfs.api.MerkleNode;
import io.ipfs.api.NamedStreamable;
import io.ipfs.multihash.Multihash;

import java.net.URL;
import java.util.ArrayList;

/**
 * Servlet implementation class DownloadServlet
 */
public class DownloadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DownloadServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

    // Use stream to download file
    private static void downloadUsingStream(String urlStr, String file) throws IOException{
    	System.out.println("Enter in file downloading");
    	File f = new File(file); 
    	long startTime=System.currentTimeMillis();
    	if(f.createNewFile()) {
    		URL url = new URL(urlStr);
            BufferedInputStream bis = new BufferedInputStream(url.openStream()); 
            FileOutputStream fis = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int count=0;
            while((count = bis.read(buffer,0,1024)) != -1)
            {
                fis.write(buffer, 0, count);
            }
            fis.close();
            bis.close();
    	}
    	long endTime=System.currentTimeMillis();
    	System.out.println("File Download time:  "+(endTime-startTime)+"ms");   
        
    }
    
    protected static String getMD5(File file){
        FileInputStream fileInputStream = null;
        try {
            MessageDigest MD5 = MessageDigest.getInstance("MD5");
            fileInputStream = new FileInputStream(file);
            byte[] buffer = new byte[8192];
            int length;
            while ((length = fileInputStream.read(buffer)) != -1) {
                MD5.update(buffer, 0, length);
            }
            return new String(Hex.encodeHex(MD5.digest()));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (fileInputStream != null){
                    fileInputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    protected static int checkMD5(String file_path, String md5_got) {
    	//0 represents secure check pass, 1 represents that no md5 for reference, 2 means the security pass failed
    	if (md5_got.contains("undefined")) {
    		return 1;
    	}else {
    		File file = new File(file_path);
    		String md5 = getMD5(file).trim();
    		System.out.println("The calculated md5 of downloaded APK is: " + md5);
    		System.out.println("The result of MD5 check is: " + md5.equals(md5_got));
    		if (md5.equals(md5_got)) {
    			return 0;
    		}else {
    			return 2;
    		}
    	}
    }
    
    //set the download parameter
    protected String download(String url){
//      String url = "http://219.76.13.166/hot.m.shouji.360tpcdn.com/191226/f12d61c38ae18278f83e0948c693d71f/com.tencent.mm_1580.apk";
      System.out.println("Enter in url parsing");
      int index = url.lastIndexOf("/");
      String  name = "";
      if(index > 0){
          name = url.substring(index + 1);
          String path = name.substring(0,10);
    	  String regEx = "[`~!@#$%^&*()\\-+={}':;,\\[\\].<>/?��%������_+|������������������������\\s]";
          Pattern p = Pattern.compile(regEx);
          Matcher m = p.matcher(path);
          String name_tmp = m.replaceAll("1");
          String random_str = RandomStringUtils.randomAlphanumeric(6);
          if(name_tmp.trim().length()>0){
              if(name_tmp.trim().contains(".apk")){  
                  name = "/home/ubuntu/agchain/apks/" +name_tmp+random_str + ".apk"; // ubuntu: /home/ubuntu/apkChain/
              }else{
                  name = "/home/ubuntu/agchain/apks/" +name_tmp+random_str + ".apk"; // ubuntu: /home/ubuntu/apkChain/
              }
          }
      }
      try {
          downloadUsingStream(url, name);
          
      } catch (IOException e) {
          e.printStackTrace();
      }
      return name;
    }
    
    private static String uploadIPFS(String path) {
    	try {
    		long IPFSStart=System.currentTimeMillis();
    		File file_get = new File(path);
    		IPFS ipfs = new IPFS("/ip4/127.0.0.1/tcp/5001");
            ipfs.refs.local();
            NamedStreamable.FileWrapper file = new NamedStreamable.FileWrapper(file_get);
            MerkleNode addResult = ipfs.add(file).get(0);
            JSONObject jsonObj = new JSONObject(addResult.toJSONString());
            String file_hash = (String) jsonObj.get("Hash");
            
            System.out.println("The file IPFS hash is: " + file_hash);
            long IPFSEnd=System.currentTimeMillis();
            System.out.println("IPFS Upload total time:  "+(IPFSEnd-IPFSStart)+"ms");
            return file_hash;
    	}catch (Exception e) {
            System.out.println("Exception: " + e);
            return "";
        }
    	
    }
    
    
    
    
    
    // parse the html page and get the download link of apk file
    private static String GetDownlaodUrl(String url) {
        String apkUrl = "";
        System.out.println("Enter in HTML parsing");
        long startTime=System.currentTimeMillis();
        if(url.contains("shouji.baidu.com")){
            // https://shouji.baidu.com/software/26603332.html
            // baidu
            try{
                Document doc = Jsoup.connect(url).timeout(50000).get();
                Elements links = doc.select("a[href]");
                Elements spans = doc.select("span");
                String md5Url = "";
                ArrayList<String> apk_urls = new ArrayList<String>();
                for (Element link : links) {
                    if (link.text().equals("APK�ļ�")){
                        apk_urls.add(link.attr("abs:href"));
                    }
                }
                for (Element span : spans) {
                    if (span.text().equals("һ����װ")){
                        md5Url = span.attr("data_url");
                    }
                }
                apkUrl = apk_urls.get(0);     
                
                String[] arr = md5Url.split("/");
                String md5 = arr[arr.length-1].replace(".apk","");
                System.out.println("The md5 got from app market is: " + md5);
                //b444362ff30d4b18fb38877811f313f1
                String tmp = "b444362ff30d4b18fb38877811f313f1";
                if(md5.length()==tmp.length()) {
                	apkUrl =  apkUrl + "," + md5; //apkUrl
                }else {
                	apkUrl =  apkUrl + "," + "undefined"; //apkUrl
                }
                System.out.println("The apkUrl is: " + apkUrl);
                
            }catch (IOException e){
                e.printStackTrace();
            }
        }else if(url.contains("m.app.so.com")) {
            // https://m.app.so.com/detail/index?pname=com.sina.weibo&id=2012
            // 360
            try{
                Document doc = Jsoup.connect(url).timeout(50000).get();
                Elements links = doc.select("a");
                String md5 = "";
                ArrayList<String> apk_urls = new ArrayList<String>();
                for (Element link : links) {
                    if (link.text().equals("��ȫ����")){
                    	 String url_link = link.attr("abs:data-url");
                    	 apk_urls.add(url_link);
                         String[] arr = url_link.split("/");
                         md5 = arr[4];
                    }
                }
                apkUrl = apk_urls.get(0); // url.get(0) is the apk url
                apkUrl = apkUrl + "," + md5; 
                System.out.println("The apkUrl is: " + apkUrl);
            }catch (IOException e){
                e.printStackTrace();
            }
        }else if (url.contains("www.lenovomm.com")){
            //https://www.lenovomm.com/appdetail/com.qiyi.video/800110750?type=1&cate=1038&cateName=%E5%BD%B1%E8%A7%86.%E8%A7%86%E9%A2%91
            //lenovo mm
            try{
                Document doc = Jsoup.connect(url).timeout(5000).get();
                Elements scripts = doc.select("script");
                Elements links = doc.select("a");
                String md5 = "";
                
                for (Element script : scripts) {
                    if(script.data().contains("NEXT_DATA")){
                        int intIndex = script.data().indexOf("apkmd5");
                        md5 = script.data().substring(intIndex+9,intIndex+41);
//                        System.out.println(md5);
                    }
                }
                for (Element link : links) {
                    if(link.text().contains("����")){
                        if(link.attr("href").contains("3g.lenovomm.com")){
                        	apkUrl = link.attr("href");
                        }

                    }
                }
                apkUrl = apkUrl + "," + md5;
                
                System.out.println("The apkUrl is: " + apkUrl); // url.get(0) is the apk url
            }catch (IOException e){
                e.printStackTrace();
            }

        } else if (url.contains("www.appchina.com")) {
            // http://www.appchina.com/app/tv.danmaku.bili
            // app china
            try{
                Document doc = Jsoup.connect(url).timeout(50000).get();
                Elements links = doc.select("a");
                Elements js = doc.getElementsByTag("script").eq(11);
                String jvs = js.toString();
                int index = jvs.indexOf("var md5");
                String md5 = jvs.substring(index+11,index+43);
                
                
                String apk_url = "";
                for (Element link : links) {
                    if (link.text().equals("�������")){
                        String[] strs =  link.attr("onclick").split("\'");
                        apk_url = strs[1];
                    }
                }
                apkUrl = apk_url + "," + md5; // apk_url is the apk url.
                System.out.println("The apkUrl is: " + apkUrl);
                
            }catch (IOException e){
                e.printStackTrace();
            }
        }else if (url.contains("zhushou.sogou.com")){
            // http://zhushou.sogou.com/apps/detail/14695.html
            String[] getAppId = url.trim().split("/");
            String appId = getAppId[getAppId.length-1].replace(".html","").trim();
            System.out.println(appId);
            String request_url = "http://zhushou.sogou.com/apps/download.html?appid="+appId;
            // http://zhushou.sogou.com/apps/download.html?appid=14695
            try{
                CloseableHttpClient httpclient = HttpClients.createDefault();
                HttpGet get = new HttpGet(request_url);
                HttpResponse response = httpclient.execute(get);
                HttpEntity entity = response.getEntity();
                String jsonStr = EntityUtils.toString(entity,"utf-8");
                JSONObject jsonObj = new JSONObject(jsonStr);
                JSONObject result = jsonObj.getJSONObject("data");
                String apk_url = result.getString("file_url");
                apkUrl = apk_url + "," + "undefined";
                System.out.println("The apkUrl is: " + apkUrl); // apk_url is the apk URL
                
                
            }catch (IOException e){
                e.getMessage().toString();
            }
        }else if (url.contains("app.meizu.com")){
           // https://app.meizu.com/apps/public/detail?package_name=org.iggymedia.periodtracker
            String appId = "";
            try{
                Document doc = Jsoup.connect(url).timeout(5000).get();
                Elements links = doc.select("input");
                for (Element link : links) {
                    if (link.attr("data-appid")!=""){
                        appId = link.attr("data-appid");
                    }
                }
            }catch (IOException e){
                e.printStackTrace();
            }
            String request_url = "https://app.meizu.com/apps/public/download.json?app_id="+appId;
            // https://app.meizu.com/apps/public/download.json?app_id=315709
            try{
                CloseableHttpClient httpclient = HttpClients.createDefault();
                HttpGet get = new HttpGet(request_url);
                HttpResponse response = httpclient.execute(get);
                HttpEntity entity = response.getEntity();
                String jsonStr = EntityUtils.toString(entity,"utf-8");
                JSONObject jsonObj = new JSONObject(jsonStr);
                JSONObject result = jsonObj.getJSONObject("value");
                String apk_url = result.getString("downloadUrl");
                apkUrl = apk_url + "," + "undefined";
                System.out.println("The apkUrl is: " + apkUrl);
                
                
            }catch (IOException e){
                e.getMessage().toString();
            }
            
            
        }else if (url.contains("www.anzhi.com")){
            // http://www.anzhi.com/pkg/3d81_com.tencent.tmgp.pubgmhd.html
            String appId = "";
            try{
                Document doc = Jsoup.connect(url).timeout(50000).get();
                Elements links = doc.select("a");
                for (Element link : links) {
                    if (link.text().contains("���ص�����")){
                        String js = link.attr("onclick");
                        appId = js.substring(js.indexOf("(")+1,js.indexOf(")"));
                    }
                }
            }catch (IOException e){
                e.printStackTrace();
            }
            String apk_url = "http://www.anzhi.com/dl_app.php?s="+appId + "&n=5";
            apkUrl = apk_url + "," + "undefined";
            System.out.println("The apkUrl is: " + apkUrl);
            // http://www.anzhi.com/dl_app.php?s=3199976&n=5
        }else if (url.contains("github.com")){
        	String[] arr = url.split("/");
            arr[2] = "raw.githubusercontent.com";
            int index = 0;
            for (int i = 0; i< arr.length; i ++){
                if(arr[i].equals("blob")){
                    index = i;
                }
            }
            ArrayList<String> list = new ArrayList<>(Arrays.asList(arr));
            list.remove(index);
            String result = list.get(0);
            for(int i = 1; i < list.size(); i++) {
                result = result + "/" + list.get(i);
            }
            System.out.println("the result is: " + result);
            apkUrl = result + "," + "undefined";
        }
        for (int i = 5; i>=0; i --){
        	if (apkUrl != ""){
                break;
            }
                try{
                    Thread.sleep(1000);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                
        }
        long endTime=System.currentTimeMillis();
        System.out.println("HTML Parsing time:  "+(endTime-startTime)+"ms");  
        return apkUrl;
    }

    
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(request,response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.setContentType("text/html;charset=utf-8");
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        try (PrintWriter out = response.getWriter()){
            String url = request.getParameter("url").trim();
            long startTime=System.currentTimeMillis();
            JSONObject jsonObject = new JSONObject();
            String[] arr = GetDownlaodUrl(url).split(",");
            String apk_url = arr[0];
            String md5_got = arr[1];
            System.out.println("The md5_got in array is: " + md5_got);
            Map<String, String> params = new HashMap<>();
            params.put("DownloadLink", apk_url);
            String file_name = download(apk_url);
            params.put("FilePath", file_name);
            if(!file_name.equals("")){
                int check = checkMD5(file_name,md5_got);
                //0 represents secure check pass, 1 represents that no md5 for reference, 2 means the security pass failed
                if (check == 0) {  
                	String ipfs_Hash = uploadIPFS(file_name);
                	long startDup=System.currentTimeMillis();
                	boolean duplicate = UserDAO.ExistAPK(ipfs_Hash);
                	long endDup=System.currentTimeMillis();
                    System.out.println("Duplicate check time:  "+(endDup-startDup)+"ms");
                	System.out.println("Duplicate Check: " + duplicate);
                    params.put("Result", "success");  
                    params.put("Md5", "checked");  
                    if (duplicate) {
                    	params.put("duplicate", "true");  
                    }else {
                    	params.put("duplicate", "false");  
                    }
                }else if (check==1){
                	String ipfs_Hash = uploadIPFS(file_name);
                	long startDup=System.currentTimeMillis();
                	boolean duplicate = UserDAO.ExistAPK(ipfs_Hash);
                	long endDup=System.currentTimeMillis();
                    System.out.println("Duplicate check time:  "+(endDup-startDup)+"ms");
                	System.out.println("in doPost: " + duplicate);
                    params.put("Result", "success");  
                    params.put("Md5", "NoReference");  
                    if (duplicate) {
                    	params.put("duplicate", "true");  
                    }else {
                    	params.put("duplicate", "false");  
                    }
                }else if (check==2){
                	params.put("Result", "success");  
                    params.put("Md5", "Failed");  
                }
            }else{
                 params.put("Result", "failed");
            }
            jsonObject.put("params", params);
            long endTime=System.currentTimeMillis();
            System.out.println("Server side Phase I time:  "+(endTime-startTime)+"ms");   
            out.print(jsonObject);
        }
	}
//test backup
}
