package fr.domotique.message;

public class InfoMessage extends OrdreInfoMessage {

	public InfoMessage(String message) throws UnknownMesssageException{
		super(message);
		if (! "I".equals(this.getMsgType())){
			throw new UnknownMesssageException("Not an information message " + message);
		}
	}

	public InfoMessage() {
		super();
		this.setMsgType("I");
	}

	

}
