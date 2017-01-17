package fr.domotique.module.saver.webget;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;

public class WebClient {

	
	protected static String[] getUrlContent(String urlToCall, String params) throws IOException{
		ArrayList<String> arrayResult = new ArrayList<String>();
		
		URL url = new URL(urlToCall + "?" + params);
		HttpURLConnection  cnx = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
		
		cnx.setReadTimeout(10000);
		cnx.setConnectTimeout(10000);
		cnx.setRequestMethod("GET");
		cnx.setRequestProperty("Content-Type", 
		           "application/x-www-form-urlencoded");
		cnx.setRequestProperty("Content-Length", "" + 
           Integer.toString(params.getBytes().length));
		cnx.setRequestProperty("Content-Language", "en-US");  
		cnx.setUseCaches (false);
		cnx.setInstanceFollowRedirects(true);
		cnx.setDoInput(true);
		cnx.setDoOutput(true);
		
		OutputStream os = cnx.getOutputStream();
		DataOutputStream wr = new DataOutputStream(os);
		wr.flush();
				
		BufferedReader reader = new BufferedReader(new InputStreamReader(cnx.getInputStream()));
        String ligne;
        while ((ligne = reader.readLine()) != null) {
        	if(! ligne.trim().equals("")){
        		arrayResult.add(ligne);
        	}
        }
        String[] result = new String[arrayResult.size()];
        wr.close();
        reader.close();
        cnx.disconnect();
    
       return result;
		
		
	}
	
}
