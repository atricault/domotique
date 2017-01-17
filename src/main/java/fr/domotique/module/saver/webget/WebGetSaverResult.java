package fr.domotique.module.saver.webget;

import fr.domotique.module.saver.ISaverResult;

public class WebGetSaverResult implements ISaverResult {

	private String[] arrayResult;
	
	public WebGetSaverResult(String[] array) {
		arrayResult = array;
	}

	@Override
	public String[] getResult() {
		return arrayResult;
	}

}
