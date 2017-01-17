package fr.domotique.module.thermostat.tempconsign.ical;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TempConsignManagerICalProperties extends Properties {

	private static TempConsignManagerICalProperties defaultInstance = null;
	
	private static final long serialVersionUID = 3823287288038556695L;

	public static TempConsignManagerICalProperties getDefaultInstance() throws Exception{
		if(defaultInstance == null){
			defaultInstance = new TempConsignManagerICalProperties();
		}
		return defaultInstance;
	}
	
	public TempConsignManagerICalProperties() throws Exception{
		loadProp("tempconsgin_ical_default.properties");
	}
	
	public TempConsignManagerICalProperties(String zone) throws Exception{
		loadProp("tempconsgin_ical_zone" + zone + ".properties");
		this.putAll(TempConsignManagerICalProperties.getDefaultInstance()); 
	}
	
	private void loadProp(String fileName) throws Exception{
		//On charge les propriétés communes
		InputStream input;
		try {
			input = this.getClass().getClassLoader().getResourceAsStream(fileName);
			try {
				this.load(input);
			}catch (IOException e) {
				throw e;
			}finally{
				input.close();
			}
		} catch (FileNotFoundException e1) {
			throw e1;
		}
	}

}
