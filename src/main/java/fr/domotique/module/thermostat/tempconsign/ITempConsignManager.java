package fr.domotique.module.thermostat.tempconsign;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface ITempConsignManager {

	public TempConsign getNextTempConsign();
	public TempConsign getCurrentTempConsign();
	public String getZone();
	public void refreshValues();
	public void stop();
	public void forceTempConsign(double newTempConsign, int duration); //minutes
	public  ArrayList<TempConsign> getTempConsignList();
}
