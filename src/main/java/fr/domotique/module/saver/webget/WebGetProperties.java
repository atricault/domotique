package fr.domotique.module.saver.webget;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class WebGetProperties extends Properties{
	private static final long serialVersionUID = 3823287288038556695L;

	private static WebGetProperties instance = null;
	
	public static WebGetProperties getInstance() throws Exception{
		if(instance == null){
			instance = new WebGetProperties();
		}
		return instance;
	}
	
	private WebGetProperties() throws Exception{
		InputStream input;
		try {
			input = this.getClass().getClassLoader().getResourceAsStream("webget_saver.properties");
			try {
				this.load(input);
			}catch (IOException e) {
				throw e;
			}finally{
				if(input != null){
					input.close();
				}
			}
		} catch (FileNotFoundException e1) {
			throw e1;
		} 
	}
	
}
