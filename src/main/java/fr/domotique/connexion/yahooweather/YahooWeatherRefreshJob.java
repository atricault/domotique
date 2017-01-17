/**
 * 
 */
package fr.domotique.connexion.yahooweather;

import java.io.IOException;

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
public class YahooWeatherRefreshJob implements  Job  {

	private static Logger logger = LogManager.getLogger(YahooWeatherRefreshJob.class.getName());
	
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		try{
			if(logger.isDebugEnabled()){
				logger.debug("Démarrage du script de récupération des données sur Yahoo Weather");
			}
			YahooWeatherService service = new YahooWeatherService();
			Channel channel = service.getForecast(YahooWeatherProperties.getInstance().getProperty("city_code"), DegreeUnit.FAHRENHEIT);
			if("ON".equalsIgnoreCase(YahooWeatherProperties.getInstance().getProperty("get_tempext"))){
				int tempExtDegF = channel.getItem().getCondition().getTemp();
				double tempExtDegC =  ((((float) tempExtDegF) - 32)/1.8);
				InfoMessage msgTempExt = new InfoMessage();
				msgTempExt.setMsgValType("TE");
				msgTempExt.setMsgVal(Double.toString(tempExtDegC));
				DomotiqueMessageManagerProxy.getInstance().addMessageToTreate(msgTempExt.encode());
			}
			if("ON".equalsIgnoreCase(YahooWeatherProperties.getInstance().getProperty("get_baro"))){
				Float baro = channel.getAtmosphere().getPressure();
				InfoMessage msgbaro = new InfoMessage();
				msgbaro.setMsgValType("BA");
				msgbaro.setMsgVal(baro.toString());
				DomotiqueMessageManagerProxy.getInstance().addMessageToTreate(msgbaro.encode());
			}
			
		} catch (JAXBException | IOException e) {
			logger.error("Erreur sur la récupération des données depuis l'API meteo de Yahoo", e);
		}catch (Exception e) {
			logger.error("Erreur sur la récupération des données depuis l'API meteo de Yahoo", e);
		}
	}

}
