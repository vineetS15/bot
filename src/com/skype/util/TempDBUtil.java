package com.skype.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class TempDBUtil {
	
	private static List<String>  nameList= new ArrayList();
	
	private static Map<String,String> map = new WeakHashMap<String,String>();
	
	public static Map<String,String> TOKENMAP = new WeakHashMap<String,String>();
	
	private static String token ;
	
	private static String expiry ;
   
	public static void storeIdentities(String id,String timestamp){
		map.put(id, timestamp);
		nameList.add(id);
		System.out.println("The new DB list is "+nameList);
	}
	
	public static boolean isActiveSession(String id){
		System.out.println("Currently map Contains "+map);
		return map.containsKey(id) ? true:false ;
	}
	
	public static boolean isThisFirstTime(String id){
		 return nameList.contains(id)?false:true;
		 
	}
	
	public static void setTokenMap(String token,String expiry){
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy H:mm:ss");
		String dateCreated = formatter.format(new Date().getTime());
		TOKENMAP.replace("token", token);
		TOKENMAP.replace("expiry",expiry);
		TOKENMAP.replace("dateCreated",dateCreated);
	}
	
	public static void configToken() {

		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy H:mm:ss");
		Date expireDate;
		Date now = new Date();
		if(TOKENMAP.get("token")==null){
			getAuthToken();
			setTokenMap(token, expiry);			
		}else{
			String expires = TOKENMAP.get("expiry");
			try {
				expireDate = formatter.parse(expires);			 
			if (now.compareTo(expireDate) > 0) {
				return;
			}else{
				getAuthToken();
				setTokenMap(token, expiry);	
			}	
			}catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	private static String getAuthToken()
	{
	    String token = "";
	    try{
            URL url = new URL("https://login.microsoftonline.com/common/oauth2/v2.0/token");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            
            conn.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream (
                    conn.getOutputStream ());
            
            wr.writeBytes ("client_id=8269e0eb-19c7-4994-8a1c-e88434ffd36e&client_secret=sc15m9AWzTmun6Ae7M7o4jL&grant_type=client_credentials&scope=https%3A%2F%2Fgraph.microsoft.com%2F.default");
            wr.flush ();
            wr.close ();
            
            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                (conn.getInputStream())));
            StringBuilder sb = new StringBuilder();
            String output;
            //System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                System.out.println(output);
                sb.append(output);
            }
            JSONObject jsonObject = (JSONObject) JSONValue.parse(sb.toString());
            token = (String) jsonObject.get("access_token");
            expiry = jsonObject.get("expires_in").toString();
            conn.disconnect();
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
	    System.out.println("The received token is "+token);
        return token;
	}

	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
	}
	
	

}
