package fr.domotique.connexion.serial;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.TooManyListenersException;

import javax.comm.CommPortIdentifier;
import javax.comm.NoSuchPortException;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import javax.comm.SerialPortEvent;
import javax.comm.SerialPortEventListener;
import javax.comm.UnsupportedCommOperationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.comm.Win32Driver;

import fr.domotique.message.manager.IMessageManager;
import fr.domotique.properties.CommonDomotiqueProperties;
import fr.domotique.DomotiqueMessageManagerProxy;

public class DomotiqueSerialJavaxCommImpl  implements SerialPortEventListener, ISerialPort {
	static Logger logger = LogManager.getLogger(DomotiqueSerialJavaxCommImpl.class.getName());
	private IMessageManager msgManager = DomotiqueMessageManagerProxy.getInstance();
	private SerialPort serialPort;
	private BufferedReader fluxLecture; 
	private Calendar lastReceivedMesssage = Calendar.getInstance();
	
	public DomotiqueSerialJavaxCommImpl() {
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
					while(serialPort.getInputStream().available() > 0){
						//lecture du buffer et affichage
						String strMsg;
						if((strMsg = ((String) fluxLecture.readLine()).trim()) != null && DomotiqueConnexionSerial.getInstance().isRunning()){
							if(logger.isDebugEnabled()){
								logger.debug("Message reçu : " + strMsg + "");
							}
							lastReceivedMesssage = Calendar.getInstance();
							msgManager.addMessageToTreate(strMsg);
						}
					}
				} catch (IOException e) {
					logger.info("IOErreur sur la reception sur le port serie", e);
				} catch (Exception e) {
					logger.info("Erreur sur la reception sur le port serie", e);
				}
				break;
		}
		
	}


	@Override
	public void initPortCom() throws Exception {

		//initialisation du driver
		if( System.getProperty("os.name").toLowerCase().indexOf("win") != -1){
 			Win32Driver w32Driver = new Win32Driver();
 			w32Driver.initialize();
 		}
		
		//récupération du port
		CommPortIdentifier portId;
		try{
			portId=CommPortIdentifier.getPortIdentifier(CommonDomotiqueProperties.getInstance().getProperty("port_name"));
		}catch(NoSuchPortException ex){
			String error = "Erreur, le port " + CommonDomotiqueProperties.getInstance().getProperty("port_name") + " n'existe pas. Les ports disponibles sont : ";
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
			serialPort.setSerialPortParams(Integer.parseInt(CommonDomotiqueProperties.getInstance().getProperty("port_speed")), SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
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
		if(logger.isDebugEnabled()){
			logger.debug("Envoi d'un message sur le port COM " + msg);
		}
		serialPort.getOutputStream().write((msg+"\r\n").getBytes("US-ASCII"));
		serialPort.getOutputStream().flush();	
	}

	@Override
	public void close() {
		try {
			fluxLecture.close();
		} catch (IOException e) {
		}
		this.serialPort.close();
	}

	@Override
	public Calendar getLastReceivedMesssage() {
		return lastReceivedMesssage;
	}
	
}
