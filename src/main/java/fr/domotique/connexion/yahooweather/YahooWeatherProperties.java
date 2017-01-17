package fr.domotique.connexion.yahooweather;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import fr.domotique.properties.DomotiqueProperties;

public class YahooWeatherProperties extends DomotiqueProperties {

	/** Holder */
	private static class SingletonHolder
	{		
		/** Instance unique non préinitialisée */
		private final static YahooWeatherProperties instance = new YahooWeatherProperties();
	}
 
	public static YahooWeatherProperties getInstance() {
		return SingletonHolder.instance;
	}
	
	private YahooWeatherProperties(){
		this.loadPropertiesFile("yahoo_weather.properties");
	}
	
}
