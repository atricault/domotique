package fr.domotique.properties;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.domotique.connexion.IDomotiqueConnexion;
import fr.domotique.message.IMessage;

public abstract class DomotiqueProperties extends Properties implements IDomotiqueProperties {
	private static Logger logger = LogManager.getLogger(DomotiqueProperties.class.getName());
	
	@Override
	public void loadPropertiesFile(String propertyFileName) {
		InputStream input = this.getClass().getClassLoader().getResourceAsStream(propertyFileName);
		try {
			this.load(input);
		}catch (IOException e) {
			logger.error("Erreur sur l'ouverture du fichier de configuration " + propertyFileName, e);
		}finally{
			if(input != null){
				try {
					input.close();
				} catch (IOException e) {
				}
			}
		}
	}

}
