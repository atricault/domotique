package fr.domotique.connexion;

import java.util.Calendar;

import fr.domotique.message.IMessage;

public interface IDomotiqueConnexion {
	public boolean isRunning();
	public void stop();
	public void setStarted();
	public void reset();
	void sendMessage(IMessage message);
	//Utiliser pour le watchdog
	public Calendar getLastReceivedMesssage();
	//Si =0 alors pas de gestion du wtchdog
	public int maxTimeWithoutMsg();
}
