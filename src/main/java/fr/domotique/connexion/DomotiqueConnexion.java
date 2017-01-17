package fr.domotique.connexion;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.DailyTimeIntervalScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;


public abstract class DomotiqueConnexion implements IDomotiqueConnexion {
	private static Logger logger = LogManager.getLogger(DomotiqueConnexion.class.getName());
	
	private boolean isrunning = false;
	private static Scheduler sched;
	
	static{
		//On initialise le watchdog
		SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();
		try {
			sched = schedFact.getScheduler();		
			JobDetail jobDetail =  JobBuilder.newJob(DomotiqueConnexionWatchDogJob.class).build();
			TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();
			triggerBuilder.withSchedule(DailyTimeIntervalScheduleBuilder.dailyTimeIntervalSchedule().withIntervalInMinutes(30));
			triggerBuilder.forJob(jobDetail);
			triggerBuilder.startNow();
			Trigger trigger = triggerBuilder.build();
	         
	        sched.scheduleJob(jobDetail, trigger);
	        sched.start();
		} catch (SchedulerException e) {
			logger.error("Impossible de charger le watchdog des connexions", e);
		}
	}
	
	public DomotiqueConnexion(){
		
	}
	
	@Override
	public boolean isRunning() {
		return isrunning;
	}

	@Override
	public void stop() {
		isrunning = false;
	}

	@Override
	public void setStarted() {
		isrunning = true;
	}

	@Override
	public int maxTimeWithoutMsg() {
		// par défaut on ne gère pas de watchdog
		return 0;
	}

	

}
