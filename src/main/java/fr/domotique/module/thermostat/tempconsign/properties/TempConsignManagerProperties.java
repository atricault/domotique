package fr.domotique.module.thermostat.tempconsign.properties;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TempConsignManagerProperties extends Properties {

	private static final long serialVersionUID = 3823287288038556695L;

	
	public TempConsignManagerProperties() throws Exception{
		loadProp("tempconsgin_default.properties");
	}
	
	public TempConsignManagerProperties(String zone) throws Exception{
		loadProp(zone);
	}
	
	private void loadProp(String zone) throws IOException{
		InputStream input;
		try {
			input = this.getClass().getClassLoader().getResourceAsStream("tempconsgin_zone" + zone + ".properties");
			try {
				this.load(input);
			}catch (IOException e) {
				//le fichier n'existe pas
				input = this.getClass().getClassLoader().getResourceAsStream("tempconsgin_default.properties");
				throw e;
			}finally{
				input.close();
			}
		} catch (FileNotFoundException e1) {
			throw e1;
		} 
	}

}
