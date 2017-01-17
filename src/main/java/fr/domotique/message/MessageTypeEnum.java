package fr.domotique.message;

import java.util.HashSet;

public class MessageTypeEnum extends HashSet<String> {
	public MessageTypeEnum(){
		super();
		//this.add("C");//Config
		this.add("I");//information
		this.add("O");//Ordre
		this.add("E");//Erreur
	}
}
