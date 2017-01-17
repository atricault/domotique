/**
 * 
 */
package fr.domotique.connexion;

import java.util.Calendar;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import fr.domotique.DomotiqueConnexionProxy;

/**
 * @author okamaugo
 *
 */
public class DomotiqueConnexionWatchDogJob implements  Job  {

	private static Logger logger = LogManager.getLogger(DomotiqueConnexionWatchDogJob.class.getName());
	
	
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		if(logger.isDebugEnabled()){
			logger.debug("Démarrage du script Watchdog sur les connexions");
		}
		
		Calendar now;
		Iterator<IDomotiqueConnexion> iteLstCnx = DomotiqueConnexionProxy.getInstance().iterator();
		while(iteLstCnx.hasNext()){
			IDomotiqueConnexion cnx = iteLstCnx.next();
			
			if(cnx.maxTimeWithoutMsg() > 0){
				now = Calendar.getInstance();
				now.add(Calendar.MINUTE, - cnx.maxTimeWithoutMsg());
				//On a une gestion de watchdog pour cette cnx
				if(cnx.getLastReceivedMesssage().before(now)){
					//On a n'a pas recu de message depuis un certain temps!!!
					//On reset la cnx
					logger.info("La connexion " + cnx.getClass().getName() + " semble perdue, on la redémarre");
					cnx.reset();
				}
			}
		}
	}

}
