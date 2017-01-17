package fr.domotique.module.saver.logger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.domotique.message.IMessage;
import fr.domotique.module.saver.BasicSaverResult;
import fr.domotique.module.saver.ISaver;
import fr.domotique.module.saver.ISaverResult;
import fr.domotique.module.saver.SaverModule;
import fr.domotique.module.thermostat.message.GazInfoMessage;

public class LogSaver implements ISaver {
	private Logger logger = LogManager.getLogger(LogSaver.class.getName());
	
	public LogSaver()
	{}
	
	@Override
	public ISaverResult saveMessage(IMessage msg) {
		logger.info(msg.toString());
		return null;
	}

}
