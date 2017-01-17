#!/bin/bash
  # 
  # Domotique 
  # 
  # chkconfig: 2345 99 99 
  # description: Domotique Daemon \ 
  # processname: domotique 
### BEGIN INIT INFO
# Provides:          domotique
# Required-Start:    $all
# Required-Stop:    
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: Script de demarrage du service de domotique
# Description:       Script de demarrage du service de domotique Java
### END INIT INFO
  case "$1" in
	start) 
			sudo mount -a
        	sudo java -Djava.library.path=/usr/lib/jni -jar /opt/domotique/domotique.jar &
            ;; 
        stop) 
            pid=`ps -aux | grep 'java -jar domotique.jar' | cut -b11,12,13,14` 
            kill -9 $pid 
                ;; 
        restart) 
            echo "Redémarrage du serveur : " 
            echo "" 
            sh $0 stop 
            sh $0 start 
            echo "" 
            echo "Serveur redémarré." 
            ;; 
        reload)
			sh $0 restart
			;;
		*) 
      		echo "Usage: domotique [start|stop|restart]" >&2 
       		exit 1
esac
exit 0