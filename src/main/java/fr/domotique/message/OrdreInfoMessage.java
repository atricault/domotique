package fr.domotique.message;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

import fr.domotique.module.common.CommonModule;

public abstract class OrdreInfoMessage extends Message {

	public enum MSG_VAL_TYPE{//Valeurs standards
		ER,HY,TI,TE,TC,TH,GZ,RE,DE,BZ,BA,ASK
	}
	
	String msgValType;
	String msgVal;
	

	public OrdreInfoMessage() {
		super();
		this.setModuleInitId("0");
		this.setZone("0");
	}
	
	public OrdreInfoMessage(String message){
		this.message = message;
		String[] msgPart= message.split(";");
		//Msg Type
		this.msgType = msgPart[0];
		//Zone
		this.zone=msgPart[1];
		
		if(MSG_VAL_TYPE.ASK.toString().equals(msgPart[2])){
			this.msgValType = MSG_VAL_TYPE.ASK.toString();
		}else{
			//Partie valeur
			String[] valPart = msgPart[2].split("=");
			this.msgValType = valPart[0];
			this.msgVal=valPart[1];
		}
		//y a t'il une Partie module
		if(msgPart.length >= 4){
			String[] modulePart = msgPart[3].split("#");
			this.moduleInitId=modulePart[1];
		}
	}

	@Override
	public String encode() {
		return this.msgType+";"+this.zone+";"+this.msgValType+"="+this.msgVal;
	}

	public String getMsgValType() {
		return msgValType;
	}

	public void setMsgValType(String msgValType) {
		this.msgValType = msgValType;
	}

	public String getMsgVal() {
		return msgVal;
	}

	public void setMsgVal(String msgVal) {
		this.msgVal = msgVal;
	}
	
	@Override
	public String getKey(){
		return super.getKey() + msgValType.toString();
	}

	@Override
	public String toString() {
		return "OrdreInfoMessage [msgValType=" + msgValType + ", msgVal=" + msgVal + ", msgType="
				+ msgType + ", moduleInitId=" + moduleInitId + ", zone=" + zone + ", receptionDate=" + receptionDate
				+ "]";
	}
	
}
