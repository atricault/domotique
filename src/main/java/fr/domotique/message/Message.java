package fr.domotique.message;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;

import fr.domotique.module.common.CommonModule;
import fr.domotique.module.saver.ISaverResult;

public abstract class Message implements IMessage{
	
	protected String message;
	protected String msgType;
	protected String moduleInitId = "0";
	protected String zone;
	protected Calendar receptionDate;
	
	@Override
	public boolean isRepeatable(){
		return false;
	}
	
	@Override
	public String getKey(){
		return this.getMsgType() + ";" + zone + ";";
	}
	
	
	
	public Calendar getReceptionDate() {
		return receptionDate;
	}
	public void setReceptionDate(Calendar receptionDate) {
		this.receptionDate = receptionDate;
	}
	
	@Override
	public String getModuleInitId(){
		return moduleInitId;
	}
	@Override
	public String getZone(){
		return zone;
	}
	@Override
	public String getMsgType(){
		return msgType;
	}
	
	
	@Override
	public String toString() {
		return "Message [msgType=" + msgType + ", moduleInitId=" + moduleInitId + ", zone="
				+ zone + ", receptionDate=" + receptionDate + "]";
	}
	
	protected String[] getUrlContent(String request, String params) throws IOException{
		ArrayList<String> arrayResult = new ArrayList<String>();

		URL url = new URL(request + "?" + params);
		HttpURLConnection  cnx = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
		
		cnx.setReadTimeout(10000);
		cnx.setConnectTimeout(10000);
		cnx.setRequestMethod("GET");
		cnx.setRequestProperty("Content-Type", 
		           "application/x-www-form-urlencoded");
		cnx.setRequestProperty("Content-Length", "" + 
           Integer.toString(params.getBytes().length));
		cnx.setRequestProperty("Content-Language", "en-US");  
		cnx.setUseCaches (false);
		cnx.setInstanceFollowRedirects(true);
		cnx.setDoInput(true);
		cnx.setDoOutput(true);
		
		OutputStream os = cnx.getOutputStream();
		DataOutputStream wr = new DataOutputStream(os);
		wr.flush();
				
		BufferedReader reader = new BufferedReader(new InputStreamReader(cnx.getInputStream()));
        String ligne;
        while ((ligne = reader.readLine()) != null) {
        	if(! ligne.trim().equals("")){
        		arrayResult.add(ligne);
        	}
        }
        String[] result = new String[arrayResult.size()];
        wr.close();
        reader.close();
        cnx.disconnect();
        return arrayResult.toArray(result);
		
	}
	
	@Override
	public void postTreatment(ISaverResult result){
		//Rien à faire
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}

	public void setModuleInitId(String moduleInitId) {
		this.moduleInitId = moduleInitId;
	}

	public void setZone(String zone) {
		this.zone = zone;
	}
	
	@Override
	public boolean equals(IMessage msg){
		if(this.getKey().equals(msg.getKey())){
			return true;
		}else{
			return false;
		}
	}
}
