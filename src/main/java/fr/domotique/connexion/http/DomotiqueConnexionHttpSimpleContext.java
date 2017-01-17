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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;


@SuppressWarnings("restriction")
public class DomotiqueConnexionHttpSimpleContext implements HttpHandler  {
	private static Logger logger = LogManager.getLogger(DomotiqueConnexionHttpSimpleContext.class.getName());
	
	/** Constructeur privé */	
	public DomotiqueConnexionHttpSimpleContext(){
	}
		
    @Override
    public void handle(HttpExchange exchange) throws IOException {
    	DomotiqueConnexionHttpUtils.parseGetParameters(exchange);
    	DomotiqueConnexionHttpUtils.parsePostParameters(exchange);
         Map<String, Object> params = (Map<String, Object>)exchange.getAttribute("parameters");
    	if(params.containsKey("message")){
    		String message = (String) params.get("message");
    		DomotiqueMessageManagerProxy.getInstance().addMessageToTreate(message);
    	}
        String response = "OK";
        exchange.sendResponseHeaders(200, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

}
