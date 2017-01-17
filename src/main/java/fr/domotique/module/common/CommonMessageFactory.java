package fr.domotique.module.common;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;

import fr.domotique.message.AskOrdreMessage;
import fr.domotique.message.ErrorMessage;
import fr.domotique.message.IMessage;
import fr.domotique.message.IMessageFactory;
import fr.domotique.message.InfoMessage;
import fr.domotique.message.OrdreMessage;
import fr.domotique.message.UnknownMesssageException;
import fr.domotique.message.OrdreInfoMessage.MSG_VAL_TYPE;
import fr.domotique.message.OrdreInfoMessage;
import fr.domotique.module.saver.ISaver;

public class CommonMessageFactory implements IMessageFactory {

	private CommonMessageFactory(){
		
	}

	/** Holder */
	private static class SingletonHolder
	{		
		/** Instance unique non préinitialisée */
		private final static CommonMessageFactory instance = new CommonMessageFactory();
	}
	
	public static CommonMessageFactory getInstance() {
		return SingletonHolder.instance;
	}
	
	
	public IMessage[] buildMessage(String message) throws UnknownMesssageException{
		try{
			IMessage[] returnMsg = new IMessage[1];
			if(message.startsWith("O")){
				if(message.indexOf(";" + OrdreInfoMessage.MSG_VAL_TYPE.ASK) != -1){
					returnMsg[0] = new AskOrdreMessage(message);
				}else{
					returnMsg[0] = new OrdreMessage(message);
				}
			}else if(message.startsWith("I")){
				returnMsg[0] = new InfoMessage(message);
			}else if(message.startsWith("E")){
				returnMsg[0] = new ErrorMessage(message);
			}else{
				throw new UnknownMesssageException("Type de message inconnu " + message);
			}
			Calendar now = Calendar.getInstance();
			returnMsg[0].setReceptionDate(now);
			return returnMsg;
		}catch (Exception ex){
			throw new UnknownMesssageException("Type de message inconnu " + message, ex);
		}
	}

}
