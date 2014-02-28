#!/bin/bash
# description: Aipal Start Stop Restart
# processname: aipal
# chkconfig: 234 20 80
aipal_home=/data00/aipal

case $1 in
start)
  sudo -u tomcat sh -c "cd $aipal_home; ./start-aipal.sh"
;;
stop)
  sudo -u tomcat sh -c "cd $aipal_home; ./stop-aipal.sh"
;;
restart)
  sudo -u tomcat sh -c "cd $aipal_home; ./stop-aipal.sh"
  sleep 1
  sudo -u tomcat sh -c "cd $aipal_home; ./start-aipal.sh"
;;
status)
  if [ -f $aipal_home/aipal.pid ] && ps -p `cat $aipal_home/aipal.pid` > /dev/null; then
    echo "aipal (pid  `cat $aipal_home/aipal.pid`) is running..."
  else
    echo "aipal is stopped"
  fi
;;
esac
exit 0
