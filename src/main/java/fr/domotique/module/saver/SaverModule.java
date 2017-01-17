package fr.domotique.module.saver;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.domotique.message.ErrorMessage;
import fr.domotique.message.IMessage;
import fr.domotique.message.InfoMessage;
import fr.domotique.message.UnknownMesssageException;
import fr.domotique.module.IDomotiqueModule;
import fr.domotique.module.common.CommonMessageFactory;
import fr.domotique.module.saver.logger.LogSaver;
import fr.domotique.module.saver.webget.WebGetErrorSaver;
import fr.domotique.module.saver.webget.WebGetSaver;


public class SaverModule implements IDomotiqueModule {
	private static Logger logger = LogManager.getLogger(SaverModule.class.getName());
	
	private HashMap<Class, ArrayList<ISaver>> savers = new HashMap<Class, ArrayList<ISaver>>();
	
	private SaverModule(){
		ArrayList<ISaver> stdSavers = new ArrayList<ISaver>();
		stdSavers.add(new LogSaver());
		stdSavers.add(new WebGetSaver());
		
		savers.put(InfoMessage.class, stdSavers);
		
		stdSavers.clear();
		stdSavers.add(new LogSaver());
		stdSavers.add(new WebGetErrorSaver());
		savers.put(ErrorMessage.class, stdSavers);
	}

	/** Holder */
	private static class SingletonHolder
	{		
		/** Instance unique non préinitialisée */
		private final static SaverModule instance = new SaverModule();
	}
	
	public static SaverModule getInstance() {
		return SingletonHolder.instance;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void treateMessage(IMessage msg) {
		if(msg != null){
			try {
				Set<Class> saversClasses = (Set<Class>) savers.keySet();
				Iterator<Class> iteSaversClass = saversClasses.iterator();
				while(iteSaversClass.hasNext()){
					Class saverClass = iteSaversClass.next();
					if(saverClass.isInstance(msg)){
						ArrayList<ISaver> tmpSavers = savers.get(saverClass);
						Iterator<ISaver> iteSavers = tmpSavers.iterator();
						while(iteSavers.hasNext()){
							iteSavers.next().saveMessage(msg);
						}
					}
				}
			} catch (Exception e) {
				logger.error("Erreur sur le traitement du message reçu " + msg.toString(), e);
			} 
		}
	}

	@Override
	public void treateMessage(String msg) {
		try {
			IMessage[] messages = CommonMessageFactory.getInstance().buildMessage(msg);
			if(messages != null){
				for(IMessage tmpMsg : messages){
					treateMessage(tmpMsg);
				}
			}
		} catch (UnknownMesssageException e) {
		}
	}

	@Override
	public void shutdown() {

	}

}
