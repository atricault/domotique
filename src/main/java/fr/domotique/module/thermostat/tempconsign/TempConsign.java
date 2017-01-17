package fr.domotique.module.thermostat.tempconsign;

import java.io.Serializable;
import java.util.Calendar;

public class TempConsign implements Serializable{

	private String zone;
	private Calendar startTempConsign;
	private double tempConsign;

	public TempConsign(){
	}
	
	public TempConsign(String zone, Calendar startTempConsign, double tempConsign){
		this.zone=zone;
		this.startTempConsign=startTempConsign;
		this.tempConsign=tempConsign;
	}
	
	public String getZone() {
		return zone;
	}
	public void setZone(String zone) {
		this.zone = zone;
	}
	public Calendar getStartTempConsign() {
		return startTempConsign;
	}
	public void setStartTempConsign(Calendar startTempConsign) {
		this.startTempConsign = startTempConsign;
	}
	public double getTempConsign() {
		return tempConsign;
	}
	public void setTempConsign(double tempConsign) {
		this.tempConsign = tempConsign;
	}
	

}
