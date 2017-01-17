package fr.domotique.connexion.serial;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.TooManyListenersException;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.domotique.DomotiqueMessageManagerProxy;
import fr.domotique.message.manager.IMessageManager;

public class DomotiqueSerialRXTXImpl  implements SerialPortEventListener, ISerialPort {
	static Logger logger = LogManager.getLogger(DomotiqueSerialRXTXImpl.class.getName());
	private SerialPort serialPort;
	private BufferedReader fluxLecture;
	private boolean running = true;
	private Calendar lastReceivedMesssage = Calendar.getInstance();
	
	public DomotiqueSerialRXTXImpl() {
		// nothing to do
	}
	
	@Override
	public void serialEvent(SerialPortEvent event) {
		
		switch (event.getEventType()) {
			case SerialPortEvent.BI :
			case SerialPortEvent.OE :
			case SerialPortEvent.FE :
			case SerialPortEvent.PE :
			case SerialPortEvent.CD :
			case SerialPortEvent.CTS :
			case SerialPortEvent.DSR :
			case SerialPortEvent.RI :
			case SerialPortEvent.OUTPUT_BUFFER_EMPTY :
				break;
			case SerialPortEvent.DATA_AVAILABLE :
				
				try {
					while(running && serialPort.getInputStream().available() > 0){
						//lecture du buffer et affichage
						String strMsg;
						if((strMsg = ((String) fluxLecture.readLine()).trim()) != null && DomotiqueConnexionSerial.getInstance().isRunning()){
							if(logger.isDebugEnabled()){
								logger.debug("Message reçu : " + strMsg + "");
							}
							lastReceivedMesssage = Calendar.getInstance();
							DomotiqueMessageManagerProxy.getInstance().addMessageToTreate(strMsg);
						}
					}
				} catch (IOException e) {
					logger.error("IOErreur sur la reception sur le port serie", e);
				} catch (Exception e) {
					logger.error("Erreur sur la reception sur le port serie", e);
				}
				break;
		}
		
	}


	@Override
	public void initPortCom() throws Exception {
		//récupération du port
		CommPortIdentifier portId;
		try{
			portId=CommPortIdentifier.getPortIdentifier(DomotiqueSerialConnexionProperties.getInstance().getProperty("port_name"));
		}catch(NoSuchPortException ex){
			String error = "Erreur, le port " + DomotiqueSerialConnexionProperties.getInstance().getProperty("port_name") + " n'existe pas. Les ports disponibles sont : ";
			//récupération de l'énumération
			Enumeration portList=CommPortIdentifier.getPortIdentifiers();
			//affichage des noms des ports
			while (portList.hasMoreElements()){
				portId=(CommPortIdentifier)portList.nextElement();
				error += "[" + portId.getName() + "]";
			}
			logger.error(error, ex);
			throw ex;
		}
		
		try {
			serialPort=(SerialPort)portId.open(this.getClass().getCanonicalName(), 10000);
		} catch (PortInUseException ex) {
			throw ex;
		}
		
		//récupération du flux
		try {

			serialPort.setInputBufferSize(8192);
			serialPort.setOutputBufferSize(8192);
			
			fluxLecture =
				new BufferedReader(
					new InputStreamReader(serialPort.getInputStream()));

		} catch (IOException e) {
			throw e;
		}
		
		try{
			serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
			serialPort.setSerialPortParams(Integer.parseInt(DomotiqueSerialConnexionProperties.getInstance().getProperty("port_speed")), SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
		}catch (UnsupportedCommOperationException ex){
			throw ex;
		}

		// ajout d'un Listener au port.
		try {
			serialPort.addEventListener(this);
		} catch (TooManyListenersException ex) {
			throw ex;
		}
		
		//paramétrage du port
		serialPort.notifyOnDataAvailable(true);
		
		if(logger.isDebugEnabled()){
			logger.debug("Port Com initialisé");
		}
	}

	@Override
	public void sendMsg(String msg) throws IOException {
		if(serialPort != null){
			if(logger.isDebugEnabled()){
				logger.debug("Envoi d'un message sur le port COM " + msg);
			}
			serialPort.getOutputStream().write((msg+"\r\n").getBytes("US-ASCII"));
			serialPort.getOutputStream().flush();
		}
	}

	@Override
	public void close() {
		running = false;
		try {
			logger.info("Fermeture du port COM " + DomotiqueSerialConnexionProperties.getInstance().getProperty("port_name"));
		} catch (Exception e) {
			logger.info("Fermeture du port COM", e);
		}
		try {
			fluxLecture.close();
		} catch (IOException e) {
			logger.error("Erreur sur la fermeture du stream du port COM", e);
		}
		this.serialPort.close();
	}

	@Override
	public Calendar getLastReceivedMesssage() {
		return lastReceivedMesssage;
	}
	
}
