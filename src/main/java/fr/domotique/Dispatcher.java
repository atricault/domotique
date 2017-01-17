package fr.domotique;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Dispatcher {
	
	private static Logger logger = LogManager.getLogger(Dispatcher.class.getName());
	
	
	private static final Map<String, Class<?>> ENTRY_POINTS =
	        new HashMap<String, Class<?>>();
	    static{
	        ENTRY_POINTS.put("start", Launcher.class);
	        ENTRY_POINTS.put("stop", Stopper.class);
	    }

	    public static void main(final String[] args) throws Throwable{
	    	Class class2Load;
	        if(args.length < 1){
	            class2Load = ENTRY_POINTS.get("start");
	        }else{
	        	class2Load = ENTRY_POINTS.get(args[0]);
	        }
	        
	        logger.info("Démarrage du dispatcher avec la classe " + class2Load.getName());
	        
	        final Class<?> entryPoint = class2Load;
	        if(entryPoint==null){
	            // throw exception, entry point doesn't exist
	        }
	        final String[] argsCopy =
	            args.length > 1
	                ? Arrays.copyOfRange(args, 1, args.length)
	                : new String[0];
	        entryPoint.getMethod("main", String[].class).invoke(null,
	            (Object) argsCopy);

	    }
	}