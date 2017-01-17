package fr.domotique.module.thermostat.message;

import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.domotique.DomotiqueMessageManagerProxy;
import fr.domotique.message.IMessage;
import fr.domotique.message.Message;
import fr.domotique.message.OrdreInfoMessage;
import fr.domotique.module.saver.ISaverResult;
import fr.domotique.module.thermostat.ThermostatModule;
import fr.domotique.module.thermostat.ThermostatProperties;
import fr.domotique.properties.CommonDomotiqueProperties;

public class ConfigMessage extends Message {
	private static Logger logger = LogManager.getLogger(ConfigMessage.class.getName());
	
	boolean ask = false;
	
	int pinTempHumi = 0;
    int pinRelay = 0;
    int pinBuzzer = 0;
    int pinThermostat = 0;
    int pinGaz = 0;
    int pinTempExt = 0;
    int portSerie = 1;
    int gestionBaro = 0;
    int gestionRelais = 0;

    @Override
	public String getKey(){
		return super.getKey() + "CONF";
	}
    
    //C;2;2;3;0;0;0;0;1;1;1
	public ConfigMessage(String message){
		this.message = message;
		String[] msgPart= message.split(";");
		//Msg Type
		this.msgType = msgPart[0];
		//Zone
		this.moduleInitId=msgPart[1];
		
		if(msgPart[2].equalsIgnoreCase(OrdreInfoMessage.MSG_VAL_TYPE.ASK.toString())){
			this.ask = true;
		}else{
			this.pinTempHumi = Integer.parseInt(msgPart[2]);
			this.pinRelay = Integer.parseInt(msgPart[3]);
			this.pinBuzzer = Integer.parseInt(msgPart[4]);
			this.pinThermostat = Integer.parseInt(msgPart[5]);
			this.pinGaz = Integer.parseInt(msgPart[6]);
			this.pinTempExt = Integer.parseInt(msgPart[7]);
			this.portSerie = Integer.parseInt(msgPart[8]);
			this.gestionBaro = Integer.parseInt(msgPart[9]);
			this.gestionRelais = Integer.parseInt(msgPart[10]);
			this.zone = msgPart[11];
		}
	}
	
	@Override
	public String encode() {
		StringBuffer strBuff = new StringBuffer();
		strBuff.append(this.msgType).append(";").append(this.moduleInitId).append(";");
		if(this.ask){
			strBuff.append(OrdreInfoMessage.MSG_VAL_TYPE.ASK);
		}else{
			strBuff.append(this.pinTempHumi).append(";");
			strBuff.append(this.pinRelay).append(";");
			strBuff.append(this.pinBuzzer).append(";");
			strBuff.append(this.pinThermostat).append(";");
			strBuff.append(this.pinGaz).append(";");
			strBuff.append(this.pinTempExt).append(";");
			strBuff.append(this.portSerie).append(";");
			strBuff.append(this.gestionBaro).append(";");
			strBuff.append(this.gestionRelais).append(";");
			strBuff.append(this.getZone());
		}
		
		return strBuff.toString();
	}

	@Override
	public String toString() {
		return "ConfigMessage [ask=" + ask + ", pinTempHumi=" + pinTempHumi + ", pinRelay=" + pinRelay + ", pinBuzzer="
				+ pinBuzzer + ", pinThermostat=" + pinThermostat + ", pinGaz=" + pinGaz + ", pinTempExt=" + pinTempExt
				+ ", portSerie=" + portSerie + ", gestionBaro=" + gestionBaro + ", gestionRelais=" + gestionRelais
				+ "]";
	}
	
	@Override
	public void postTreatment(ISaverResult result){
		
	}

	public boolean isAsk() {
		return ask;
	}

}
