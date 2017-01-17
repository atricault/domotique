package fr.domotique.module.videomonitoring.message;

import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.domotique.DomotiqueMessageManagerProxy;
import fr.domotique.message.IMessage;
import fr.domotique.message.Message;
import fr.domotique.message.OrdreInfoMessage;
import fr.domotique.message.OrdreMessage;
import fr.domotique.message.UnknownMesssageException;
import fr.domotique.module.saver.ISaverResult;
import fr.domotique.module.thermostat.ThermostatModule;
import fr.domotique.module.thermostat.ThermostatProperties;
import fr.domotique.properties.CommonDomotiqueProperties;

public class VideoActivateMessage extends OrdreMessage {
	private static Logger logger = LogManager.getLogger(VideoActivateMessage.class.getName());

	
    //Valeurs possibles ASK;ON;OFF;ACTIV;DESA
	public VideoActivateMessage(String message) throws UnknownMesssageException{
		super(message);
		this.setMsgValType("VD");
	}
	
	public VideoActivateMessage(){
		super();
		this.setMsgValType("VD");
	}
	
	
	@Override
	public void postTreatment(ISaverResult result){
		
	}

}
