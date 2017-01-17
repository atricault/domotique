package fr.domotique.message;

import java.util.ArrayList;
import java.util.HashSet;

import fr.domotique.module.saver.ISaver;

public interface IMessageFactory {
	public IMessage[] buildMessage(String message) throws UnknownMesssageException;
	
}
