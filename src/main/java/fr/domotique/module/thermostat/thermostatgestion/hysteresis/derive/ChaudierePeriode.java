package fr.domotique.module.thermostat.thermostatgestion.hysteresis.derive;

import java.io.Serializable;
import java.util.Hashtable;

import fr.domotique.message.InfoMessage;
import fr.domotique.message.Message;

public class ChaudierePeriode implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3170172909345512463L;
	
	final private int  NBRE_CYCLE_PAR_PERIODE = 4;
	Hashtable<ChaudiereCycle.CYCLE_TYPE, ChaudiereCycle> chaudiereCycle = null;
	
	private boolean completed = false;
	private boolean interrupted = false;
	
	private int cycleEnCoursIndex = 0;
	private ChaudiereCycle.CYCLE_TYPE cycleEnCours = ChaudiereCycle.CYCLE_TYPE.LANCEMENT;
	
	public ChaudiereCycle.CYCLE_TYPE getCycleEnCours() {
		return cycleEnCours;
	}

	private double tempExtAvg = 0;
	private int compteur = 0;
	private double tempIntStart = 0;
	private double tempIntMax = 0;
	private double tempCons = 0;
	private long dureePourAtteindreTempCons = 0;
	private long dureeDArret = 0;
	
	public ChaudierePeriode(){
		cycleEnCoursIndex = 0;
		
		chaudiereCycle = new Hashtable<ChaudiereCycle.CYCLE_TYPE, ChaudiereCycle>(NBRE_CYCLE_PAR_PERIODE);
		chaudiereCycle.put(ChaudiereCycle.CYCLE_TYPE.LANCEMENT, new ChaudiereCycle(ChaudiereCycle.CYCLE_TYPE.LANCEMENT));
		chaudiereCycle.put(ChaudiereCycle.CYCLE_TYPE.CHAUFFE, new ChaudiereCycle(ChaudiereCycle.CYCLE_TYPE.CHAUFFE));
		chaudiereCycle.put(ChaudiereCycle.CYCLE_TYPE.ARRET, new ChaudiereCycle(ChaudiereCycle.CYCLE_TYPE.ARRET));
		chaudiereCycle.put(ChaudiereCycle.CYCLE_TYPE.REFROIDISSEMENT, new ChaudiereCycle(ChaudiereCycle.CYCLE_TYPE.REFROIDISSEMENT));
		
		interrupted = false;
	}
	
	public boolean addCurrentValues(Hashtable<String, InfoMessage> currentValues){
		if( ! interrupted && ! completed){
			boolean processNewValuesOK = chaudiereCycle.get(cycleEnCours).processNewValues(currentValues);
			
			
			if(! processNewValuesOK){
				avanceCycle();
				if(cycleEnCoursIndex < NBRE_CYCLE_PAR_PERIODE){
					processNewValuesOK = chaudiereCycle.get(cycleEnCours).processNewValues(currentValues);
				}

				if(! processNewValuesOK){
					computeStats();
					completed = true;
					return false;
				}
			}
			
			//On calcul la température exterieur moyenne
			tempExtAvg = (tempExtAvg*compteur + chaudiereCycle.get(cycleEnCours).getTempExtEnd())/(compteur+1);
			compteur++;
			
			//Si la température de consigne a change on flag le cycle de periode comme interrompu
			if(chaudiereCycle.get(ChaudiereCycle.CYCLE_TYPE.LANCEMENT).getTempConsStart() != chaudiereCycle.get(cycleEnCours).getTempConsEnd()){
				interrupted = true;
				computeStats();
				return false;
			}
			
			return true;
		}else{
			return false;
		}
	}
	private void avanceCycle(){
		cycleEnCoursIndex++;
		switch (cycleEnCoursIndex){
			case 0:
				cycleEnCours = ChaudiereCycle.CYCLE_TYPE.LANCEMENT;
				break;
			case 1:
				cycleEnCours = ChaudiereCycle.CYCLE_TYPE.CHAUFFE;
				break;
			case 2:
				cycleEnCours = ChaudiereCycle.CYCLE_TYPE.ARRET;
				break;
			case 3:
				cycleEnCours = ChaudiereCycle.CYCLE_TYPE.REFROIDISSEMENT;
				break;
		}
	}
	
	public void terminate(){
		interrupted = true;
		computeStats();
	}
	
	private void computeStats(){
		if(interrupted  && 
				(cycleEnCours == ChaudiereCycle.CYCLE_TYPE.CHAUFFE || cycleEnCours == ChaudiereCycle.CYCLE_TYPE.LANCEMENT)){
			//On regarde si on a été interrompu dans la periode de chauffe ou delancement
			//On ne peut pas calculer les valeurs
			return;
		}
		//La temperature de consigne n'a pas du bouger
		tempCons = chaudiereCycle.get(ChaudiereCycle.CYCLE_TYPE.LANCEMENT).getTempConsStart();
		
		//La temperature intérieure de debut est la même que celle de debut du lancement
		tempIntStart = chaudiereCycle.get(ChaudiereCycle.CYCLE_TYPE.LANCEMENT).getTempIntStart();
		
		//La durée de montée pour atteindre la temperature de consigne correspond
		// à la duree de la phase de lancement et celle de de chauffe
		dureePourAtteindreTempCons = chaudiereCycle.get(ChaudiereCycle.CYCLE_TYPE.CHAUFFE).getDateEnd().getTime() - chaudiereCycle.get(ChaudiereCycle.CYCLE_TYPE.LANCEMENT).getDateStart().getTime();
				
		if(interrupted  && cycleEnCours == ChaudiereCycle.CYCLE_TYPE.ARRET){
			//On regarde si on a été interrompu dans la periode de chauffe ou delancement
			//On ne peut pas calculer les valeurs
			return;
		}
		//La temperature intérieure de maximale est la même que celle de fin de l'arret
		tempIntMax = chaudiereCycle.get(ChaudiereCycle.CYCLE_TYPE.ARRET).getTempIntEnd();
		//La durée de continuation de montée en température corresponde
		// à la durée de arret
		dureeDArret = chaudiereCycle.get(ChaudiereCycle.CYCLE_TYPE.ARRET).getDateEnd().getTime() - chaudiereCycle.get(ChaudiereCycle.CYCLE_TYPE.ARRET).getDateStart().getTime();
	}

	public double compareScore(double score){
		return score - getScore();
	}
	
	public double getScore(){
		return calculScore(getTempIntStart(),getTempCons(),getTempExtAvg());
	}
	
	public static double calculScore(double tempInt, double tempCons, double tempExt){
		//return 2* tempCons - (tempInt + tempExt);
		return ThermostatGestionDeriveHisteresis.calculScore(tempInt, tempExt, tempCons, 0);
	}
	
	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public boolean isInterrupted() {
		return interrupted;
	}

	public void setInterrupted(boolean interrupted) {
		this.interrupted = interrupted;
	}

	public double getTempExtAvg() {
		return tempExtAvg;
	}

	public void setTempExtAvg(double tempExtAvg) {
		this.tempExtAvg = tempExtAvg;
	}

	public double getTempIntStart() {
		return tempIntStart;
	}

	public void setTempIntStart(double tempIntStart) {
		this.tempIntStart = tempIntStart;
	}

	public double getTempIntMax() {
		return tempIntMax;
	}

	public void setTempIntMax(double tempIntMax) {
		this.tempIntMax = tempIntMax;
	}

	public double getTempCons() {
		return tempCons;
	}

	public void setTempCons(double tempCons) {
		this.tempCons = tempCons;
	}

	public long getDureePourAtteindreTempCons() {
		return dureePourAtteindreTempCons;
	}

	public void setDureePourAtteindreTempCons(long dureePourAtteindreTempCons) {
		this.dureePourAtteindreTempCons = dureePourAtteindreTempCons;
	}

	public long getDureeDArret() {
		return dureeDArret;
	}

	public void setDureeDArret(long dureeDArret) {
		this.dureeDArret = dureeDArret;
	}
	
	@Override
	public String toString() {
		return "ChaudierePeriode [completed=" + completed + ", interrupted="
				+ interrupted + ", cycleEnCoursIndex=" + cycleEnCoursIndex + ", cycleEnCours=" + cycleEnCours
				+ ", tempExtAvg=" + tempExtAvg + ", compteur=" + compteur + ", tempIntStart=" + tempIntStart
				+ ", tempIntMax=" + tempIntMax + ", tempCons=" + tempCons + ", dureePourAtteindreTempCons="
				+ dureePourAtteindreTempCons + ", dureeDArret=" + dureeDArret + "]";
	}
}
