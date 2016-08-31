package com.pactera.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.URL;
import java.util.*;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import net.sf.json.JSONObject;

public class RoleUtil {

	private String UAAEncoding="Basic YWRtaW46bXl1YWE=";
	private String TokenURL="https://d106d990-b436-4079-a3e7-4f9ef38839bc.predix-uaa.run.aws-usw02-pr.ice.predix.io/oauth/token";
	private String CheckMembershipURL="https://d106d990-b436-4079-a3e7-4f9ef38839bc.predix-uaa.run.aws-usw02-pr.ice.predix.io/Groups/groupID/members/memberID";

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
//        	ce.printStackTrace();
        } catch (Exception e) {
//        	e.printStackTrace();
        }
        return jsonObject;
    }
    
    //get UAA token
    private String getUAAToken(){
    	Map<String,String> headers=new HashMap<String,String>();
    	headers.put("Pragma","no-cache");
    	headers.put("Content-Type","application/x-www-form-urlencoded");
    	headers.put("Accept","application/json");
    	headers.put("Cache-Control","no-cache");
    	headers.put("authorization",UAAEncoding);
    	headers.put("Connection","keep-alive");
    	String body="grant_type=client_credentials";
    	
    	String token =httpRequest(TokenURL,"POST",body,headers).get("access_token").toString();
    	return token;
    }
    
    //check membership
    private boolean checkGroupMembership(String groupID,String memberID,String token){
    	CheckMembershipURL=CheckMembershipURL.replace("groupID", groupID);
    	CheckMembershipURL=CheckMembershipURL.replace("memberID", memberID);
    	
    	Map<String,String> headers=new HashMap<String,String>();
    	headers.put("Authorization", "bearer "+token);
    	
    	Boolean value;
    	try{
    	   httpRequest(CheckMembershipURL,"GET",null,headers).toString();
    	   value=true;
    	}catch(Exception ex){
    		value=false;
    	}

    	return value;
    }
    
    //finds out groups
    public String checkGroups(String memberID){
    	String role="-1";
    	String token=getUAAToken();
    	System.out.println(token);
    	if(checkGroupMembership("feea4630-59c5-4eb4-b263-a182ab5097ed",memberID,token)){
    		role="user";
    	}else if(checkGroupMembership("feea4630-59c5-4eb4-b263-a182ab5097ed",memberID,token)){
    		role="warehouse";
    	}else if (checkGroupMembership("feea4630-59c5-4eb4-b263-a182ab5097ed",memberID,token)){
    		role="admin";
    	}
    	return role;
    }
    
}
