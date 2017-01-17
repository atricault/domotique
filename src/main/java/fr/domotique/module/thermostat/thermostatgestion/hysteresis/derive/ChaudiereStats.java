package fr.domotique.module.thermostat.thermostatgestion.hysteresis.derive;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.domotique.message.InfoMessage;
import fr.domotique.message.Message;
import fr.domotique.message.OrdreInfoMessage;
import fr.domotique.module.thermostat.ThermostatProperties;
import fr.domotique.module.thermostat.thermostatgestion.hysteresis.derive.ChaudiereCycle.CYCLE_TYPE;

public class ChaudiereStats extends  ArrayList<ChaudierePeriode> implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1390620108616049417L;
	private static Logger logger = LogManager.getLogger(ChaudiereStats.class.getName());
	private int maxStatsCount = 1000;
	private boolean currentRelaisVal = false;
	private ChaudierePeriode currentChaudierePeriode =  null;
	
	//private ArrayList<ChaudierePeriode> stats;
	private String zone = "0";
	
	@SuppressWarnings("unchecked")
	public ChaudiereStats(String zone){
		super();
		this.zone=zone;
		//on charge les données de cycles des précédents démarrages
		try{
			String fileCyclesChaudiereStr = ThermostatProperties.getInstance().getProperty("hysteresis_derive_save_cycles_file");
			fileCyclesChaudiereStr += zone + ".dat";
			File fileCyclesChaudiere = new File(fileCyclesChaudiereStr);
			BufferedInputStream bufIS;
			if(fileCyclesChaudiere.exists()){
				bufIS = new BufferedInputStream(new FileInputStream(fileCyclesChaudiere));
			}else{
				bufIS = new BufferedInputStream(this.getClass().getClassLoader().getResourceAsStream(fileCyclesChaudiereStr));
			}
			ObjectInputStream ois = new ObjectInputStream(bufIS);
			ArrayList<ChaudierePeriode> loadedStats = (ArrayList<ChaudierePeriode>)ois.readObject();
			ois.close();
			
			//On pruge le fichier sauvegardé
			Iterator<ChaudierePeriode> iteStats = loadedStats.iterator();
			int i = 0;
			while(true){
				ChaudierePeriode chPeriod = loadedStats.get(i);
				if(chPeriod.isInterrupted() && (chPeriod.getCycleEnCours() == CYCLE_TYPE.LANCEMENT
						|| chPeriod.getCycleEnCours() ==  CYCLE_TYPE.CHAUFFE)){
					loadedStats.remove(chPeriod);
				}
				i++;
				if(i >= loadedStats.size()){
					break;
				}
			}
			while(loadedStats.size()>=maxStatsCount){
				loadedStats.remove(0);
			}
			
			this.addAll(loadedStats);
			
			
			if(logger.isDebugEnabled()){
				logger.debug(this.size() + " précédents cycles statistiques de chaudière ont été chargés pour la zone " + zone);
				for(ChaudierePeriode chPeriod : this){
					logger.debug(chPeriod.toString());
				}
			}
		}catch(Exception ex){
			logger.info("Impossible de charger les précédents cycles de chaudière pour la zone " + zone, ex);
			//this = new ArrayList<ChaudierePeriode>();
		}
		this.add(new ChaudierePeriode());
	}
	
	public double[] getTimesAnticipationSimilarChaudierePeriode(Hashtable<String, InfoMessage> currentValues, double nextTempCons){
		double[] returnValues = {0,0};
		
		//Il faut que l'on estime le temps qu'il va nous falloir pour atteindre 
		//la température de consigne a partir de la température intérieure et de la température exterieure
		double tempExt = 0; 
		if(currentValues.containsKey(OrdreInfoMessage.MSG_VAL_TYPE.TE.toString())){
			tempExt = Double.parseDouble(currentValues.get(OrdreInfoMessage.MSG_VAL_TYPE.TE.toString()).getMsgVal());
		}
		double tempInt = 0;
		if(currentValues.containsKey(OrdreInfoMessage.MSG_VAL_TYPE.TI.toString())){
			tempInt = Double.parseDouble(currentValues.get(OrdreInfoMessage.MSG_VAL_TYPE.TI.toString()).getMsgVal());
		}
		double tempCons = nextTempCons;
		/*if(currentValues.containsKey(OrdreInfoMessage.MSG_VAL_TYPE.TC.toString())){
			tempCons = Double.parseDouble(currentValues.get(OrdreInfoMessage.MSG_VAL_TYPE.TC.toString()).getMsgVal());
		}*/
		
		//On retrouve les periodes de chaudières similaires directement inférieur
		//et directement suppérieure puis on retourne la moyenne pondérée des 2
		double moyTimeAnticipationLancement = 0;
		double moyTimeAnticipationArret = 0;
		
		double ponderationCumul = 0;
		
		double maxDiffScore = Double.parseDouble(ThermostatProperties.getInstance().getProperty("maxDiffScore", "5"));
		double maxDiffTempInt = Double.parseDouble(ThermostatProperties.getInstance().getProperty("maxDiffTempInt", "3"));
		double maxDiffTempExt = Double.parseDouble(ThermostatProperties.getInstance().getProperty("maxDiffTempExt", "5"));
		double maxDiffTempCons = Double.parseDouble(ThermostatProperties.getInstance().getProperty("maxDiffTempCons", "1"));
		
		double scoreCible = ChaudierePeriode.calculScore(tempInt, tempCons,  tempExt);
		
		if(logger.isDebugEnabled()){
			logger.debug("Les statistiques de cycles de chaudière comportent " + this.size() + " éléments");
		}
		
		if(! this.isEmpty()){
			int countPeriod = 0;
			for (int i = 0 ; i < this.size()-1; i++){
				ChaudierePeriode tmpChaudierePeriode = this.get(i);
				
				if(tmpChaudierePeriode.getCycleEnCours().equals(CYCLE_TYPE.ARRET)
						|| tmpChaudierePeriode.getCycleEnCours().equals(CYCLE_TYPE.REFROIDISSEMENT)){
				
					double diffscore = scoreCible - tmpChaudierePeriode.getScore();
					//Si on tombe exactement sur les mêmes valeurs, on retourne
					if(tmpChaudierePeriode.getTempExtAvg() == tempExt
							&& tmpChaudierePeriode.getTempIntStart() == tempInt
							&& tmpChaudierePeriode.getTempCons() == tempCons){
						returnValues[0] = tmpChaudierePeriode.getDureePourAtteindreTempCons();
						returnValues[1] = tmpChaudierePeriode.getDureeDArret();
						
						if(logger.isDebugEnabled()){
							logger.debug("Une des périodes de statistiques de cycles de chaudière correspond");
						}
						
						return returnValues;
					}else if (Math.abs(tmpChaudierePeriode.getTempExtAvg() - tempExt) < maxDiffTempExt
						&& Math.abs(tmpChaudierePeriode.getTempIntStart() - tempInt) < maxDiffTempInt
						&& Math.abs(tmpChaudierePeriode.getTempCons() - tempCons) < maxDiffTempCons
						&& Math.abs(diffscore) < maxDiffScore){
						//On réalise une moyenne glissante pondérée par le score 
						//double ponderation  = ((100 - diffscore)/100);
						double ponderation  = 1-(diffscore/scoreCible);
						//Application d'une loi normale (courbe de gauss) à la pondération
						//(1/(mu*sqr(2*pi)))*e^(-((x-1/mu)^2)/2)
						double mu = 0.2;
						ponderation = (1 / (0.4*Math.sqrt(2*Math.PI))) * Math.pow(Math.E, (-( Math.pow((ponderation-1)/0.1, 2)/2))); 
						//ponderation = 1/ponderation;
						ponderationCumul += ponderation;
						if(logger.isDebugEnabled()){
							logger.debug("Ajout d'une des périodes de statistiques de cycles de chaudière. Avec la ponderarion " + ponderation  + " pour une différence de score de " + diffscore + " (" + diffscore/scoreCible*100 + "%). Les valeurs sont " + tmpChaudierePeriode.getDureeDArret() +" Arret et " + tmpChaudierePeriode.getDureePourAtteindreTempCons()  + " lancement avec des valeurs de temperature de : TE="+tmpChaudierePeriode.getTempExtAvg()+";TIstart="+tmpChaudierePeriode.getTempIntStart()+";TImax="+tmpChaudierePeriode.getTempIntMax()+";TC="+tmpChaudierePeriode.getTempCons());
						}
						
						countPeriod++;
						//moyTimeAnticipationLancement = (moyTimeAnticipationLancement * oldPondeCumul + tmpChaudierePeriode.getDureePourAtteindreTempCons() * ponderation) / (ponderationCumul);
						//moyTimeAnticipationArret = (moyTimeAnticipationArret * oldPondeCumul + tmpChaudierePeriode.getDureeDArret() * ponderation) / (ponderationCumul);
						moyTimeAnticipationLancement += tmpChaudierePeriode.getDureePourAtteindreTempCons() * ponderation * (1+diffscore/scoreCible);
						moyTimeAnticipationArret += tmpChaudierePeriode.getDureeDArret() * ponderation * (1+diffscore/scoreCible);

					}
				}
			}
			if(ponderationCumul != 0){
				returnValues[0] = moyTimeAnticipationLancement/ponderationCumul;
				returnValues[1] = moyTimeAnticipationArret/ponderationCumul;
				
				if(logger.isDebugEnabled()){
					logger.debug("Les moyennes des périodes de statistiques de cycles de chaudière sont " + returnValues[1]  +" Arret et " + returnValues[0]  + " lancement. avec des valeurs de temperature de : TE="+tempExt+";TI="+tempInt+";TC="+tempCons);
				}
			}else{
				if(logger.isDebugEnabled()){
					logger.debug("Aucune statistiques de perdiode de chauffe ne correspond à ces valeurs de temperature de : TE="+tempExt+";TI="+tempInt+";TC="+tempCons);
				}
			}
			
		}
		return returnValues;
	}
	
	public void terminateCurrentPeriodAndAddNew(){
		ChaudierePeriode currentChaudierePeriode = this.get(this.size()-1);
		currentChaudierePeriode.terminate();
		this.add(new ChaudierePeriode());
		
	}
	
	public void addStatValFIFO(Hashtable<String, InfoMessage> currentValues){
		ChaudierePeriode tmpChaudierePeriode;
		if(this.isEmpty()){
			tmpChaudierePeriode = new ChaudierePeriode();
			tmpChaudierePeriode.addCurrentValues(currentValues);
			this.add(tmpChaudierePeriode);
			currentChaudierePeriode = tmpChaudierePeriode;
		}else{
			ChaudierePeriode currentChaudierePeriode = this.get(this.size()-1);
			if(! currentChaudierePeriode.addCurrentValues(currentValues)){
				//La perdiode de chaudière est terminee
				//Interrupted ou complete
				//On en ajoute 1 nouvelle seulement si le relais vient de passer à ON
				boolean newRelaisVal;
				if("1".equals(currentValues.get(OrdreInfoMessage.MSG_VAL_TYPE.RE.toString()).getMsgVal())){
					newRelaisVal = true;
				}else{
					newRelaisVal = false;
				}
				if(! currentRelaisVal && newRelaisVal){
					initNewChaudiereStat(currentValues);
				}
				currentRelaisVal = newRelaisVal;
			}
		}	
	}

	public void initNewChaudiereStat(Hashtable<String, InfoMessage> currentValues){
		if(logger.isDebugEnabled()){
			logger.debug("Debut d'une nouvelle période de chauffe pour la zone " + zone);
		}
		
		//On enregistre les stats sur disque
		try {
			String fileCyclesChaudiere = ThermostatProperties.getInstance().getProperty("hysteresis_derive_save_cycles_file");
			fileCyclesChaudiere += zone + ".dat";
			File dataFile = new File(fileCyclesChaudiere);
			if(! dataFile.exists()){
				dataFile.getParentFile().mkdirs();
				dataFile.createNewFile();
			}
			ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(dataFile)));
			oos.writeObject(this);
			oos.close();
		}catch(Exception ex){
			logger.info("Impossible de sauvegarder les cycles de chaudière", ex);
		}
		
		ChaudierePeriode tmpChaudierePeriode = new ChaudierePeriode();
		tmpChaudierePeriode.addCurrentValues(currentValues);
		
		this.add(tmpChaudierePeriode);
		currentChaudierePeriode = tmpChaudierePeriode;
	}
	
	public ChaudierePeriode getCurrentChaudierePeriode() {
		return currentChaudierePeriode;
	}
	
	
	
}
