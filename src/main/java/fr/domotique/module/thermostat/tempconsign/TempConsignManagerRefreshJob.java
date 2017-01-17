package fr.domotique.module.thermostat.tempconsign;

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

import fr.domotique.module.thermostat.tempconsign.TempConsign;
import fr.domotique.module.thermostat.thermostatgestion.hysteresis.ThermostatGestionHisteresis;

public class TempConsignManagerRefreshJob implements  Job  {
	static Logger logger = LogManager.getLogger(TempConsignManagerRefreshJob.class.getName());
	
	
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		
		Enumeration<String> zoneList = TempConsignManager.getZoneListExist();
		
		while(zoneList.hasMoreElements()){
			String zone = zoneList.nextElement();
			ITempConsignManager currentTempConsignManager;
			try {
				currentTempConsignManager = TempConsignManager.getInstance(zone);

				currentTempConsignManager.refreshValues();		
			
			} catch (Exception e) {
				logger.error("Erreur sur le scheduler de rafraichissement des temperatures de consign de la zone " + zone, e);
			}
			
			
		}
	}

}
