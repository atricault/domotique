package fr.domotique.properties;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class CommonDomotiqueProperties extends DomotiqueProperties{
	private static final long serialVersionUID = 3823287288038556695L;

	private static CommonDomotiqueProperties instance = null;
	
	public static CommonDomotiqueProperties getInstance() throws Exception{
		if(instance == null){
			instance = new CommonDomotiqueProperties();
		}
		return instance;
	}
	
	private CommonDomotiqueProperties() throws Exception{
		this.loadPropertiesFile("domotique.properties");
	}
	
}
