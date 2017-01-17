package fr.domotique.module.thermostat.message;

import java.net.URLEncoder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.domotique.message.InfoMessage;
import fr.domotique.message.UnknownMesssageException;
import fr.domotique.properties.CommonDomotiqueProperties;

public class TempIntInfoMessage extends InfoMessage{
	static Logger logger = LogManager.getLogger(TempIntInfoMessage.class.getName());
	
	public TempIntInfoMessage(){
		super();
		this.setMsgType("I");
		this.setMsgValType("TI");
	}
	
	public TempIntInfoMessage(String message) throws UnknownMesssageException {
		super(message);
	}

}
