package fr.domotique.message;

public class OrdreMessage extends OrdreInfoMessage {

	
	public boolean isRepeatable(){
		return true;
	}
	
	public OrdreMessage() {
		super();
		this.setMsgType("O");
	}
	
	public OrdreMessage(String message) throws UnknownMesssageException {
		super(message);
		if (! "O".equals(this.getMsgType())){
			throw new UnknownMesssageException("Not an order message " + message);
		}
	}

	public OrdreMessage(String zone, MSG_VAL_TYPE valType, String val) {
		super("O;" + zone + ";" + valType.toString() + "=" + val + ";#0#");
	}

	@Override
	public String encode() {
		return this.msgType+";"+this.zone+";"+this.msgValType+"="+this.msgVal;
	}

	@Override
	public String getKey() {
		return zone + ";" + this.getMsgValType().toString();
	}

}
