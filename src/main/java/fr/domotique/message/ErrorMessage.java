package fr.domotique.message;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class ErrorMessage extends Message {

	String erreur;
	
	@Override
	public String getKey(){
		return super.getKey() + "ERR" + message;
	}
	
	public ErrorMessage(String message){
		this.message = message;
		String[] msgPart= message.split(";");
		//Msg Type
		this.msgType = msgPart[0];
		//Zone
		this.moduleInitId=msgPart[1].replace("#", "");
		this.erreur=message.substring(4);
	}
	
	@Override
	public String encode() {
		return this.msgType +";"+this.moduleInitId+";"+this.erreur;
	}

	@Override
	public String toString() {
		return "ErrorMessage [erreur=" + erreur + ", msgType=" + msgType + ", moduleInitId=" + moduleInitId + ", zone="
				+ zone + ", receptionDate=" + receptionDate + "]";
	}


	/*@Override
	public String[] callURL(String serveur) throws Exception {
		String urlToCall = serveur + "/insert_err.php";
		String params = "erreur=" + URLEncoder.encode(this.erreur, "UTF-8") + "&module=" + URLEncoder.encode(this.moduleInitId, "UTF-8");
		
		String[] result = getUrlContent(urlToCall, params);
		return result;
	}*/

}
