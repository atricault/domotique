package fr.domotique.module.thermostat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import fr.domotique.properties.DomotiqueProperties;

public class ThermostatProperties extends DomotiqueProperties{
	private static final long serialVersionUID = 3823287288038556695L;

	/** Holder */
	private static class SingletonHolder
	{		
		/** Instance unique non préinitialisée */
		private final static ThermostatProperties instance = new ThermostatProperties();
	}
 
	public static ThermostatProperties getInstance() {
		return SingletonHolder.instance;
	}
	
	private ThermostatProperties(){
		this.loadPropertiesFile("thermostat.properties");
	}
	
}
