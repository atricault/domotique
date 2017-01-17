package fr.domotique.module;

import fr.domotique.message.IMessage;
import fr.domotique.message.IMessageFactory;

public interface IDomotiqueModule {
	public void treateMessage(IMessage msg);
	public void treateMessage(String msg);
	public void shutdown();
}
