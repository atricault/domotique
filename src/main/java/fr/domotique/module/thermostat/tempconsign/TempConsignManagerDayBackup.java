package fr.domotique.module.thermostat.tempconsign;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import fr.domotique.module.thermostat.ThermostatProperties;
import fr.domotique.module.thermostat.tempconsign.TempConsign;
import fr.domotique.module.thermostat.thermostatgestion.hysteresis.ThermostatGestionHisteresis;
import fr.domotique.module.thermostat.thermostatgestion.hysteresis.derive.ChaudierePeriode;

public class TempConsignManagerDayBackup implements  Job  {
	static Logger logger = LogManager.getLogger(TempConsignManagerDayBackup.class.getName());
	
	
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		
		Enumeration<String> zoneList = TempConsignManager.getZoneListExist();
		
		while(zoneList.hasMoreElements()){
			String zone = zoneList.nextElement();
			ITempConsignManager currentTempConsignManager;
			try {
				currentTempConsignManager = TempConsignManager.getInstance(zone);
				ArrayList<TempConsign> tcList = currentTempConsignManager.getTempConsignList();		
				if(tcList != null && ! tcList.isEmpty()){
					//Calendar tcForDay = tcList.get(0).getStartTempConsign();
					Calendar tcForDay = Calendar.getInstance();
					if(logger.isDebugEnabled()){
						logger.debug("Sauvegarde des données de température de consigne pour la zone " + zone + " pour la journée " + tcForDay.get(Calendar.DAY_OF_WEEK) );
					}
					
					ArrayList<TempConsign> tcListForDay = filterTCListForDay(tcList, tcForDay);
					
					String fileSaveTC = ThermostatProperties.getInstance().getProperty("temp_consign_save_day_file");
					fileSaveTC += "day" + tcForDay.get(Calendar.DAY_OF_WEEK) + "_zone" + zone + ".dat";
					File dataFile = new File(fileSaveTC);
					if(! dataFile.exists()){
						if(dataFile.getParentFile() != null){
							dataFile.getParentFile().mkdirs();
						}
						dataFile.createNewFile();
					}
					ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(dataFile)));
					oos.writeObject(tcListForDay);
					oos.close();
							
				}
			} catch (Exception e) {
				logger.error("Erreur sur la sauvegarde des données de température de consigne pour la zone " + zone , e);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public static ArrayList<TempConsign> filterTCListForDay(ArrayList<TempConsign> tcListInit, Calendar day){
		//On n'enregistre que les TC de la journée
		ArrayList<TempConsign> tcListForDay  = new ArrayList<TempConsign>();
		Calendar dayBefore = (Calendar) day.clone();
		dayBefore.add(Calendar.HOUR_OF_DAY, -24);
		Calendar dayAfter = (Calendar) day.clone();
		dayAfter.add(Calendar.HOUR_OF_DAY, 24);
		for(TempConsign tmpConsign : tcListInit){
			if(tmpConsign.getStartTempConsign().get(Calendar.DAY_OF_WEEK) == day.get(Calendar.DAY_OF_WEEK)
					|| tmpConsign.getStartTempConsign().get(Calendar.DAY_OF_WEEK) == dayBefore.get(Calendar.DAY_OF_WEEK)
					|| tmpConsign.getStartTempConsign().get(Calendar.DAY_OF_WEEK) == dayAfter.get(Calendar.DAY_OF_WEEK)){
				if(! (tmpConsign instanceof TempConsignForced)){
					tcListForDay.add(tmpConsign);
				}
			}
		}
		return tcListForDay;
	}
	
	
	@SuppressWarnings("unchecked")
	public static ArrayList<TempConsign> loadSavedTC(String zone){
		
		try {
			Calendar tcForDay = Calendar.getInstance();
			
			if(logger.isDebugEnabled()){
				logger.debug("On récupère les données de température de consigne sur le disque pour la zone " + zone + " pour la journée " + tcForDay.get(Calendar.DAY_OF_WEEK) );
			}
			
			String fileSaveTC = ThermostatProperties.getInstance().getProperty("temp_consign_save_day_file");
			fileSaveTC += "day" + tcForDay.get(Calendar.DAY_OF_WEEK) + "_zone" + zone + ".dat";
			File dataFile = new File(fileSaveTC);
	
			BufferedInputStream bufIS;
			if(dataFile.exists()){
				bufIS = new BufferedInputStream(new FileInputStream(dataFile));
			}else{
				bufIS = new BufferedInputStream(TempConsignManagerDayBackup.class.getClassLoader().getResourceAsStream(fileSaveTC));
			}
			ObjectInputStream ois = new ObjectInputStream(bufIS);
			ArrayList<TempConsign> loadedTC = (ArrayList<TempConsign>)ois.readObject();
			ois.close();
			
			return loadedTC;
		} catch (Exception e) {
			logger.error("Erreur sur la récupération des données de température de consigne sauvegardées pour la zone " + zone , e);
		}
		return null;
	}

}
