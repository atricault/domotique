package fr.domotique.message;

import java.util.Calendar;

import fr.domotique.module.saver.ISaver;
import fr.domotique.module.saver.ISaverResult;

public interface IMessage {
	//Format des ordres/infos recues
	//On est limité a 32 bytes
	//<O|I>(Odre ou info);<ID Zone ou 0 pour toutes zones><HY|TI|TE|TC|GZ|RE>=<valeur>;<Liste des ID des modules ayant recus et envoyes le msg sep #, format #ID1#ID2#...>
	//Msg d'erreur
	//<E>;<Liste des ID des modules ayant recus et envoyes le msg sep #, format #ID1#ID2#...>;<Msg d'erreur>
	//message de config
	//<C>;<Module_Id>;<PIN_TEMPHUMI>;<PIN_RELAY>;<PIN_BUZZER>;<PIN_THERMOSTAT>;<PIN_GAZ>;<PIN_TEMPEXT>;<PORT_SERIE>;<GESTION_RELAIS>;<ZONE>;<Liste des ID des modules ayant recus et envoyes le msg sep #, format #ID1#ID2#...>

	public String encode();
	public String getMsgType();
	public String getModuleInitId();
	public String getZone();
	public Calendar getReceptionDate();
	public void setMsgType(String msgType);
	public void setModuleInitId(String moduleInitId);
	public void setZone(String zone) ;
	
	public void setReceptionDate(Calendar cal);
	public String getKey();
	public boolean isRepeatable();
	public String toString();
	
	public void postTreatment(ISaverResult result);
	public boolean equals(IMessage msg);
	
}
