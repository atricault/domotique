package fr.domotique.module.thermostat.tempconsign;

import java.io.Serializable;
import java.util.Calendar;

public class TempConsignForced extends TempConsign implements Serializable {
	private Calendar endTempConsign = Calendar.getInstance();
	
	public TempConsignForced(String zone, double tempConsign, int duration){
		super(zone, Calendar.getInstance(), tempConsign);
		endTempConsign.add(Calendar.MINUTE, duration);
	}
	
	public boolean isStillValid(){
		if(Calendar.getInstance().after(endTempConsign)){
			return true;
		}else{
			return false;
		}
	}
}
