package fr.domotique.module.thermostat;

import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;


public class ThermostatRefreshJob implements Job{
	static Logger logger = LogManager.getLogger(ThermostatRefreshJob.class.getName());
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		
		ThermostatModule.getInstance().refreshCurrentValues();

	}

}
