package fr.domotique.connexion.http;

import fr.domotique.DomotiqueMessageManagerProxy;
import fr.domotique.connexion.DomotiqueConnexion;
import fr.domotique.message.IMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;


@SuppressWarnings("restriction")
public class DomotiqueConnexionJsonContext implements HttpHandler  {
	private static Logger logger = LogManager.getLogger(DomotiqueConnexionJsonContext.class.getName());
	
	/** Constructeur privé */	
	public DomotiqueConnexionJsonContext(){
	}
		
	@Override
    public void handle(HttpExchange exchange) throws IOException {
    	DomotiqueConnexionHttpUtils.parseGetParameters(exchange);
    	DomotiqueConnexionHttpUtils.parsePostParameters(exchange);
        Map<String, Object> params = (Map<String, Object>)exchange.getAttribute("parameters");
        
        Set<String> jsonDatas = params.keySet();
        for(String jsonData : jsonDatas){
        	if(jsonData.startsWith("{")){
        		JSONObject json = new JSONObject(jsonData);
        		JSONArray messages = json.getJSONArray("message");
        		for(Object jsonObj : messages){
        			DomotiqueMessageManagerProxy.getInstance().addMessageToTreate(jsonObj.toString());
        		}
        		
    		}
        }
    
    	JSONObject response = new JSONObject();
    	response.put("result", "OK");
        
    	String strReponse = JSONObject.quote(response.toString());
    	exchange.getResponseHeaders().add("Access-Control-Allow-Origin", DomotiqueConnexionHttpProperties.getInstance().getProperty("json_allow_origin", "*"));
        exchange.sendResponseHeaders(200, strReponse.length());
        OutputStream os = exchange.getResponseBody();
        os.write(strReponse.getBytes());
        os.close();
    }}
