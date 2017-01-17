package fr.domotique.module.saver;

import fr.domotique.message.IMessage;

public interface ISaver {
 public ISaverResult saveMessage(IMessage msg);
}
