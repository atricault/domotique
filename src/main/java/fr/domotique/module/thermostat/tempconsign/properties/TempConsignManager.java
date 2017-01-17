package fr.domotique.module.thermostat.tempconsign.properties;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.domotique.module.thermostat.tempconsign.TempConsign;

public class TempConsignManager extends fr.domotique.module.thermostat.tempconsign.TempConsignManager {

	static Logger logger = LogManager.getLogger(TempConsignManager.class.getName());
	private TempConsignManagerProperties properties = null;
	
	
	public static TempConsignManager getInstance(String zone) throws Exception{
		TempConsignManager tmpConsignManager = (TempConsignManager) getInstancesList().get(zone);
		if(tmpConsignManager == null){
			tmpConsignManager = new TempConsignManager(zone);
			getInstancesList().put(zone, tmpConsignManager);
		}
		return tmpConsignManager;
	}
	
	private TempConsignManager(String zone) throws Exception{
		super(zone);
		properties = new TempConsignManagerProperties(zone);
		
        //On initialise le module
        initTempConsign();	
        
        refreshValues();
        
        initRefresh(Integer.parseInt(properties.getProperty("refresh_frequence")));
   	}

	private void initTempConsign() throws Exception{
		for(int i =1;i<= 7; i++){
			String journee = properties.getProperty("day_" + i);
			String[] periodes = journee.split(";");
			for (int j = 0; j < periodes.length; j+=2){
				TempConsign tempConsing = new TempConsign();
				tempConsing.setZone(getZone());
				
				String[] timing = periodes[j].split(":");
				Calendar cal = Calendar.getInstance();
				cal.setWeekDate(cal.get(Calendar.YEAR), cal.get(Calendar.WEEK_OF_YEAR), i);
				cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timing[0]));
				cal.set(Calendar.MINUTE, Integer.parseInt(timing[1]));
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
				
				tempConsing.setStartTempConsign(cal);
				tempConsing.setTempConsign(Double.parseDouble(periodes[j+1]));
				if(logger.isDebugEnabled()){
					logger.debug("Ajout d'une temperature de consigne pour la zone " + getZone() + " Temp[" + periodes[j+1] + "] Start at [" + loggerDateFormat.format(cal.getTime()));
				}
				getTempConsignList().add(tempConsing);
			}
		}
	}
	
}
