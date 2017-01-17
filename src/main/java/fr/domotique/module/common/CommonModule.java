package fr.domotique.module.common;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.domotique.DomotiqueModuleProxy;
import fr.domotique.message.IMessage;
import fr.domotique.message.InfoMessage;
import fr.domotique.message.Message;
import fr.domotique.message.UnknownMesssageException;
import fr.domotique.module.IDomotiqueModule;
import fr.domotique.module.saver.ISaver;
import fr.domotique.module.thermostat.message.ConfigMessage;
import fr.domotique.properties.CommonDomotiqueProperties;

public class CommonModule implements IDomotiqueModule{
	private static Logger logger = LogManager.getLogger(CommonModule.class.getName());
	private Hashtable<String, Hashtable<String, InfoMessage>> currentValues = new Hashtable<String, Hashtable<String, InfoMessage>>();
	

	private CommonModule(){
		
	}

	/** Holder */
	private static class SingletonHolder
	{		
		/** Instance unique non préinitialisée */
		private final static CommonModule instance = new CommonModule();
	}
	
	public static CommonModule getInstance() {
		return SingletonHolder.instance;
	}
	
	@Override
	public void treateMessage(String msg) {
		
		try {
			IMessage[] messages = CommonMessageFactory.getInstance().buildMessage(msg);
			for(IMessage tmpMsg : messages){
				treateMessage(tmpMsg);
			}
		} catch (UnknownMesssageException e) {
			if(logger.isDebugEnabled()){
				logger.debug("Erreur sur le traitement du message " + msg, e);
			}
		}
	}
	
	@Override
	public void treateMessage(IMessage msg) {
		if("I".equals(msg.getMsgType())){
			Hashtable<String, InfoMessage> curValForZone = currentValues.get(msg.getZone());
			if(curValForZone == null){
				curValForZone = new Hashtable<String, InfoMessage>();
			}
			curValForZone.put(((InfoMessage) msg).getMsgValType().toString(), (InfoMessage) msg);
			currentValues.put(msg.getZone(), curValForZone);
			
			if("0".equals(msg.getZone())){
				//On est sur la zone 0. On ajoute les valeurs dans toutes les autres zones
				for(String tmpZone : currentValues.keySet()){
					if(! "0".equals(tmpZone)){
						curValForZone = currentValues.get(tmpZone);
						if(curValForZone == null){
							curValForZone = new Hashtable<String, InfoMessage>();
						}
						curValForZone.put(((InfoMessage) msg).getMsgValType().toString(), (InfoMessage) msg);
						currentValues.put(tmpZone , curValForZone);
					}
				}
			}
		}
	}

	public Hashtable<String, Hashtable<String, InfoMessage>> getCurrentValues() {
		return currentValues;
	}

	public void removeCurrentValueForZone(String zone, String key){
		Hashtable<String, InfoMessage> curValForZone = currentValues.get(zone);
		if(curValForZone == null){
			return;
		}
		curValForZone.remove(key);
		
	}
	
	@Override
	public void shutdown() {

	}
	
}
