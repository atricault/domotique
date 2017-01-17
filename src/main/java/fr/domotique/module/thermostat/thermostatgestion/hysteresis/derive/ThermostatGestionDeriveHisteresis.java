package fr.domotique.module.thermostat.thermostatgestion.hysteresis.derive;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Hashtable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.domotique.message.InfoMessage;
import fr.domotique.message.Message;
import fr.domotique.message.OrdreInfoMessage;
import fr.domotique.module.thermostat.tempconsign.ITempConsignManager;
import fr.domotique.module.thermostat.tempconsign.TempConsignManager;
import fr.domotique.module.thermostat.thermostatgestion.IThermostatGestion;
import fr.domotique.module.thermostat.thermostatgestion.hysteresis.ThermostatGestionHisteresis;
import fr.domotique.properties.CommonDomotiqueProperties;

public class ThermostatGestionDeriveHisteresis extends ThermostatGestionHisteresis {
	private double[] timesAnticipationSimilarChaudierePeriode = null;

	private static Logger logger = LogManager.getLogger(ThermostatGestionDeriveHisteresis.class.getName());

	private ChaudiereStats stats;;
	private boolean switchRelayAnticipation = false;
	
	public ThermostatGestionDeriveHisteresis(ITempConsignManager tempConsignManager) throws IOException {
		super(tempConsignManager);
		stats = new ChaudiereStats(tempConsignManager.getZone());
	}

	@Override
	public ITempConsignManager getTempConsignManager() {
		return super.getTempConsignManager();
	}

	@Override
	public boolean getRelaisEtat(Hashtable<String, InfoMessage> currentValues) {
		try{
			if(currentValues.containsKey(OrdreInfoMessage.MSG_VAL_TYPE.TI.toString())
					&& currentValues.containsKey(OrdreInfoMessage.MSG_VAL_TYPE.TC.toString())
					&& currentValues.containsKey(OrdreInfoMessage.MSG_VAL_TYPE.RE.toString())){
				stats.addStatValFIFO(currentValues);
			}
		}catch(Exception ex){
			if(logger.isDebugEnabled()){
				logger.debug("Erreur sur l'ajout des valeurs courantes aux stats", ex);
			}
		}
		if(currentValues.containsKey(OrdreInfoMessage.MSG_VAL_TYPE.TI.toString())){
			double score = getScore(currentValues);
			double nextScrore = this.getScore(currentValues, this.getTempConsignManager().getNextTempConsign().getTempConsign());
			if(score < 100 ){
				setCurrentRelaisEtatON(true);
				switchRelayAnticipation = false;
			}else if(nextScrore > 100){
				switchRelayAnticipation = false;
				setCurrentRelaisEtatON(false);
			}else{
				//On ne check l'anticipation que pour les changements de temperature de consigne qui vont avoir lieu dans l'heure
				long timeNow = (new Date()).getTime();
				long timeNextEvent = this.getTempConsignManager().getNextTempConsign().getStartTempConsign().getTimeInMillis();
				//On test l'anticipation pour les prochains évenements dans l'heure
				if(timeNextEvent - timeNow < 60 * 60 * 1000 && timeNextEvent - timeNow > 0){	
					//Seulement si le relais est à OFF
					if(currentValues.containsKey(OrdreInfoMessage.MSG_VAL_TYPE.RE.toString())
							&& "0".equals(currentValues.get(OrdreInfoMessage.MSG_VAL_TYPE.RE.toString()).getMsgVal())){
						//seulement si la temperature de consigne actuelle est inferieure à celle de la prochaine consign 
						if(this.getTempConsignManager().getNextTempConsign().getTempConsign() > this.getTempConsignManager().getCurrentTempConsign().getTempConsign()){
							
							double[] timeAnticip = stats.getTimesAnticipationSimilarChaudierePeriode(currentValues, this.getTempConsignManager().getNextTempConsign().getTempConsign());
							
							if(logger.isDebugEnabled()){
								logger.debug("Anticipation de l'allumage pour la zone " + this.getZone() + " estimée à " + timeAnticip[0] + " millisecondes pour une prochaine température de consigne de " + this.getTempConsignManager().getNextTempConsign().getTempConsign() + "°C et une température intérieure acctuelle de " + currentValues.get(OrdreInfoMessage.MSG_VAL_TYPE.TI.toString()).getMsgVal() + "°C");
							}
							
							if(timeNextEvent - timeNow < timeAnticip[0]){
								if(logger.isDebugEnabled()){
									logger.debug("Anticipation de l'allumage du chauffage pour la zone " + this.getZone());
								}

								this.getTempConsignManager().forceTempConsign(this.getTempConsignManager().getNextTempConsign().getTempConsign(), (new Double((timeAnticip[0]/1000)/60)).intValue());
								switchRelayAnticipation = true;
								setCurrentRelaisEtatON(true);	
							}
						}
					}
				}
				if(! switchRelayAnticipation){
					if(score > 100){
						setCurrentRelaisEtatON(false);
					}
				}
			}
		}else{
			switchRelayAnticipation = false;
			setCurrentRelaisEtatON(false);
		}
		return isCurrentRelaisEtatON();
	}
	
}
