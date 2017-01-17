package fr.domotique.module.thermostat.thermostatgestion;

import java.util.Hashtable;

import fr.domotique.message.InfoMessage;
import fr.domotique.module.thermostat.tempconsign.ITempConsignManager;

public interface IThermostatGestion {

	public ITempConsignManager getTempConsignManager();
	public boolean getRelaisEtat(Hashtable<String, InfoMessage> currentValues);
	public double getScore(Hashtable<String, InfoMessage> currentValues);
	public double getScore(Hashtable<String, InfoMessage> currentValues, double tempConsign);
	
	public boolean isCurrentRelaisEtatON();
	
	public String getZone();
}
