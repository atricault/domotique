package fr.domotique.module.thermostat.message;

import java.net.URLEncoder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.domotique.message.InfoMessage;
import fr.domotique.message.UnknownMesssageException;
import fr.domotique.properties.CommonDomotiqueProperties;

public class ForcedTempConsignInfoMessage extends TempConsignInfoMessage{
	static Logger logger = LogManager.getLogger(ForcedTempConsignInfoMessage.class.getName());
	
	public ForcedTempConsignInfoMessage(){
		super();
		this.setMsgType("I");
		this.setMsgValType("FTC");
	}
	
	public ForcedTempConsignInfoMessage(String message) throws UnknownMesssageException {
		super(message);
	}

}
