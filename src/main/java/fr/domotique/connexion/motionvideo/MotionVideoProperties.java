package fr.domotique.connexion.motionvideo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import fr.domotique.properties.DomotiqueProperties;

public class MotionVideoProperties extends DomotiqueProperties {

	/** Holder */
	private static class SingletonHolder
	{		
		/** Instance unique non préinitialisée */
		private final static MotionVideoProperties instance = new MotionVideoProperties();
	}
 
	public static MotionVideoProperties getInstance() {
		return SingletonHolder.instance;
	}
	
	private MotionVideoProperties(){
		this.loadPropertiesFile("motion_video.properties");
	}
	
}
