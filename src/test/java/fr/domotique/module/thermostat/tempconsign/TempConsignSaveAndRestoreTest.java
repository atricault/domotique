package fr.domotique.module.thermostat.tempconsign;

import static org.junit.Assert.assertNotEquals;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;

import static org.junit.Assert.assertEquals;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.quartz.JobExecutionException;

public class TempConsignSaveAndRestoreTest {
	private static Logger logger = LogManager.getLogger(TempConsignSaveAndRestoreTest.class.getName());
	
	@Test
	  public void evaluatesExpression() throws Exception {
		String zone="1";
		ITempConsignManager manager = fr.domotique.module.thermostat.tempconsign.properties.TempConsignManager.getInstance(zone);
		//fr.domotique.module.thermostat.tempconsign.properties.TempConsignManager tcSetter = fr.domotique.module.thermostat.tempconsign.properties.TempConsignManager.getInstance(zone);
		manager.refreshValues();

		ArrayList<TempConsign> savedTC = TempConsignManagerDayBackup.loadSavedTC(zone);
		int sizeOfTCListSaved = savedTC.size();
		
		//On sauvegarde
		TempConsignManagerDayBackup backupManger = new TempConsignManagerDayBackup();
		backupManger.execute(null);

		int sizeOfTCListInit = TempConsignManagerDayBackup.filterTCListForDay(manager.getTempConsignList(), Calendar.getInstance()).size();

		assertEquals(sizeOfTCListInit, sizeOfTCListSaved);

		
		savedTC = TempConsignManagerDayBackup.loadSavedTC(zone);
		sizeOfTCListSaved = savedTC.size();
		
		assertEquals(sizeOfTCListInit, sizeOfTCListSaved);

	  }
}
