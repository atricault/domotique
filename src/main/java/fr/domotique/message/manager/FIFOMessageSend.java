package fr.domotique.message.manager;

import java.util.ConcurrentModificationException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.domotique.DomotiqueConnexionProxy;
import fr.domotique.DomotiqueMessageManagerProxy;
import fr.domotique.message.IMessage;

public class FIFOMessageSend extends Thread  {
	private static Logger logger = LogManager.getLogger(FIFOMessageSend.class.getName());
	private boolean running = false;
	private LinkedBlockingQueue<IMessage> msgAEnvoyer;
	
	public FIFOMessageSend(LinkedBlockingQueue<IMessage> msgQueue){
		msgAEnvoyer = msgQueue;
		this.start();
	}
	
	
	public void run() {
		this.running = true;
		
		while (this.running) {			
			
			try {
				IMessage currMsg = msgAEnvoyer.take();
			
				if(logger.isDebugEnabled()){
					logger.debug("Envoi d'un message : " + currMsg.toString());
				}
				DomotiqueConnexionProxy.getInstance().sendMessage(currMsg);
			}catch (ConcurrentModificationException CME){
				logger.error("ConcurrentModificationException sur la queue de message a envoyer", CME);
			} catch (InterruptedException e) {
				logger.error("InterruptedException sur la queue de message a envoyer", e);
				this.running = false;
				Thread.currentThread().interrupt(); // Très important de réinterrompre
	            break; // Sortie de la boucle infinie
	        }catch(Exception e){
	        	logger.error("Erreur sur le traitement de la queue de message a envoyer", e);
	        }
		}
	}
	
	public void shutdown(){
		this.running = false;
	}


}
