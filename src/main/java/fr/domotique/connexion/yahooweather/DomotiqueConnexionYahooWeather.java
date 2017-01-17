/**
 * 
 */
package fr.domotique.connexion.yahooweather;

import java.io.IOException;
import java.util.Calendar;

import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.DailyTimeIntervalScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import com.github.fedy2.weather.YahooWeatherService;
import com.github.fedy2.weather.data.Channel;
import com.github.fedy2.weather.data.unit.DegreeUnit;

import fr.domotique.DomotiqueMessageManagerProxy;
import fr.domotique.connexion.DomotiqueConnexion;
import fr.domotique.message.IMessage;
import fr.domotique.message.InfoMessage;
import fr.domotique.module.thermostat.tempconsign.TempConsignManagerRefreshJob;

/**
 * @author okamaugo
 *
 */
public class DomotiqueConnexionYahooWeather extends DomotiqueConnexion {

	private Scheduler sched;
	private static Logger logger = LogManager.getLogger(DomotiqueConnexionYahooWeather.class.getName());
	
	/** Constructeur privé */	
	private DomotiqueConnexionYahooWeather(){
		if(logger.isDebugEnabled()){
			logger.debug("Chargement de la connexion à l'API Yahoo Weather");
		}
		
		startScheduler();
	}
 
	/** Holder */
	private static class SingletonHolder
	{		
		/** Instance unique non préinitialisée */
		private final static DomotiqueConnexionYahooWeather instance = new DomotiqueConnexionYahooWeather();
	}
 
	public static DomotiqueConnexionYahooWeather getInstance() {
		
		return SingletonHolder.instance;
	}

	private void startScheduler(){
		SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();
		try {
			sched = schedFact.getScheduler();		
			JobDetail jobDetail =  JobBuilder.newJob(YahooWeatherRefreshJob.class).build();
			TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();
			triggerBuilder.withSchedule(DailyTimeIntervalScheduleBuilder.dailyTimeIntervalSchedule().withIntervalInMinutes(Integer.parseInt(YahooWeatherProperties.getInstance().getProperty("refresh_frequence", "10"))));
			triggerBuilder.forJob(jobDetail);
			triggerBuilder.startNow();
			Trigger trigger = triggerBuilder.build();
	         
	        sched.scheduleJob(jobDetail, trigger);
	        sched.start();
	        this.setStarted();
		} catch (SchedulerException e) {
			logger.error("Impossible de charger la connexion vers l'API Yahoo Weather");
		}
	}
	
	@Override
	public void stop() {
		super.stop();
		
		try {
			sched.shutdown();
		} catch (SchedulerException e) {
			logger.error("Impossible d' arreter la connexion vers l'API Yahoo Weather", e);
		}
	}
	
	@Override
	public void reset() {
		this.stop();
		this.startScheduler();
	}

	@Override
	public void sendMessage(IMessage message) {
		//On ne fait rien
	}

	@Override
	public Calendar getLastReceivedMesssage() {
		return null;
	}


}
