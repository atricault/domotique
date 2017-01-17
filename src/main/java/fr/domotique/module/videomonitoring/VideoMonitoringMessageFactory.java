package fr.domotique.module.videomonitoring;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.domotique.message.AskOrdreMessage;
import fr.domotique.message.ErrorMessage;
import fr.domotique.message.IMessage;
import fr.domotique.message.IMessageFactory;
import fr.domotique.message.InfoMessage;

import fr.domotique.message.OrdreMessage;
import fr.domotique.message.manager.IMessageManager;
import fr.domotique.module.saver.ISaver;
import fr.domotique.module.thermostat.message.BuzzerInfoMessage;
import fr.domotique.module.thermostat.message.ConfigMessage;
import fr.domotique.module.thermostat.message.GazInfoMessage;
import fr.domotique.module.thermostat.message.TempConsignInfoMessage;
import fr.domotique.module.thermostat.message.TempIntInfoMessage;
import fr.domotique.module.videomonitoring.message.VideoActivateMessage;
import fr.domotique.properties.CommonDomotiqueProperties;
import fr.domotique.message.UnknownMesssageException;

public class VideoMonitoringMessageFactory implements IMessageFactory{

	/** Holder */
	private static class SingletonHolder
	{		
		/** Instance unique non préinitialisée */
		private final static VideoMonitoringMessageFactory instance = new VideoMonitoringMessageFactory();
	}
 
	public static VideoMonitoringMessageFactory getInstance() {
		return SingletonHolder.instance;
	}
	
	/** Constructeur privé */	
	private VideoMonitoringMessageFactory(){
	}
 
	@Override
	public IMessage[] buildMessage(String message) throws UnknownMesssageException {
		IMessage[] returnMsg = new IMessage[1];
		String[] msgPart= message.split(";");
		if(msgPart[2].startsWith("TC")){
			returnMsg[0] = new TempConsignInfoMessage(message);
		}else if(msgPart[2].startsWith("VD")){
			returnMsg[0] = new VideoActivateMessage(message);
		}else{
			throw new UnknownMesssageException("Type de message inconnu");
		}
		
		if(returnMsg[0] != null){
			Calendar now = Calendar.getInstance();
			returnMsg[0].setReceptionDate(now);
		}
		return returnMsg;
	}
}
