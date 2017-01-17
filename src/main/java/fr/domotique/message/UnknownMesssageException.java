package fr.domotique.message;

public class UnknownMesssageException extends Exception {
	private static final long serialVersionUID = -1824968843837528891L;

	public UnknownMesssageException(String msg){
		super(msg);
	}

	public UnknownMesssageException(String string, Exception ex) {
		super(string, ex);
	}
}
