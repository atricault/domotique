package fr.domotique.message.manager;

import java.util.Calendar;
import java.util.ConcurrentModificationException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.domotique.DomotiqueModuleProxy;
import fr.domotique.message.IMessage;

public class FIFOMessageReceive extends Thread{
	private static Logger logger = LogManager.getLogger(FIFOMessageReceive.class.getName());
	private LinkedBlockingQueue<String> msgATraiter;
	private boolean running = false;
	
	public FIFOMessageReceive(LinkedBlockingQueue<String> msgQueue){
		msgATraiter = msgQueue;
		this.start();
	}
	
	@Override
	public void run() {
		this.running = true;
		Calendar lastInfoRecue = Calendar.getInstance();

		while (this.running) {
			try {
				String msg = msgATraiter.take();
				DomotiqueModuleProxy.getInstance().treateMessage(msg);
			}catch (ConcurrentModificationException CME){
				logger.error("ConcurrentModificationException sur la queue de message recus", CME);
			}catch (InterruptedException e) {
				this.running = false;
				Thread.currentThread().interrupt(); // Très important de réinterrompre
	            break; // Sortie de la boucle infinie
			}catch (Exception e) {
				logger.info("Erreur sur le traitement d'un message non gérée par les modules", e);
			}
		}
	}
	
	public void shutdown(){
		this.running = false;
	}
}
