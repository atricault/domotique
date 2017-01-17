package fr.domotique.module.thermostat.message;

import java.net.URLEncoder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.domotique.message.IMessage;
import fr.domotique.message.InfoMessage;
import fr.domotique.message.UnknownMesssageException;
import fr.domotique.properties.CommonDomotiqueProperties;

public class BuzzerInfoMessage extends InfoMessage {
	static Logger logger = LogManager.getLogger(BuzzerInfoMessage.class.getName());
	
	
	public BuzzerInfoMessage(String message) throws UnknownMesssageException {
		super(message);
	}
	
	public IMessage sendAlert() {
		try {
			if("1".equals(this.getMsgVal())){
				String urlToCall = CommonDomotiqueProperties.getInstance().getProperty("buzzer_urlalerte");
				
				urlToCall += URLEncoder.encode("Le buzzer est allumé","UTF-8");
				
				String[] result = getUrlContent(urlToCall, "");
			}
		} catch (Exception e) {
			logger.error("Erreur sur l'alerte d'allumage du buzzer");
		}
		return null;
	}

}
