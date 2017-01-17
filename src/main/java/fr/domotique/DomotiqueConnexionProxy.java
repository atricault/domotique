package fr.domotique;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.domotique.connexion.DomotiqueConnexion;
import fr.domotique.connexion.IDomotiqueConnexion;
import fr.domotique.connexion.http.DomotiqueConnexionHttp;
import fr.domotique.message.IMessage;

public class DomotiqueConnexionProxy extends DomotiqueConnexion{
	private ArrayList<IDomotiqueConnexion> listOfCnx = new ArrayList<IDomotiqueConnexion>();
	

	private static Logger logger = LogManager.getLogger(DomotiqueConnexionProxy.class.getName());
	
	
	private DomotiqueConnexionProxy(){}
	/** Holder */
	private static class SingletonHolder
	{		
		/** Instance unique non préinitialisée */
		private final static DomotiqueConnexionProxy instance = new DomotiqueConnexionProxy();
	}
	
	public static DomotiqueConnexionProxy getInstance() {
		return SingletonHolder.instance;
	}

	public void addConnexionToManage(IDomotiqueConnexion cnx){
		listOfCnx.add(cnx);
	}
	
	public void sendMessage(IMessage msg){
		Iterator<IDomotiqueConnexion> iteCnx = listOfCnx.iterator();
		while(iteCnx.hasNext()){
			IDomotiqueConnexion cnx = iteCnx.next();
			try{
				cnx.sendMessage(msg);
			}catch (Exception e){
				logger.info("Erreur sur le traitement du message " + msg.toString() + " avec la connexion " + cnx.getClass(), e);
			}
		}
	}

	@Override
	public void reset() {
		Iterator<IDomotiqueConnexion> iteCnx = listOfCnx.iterator();
		while(iteCnx.hasNext()){
			IDomotiqueConnexion cnx = iteCnx.next();
			try{
				cnx.reset();
			}catch (Exception e){
				logger.error("Erreur sur le reset de la connexion " + cnx.getClass(), e);
			}
		}
	}
	
	@Override
	public void stop() {
		Iterator<IDomotiqueConnexion> iteCnx = listOfCnx.iterator();
		while(iteCnx.hasNext()){
			IDomotiqueConnexion cnx = iteCnx.next();
			try{
				cnx.stop();
			}catch (Exception e){
				logger.error("Erreur sur le stop de la connexion " + cnx.getClass(), e);
			}
		}
	}

	@Override
	public Calendar getLastReceivedMesssage() {
		return null;
	}

	public Iterator<IDomotiqueConnexion> iterator() {
		return listOfCnx.iterator();
	}
	
}
