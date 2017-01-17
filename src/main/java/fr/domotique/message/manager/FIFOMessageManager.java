package fr.domotique.message.manager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.ConcurrentModificationException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.SchedulerException;

import fr.domotique.message.IMessage;
import fr.domotique.message.InfoMessage;
import fr.domotique.message.Message;
import fr.domotique.module.thermostat.message.ConfigMessage;
import fr.domotique.properties.CommonDomotiqueProperties;



public class FIFOMessageManager extends MessageManager{
	
	private static Logger logger = LogManager.getLogger(FIFOMessageManager.class.getName());
	private LinkedBlockingQueue<String> msgATraiter = new LinkedBlockingQueue<String>();
	private LinkedBlockingQueue<IMessage> msgAEnvoyer = new LinkedBlockingQueue<IMessage>();
	
	private FIFOMessageReceive fifoReceive;
	private FIFOMessageSend fifoSend;
	
	private boolean running = true;
	
	/** Constructeur privé */	
	private FIFOMessageManager()
	{
		fifoReceive = new FIFOMessageReceive(msgATraiter);
		fifoSend = new FIFOMessageSend(msgAEnvoyer);
		
	}
 
	/** Holder */
	private static class SingletonHolder
	{		
		/** Instance unique non préinitialisée */
		private final static FIFOMessageManager instance = new FIFOMessageManager();
	}
 
	public static IMessageManager getInstance() {
		return SingletonHolder.instance;
	}
	
	@Override
	public void addMessageToTreate(String msg) {
		msgATraiter.add(msg);
	}
	@Override
	public void addMessageToSend(IMessage msg){
		if(logger.isDebugEnabled()){
			logger.debug("Message a envoyer, mise en file d'attente " + msg.encode());
		}
		//On ajoute a la pile de msg a envoyer
		try {
			msgAEnvoyer.put(msg);
		} catch (InterruptedException e) {
			logger.debug("Erreur sur la mise en queue du message " + msg.encode(), e);
		}
	}

	@Override
	public void stop() {
		fifoReceive.shutdown();
		fifoSend.shutdown();
	}
}
