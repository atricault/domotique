package fr.domotique.module.thermostat.thermostatgestion.hysteresis.derive;

import static org.junit.Assert.assertNotEquals;

import java.util.Hashtable;

import static org.junit.Assert.assertEquals;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import fr.domotique.connexion.http.DomotiqueConnexionHttp;
import fr.domotique.message.InfoMessage;
import fr.domotique.message.OrdreInfoMessage;

public class ChaudiereStatsTest {
	private static Logger logger = LogManager.getLogger(ChaudiereStatsTest.class.getName());
	
	@Test
	  public void evaluatesExpression() {
		String zone="1";
		ChaudiereStats stats = new ChaudiereStats(zone);
		assertNotEquals(stats.size(), 0);

		Hashtable<String, InfoMessage> currentValues = new Hashtable<String, InfoMessage>();
		InfoMessage msgTI = new InfoMessage();
		msgTI.setZone(zone);
		msgTI.setMsgVal("19.7");
		currentValues.put("TI", msgTI);
		InfoMessage msgTE = new InfoMessage();
		msgTE.setZone("0");
		msgTE.setMsgVal("5");
		currentValues.put("TE", msgTE);
		InfoMessage msgTC = new InfoMessage();
		msgTC.setZone(zone);
		msgTC.setMsgVal("18");
		currentValues.put("TC", msgTC);
		InfoMessage msgRE = new InfoMessage();
		msgRE.setZone(zone);
		msgRE.setMsgVal("0");
		currentValues.put("RE", msgRE);
		stats.addStatValFIFO(currentValues);
		
		logger.debug(stats.getTimesAnticipationSimilarChaudierePeriode(currentValues, 21.5)[0] + " millisecondes d'nticipation");
		
		/*currentValues.get(OrdreInfoMessage.MSG_VAL_TYPE.TC.toString()).setMsgVal("19.5");
		currentValues.get(OrdreInfoMessage.MSG_VAL_TYPE.RE.toString()).setMsgVal("1");
		stats.addStatValFIFO(currentValues);
		*/
		msgTE = new InfoMessage();
		msgTE.setZone("0");
		msgTE.setMsgVal("12");
		currentValues.put("TE", msgTE);
		
		stats.addStatValFIFO(currentValues);
	  }
}
