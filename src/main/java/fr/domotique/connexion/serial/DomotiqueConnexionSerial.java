package fr.domotique.connexion.serial;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.ConcurrentModificationException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.DailyTimeIntervalScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import fr.domotique.connexion.DomotiqueConnexion;
import fr.domotique.connexion.DomotiqueConnexionException;
import fr.domotique.connexion.IDomotiqueConnexion;
import fr.domotique.message.IMessage;
import fr.domotique.message.InfoMessage;
import fr.domotique.message.Message;
import fr.domotique.module.thermostat.ThermostatRefreshJob;
import fr.domotique.module.thermostat.message.ConfigMessage;
import fr.domotique.module.thermostat.tempconsign.ITempConsignManager;
import fr.domotique.module.thermostat.thermostatgestion.IThermostatGestion;
import fr.domotique.properties.CommonDomotiqueProperties;


public class DomotiqueConnexionSerial extends DomotiqueConnexion{
	private static Logger logger = LogManager.getLogger(DomotiqueConnexionSerial.class.getName());
	private ISerialPort serialCnx = null;
	
	/** Constructeur privé */	
	private DomotiqueConnexionSerial(){
		super();
		if(logger.isDebugEnabled()){
			logger.debug("Fichier d'initialisation chargé");
		}
		try {
			String serialDriver = DomotiqueSerialConnexionProperties.getInstance().getProperty("serial_driver_class");
			Class serialDriverClass = this.getClass().getClassLoader().loadClass(serialDriver);
			serialCnx = (ISerialPort) serialDriverClass.newInstance();
			serialCnx.initPortCom(); 
			this.setStarted();
		} catch (Exception ex) {
			logger.error("Erreur sur l'initialisation du port COM", ex);
			return;
		}
	}
 
	/** Holder */
	private static class SingletonHolder
	{		
		/** Instance unique non préinitialisée */
		private final static DomotiqueConnexionSerial instance = new DomotiqueConnexionSerial();
	}
 
	public static DomotiqueConnexionSerial getInstance() {
		
		return SingletonHolder.instance;
	}

	@Override
	public void reset() {
		try {
			serialCnx.close();
			serialCnx.initPortCom();
		} catch (Exception e) {
			logger.error("Erreur sur le reset du port COM", e);
			try {
				serialCnx.close();
				serialCnx.initPortCom();
			} catch (Exception e1) {
				logger.error("Erreur sur la seconde tentative de reset du port COM", e1);
				this.stop();
			}

		}
	}

	@Override
	public void sendMessage(IMessage message) {
		try {
			serialCnx.sendMsg(message.encode());
		} catch (IOException e) {
			logger.error("Erreur sur l'envoi du message " + message, e);
		}
	}

	@Override
	public Calendar getLastReceivedMesssage() {
		return serialCnx.getLastReceivedMesssage();
	}

	@Override
	public int maxTimeWithoutMsg() {
		return 10;
	}
	
	

}
