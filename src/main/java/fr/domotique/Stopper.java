package fr.domotique;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * A Tool you can use to stop your service process gracefully via JMX locally and safely.
 *
 * Shutdown needs tools.jar in classpath, so to run this class, assign the path of tools.jar in your system in the shell script, it's a must prerequisite!
 *
 * @since 2014-10-24
 */
public class Stopper{

	private static Logger logger = LogManager.getLogger(Stopper.class.getName());
	
    public static final String LOCAL_CONNECTOR_ADDRESS_URL = "com.sun.management.jmxremote.localConnectorAddress";

    /**
     * current process's pid
     */
    private String pid;
    /**
     * the managed bean's name of the top service that we will stop
     */
    private String mbeanName;
    /**
     * the operation name of the managed bean, usually named "stop", "shutdown", "destroy" without any parameters.
     */
    private String mbeanMethodName;

    protected JMXServiceURL getConnectorAddressAsPerPid(String pid) throws IOException, AttachNotSupportedException, AgentLoadException, AgentInitializationException {
        VirtualMachine vm = VirtualMachine.attach(pid);
        String connectorAddress = vm.getAgentProperties().getProperty(LOCAL_CONNECTOR_ADDRESS_URL);
        if (connectorAddress == null) {
            String agent = vm.getSystemProperties().getProperty("java.home") + File.separator + "lib" + File.separator + "management-agent.jar";
            vm.loadAgent(agent);
            connectorAddress = vm.getAgentProperties().getProperty(LOCAL_CONNECTOR_ADDRESS_URL);
        }
        return new JMXServiceURL(connectorAddress);
    }

    public Object execute() throws Throwable {
    	System.out.println("6");
        validate(pid, "pid");
        System.out.println("7");
        validate(mbeanName, "mbeanName");
        System.out.println("8");
        validate(mbeanMethodName, "mbeanMethodName");
        System.out.println("9");
        JMXConnector jmxConnector = null;
        
	    try {
	    	jmxConnector = JMXConnectorFactory.newJMXConnector(getConnectorAddressAsPerPid(getPid()), null);
	    	System.out.println("10");
	    	jmxConnector.connect();
	    	System.out.println("11");
            MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();
            System.out.println("12");
            return connection.invoke(ObjectName.getInstance(getMbeanName()), getMbeanMethodName(), null, null);
        }catch(Throwable e){
        	logger.error("Erreur lors de l'arret du service via JMX", e);
        	throw e;
        } finally {
        	System.out.println("13");
        	if(jmxConnector != null){
        		jmxConnector.close();
        	}
        }
    }

    protected void validate(String property, String propertyName) {
        if (property == null || property.trim().isEmpty())
            throw new IllegalStateException("[" + propertyName + "] must be set");
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getMbeanName() {
        return mbeanName;
    }

    public void setMbeanName(String mbeanName) {
        this.mbeanName = mbeanName;
    }

    public String getMbeanMethodName() {
        return mbeanMethodName;
    }

    public void setMbeanMethodName(String mbeanMethodName) {
        this.mbeanMethodName = mbeanMethodName;
    }

    /**
     * Shutdown shutdown = new Shutdown();
     * shutdown.setPid("7198");
     * shutdown.setMbeanName("com.sun.management:type=DiagnosticCommand");
     * shutdown.setMbeanMethodName("vmSystemProperties");
     * System.out.println(shutdown.execute());
     */
    public static void main(String[] args) throws Throwable {
    	logger.info("Lancement du processus d'arret du service domotique");
        Stopper stopper = new Stopper();
        System.out.println("1");
        stopper.setPid(args[0]);
        System.out.println("2");
        stopper.setMbeanName(Launcher.jmxBeanName);
        System.out.println("3");
        stopper.setMbeanMethodName("stop");
        System.out.println("4");
        stopper.execute();   
        System.out.println("5");
    }
}