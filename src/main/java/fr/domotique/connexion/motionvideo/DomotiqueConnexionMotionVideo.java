/**
 * 
 */
package fr.domotique.connexion.motionvideo;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import fr.domotique.connexion.DomotiqueConnexion;
import fr.domotique.message.IMessage;
import fr.domotique.module.videomonitoring.message.VideoActivateMessage;

/**
 * @author okamaugo
 *
 */
public class DomotiqueConnexionMotionVideo extends DomotiqueConnexion {

	private static Logger logger = LogManager.getLogger(DomotiqueConnexionMotionVideo.class.getName());
	
	private URL urlServer;
	
	
	/** Constructeur privé */	
	private DomotiqueConnexionMotionVideo(){
		if(logger.isDebugEnabled()){
			logger.debug("Chargement de la connexion à l'API HTTP Motion");
		}
		try {
			String serverHost = MotionVideoProperties.getInstance().getProperty("motion_server");
			int serverPort = Integer.parseInt(MotionVideoProperties.getInstance().getProperty("motion_server_port"));
			urlServer = new URL("HTTP", serverHost, serverPort, "");
			if(logger.isDebugEnabled()){
				logger.debug("Le serveur Motion est actif sur " + urlServer.toString());
			}
		} catch (MalformedURLException e) {
			logger.error("Impossible de charger l'URL du serveur, utilisation de l'URL par défaut : localhost:8080", e);
		}catch (Exception e) {
			logger.error("Impossible de charger l'URL du serveur, utilisation de l'URL par défaut : localhost:8080", e);
		}
		
	}
 
	/** Holder */
	private static class SingletonHolder
	{		
		/** Instance unique non préinitialisée */
		private final static DomotiqueConnexionMotionVideo instance = new DomotiqueConnexionMotionVideo();
	}
 
	public static DomotiqueConnexionMotionVideo getInstance() {
		
		return SingletonHolder.instance;
	}


	
	@Override
	public void reset() {
	}

	@Override
	public void sendMessage(IMessage message) {
		if(message instanceof VideoActivateMessage){
			try {
				if ("ON".equals(((VideoActivateMessage) message).getMsgVal())){
					URL urlActivate = new URL(urlServer, "0/detection/start");
					URLConnection cnx = urlActivate.openConnection();
					cnx.connect();
					cnx.getInputStream();
					if(logger.isDebugEnabled()){
						logger.debug("Activation de la détection de mouvement par videosurveillance sur le serveur Motion");
					}
				}else if ("OFF".equals(((VideoActivateMessage) message).getMsgVal())){
					URL urlDesactivate = new URL(urlServer, "0/detection/pause");
					URLConnection cnx = urlDesactivate.openConnection();
					cnx.connect();
					cnx.getInputStream();
					if(logger.isDebugEnabled()){
						logger.debug("Desactivation de la détection de mouvement par videosurveillance sur le serveur Motion");
					}
				}
			} catch (IOException e) {
				logger.error("Impossible de contacter le server Motion sur "+urlServer.toString(), e);
			}
		}
	
	
	}



	@Override
	public Calendar getLastReceivedMesssage() {
		//On ne gère pas de message entrant
		return null;
	}
	
	


}
