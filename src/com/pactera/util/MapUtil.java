package com.pactera.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

public class MapUtil {
	
	private  String userEncoding="bWFwMTptYXAx";
	private  String Token_URL="https://d106d990-b436-4079-a3e7-4f9ef38839bc.predix-uaa.run.aws-usw02-pr.ice.predix.io/oauth/token?grant_type=client_credentials";
    private  String Get_Collection_API="https://intelligent-mapping-prod.run.aws-usw02-pr.ice.predix.io/api/collections/";
	 /**
     * 发起https请求并获取结果
     *
     * @param requestUrl 请求地址
     * @param requestMethod 请求方式（GET、POST）
     * @param outputStr 提交的数据
     * @return JSONObject(通过JSONObject.get(key)的方式获取json对象的属性值)
     */
    private  JSONObject httpRequest(String requestUrl, String requestMethod, String outputStr,Map<String,String> headers) {
        JSONObject jsonObject = null;
        StringBuffer buffer = new StringBuffer();
        try {
            // 创建SSLContext对象，并使用我们指定的信任管理器初始化  
            TrustManager[] tm = {new MyX509TrustManager()};
            SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
            sslContext.init(null, tm, new java.security.SecureRandom());
            // 从上述SSLContext对象中得到SSLSocketFactory对象  
            SSLSocketFactory ssf = sslContext.getSocketFactory();

            URL url = new URL(requestUrl);
            HttpsURLConnection httpUrlConn = (HttpsURLConnection) url.openConnection();
            httpUrlConn.setSSLSocketFactory(ssf);
            if(null!=headers){
            	for(String key:headers.keySet()){
            		httpUrlConn.setRequestProperty(key,headers.get(key));
            	}
            }
//            httpUrlConn.setRequestProperty("Authorization","Basic "+ userEncoding);
            
            httpUrlConn.setDoOutput(true);
            httpUrlConn.setDoInput(true);
            httpUrlConn.setUseCaches(false);
            // 设置请求方式（GET/POST）  
            httpUrlConn.setRequestMethod(requestMethod);

            if ("GET".equalsIgnoreCase(requestMethod)) {
                httpUrlConn.connect();
            }

            // 当有数据需要提交时  
            if (null != outputStr) {
                OutputStream outputStream = httpUrlConn.getOutputStream();
                // 注意编码格式，防止中文乱码  
                outputStream.write(outputStr.getBytes("UTF-8"));
                outputStream.close();
            }

            // 将返回的输入流转换成字符串  
            InputStream inputStream = httpUrlConn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String str = null;
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            bufferedReader.close();
            inputStreamReader.close();
            // 释放资源  
            inputStream.close();
            inputStream = null;
            httpUrlConn.disconnect();
            jsonObject = JSONObject.fromObject(buffer.toString());
        } catch (ConnectException ce) {
//            log.error("Weixin server connection timed out.");
        	ce.printStackTrace();
        } catch (Exception e) {
//            log.error("https request error:{}", e);
        	e.printStackTrace();
        }
        return jsonObject;
    }
    
    public String getGeoJson(){
    	Map<String,String> headers=new HashMap<String,String>();
    	headers.put("Authorization","Basic "+ userEncoding);
    	String token=httpRequest(Token_URL,"GET",null,headers).get("access_token").toString();
    	
    	Map<String,String> header2=new HashMap<String,String>();
    	header2.put("Content-Type", "application/json");
    	header2.put("Authorization", "Bearer "+token);
    	header2.put("Predix-Zone-Id", "1242fb86-fdc1-4cc1-9649-b544bfdfbbe9");
    	
    	return httpRequest(Get_Collection_API+"cmap","GET",null,header2).toString();
    }
}
