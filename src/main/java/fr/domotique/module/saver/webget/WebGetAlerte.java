package fr.domotique.module.saver.webget;

import java.net.URLEncoder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.domotique.message.IMessage;
import fr.domotique.module.saver.ISaver;
import fr.domotique.module.saver.ISaverResult;
import fr.domotique.properties.CommonDomotiqueProperties;

public class WebGetAlerte implements ISaver {

	private static Logger logger = LogManager.getLogger(WebGetAlerte.class.getName());
	
	public WebGetAlerte()
	{}


	@Override
	public ISaverResult saveMessage(IMessage msg) {
		try {
			String urlToCall = CommonDomotiqueProperties.getInstance().getProperty("url_alerte");
			
			urlToCall += URLEncoder.encode("Alerte survenue " + msg.toString(),"UTF-8");

			return (ISaverResult) new WebGetSaverResult(ApacheWebClientImpl.getUrlContent(urlToCall, ""));
			
		} catch (Exception e) {
			logger.error("Erreur sur l'alerte " + msg.toString(),e);
		}
		return null;
	}


}
