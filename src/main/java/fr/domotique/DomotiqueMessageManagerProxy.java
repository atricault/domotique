package fr.domotique;

import java.lang.reflect.Method;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.domotique.message.IMessage;
import fr.domotique.message.manager.IMessageManager;
import fr.domotique.message.manager.MessageManager;
import fr.domotique.properties.CommonDomotiqueProperties;

public class DomotiqueMessageManagerProxy extends MessageManager {
	private static Logger logger = LogManager.getLogger(DomotiqueMessageManagerProxy.class.getName());
	
	
	private IMessageManager messageManager;
	
	/** Constructeur privé */	
	private DomotiqueMessageManagerProxy(){
		try {
			String messageManagerClassStr = CommonDomotiqueProperties.getInstance().getProperty("domotique_message_manager_class");
			Class messageManagerClass = this.getClass().getClassLoader().loadClass(messageManagerClassStr);
			Method messageManagerInstanceMethod = messageManagerClass.getMethod("getInstance", (Class[]) null);
			messageManager = (IMessageManager) messageManagerInstanceMethod.invoke(null, (Object[]) null);
		} catch (NoSuchMethodException | SecurityException e) {
			logger.error("Erreur sur le lancement du message manager proxy", e);
		} catch (Exception e) {
			logger.error("Erreur sur le lancement du message manager proxy", e);
		}
	}
 
	/** Holder */
	private static class SingletonHolder
	{		
		/** Instance unique non préinitialisée */
		private final static DomotiqueMessageManagerProxy instance = new DomotiqueMessageManagerProxy();
	}
 
	public static IMessageManager getInstance() {
		return SingletonHolder.instance;
	}
	
	@Override
	public void addMessageToTreate(String msg) {
		if(logger.isDebugEnabled()){
			logger.debug("Ajout d'un message a traiter " + msg);
		}
		messageManager.addMessageToTreate(msg);
	}

	@Override
	public void addMessageToSend(IMessage msg) {
		if(logger.isDebugEnabled()){
			logger.debug("Ajout d'un message a envoyer " + msg);
		}
		messageManager.addMessageToSend(msg);
		
	}

	@Override
	public void stop() {
		messageManager.stop();
	}

	
}
