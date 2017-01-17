package fr.domotique.module.thermostat.message;

import java.net.URLEncoder;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.domotique.DomotiqueConnexionProxy;
import fr.domotique.DomotiqueMessageManagerProxy;
import fr.domotique.message.IMessage;
import fr.domotique.message.InfoMessage;
import fr.domotique.message.OrdreInfoMessage.MSG_VAL_TYPE;
import fr.domotique.message.OrdreMessage;
import fr.domotique.message.UnknownMesssageException;
import fr.domotique.module.thermostat.ThermostatProperties;
import fr.domotique.properties.CommonDomotiqueProperties;

public class GazInfoMessage extends InfoMessage {
	static private Logger logger = LogManager.getLogger(GazInfoMessage.class.getName());
	static private HashMap<String, Boolean> buzzersEtatParZone = new HashMap<String, Boolean>();
	
	public GazInfoMessage(String message) throws UnknownMesssageException {
		super(message);
		Boolean buzzerEtat = false;
		Boolean buzzerCurrentEtat = false;
		if(buzzersEtatParZone.containsKey(this.zone)){
			buzzerCurrentEtat = buzzersEtatParZone.get(this.zone); 
		}
		
		if(Integer.parseInt(this.getMsgVal()) > Integer.parseInt(ThermostatProperties.getInstance().getProperty("gaz_limite"))){
			//Allumer le buzzer
			buzzerEtat = true;	
		}
		if(! buzzersEtatParZone.containsKey(this.zone) || buzzerEtat != buzzersEtatParZone.get(this.zone)){
			buzzersEtatParZone.put(this.zone, buzzerEtat);
		}
		
		if(buzzerCurrentEtat.equals(buzzerEtat)){
			OrdreMessage msgOnBuzzer;
			if(buzzerEtat){
				msgOnBuzzer = new OrdreMessage(this.zone,MSG_VAL_TYPE.BZ , "1");
			}else{
				msgOnBuzzer = new OrdreMessage(this.zone,MSG_VAL_TYPE.BZ , "0");
			}
			DomotiqueMessageManagerProxy.getInstance().addMessageToSend(msgOnBuzzer);
		}
	}

}
