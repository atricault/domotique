package fr.domotique.module.thermostat.thermostatgestion.hysteresis.derive;

import java.io.Serializable;
import java.util.Date;
import java.util.Hashtable;

import fr.domotique.message.InfoMessage;
import fr.domotique.message.Message;
import fr.domotique.message.OrdreInfoMessage;

public class ChaudiereCycle implements Serializable {

	public enum CYCLE_TYPE{
		LANCEMENT,
		CHAUFFE,
		ARRET,
		REFROIDISSEMENT
	}
	
	private CYCLE_TYPE cycleType;
	private boolean relaisON;
	private boolean init = false;
	private boolean completed = false;
	private int compteur = 0;
	private int compteurTempExt = 0;
	
	
	private double tempExtStart;
	private double tempIntStart;
	private double tempConsStart;
	private Date dateStart;
	
	private double tempExtAvg;
	private double tempIntAvg;
	
	private double tempExtEnd;
	private double tempIntEnd;
	private double tempConsEnd;
	private Date dateEnd;
	
	public ChaudiereCycle(ChaudiereCycle.CYCLE_TYPE type){
		this.cycleType = type;
	}
	
	public CYCLE_TYPE getCycleType() {
		return cycleType;
	}
	public void setCycleType(CYCLE_TYPE cycleType) {
		this.cycleType = cycleType;
	}
	public double getTempExtStart() {
		return tempExtStart;
	}
	public void setTempExtStart(double tempExtStart) {
		this.tempExtStart = tempExtStart;
	}
	public double getTempIntStart() {
		return tempIntStart;
	}
	public void setTempIntStart(double tempIntStart) {
		this.tempIntStart = tempIntStart;
	}
	public double getTempConsStart() {
		return tempConsStart;
	}
	public void setTempConsStart(double tempConsStart) {
		this.tempConsStart = tempConsStart;
	}
	public Date getDateStart() {
		return dateStart;
	}
	public void setDateStart(Date dateStart) {
		this.dateStart = dateStart;
	}
	public double getTempExtEnd() {
		return tempExtEnd;
	}
	public void setTempExtEnd(double tempExtEnd) {
		this.tempExtEnd = tempExtEnd;
	}
	public double getTempIntEnd() {
		return tempIntEnd;
	}
	public void setTempIntEnd(double tempIntEnd) {
		this.tempIntEnd = tempIntEnd;
	}
	public double getTempConsEnd() {
		return tempConsEnd;
	}
	public void setTempConsEnd(double tempConsEnd) {
		this.tempConsEnd = tempConsEnd;
	}
	public Date getDateEnd() {
		return dateEnd;
	}
	public void setDateEnd(Date dateEnd) {
		this.dateEnd = dateEnd;
	}
	
	public boolean processNewValues(Hashtable<String, InfoMessage> currentValues){
		boolean newEtatRelais;
		if("1".equals(currentValues.get(OrdreInfoMessage.MSG_VAL_TYPE.RE.toString()).getMsgVal())){
			newEtatRelais = true; 
		}else{
			newEtatRelais = false; 
		}
		if(init){
			double newTempIntEnd = Double.parseDouble(currentValues.get(OrdreInfoMessage.MSG_VAL_TYPE.TI.toString()).getMsgVal());
			boolean phaseChange = false;
			//Evaluation du changement de phase
			if(this.cycleType == CYCLE_TYPE.LANCEMENT){
				//On change de phase lorsque la temperature intérieure remonte
				if(newTempIntEnd > tempIntEnd){
					phaseChange = true;
				}
			}else if(this.cycleType == CYCLE_TYPE.CHAUFFE){
				//On change de phase quand le relais s'éteind
				phaseChange = ! newEtatRelais;
			}else if(this.cycleType == CYCLE_TYPE.ARRET){
				//On change de phase lorsque la temperature interieure comence a redescendre
				if(newTempIntEnd < tempIntEnd){
					phaseChange = true;
				}
			}
			
			if(! phaseChange){
				
				if(currentValues.contains(OrdreInfoMessage.MSG_VAL_TYPE.TE.toString())){
					tempExtEnd = Double.parseDouble(currentValues.get(OrdreInfoMessage.MSG_VAL_TYPE.TE.toString()).getMsgVal());
					tempExtAvg = (tempExtAvg*compteurTempExt + tempExtEnd)/(compteurTempExt+1);
					compteurTempExt++;
				}
				tempIntEnd = newTempIntEnd;
				tempIntAvg = (tempIntAvg * compteur + tempIntEnd)/(compteur+1);
				compteur++;
				
				Double tmpTempCons = null;
				if(currentValues.containsKey("FTC")){
					tmpTempCons = Double.parseDouble(currentValues.get("FTC").getMsgVal());
				}else{
					tmpTempCons = Double.parseDouble(currentValues.get(OrdreInfoMessage.MSG_VAL_TYPE.TC.toString()).getMsgVal());
				}
				tempConsEnd = tmpTempCons;
				return true;
			}else{
				dateEnd = new Date();
				completed = true;
				return false;
			}
		}else{
			Double tmpTempExt = null;
			if(currentValues.containsKey(OrdreInfoMessage.MSG_VAL_TYPE.TE.toString())){
				tmpTempExt = Double.parseDouble(currentValues.get(OrdreInfoMessage.MSG_VAL_TYPE.TE.toString()).getMsgVal());
			}
			Double tmpTempCons = null;
			if(currentValues.containsKey("FTC")){
				tmpTempCons = Double.parseDouble(currentValues.get("FTC").getMsgVal());
			}else{
				tmpTempCons = Double.parseDouble(currentValues.get(OrdreInfoMessage.MSG_VAL_TYPE.TC.toString()).getMsgVal());
			}
			
			initCycle(Double.parseDouble(currentValues.get(OrdreInfoMessage.MSG_VAL_TYPE.TI.toString()).getMsgVal()),
					tmpTempCons,
					tmpTempExt,
					newEtatRelais);
			
			return true;
		}
	}
	
	public void initCycle(Double tempInt, Double tempCons, Double tempExt, boolean relaisON){
		init = true;
		dateStart = new Date();		
		
		this.relaisON = relaisON;
		if(tempExt != null){
			tempExtStart = tempExt;
			tempExtAvg = tempExtStart;
			tempExtEnd = tempExtStart;
			compteurTempExt++;
		}
		tempIntStart = tempInt ;
		tempIntAvg = tempIntStart;
		tempIntEnd = tempIntStart;
		compteur++;
		
		tempConsStart = tempCons;
		tempConsEnd = tempConsStart;

	}
}
