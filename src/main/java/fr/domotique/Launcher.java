package fr.domotique;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.domotique.connexion.IDomotiqueConnexion;
import fr.domotique.connexion.serial.DomotiqueConnexionSerial;
import fr.domotique.connexion.serial.DomotiqueSerialConnexionProperties;
import fr.domotique.connexion.serial.ISerialPort;
import fr.domotique.message.IMessage;
import fr.domotique.module.IDomotiqueModule;
import fr.domotique.properties.CommonDomotiqueProperties;
import fr.domotique.properties.DomotiqueProperties;
public class Launcher implements DynamicMBean{

	public final static String jmxBeanName = Launcher.class.getPackage().getName() + ":type=" + Launcher.class.getSimpleName();
	
	private static boolean running = true;
	private static BufferedReader clavier = null;
	private static Logger logger = LogManager.getLogger(Launcher.class.getName());
	private static Thread killedThread = new Thread() {
														    public void run() {
														    	Launcher.getInstance().stop();
														    	this.interrupt();
														    }
														};
	
														
	/** Constructeur privé */	
	private Launcher(){
	}
 
	/** Holder */
	private static class SingletonHolder
	{		
		/** Instance unique non préinitialisée */
		private final static Launcher instance = new Launcher();
	}
 
	public static Launcher getInstance() {
		
		return SingletonHolder.instance;
	}
	
	public static ObjectName jmxName;
	
	public static void main(String[] args) {
		try{
			Runtime.getRuntime().addShutdownHook(killedThread);
			
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
			try {
			 // Uniquely identify the MBeans and register them with the platform MBeanServer
				
				jmxName = new ObjectName(jmxBeanName);
				//jmxName = new ObjectName("SimpleAgent:name=DomotiqueLauncher");
				mbs.registerMBean( Launcher.getInstance(), jmxName);
			    logger.info("Enregistrement JMX terminé");
			} catch(Exception e) {
				logger.error("Erreur lors de l'enregistrement JMX", e);
			}
			
			
			//"interface utilisateur"
			System.out.println("taper quit<enter> pour quitter");
			
			//Lancement du message manager
			DomotiqueMessageManagerProxy.getInstance();
			
			
			//Lancement des connexions
			String cnxStr = CommonDomotiqueProperties.getInstance().getProperty("connexion_class");
			String cnxs[] = cnxStr.split(";");
			for(String cnx : cnxs){
				try{
					if(logger.isDebugEnabled()){
						logger.debug("Chargement de la connexion " + cnx.trim());
					}
					Class cnxClass = Launcher.class.getClassLoader().loadClass(cnx.trim());
					Method getInstanceMethode = cnxClass.getMethod("getInstance", (Class[]) null);
					IDomotiqueConnexion cnxObj = (IDomotiqueConnexion) getInstanceMethode.invoke(null,(Object[]) null);
					DomotiqueConnexionProxy.getInstance().addConnexionToManage(cnxObj);
				}catch(Exception e){
					logger.error("Erreur sur le chargement de la connexion " + cnx.trim(), e);
				}
			}
			
			//Lancement de tous les modules
			String moduleStr = CommonDomotiqueProperties.getInstance().getProperty("domotique_modules_classes");
			String modules[] = moduleStr.split(";");
			for(String tmpModule : modules){
				try{
					if(logger.isDebugEnabled()){
						logger.debug("Chargement du module " + tmpModule.trim());
					}
					Class moduleClass = Launcher.class.getClassLoader().loadClass(tmpModule);
					Method getInstanceMethode = moduleClass.getMethod("getInstance", (Class[]) null);
					IDomotiqueModule moduleObj = (IDomotiqueModule) getInstanceMethode.invoke(null, (Object[]) null);
					DomotiqueModuleProxy.getInstance().addModuleToManage(moduleObj);
				}catch(Exception e){
					logger.error("Erreur sur le chargement du module " + tmpModule.trim(), e);
				}
			}
			
		}catch (Exception ex){
			logger.error("Erreur sur le l'initialisation du système", ex);
		}
			//On commence par demander à tous les modules leurs infos
			/*try {
				thermo.serialPort.getOutputStream().write(("O;0;ASK\r\n").getBytes("US-ASCII"));
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace(System.out);
			} catch (IOException e1) {
				e1.printStackTrace(System.out);
			}*/
			
			//construction flux lecture
			clavier =
				new BufferedReader(new InputStreamReader(System.in));
			//lecture sur le flux entrée.
			
			
			try {
				
				while (running) {
					String lu = clavier.readLine();
					if("quit".equalsIgnoreCase(lu)){
						break;
					}else if(lu != null && ! "".equals(lu)){
						try {
							DomotiqueMessageManagerProxy.getInstance().addMessageToTreate(lu);
						} catch (Exception e) {
							logger.error("Message reçu dans la console non compris "+lu, e);
						}
					}
				}
			} catch (IOException e) {
			}
			getInstance().stop();
			
	}
	
	
	private void stop(){
		if(running){
			logger.info("Arret du service de domotique");
			running = false;
			try{
				DomotiqueConnexionProxy.getInstance().stop();
			}catch(Exception e){
				logger.error("Erreur sur l'arret des connexions", e);
			}
			try{
				DomotiqueMessageManagerProxy.getInstance().stop();
			}catch(Exception e){
				logger.error("Erreur sur l'arret du message manager", e);
			}
			try{
				DomotiqueModuleProxy.getInstance().shutdown();
			}catch(Exception e){
				logger.error("Erreur sur l'arret des modules", e);
			}
			Runtime.getRuntime().removeShutdownHook(killedThread);
			try{
				clavier.close();
			}catch(Exception e){
				logger.error("Erreur sur l'arret la console", e);
			}
		}
	}

	@Override
	public Object getAttribute(String attribute)
			throws AttributeNotFoundException, MBeanException, ReflectionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAttribute(Attribute attribute)
			throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public AttributeList getAttributes(String[] attributes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AttributeList setAttributes(AttributeList attributes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object invoke(String actionName, Object[] params, String[] signature)
			throws MBeanException, ReflectionException {
		if("stop".equals(actionName)){
			getInstance().stop();
		}
		return null;
	}

	@Override
	public MBeanInfo getMBeanInfo() {

     MBeanAttributeInfo[] attrs = {};
     MBeanOperationInfo[] opers = { new MBeanOperationInfo("stop", "Stop the domotique service", null, "void", 1) };

     return new MBeanInfo(this.getClass().getName(), "Launcher du service de domotique", attrs, null, opers, null);
	}


}
