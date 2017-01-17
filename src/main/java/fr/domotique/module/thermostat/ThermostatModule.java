package fr.domotique.module.thermostat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.DailyTimeIntervalScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import fr.domotique.DomotiqueConnexionProxy;
import fr.domotique.DomotiqueMessageManagerProxy;
import fr.domotique.connexion.serial.ISerialPort;
import fr.domotique.message.AskOrdreMessage;
import fr.domotique.message.IMessage;
import fr.domotique.message.IMessageFactory;
import fr.domotique.message.InfoMessage;
import fr.domotique.message.Message;
import fr.domotique.message.OrdreInfoMessage;
import fr.domotique.message.OrdreMessage;
import fr.domotique.message.UnknownMesssageException;
import fr.domotique.module.IDomotiqueModule;
import fr.domotique.module.common.CommonModule;
import fr.domotique.module.thermostat.message.BuzzerInfoMessage;
import fr.domotique.module.thermostat.message.ConfigMessage;
import fr.domotique.module.thermostat.message.GazInfoMessage;
import fr.domotique.module.thermostat.message.TempIntInfoMessage;
import fr.domotique.module.thermostat.tempconsign.ITempConsignManager;
import fr.domotique.module.thermostat.thermostatgestion.IThermostatGestion;
import fr.domotique.properties.CommonDomotiqueProperties;

public class ThermostatModule implements IDomotiqueModule {

	private IMessageFactory msgFactory = ThermostatMessageFactory.getInstance();
	private static Logger logger = LogManager.getLogger(ThermostatModule.class.getName());
	private ISerialPort serialCnx = null;
	private boolean running = true;
	private Scheduler sched = null;
	
	private Hashtable<String, IThermostatGestion> thermoGestion = new Hashtable<String, IThermostatGestion>();
	private IThermostatGestion chaudiereGestion;
	
	private ITempConsignManager tempConsignManager = null;
	
	private ArrayList<String> zoneAskChaudiereON = new ArrayList<String>();
	
	private Hashtable<String, Calendar> lastSendTCFormZones = new Hashtable<String, Calendar>();

	/** Holder */
	private static class SingletonHolder
	{		
		/** Instance unique non préinitialisée */
		private final static ThermostatModule instance = new ThermostatModule();
	}
	
	public static ThermostatModule getInstance() {
		return SingletonHolder.instance;
	}
	
	
	private ThermostatModule(){	

		if(logger.isDebugEnabled()){
			logger.debug("Fichier d'initialisation chargé");
		}
		
		Enumeration<Object> propKeys = null;
		try {
			propKeys = ThermostatProperties.getInstance().keys();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while(propKeys != null && propKeys.hasMoreElements()){
			String propKey = propKeys.nextElement().toString();
			//on instantier les gestionnaires de temperatures de consigne par zone
			if(propKey.toString().startsWith("tempConsignClass")){
			
				String tmpZone = propKey.substring(propKey.toString().indexOf("_")+1);
				lastSendTCFormZones.put(tmpZone, Calendar.getInstance());
				//On commence par instancier un gestionnaire de temp de consigne
				
				String[] listClassTempCons = ThermostatProperties.getInstance().getProperty(propKey).split(";");
				
				for(String classToLoadNameTempConsign : listClassTempCons){
					try{
						if(logger.isDebugEnabled()){
							logger.debug("Loading Temp Consign Manager " + classToLoadNameTempConsign);
						}
						Class<ITempConsignManager> classToLoad = (Class<ITempConsignManager>) this.getClass().getClassLoader().loadClass(classToLoadNameTempConsign);
						Method instanceMethod = classToLoad.getDeclaredMethod("getInstance",  String.class);
						
						tempConsignManager = (ITempConsignManager) instanceMethod.invoke(tempConsignManager, tmpZone);//classToLoad.getDeclaredConstructor(String.class).newInstance(tmpZone) ;
						
						break;
					}catch(Exception ex){
						logger.error("Erreur sur le chargement du Temp Consign Manager", ex);
					}
				}
				
				if(tempConsignManager != null){
					String[] listClass = ThermostatProperties.getInstance().getProperty("thermostatGestionClass_" + tmpZone).split(";");
					IThermostatGestion tempThermostatGestion = null;
					for(String classToLoadNameThermoGestion : listClass){
						try{
							if(logger.isDebugEnabled()){
								logger.debug("Loading Thermo Manager " + classToLoadNameThermoGestion);
							}
							Class<IThermostatGestion> classToLoad = (Class<IThermostatGestion>) this.getClass().getClassLoader().loadClass(classToLoadNameThermoGestion);
							tempThermostatGestion = classToLoad.getDeclaredConstructor(ITempConsignManager.class).newInstance(tempConsignManager) ;

							thermoGestion.put(tmpZone, tempThermostatGestion);
							if(tmpZone.equals(ThermostatProperties.getInstance().getProperty("zone_gestion_chaudiere"))){
								this.chaudiereGestion = tempThermostatGestion;
							}
							
							break;
						}catch(Exception ex){
							logger.error("Erreur sur le chargement du Gestionnaire de thermostat", ex);
						}
					}
				}
			
			}else if(propKey.toString().startsWith("conf_module")){
					
				String tmpModule = propKey.substring(propKey.toString().lastIndexOf("_")+1);
				sendConfMessageForModule(tmpModule);
			}
		}
		
		refreshCurrentValues();
		
		//On crée un scheduler
		try {
			SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();
			sched = schedFact.getScheduler();
			
			JobDetail jobDetail =  JobBuilder.newJob(ThermostatRefreshJob.class).build();
			
			TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();
			triggerBuilder.withSchedule(DailyTimeIntervalScheduleBuilder.dailyTimeIntervalSchedule().withIntervalInMinutes(Integer.parseInt(ThermostatProperties.getInstance().getProperty("refresh_frequence"))));
			triggerBuilder.forJob(jobDetail);
			triggerBuilder.startNow();
			Trigger trigger = triggerBuilder.build();
	         
	        sched.scheduleJob(jobDetail, trigger);
	        
	        sched.start();
	        if(logger.isDebugEnabled()){
				logger.debug("Scheduler démarré");
			}
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
			
		
	public void stopThread() {
		running = false;
	}

	public void refreshCurrentValues(){
		if(logger.isDebugEnabled()) {
			logger.debug("Début de l'appel a la gestion du thermostat");
		}
		Calendar now = Calendar.getInstance();
		Enumeration<String> thermogestionListZone = this.getThermoGestion().keys();
		while(thermogestionListZone.hasMoreElements()){
			String zone = thermogestionListZone.nextElement();
			boolean zoneOK = true;
			IThermostatGestion thermoManager = this.getThermoGestion().get(zone);
			
			if(logger.isDebugEnabled()) {
				logger.debug("Gestion du thermostat pour la zone " + zone);
			}
			
			String strEtatRelais;
			Hashtable<String, InfoMessage> currentValues = CommonModule.getInstance().getCurrentValues().get(zone);
			if(currentValues != null){
				
				//On regarde l'age des valeurs surtout celle de la temp intérieure
				//Si elle a plus de 15 minutes, peut etre que le module Arduino est HS
				//On redemande les infos
				if(currentValues.get("TI") != null){
					Calendar lastReceiveTI = currentValues.get("TI").getReceptionDate();
					Calendar today = Calendar.getInstance();
					today.setTime(new Date());

					if(today.getTimeInMillis() - lastReceiveTI.getTimeInMillis() > 15 * 60 * 1000){
						zoneOK=false;						
					}
				}
			}else{
				zoneOK=false;
			}
			
			if(zoneOK){
				if(logger.isDebugEnabled()) {
					logger.debug("Preparation des ordres a envoyer pour la " + zone);
				}
				
				Calendar lastSendTC = lastSendTCFormZones.get(zone);
				
				Calendar nextSendTC = Calendar.getInstance();
				nextSendTC.add(Calendar.MINUTE, -20);
				if(currentValues.get(OrdreInfoMessage.MSG_VAL_TYPE.TC.toString()) == null  
						|| lastSendTC.compareTo(nextSendTC) <= 0
						|| thermoManager.getTempConsignManager().getCurrentTempConsign().getTempConsign() != Double.parseDouble(currentValues.get(OrdreInfoMessage.MSG_VAL_TYPE.TC.toString()).getMsgVal())){
					//La température de consigne a change ou cela plus de 20 minutes qu'on ne l'a pas envoyée au module Arduino
					OrdreMessage oMsgTemp = new OrdreMessage(zone, OrdreInfoMessage.MSG_VAL_TYPE.TC, Double.toString(thermoManager.getTempConsignManager().getCurrentTempConsign().getTempConsign()));
					DomotiqueMessageManagerProxy.getInstance().addMessageToSend(oMsgTemp);
					lastSendTCFormZones.put(zone, Calendar.getInstance());
				}
				boolean currentEtatChaudiere = false;
				if(chaudiereGestion != null){
					currentEtatChaudiere = chaudiereGestion.isCurrentRelaisEtatON();
				}
				
				boolean currentEtatRelais = thermoManager.isCurrentRelaisEtatON();
				boolean etatRelais = thermoManager.getRelaisEtat(currentValues);
				
				
				if(currentEtatRelais != etatRelais || ! currentValues.containsKey(OrdreInfoMessage.MSG_VAL_TYPE.RE.toString())){
					//L'état du relais a changé
				
					if(etatRelais){
						strEtatRelais = "1";
						if(! zoneAskChaudiereON.contains(zone)){
							zoneAskChaudiereON.add(zone);
						}
					}else{
						strEtatRelais = "0";
						if(zoneAskChaudiereON.contains(zone)){
							zoneAskChaudiereON.remove(zone);
						}
					}
					
					if(! zone.equals(chaudiereGestion.getZone())){
						//On allume/eteinds le relais de la zone concernée, autre que la chaudiere
						OrdreMessage oMsgRelais = new OrdreMessage(zone, OrdreInfoMessage.MSG_VAL_TYPE.RE, strEtatRelais);
						DomotiqueMessageManagerProxy.getInstance().addMessageToSend(oMsgRelais);
					}
					
					if(zoneAskChaudiereON.isEmpty()){
						//On éteind la chaudière
						OrdreMessage oMsgRelaisChaudiere = new OrdreMessage(chaudiereGestion.getZone(), OrdreInfoMessage.MSG_VAL_TYPE.RE, "0");
						DomotiqueMessageManagerProxy.getInstance().addMessageToSend(oMsgRelaisChaudiere);
					}else{
						//On allume la chaudière
						OrdreMessage oMsgRelaisChaudiere = new OrdreMessage(chaudiereGestion.getZone(), OrdreInfoMessage.MSG_VAL_TYPE.RE, "1");
						DomotiqueMessageManagerProxy.getInstance().addMessageToSend(oMsgRelaisChaudiere);
					}
	
				}
			}else{
				//Les currents values de cette zone sont vides. On va lui envoyer une demande d'infos
				try {
					OrdreMessage msgOrdreTC = new OrdreMessage(zone, OrdreInfoMessage.MSG_VAL_TYPE.TC, Double.toString(thermoManager.getTempConsignManager().getCurrentTempConsign().getTempConsign()));
					AskOrdreMessage msgOrdreASK = new AskOrdreMessage();
					msgOrdreASK.setZone(zone);
					
					DomotiqueMessageManagerProxy.getInstance().addMessageToSend(msgOrdreTC);
					DomotiqueMessageManagerProxy.getInstance().addMessageToSend(msgOrdreASK);
				} catch (Exception e) {
					logger.error("Le module de la zone " + zone + "ne semble pas bien initialisé.", e);
				}
			}
		}
		//On evoie une demande d'infos a chaque fois
		//ThermostatConnexion.getInstance().addMessageToFIFO("O;0;ASK");
		if(logger.isDebugEnabled()) {
			logger.debug("Fin de l'appel a la gestion du thermostat");
		}
	}
	
	public Hashtable<String, IThermostatGestion> getThermoGestion() {
		return thermoGestion;
	}

	public void setThermoGestion(Hashtable<String, IThermostatGestion> thermoGestion) {
		this.thermoGestion = thermoGestion;
	}

	private void sendConfMessageForModule(String module){
		
		try {
			ConfigMessage msgConf = new ConfigMessage("C;" + module + ";" + ThermostatProperties.getInstance().getProperty("conf_module_" + module));
			DomotiqueMessageManagerProxy.getInstance().addMessageToSend(msgConf);
		} catch (Exception e) {
			logger.error("Erreur sur l'envoi du message de conf pour le module " + module, e);
		}
		
	}
	
	@Override
	public void treateMessage(IMessage msg) {
		if(msg instanceof ConfigMessage){
			if(((ConfigMessage) msg).isAsk()){
				sendConfMessageForModule(msg.getModuleInitId());
			}
		}else if(msg instanceof InfoMessage){
			refreshCurrentValues();
		}
		
	}


	@Override
	public void treateMessage(String msg) {
		try{
			IMessage[] messages = msgFactory.buildMessage(msg);
			for(IMessage message : messages){
				treateMessage(message);
			}
		}catch(UnknownMesssageException UME){
			//On n'a pas a traiter ce message
		}
	}


	@Override
	public void shutdown() {
		stopThread();
		tempConsignManager.stop();
	}

}
