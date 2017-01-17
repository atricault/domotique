package fr.domotique.connexion.http;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import fr.domotique.properties.DomotiqueProperties;

public class DomotiqueConnexionHttpProperties extends DomotiqueProperties {

	/** Holder */
	private static class SingletonHolder
	{		
		/** Instance unique non préinitialisée */
		private final static DomotiqueConnexionHttpProperties instance = new DomotiqueConnexionHttpProperties();
	}
 
	public static DomotiqueConnexionHttpProperties getInstance() {
		return SingletonHolder.instance;
	}
	
	private DomotiqueConnexionHttpProperties(){
		this.loadPropertiesFile("http_server.properties");
	}
	
}
