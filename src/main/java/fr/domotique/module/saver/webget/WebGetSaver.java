package fr.domotique.module.saver.webget;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.domotique.message.ErrorMessage;
import fr.domotique.message.IMessage;
import fr.domotique.message.InfoMessage;
import fr.domotique.message.Message;
import fr.domotique.message.OrdreInfoMessage;
import fr.domotique.module.common.CommonModule;
import fr.domotique.module.saver.BasicSaverResult;
import fr.domotique.module.saver.ISaver;
import fr.domotique.module.saver.ISaverResult;
import fr.domotique.module.thermostat.message.GazInfoMessage;

public class WebGetSaver implements ISaver {

	private static Logger logger = LogManager.getLogger(WebGetSaver.class.getName());
	
	
	/** Constructeur privé */	
	public WebGetSaver()
	{}

	@Override
	public ISaverResult saveMessage(IMessage msg) {
		try {
			//On ne sauvegarde que si les valeurs ont changées
			if(msg instanceof InfoMessage){
				/*
				try{
					InfoMessage curVal = CommonModule.getInstance().getCurrentValues().get(msg.getZone()).get(((InfoMessage) msg).getMsgValType());
					if(((InfoMessage) msg).equals(curVal)){
						if(((InfoMessage) msg).getMsgVal().equals(curVal.getMsgVal())){
							return null;
						}else{
							//On est peut etre dans le cadre d'un valeur numérique. On test la conversion
							try{
								double curNVal = Double.parseDouble(curVal.getMsgVal());
								double msgNVal = Double.parseDouble(((InfoMessage) msg).getMsgVal());
								if(curNVal == msgNVal){
									return null;
								}
							}catch (NumberFormatException e){
								//ce n'est pas le cas!
							}
						}
					}
				}catch(Exception ex){
					//On ne fait rien
				}
				*/
				String urlToCall= WebGetProperties.getInstance().getProperty("url_serveur");
				String params = msg.toString();
				params = params.substring(params.indexOf("[")+1);
				params = params.replaceAll(", ", "&");
				params = params.replace("]", "");
				
				if(msg instanceof OrdreInfoMessage){
					urlToCall += "/insert_info.php";
				}else if(msg instanceof ErrorMessage){
					urlToCall += "/insert_err.php";
				}
				
				if(logger.isDebugEnabled()){
					logger.debug("Envoi de la requete sur " + urlToCall + " avec les paramètres " + params);
				}
				
		        return (ISaverResult) new WebGetSaverResult(ApacheWebClientImpl.getUrlContent(urlToCall, params));
			}
		} catch (Exception e) {
			logger.error("Impossible d'enregistrer le message sur le service Web " + msg.toString(), e);
		}
        
		return null;
	}

}
