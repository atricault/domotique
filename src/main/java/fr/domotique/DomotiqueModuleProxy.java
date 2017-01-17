package fr.domotique;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.domotique.message.IMessage;
import fr.domotique.message.IMessageFactory;
import fr.domotique.message.manager.IMessageManager;
import fr.domotique.module.IDomotiqueModule;

public class DomotiqueModuleProxy implements IDomotiqueModule{
	private static Logger logger = LogManager.getLogger(DomotiqueModuleProxy.class.getName());
	
	private ArrayList<IDomotiqueModule> listOfModules = new ArrayList<IDomotiqueModule>();
	private boolean running = false;
	/** Constructeur privé */	
	private DomotiqueModuleProxy(){

	}
 
	/** Holder */
	private static class SingletonHolder
	{		
		/** Instance unique non préinitialisée */
		private final static DomotiqueModuleProxy instance = new DomotiqueModuleProxy();
	}
	
	public static DomotiqueModuleProxy getInstance() {
		return SingletonHolder.instance;
	}

	public void addModuleToManage(IDomotiqueModule module){
		listOfModules.add(module);
	}
	

	@Override
	public void treateMessage(IMessage msg) {
		Iterator<IDomotiqueModule> iteModules = listOfModules.iterator();
		while(iteModules.hasNext()){
			IDomotiqueModule module = iteModules.next();
			module.treateMessage(msg);
		}
	}
	
	@Override
	public void treateMessage(String msg) {
		Iterator<IDomotiqueModule> iteModules = listOfModules.iterator();
		while(iteModules.hasNext()){
			IDomotiqueModule module = iteModules.next();
			try{
				module.treateMessage(msg);
			}catch(Exception ex){
				logger.info("Erreur sur le traitement du message " + msg.toString() + " par le module " + module.getClass().getName(), ex);
			}
		}
	}

	@Override
	public void shutdown() {
		Iterator<IDomotiqueModule> iteModules = listOfModules.iterator();
		while(iteModules.hasNext()){
			IDomotiqueModule module = iteModules.next();
			try{
				module.shutdown();
			}catch(Exception ex){
				logger.info("Erreur sur l'arret du module " + module.getClass().getName(), ex);
			}
			
		}
	}
	
}
