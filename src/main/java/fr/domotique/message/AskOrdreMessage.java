package fr.domotique.message;

public class AskOrdreMessage extends OrdreMessage {
	public AskOrdreMessage(String message) throws UnknownMesssageException {
		super(message);
	}
	
	public AskOrdreMessage() {
		super();
		this.setMsgValType("ASK");
	}
	
	public boolean isRepeatable(){
		return false;
	}
	
	@Override
	public String encode() {
		return this.msgType+";"+this.zone+";"+this.msgValType;
	}
}
