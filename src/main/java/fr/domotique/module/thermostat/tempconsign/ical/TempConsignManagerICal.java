package fr.domotique.module.thermostat.tempconsign.ical;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.DailyTimeIntervalScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import fr.domotique.module.thermostat.tempconsign.TempConsign;
import fr.domotique.module.thermostat.tempconsign.TempConsignForced;
import fr.domotique.module.thermostat.tempconsign.TempConsignManager;
import fr.domotique.module.thermostat.tempconsign.TempConsignManagerRefreshJob;
import net.fortuna.ical4j.filter.Filter;
import net.fortuna.ical4j.filter.PeriodRule;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.util.Calendars;

public class TempConsignManagerICal extends TempConsignManager  {

	private static boolean refreshICalJobInit = false;
	private static Logger logger = LogManager.getLogger(TempConsignManagerICal.class.getName());

	private TempConsignManagerICalProperties properties = null;
	
	private SimpleDateFormat iCalDateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
	
	public static TempConsignManagerICal getInstance(String zone) throws Exception{
		TempConsignManagerICal tmpConsignManager = (TempConsignManagerICal) getInstancesList().get(zone);
		if(tmpConsignManager == null){
			tmpConsignManager = new TempConsignManagerICal(zone);
			getInstancesList().put(zone, tmpConsignManager);
			
			if(! refreshICalJobInit){
				//On ajoute un job pour rafraichier les données du iCal
				SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();
				Scheduler sched = schedFact.getScheduler();
				
				JobDetail jobDetail =  JobBuilder.newJob(TempConsignManagerICalRefreshJob.class).build();
	
				TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();
				triggerBuilder.withSchedule(DailyTimeIntervalScheduleBuilder.dailyTimeIntervalSchedule().withIntervalInMinutes(Integer.parseInt(tmpConsignManager.properties.getProperty("refresh_ical_frequence"))));
				triggerBuilder.forJob(jobDetail);
				triggerBuilder.startNow();
				Trigger trigger = triggerBuilder.build();
		         
		        sched.scheduleJob(jobDetail, trigger);
			
		        sched.start();
		        refreshICalJobInit = true;
			}
		}
		return tmpConsignManager;
	}
	
	private TempConsignManagerICal(String zone) throws Exception{
		super(zone);
		properties = new TempConsignManagerICalProperties(zone);
		
        //On initialise le module
        assignTempConsignList();	
        
        refreshValues();
        
        initRefresh(Integer.parseInt(properties.getProperty("refresh_frequence")));
        
  	}

	public void assignTempConsignList() throws Exception{
		
		net.fortuna.ical4j.model.Calendar ical = Calendars.load(new URL(properties.getProperty("calendar_adresse")));
		if(ical == null){
			throw new Exception("Erreur sur le chargement de l'iCal " + properties.getProperty("calendar_adresse"));
		}
		Calendar now = Calendar.getInstance();
		now.setTime(new Date());
		
		//Détermination de la période de filtrage sur 1 jour avant et 1 jour après
		Calendar startPeriod = Calendar.getInstance();
		startPeriod.setTime(new Date());
		startPeriod.add(Calendar.DAY_OF_MONTH, -1);
		
		Calendar endPeriod = Calendar.getInstance();
		endPeriod.setTime(new Date());
		endPeriod.add(Calendar.DAY_OF_MONTH, 1);
		// create a period starting now with a duration of one (1) day..
		Period period = new Period(new DateTime(startPeriod.getTime()), new DateTime(endPeriod.getTime()));
		PeriodRule[] rules =  {new PeriodRule(period)};
		Filter filter = new Filter(rules, Filter.MATCH_ANY);

		Collection listEvent = filter.filter(ical.getComponents(Component.VEVENT));
		
		if(listEvent == null || listEvent.isEmpty()){
			throw new Exception("L'iCal est vide " + properties.getProperty("calendar_adresse"));
		}
		
		@SuppressWarnings("unchecked")
		Iterator<Component> iteEvent = listEvent.iterator();
		if(getTempConsignList() != null && ! getTempConsignList().isEmpty()){
			getTempConsignList().clear();
		}
		
		while(iteEvent.hasNext()){
			Component tmpEvent = iteEvent.next();

			//Gestion de la récurence sur les événements
			PeriodList listPeriod = tmpEvent.calculateRecurrenceSet(period);
			for (Object eventDate : listPeriod){
				try{
					Property tempVal = tmpEvent.getProperty(Property.SUMMARY);
					if(tempVal != null){
						
						Calendar tmpCalEndEvent = Calendar.getInstance();
						tmpCalEndEvent.setTime(((Period)eventDate).getEnd());
						if(tmpCalEndEvent.after(now)){
								
							TempConsign tempConsing = new TempConsign();
							tempConsing.setZone(getZone());
							
							Calendar tmpCal = Calendar.getInstance();
							tmpCal.setTime(((Period)eventDate).getStart());
							tempConsing.setStartTempConsign(tmpCal);
							
							tempConsing.setTempConsign(Double.parseDouble(tempVal.getValue()));
			
							if(logger.isDebugEnabled()){
								logger.debug("Ajout d'une temperature de consigne pour la zone " + getZone() + " Temp[" + tempConsing.getTempConsign() + "] Start at [" + loggerDateFormat.format(tempConsing.getStartTempConsign().getTime()));
							}
							getTempConsignList().add(tempConsing);
						}
					}
				}catch (Exception ex){
					logger.error("Erreur sur le chargement de l'événement " + tmpEvent.toString(), ex);
				}
			}
		}
	}
	
}
