package fr.domotique.module.thermostat;

import static org.junit.Assert.*;

import org.junit.Test;

import fr.domotique.DomotiqueMessageManagerProxy;
import fr.domotique.message.InfoMessage;
import fr.domotique.module.common.CommonModule;

public class AjoutTempExtTest {

	@Test
	public void test() {
		InfoMessage msgTI = new InfoMessage();
		msgTI.setZone("1");
		msgTI.setMsgValType("TI");
		msgTI.setMsgVal("220");
		CommonModule.getInstance().treateMessage(msgTI);
		
		InfoMessage msgTE = new InfoMessage();
		msgTE.setZone("0");
		msgTE.setMsgValType("TE");
		msgTE.setMsgVal("120");
		CommonModule.getInstance().treateMessage(msgTE);
		
		assertEquals(CommonModule.getInstance().getCurrentValues().get("1").get("TE").getMsgVal(), "120");
	}

}
