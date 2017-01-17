package fr.domotique.module.thermostat.thermostatgestion.hysteresis;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Hashtable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.domotique.message.InfoMessage;
import fr.domotique.message.Message;
import fr.domotique.message.OrdreInfoMessage;
import fr.domotique.module.thermostat.ThermostatProperties;
import fr.domotique.module.thermostat.tempconsign.ITempConsignManager;
import fr.domotique.module.thermostat.tempconsign.TempConsignManager;
import fr.domotique.module.thermostat.thermostatgestion.IThermostatGestion;
import fr.domotique.properties.CommonDomotiqueProperties;

public class ThermostatGestionHisteresis implements IThermostatGestion{
	static Logger logger = LogManager.getLogger(ThermostatGestionHisteresis.class.getName());
	
	//Gestion des modules de thermostat
	//Détermination de la température de consigne
	//gestion des relais
	private double hysteresis_haut = 0.2;
	private double hysteresis_bas = 0.2;
	
	private ITempConsignManager tempConsignManager;
	private boolean currentRelaisEtatON = false;

	public ThermostatGestionHisteresis (ITempConsignManager tempConsignManager) throws IOException{
		try {
			hysteresis_haut = Double.parseDouble(ThermostatProperties.getInstance().getProperty("hysteresis_haut", "0.2"));
			hysteresis_bas = Double.parseDouble(ThermostatProperties.getInstance().getProperty("hysteresis_bas", "0.2"));
		
			this.tempConsignManager = tempConsignManager;
		} catch (Exception e) {
			logger.
			error("Erreur sur l'initialisation de gestionnaire de thermostat Histeresis", e);
		}

	}

	@Override
	public ITempConsignManager getTempConsignManager() {
		return this.tempConsignManager;
	}

	@Override
	public boolean getRelaisEtat(Hashtable<String, InfoMessage> currentValues) {
		if(currentValues.containsKey(OrdreInfoMessage.MSG_VAL_TYPE.TI.toString())){
			double score = getScore(currentValues);
			if(score < 100 ){
				currentRelaisEtatON = true;
			}else if(score > 100 ){
				currentRelaisEtatON = false;
			}
		}else{
			currentRelaisEtatON = false;
		}
		return currentRelaisEtatON;
	}

	public static double calculScore(double tempInt, double tempExt, double tempCons, double hysteresis){
		double score = 0;
		
		score = tempInt + hysteresis;
		score -= (tempCons - tempExt)/100;
		score = (score / tempCons) *100;
		return score;
	}
	
	
	@Override
	public double getScore(Hashtable<String, InfoMessage> currentValues) {
		return getScore(currentValues, getTempConsignManager().getCurrentTempConsign().getTempConsign());
	}
	
	@Override
	public double getScore(Hashtable<String, InfoMessage> currentValues, double tempCons) {
		//Score sur 100.
		//Si score < 100 alors allumage du relais
		//Si score > 100 alors extinction du relais
		double tempInt = Double.parseDouble(currentValues.get(OrdreInfoMessage.MSG_VAL_TYPE.TI.toString()).getMsgVal());
		
		double hysteresis = 0;

		//Calcul du score sur une note de 100
		//suivant la formule 100 * (TI - ((TC-TE)/100) - histe_bas) / TC 	
		boolean selectHisteBas = true;
		if("1".equals(currentValues.get(OrdreInfoMessage.MSG_VAL_TYPE.RE.toString()))
				|| isCurrentRelaisEtatON()){
			//La chaudière est allumée on doit prendre l'histeresis haut
			selectHisteBas = false;
		}else if(! currentValues.containsKey(OrdreInfoMessage.MSG_VAL_TYPE.RE.toString())){
			//On n'a pas l'état courant du relais, on verifie en fonction des températures s'il devrait ou non etre allumé
			if(tempInt > tempCons){
				selectHisteBas = false;
			}
		}
		
		if(selectHisteBas){
			hysteresis = this.hysteresis_bas;
		}else{
			hysteresis = -1 * this.hysteresis_haut;
		}
		
		double tempExt = 0;
		//Le score est affacté par la différence de entre la temperature de consigne
		//et la température exterieure a hauteur de 1% de cette difference
		if(currentValues.containsKey(OrdreInfoMessage.MSG_VAL_TYPE.TE.toString())){
			tempExt = (Double.parseDouble(currentValues.get(OrdreInfoMessage.MSG_VAL_TYPE.TE.toString()).getMsgVal()));
		}else{
			//Comme ca on annule l'effet de l'absence de tempExt
			tempExt = tempCons ;
		}
				
		return calculScore(tempInt, tempExt, tempCons, hysteresis);

	}

	
	@Override
	public String getZone() {
		return tempConsignManager.getZone();
	}
	
	public boolean isCurrentRelaisEtatON() {
		return currentRelaisEtatON;
	}
	public void setCurrentRelaisEtatON(boolean currentRelaisEtatON) {
		this.currentRelaisEtatON = currentRelaisEtatON;
	}
	
	public double getHysteresisHaut() {
		return hysteresis_haut;
	}

	public void setHysteresisHaut(double hysteresis) {
		this.hysteresis_haut = hysteresis;
	}
	
	public double getHysteresisBas() {
		return hysteresis_bas;
	}

	public void setHysteresisBas(double hysteresis) {
		this.hysteresis_bas = hysteresis;
	}
}
