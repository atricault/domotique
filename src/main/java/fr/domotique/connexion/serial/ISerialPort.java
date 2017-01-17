package fr.domotique.connexion.serial;

import java.io.IOException;
import java.util.Calendar;

import fr.domotique.message.manager.IMessageManager;

public interface ISerialPort {
	public void initPortCom() throws Exception;
	public void sendMsg(String msg) throws IOException;
	public void close();
	public Calendar getLastReceivedMesssage();
}
