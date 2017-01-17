package fr.domotique.message.manager;

import java.util.ArrayList;

import fr.domotique.message.IMessage;

public interface IMessageManager {
	public void addMessageToTreate(String msg);
	public void addMessageToSend(IMessage msg);
	public void addMessageToSend(ArrayList<IMessage> msg);
	public void stop();
}
