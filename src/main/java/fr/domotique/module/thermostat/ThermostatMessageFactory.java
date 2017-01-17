package fr.domotique.module.thermostat;

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
import fr.domotique.module.thermostat.message.ForcedTempConsignInfoMessage;
import fr.domotique.module.thermostat.message.GazInfoMessage;
import fr.domotique.module.thermostat.message.TempConsignInfoMessage;
import fr.domotique.module.thermostat.message.TempIntInfoMessage;
import fr.domotique.properties.CommonDomotiqueProperties;
import fr.domotique.message.UnknownMesssageException;

public class ThermostatMessageFactory implements IMessageFactory{

	/** Holder */
	private static class SingletonHolder
	{		
		/** Instance unique non préinitialisée */
		private final static ThermostatMessageFactory instance = new ThermostatMessageFactory();
	}
 
	public static ThermostatMessageFactory getInstance() {
		return SingletonHolder.instance;
	}
	
	/** Constructeur privé */	
	private ThermostatMessageFactory(){
	}
 
	@Override
	public IMessage[] buildMessage(String message) throws UnknownMesssageException {
		IMessage[] returnMsg = new IMessage[1];
		String[] msgPart= message.split(";");
		if("C".equals(msgPart[0])){
			returnMsg[0] = new ConfigMessage(message);
		}else if("I".equals(msgPart[0])){
			if("TI".equals(msgPart[2])){
				returnMsg[0] = new TempIntInfoMessage(message);
			}else if("TC".equals(msgPart[2])){
				returnMsg[0] = new TempConsignInfoMessage(message);
			}else if("FTC".equals(msgPart[2])){
				returnMsg[0] = new ForcedTempConsignInfoMessage(message);
			}else if("GZ".equals(msgPart[2])){
				returnMsg[0] = new GazInfoMessage(message);
			}else if("BZ".equals(msgPart[2])){
				returnMsg[0] = new BuzzerInfoMessage(message);
			}else{
				throw new UnknownMesssageException("Type de message inconnu");
			}
		}
		if(returnMsg[0] != null){
			Calendar now = Calendar.getInstance();
			returnMsg[0].setReceptionDate(now);
		}
		return returnMsg;
	}
}
