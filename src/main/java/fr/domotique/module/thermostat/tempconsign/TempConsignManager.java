package fr.domotique.module.thermostat.tempconsign;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.CronExpression;
import org.quartz.CronTrigger;
import org.quartz.DailyTimeIntervalScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.triggers.CronTriggerImpl;

import fr.domotique.Launcher;
import fr.domotique.module.common.CommonModule;
import fr.domotique.module.thermostat.message.ForcedTempConsignInfoMessage;



public abstract class TempConsignManager implements ITempConsignManager {
	private static Logger logger = LogManager.getLogger(TempConsignManager.class.getName());
	
	public SimpleDateFormat loggerDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	
	private TempConsign nextTempConsign = null;
	private TempConsign currentTempConsign = null;
	private TempConsignForced tempConsignForced = null;
	
	private ArrayList<TempConsign> tempConsignList = new ArrayList<TempConsign>();
	private String zone;
	
	private Scheduler sched;
	private Scheduler schedBackupTCPerDay;
	private static boolean initRefreshJobInitialised = false;
	
	public static ITempConsignManager getInstance(String zone) throws Exception{
		return getInstancesList().get(zone);
	}
	
	public TempConsignManager(String zone){
		this.zone = zone;
	}
	
	private static Hashtable<String, ITempConsignManager> instancesList = new Hashtable<String, ITempConsignManager>();
	
	public static Hashtable<String, ITempConsignManager> getInstancesList(){
		return instancesList;
	}
	
	public static Enumeration<String> getZoneListExist(){
		return instancesList.keys();
	}
	
	public void initRefresh(int refreshInterval) throws SchedulerException{
		SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();
		if (! initRefreshJobInitialised){
			
			sched = schedFact.getScheduler();
			JobDetail jobDetail =  JobBuilder.newJob(TempConsignManagerRefreshJob.class).build();
			TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();
			triggerBuilder.withSchedule(DailyTimeIntervalScheduleBuilder.dailyTimeIntervalSchedule().withIntervalInMinutes(refreshInterval));
			triggerBuilder.forJob(jobDetail);
			triggerBuilder.startNow();
			Trigger trigger = triggerBuilder.build();
	        sched.scheduleJob(jobDetail, trigger);
	        sched.start();
	        
	        schedBackupTCPerDay = schedFact.getScheduler();
			JobDetail jobDetailBackupTCPerDay =  JobBuilder.newJob(TempConsignManagerDayBackup.class).build();
			/*TriggerBuilder<Trigger> triggerBuilderBackupTCPerDay = TriggerBuilder.newTrigger();
			triggerBuilderBackupTCPerDay.withSchedule(DailyTimeIntervalScheduleBuilder.dailyTimeIntervalSchedule().onEveryDay());
			triggerBuilderBackupTCPerDay.forJob(jobDetailBackupTCPerDay);
			triggerBuilderBackupTCPerDay.startNow();
			Trigger triggerBackupTCPerDay = triggerBuilderBackupTCPerDay.build();*/
			
			try {
				CronTriggerImpl triggerBackupTCPerDay = new CronTriggerImpl();
				triggerBackupTCPerDay.setName("SaveTCForDay");
				triggerBackupTCPerDay.setCronExpression("0 30 1 ? * *");
				schedBackupTCPerDay.scheduleJob(jobDetailBackupTCPerDay, triggerBackupTCPerDay);
				schedBackupTCPerDay.start();
			} catch (ParseException e) {
				logger.error("Impossible de lancer la tache de sauvegarde des tempratures de consigne", e);
			}
			
	        
	        initRefreshJobInitialised = true;
		}
		
	}
	
	public void refreshValues(){
		//On check si la temperature de consigne est tjs forcée
		getForcedTempCons();
		
		Calendar today = Calendar.getInstance();
		today.setTime(new Date());

		long diffWithCurrent = -3600*1000*24*10;
		long diffWithNext = 3600*1000*24*10;
		
		if(this.getTempConsignList().size() == 0){
			//On n'a aucune température de consigne!!!
			//On va essayé d'aller les chercher sur la sauvegarde de la semaine dernière glissante
			ArrayList<TempConsign> savedConsignList = TempConsignManagerDayBackup.loadSavedTC(zone);
			if(savedConsignList != null){
				tempConsignList = savedConsignList; 
			}
		}
		
		if(this.getTempConsignList().size() > 1){
			TempConsign tmpNextTempConsign = this.getTempConsignList().get(0);
			TempConsign tmpCurrentTempConsign = this.getTempConsignList().get(1);
			
			Iterator<TempConsign> iteConsignList =  this.getTempConsignList().iterator();
			while(iteConsignList.hasNext()){
				TempConsign tempConsign = iteConsignList.next();
				Calendar tmpCalendar = tempConsign.getStartTempConsign();
				tmpCalendar.setWeekDate(today.get(Calendar.YEAR), today.get(Calendar.WEEK_OF_YEAR), tmpCalendar.get(Calendar.DAY_OF_WEEK));
				//On compare avec les valeurs stockees
				//int tmpDiff = tmpCurrentTempConsign.getStartTempConsign().compareTo(tmpCalendar);
				long tmpDiff = tmpCalendar.getTimeInMillis() - today.getTimeInMillis();
				if(tmpDiff <= 0 && tmpDiff > diffWithCurrent){
					tmpCurrentTempConsign = tempConsign;
					diffWithCurrent = tmpDiff;
					
				}else if(tmpDiff > 0 && tmpDiff < diffWithNext){
					tmpNextTempConsign = tempConsign;
					diffWithNext = tmpDiff;
					
				}
			}
			
			nextTempConsign = tmpNextTempConsign;
			currentTempConsign = tmpCurrentTempConsign;
		}else if(this.getTempConsignList().size() ==  1){
			currentTempConsign = this.getTempConsignList().get(0);
			nextTempConsign = currentTempConsign;
		}
	}
	
	public String getZone() {
		return zone;
	}

	@Override
	public TempConsign getNextTempConsign() {
		return nextTempConsign;
	}

	@Override
	public TempConsign getCurrentTempConsign() {
		TempConsign tempCons = getForcedTempCons();
		
		if(tempCons == null){
			tempCons = currentTempConsign;
		}
		return tempCons;
	}

	public synchronized ArrayList<TempConsign> getTempConsignList() {
		return tempConsignList;
	}

	public synchronized void setTempConsignList(ArrayList<TempConsign> tempConsignList) {
		this.tempConsignList = tempConsignList;
	}

	
	@Override
	public void stop() {
		try {
			if(sched != null){
				sched.shutdown();
			}
		} catch (SchedulerException e) {
			logger.error("Erreur sur l'arret du scheduler du manager de temperature de consigne", e);
		}
	}

	
	public void forceTempConsign(double newTempConsign, int duration){
		tempConsignForced = new TempConsignForced(zone, newTempConsign, duration);
		ForcedTempConsignInfoMessage tmpMsg = new ForcedTempConsignInfoMessage();
		tmpMsg.setMsgVal(Double.toString(newTempConsign));
		CommonModule.getInstance().treateMessage(tmpMsg);
	}
	
	private TempConsignForced getForcedTempCons(){
		if(tempConsignForced != null && tempConsignForced.isStillValid()){
			return tempConsignForced;
		}else{
			tempConsignForced = null;
			CommonModule.getInstance().removeCurrentValueForZone(zone, "FTC");
			return null;
		}
		
	}
	
}
