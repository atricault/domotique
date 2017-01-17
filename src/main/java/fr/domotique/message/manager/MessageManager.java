package fr.domotique.message.manager;

import java.util.ArrayList;
import java.util.Iterator;

import fr.domotique.message.IMessage;

public abstract class MessageManager implements IMessageManager {


	@Override
	public void addMessageToSend(ArrayList<IMessage> msg) {
		Iterator<IMessage> iteMsg = msg.iterator();
		while(iteMsg.hasNext()){
			this.addMessageToSend(iteMsg.next());
		}
	}
}
