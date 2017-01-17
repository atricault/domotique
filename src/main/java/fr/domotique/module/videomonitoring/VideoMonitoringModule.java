package fr.domotique.module.videomonitoring;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import fr.domotique.DomotiqueMessageManagerProxy;
import fr.domotique.message.IMessage;
import fr.domotique.message.UnknownMesssageException;
import fr.domotique.module.IDomotiqueModule;
import fr.domotique.module.thermostat.message.TempConsignInfoMessage;
import fr.domotique.module.videomonitoring.message.VideoActivateMessage;

public class VideoMonitoringModule implements IDomotiqueModule {
	private boolean currentStatus = false;
	private static Logger logger = LogManager.getLogger(VideoMonitoringModule.class.getName());
	VideoMonitoringMessageFactory msgFactory = VideoMonitoringMessageFactory.getInstance();
	
	private boolean useTempConsign = false;
	private String zoneTempConsign = "0";
	private double miniTempConsign = 0;
	
	/** Holder */
	private static class SingletonHolder
	{		
		/** Instance unique non préinitialisée */
		private final static VideoMonitoringModule instance = new VideoMonitoringModule();
	}
	
	public static VideoMonitoringModule getInstance() {
		return SingletonHolder.instance;
	}
	
	
	private VideoMonitoringModule(){	
		
		zoneTempConsign = VideoMonitoringProperties.getInstance().getProperty("zone_thermo_tc");
		if(! "0".equals(zoneTempConsign)){	
			String strMiniTempConsign = VideoMonitoringProperties.getInstance().getProperty("temperature_mini"); 
			miniTempConsign= Double.parseDouble(strMiniTempConsign );
			useTempConsign = true;
			if(logger.isDebugEnabled()){
				logger.debug("Le controle de la video-surveillance est activé pour la zone " + zoneTempConsign + " avec une température minimale de " + miniTempConsign);
			}
		}
	}
			
		
	public void stopThread() {

	}

	@Override
	public void treateMessage(IMessage msg) {
		if(msg instanceof TempConsignInfoMessage){
			if(useTempConsign){
				if(msg.getZone().equals(zoneTempConsign)){
					double tempConsign = Double.parseDouble(((TempConsignInfoMessage) msg).getMsgVal()); 
					VideoActivateMessage vdoActivate = new VideoActivateMessage();
					if(miniTempConsign >= tempConsign){
						vdoActivate.setMsgVal("ON");
						currentStatus = true;
						if(logger.isDebugEnabled()){
							logger.debug("Activation de la détection de mouvement par videosurveillance");
						}
					}else{
						vdoActivate.setMsgVal("OFF");
						currentStatus = false;
						if(logger.isDebugEnabled()){
							logger.debug("Desactivation de la détection de mouvement par videosurveillance");
						}
					}
					DomotiqueMessageManagerProxy.getInstance().addMessageToSend(vdoActivate);
				}
			}
		}else if(msg instanceof VideoActivateMessage){
			VideoActivateMessage vdoActivate = null;
			if("ASK".equals(((VideoActivateMessage) msg).getMsgVal())){	
				vdoActivate = new VideoActivateMessage();
				if(currentStatus){
					vdoActivate.setMsgVal("ON");
				}else{
					vdoActivate.setMsgVal("OFF");
				}
			}else if("ACTIV".equals(((VideoActivateMessage) msg).getMsgVal())){
				vdoActivate = new VideoActivateMessage();
				vdoActivate.setMsgVal("ON");
				currentStatus = true;
			}else if("DESA".equals(((VideoActivateMessage) msg).getMsgVal())){
				vdoActivate = new VideoActivateMessage();
				vdoActivate.setMsgVal("OFF");
				currentStatus = false;
			}
			if(vdoActivate != null){
				DomotiqueMessageManagerProxy.getInstance().addMessageToSend(vdoActivate);
			}
		}
		
	}


	@Override
	public void treateMessage(String msg) {
		try{
			IMessage[] messages = msgFactory.buildMessage(msg);
			for(IMessage message : messages){
				treateMessage(message);
			}
		}catch(UnknownMesssageException UME){
			//On n'a pas a traiter ce message
		}
	}


	@Override
	public void shutdown() {
	}

}
