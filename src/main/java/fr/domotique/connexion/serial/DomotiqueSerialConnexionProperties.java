package fr.domotique.connexion.serial;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import fr.domotique.properties.DomotiqueProperties;

public class DomotiqueSerialConnexionProperties extends DomotiqueProperties{
	private static final long serialVersionUID = 3823287288038556695L;

	/** Holder */
	private static class SingletonHolder
	{		
		/** Instance unique non préinitialisée */
		private final static DomotiqueSerialConnexionProperties instance = new DomotiqueSerialConnexionProperties();
	}
 
	public static DomotiqueSerialConnexionProperties getInstance() {
		return SingletonHolder.instance;
	}
	
	private DomotiqueSerialConnexionProperties(){
		this.loadPropertiesFile("serial_connexion.properties");
	}
	
}
