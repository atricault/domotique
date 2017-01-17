package fr.domotique.module.videomonitoring;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import fr.domotique.properties.DomotiqueProperties;

public class VideoMonitoringProperties extends DomotiqueProperties{
	private static final long serialVersionUID = 3823287288038556695L;

	/** Holder */
	private static class SingletonHolder
	{		
		/** Instance unique non pr�initialis�e */
		private final static VideoMonitoringProperties instance = new VideoMonitoringProperties();
	}
 
	public static VideoMonitoringProperties getInstance() {
		return SingletonHolder.instance;
	}
	
	private VideoMonitoringProperties(){
		this.loadPropertiesFile("video_monitoring.properties");
	}
	
}
