package com.skype.bot;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.skype.util.TempDBUtil;

/**
 * Servlet implementation class HandleIncomingMessages
 */
@WebServlet("/HandleIncomingMessages")
public class HandleIncomingMessages extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public HandleIncomingMessages() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TODO Auto-generated method stub
      
//      String token = getAuthToken();
      String url = "https://apis.skype.com/v3/conversations/";
      StringBuffer jb = new StringBuffer();
      String line = null;
      String respMessage = null;
      String from  = null;
      try {
        BufferedReader reader = request.getReader();
        while ((line = reader.readLine()) != null)
          jb.append(line);
      } catch (Exception e) { /*report an error*/ }
      /*JSONArray jsonArray = (JSONArray) JSONValue.parse(jb.toString());
    JSONObject jsonObject= (JSONObject) jsonArray.get(0);*/

      JSONObject jsonObject= (JSONObject) JSONValue.parse(jb.toString());
      JSONObject jsonObject1 = (JSONObject) jsonObject.get("from");
      String idFrom = jsonObject1.get("id").toString();
      url = url + idFrom + "/activities/";

      //IF user is landing first time
      if(TempDBUtil.isThisFirstTime(idFrom)){
    	  respMessage = "Hey <b>"+ jsonObject1.get("name")+ "</b> May i help you" ;
    	  TempDBUtil.storeIdentities(idFrom, jsonObject1.get("name").toString());
      }//If user has active session
      else if(TempDBUtil.isActiveSession(idFrom)){
    	  respMessage = "What a lovely day today!" ;    			  
      }//User is returning
      else{
    	  respMessage = "Welcome back "+ jsonObject1.get("name");
      }
     
	System.out.println("Complete Post URL is "+url);
	
	TempDBUtil.configToken();
    URL obj = new URL(url);
    HttpURLConnection con = (HttpURLConnection) obj.openConnection();
    con.setRequestMethod("POST");
    con.setRequestProperty("Content-Type", "application/json");
    con.setRequestProperty("Authorization", "Bearer "+/*getAuthToken()*/TempDBUtil.TOKENMAP.get("token"));
    // For POST only - START
    con.setDoOutput(true);
  //Send request
    JSONObject obj2 = new JSONObject();
    obj2.put("type", "message"+"/"+"text");
    obj2.put("text", respMessage);
    
    DataOutputStream wr = new DataOutputStream (
                con.getOutputStream ());
    wr.writeBytes (obj2.toString());
    System.out.println(obj2.toString());
    wr.flush ();
    wr.close ();
    // For POST only - END
    int responseCode = con.getResponseCode();
    System.out.println("POST Response Code :: " + responseCode);
    if (responseCode == HttpURLConnection.HTTP_OK) { //success
        BufferedReader in = new BufferedReader(new InputStreamReader(
                con.getInputStream()));
        String inputLine;
        StringBuffer sb = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            sb.append(inputLine);
        }
        in.close();

        // print result
    } else {
        System.out.println("POST request not worked");
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
            //expiry =(Long) jsonObject.get("expires_in");
            conn.disconnect();
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
	    System.out.println("The received token is "+token);
        return token;
	}
	
}
