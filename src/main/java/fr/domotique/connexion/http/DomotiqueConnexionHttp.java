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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;


@SuppressWarnings("restriction")
public class DomotiqueConnexionHttp extends DomotiqueConnexion  {
	private static Logger logger = LogManager.getLogger(DomotiqueConnexionHttp.class.getName());
	private HttpServer server;
	
	/** Constructeur privé */	
	private DomotiqueConnexionHttp(){
		initHttpServer();
	}
	
	/** Holder */
	private static class SingletonHolder
	{		
		/** Instance unique non préinitialisée */
		private final static DomotiqueConnexionHttp instance = new DomotiqueConnexionHttp();
	}
 
	public static DomotiqueConnexionHttp getInstance() {
		
		return SingletonHolder.instance;
	}

	private void initHttpServer(){
		if(logger.isDebugEnabled()){
			logger.debug("Démarrage de la connexion HTTP");
		}
		try{
			server = HttpServer.create(new InetSocketAddress(Integer.parseInt(DomotiqueConnexionHttpProperties.getInstance().getProperty("http_server_port"))), 0);
			server.createContext("/simple", new DomotiqueConnexionHttpSimpleContext());
			server.createContext("/json", new DomotiqueConnexionJsonContext());
			server.setExecutor(null); // creates a default executor
	        server.start();
	        this.setStarted();
		} catch (IOException e) {
			logger.error("Erreur lors du démarrage du serveur HTTP", e);
		}
	}
	    
	@Override
	public void reset() {
		this.stop();
		initHttpServer();
	}

	@Override
	public void sendMessage(IMessage message) {
		//Uniquement en réception
	}

	@Override
	public void stop() {
		super.stop();
		server.stop(5);
	}

	@Override
	public Calendar getLastReceivedMesssage() {
		//On ne gère pas de message entrant
		return null;
	}

}
