cd /home/centos/ANALYTICS_MODULE/analytics_module 

/home/centos/ANALYTICS_MODULE/bin/celery multi restart worker -A smartcity \
--loglevel=INFO \
--workdir=/home/centos/ANALYTICS_MODULE/analytics_module \
--pidfile=/home/centos/ANALYTICS_MODULE/analytics_module/celery/run/celery-worker-smartcity.pid \
--logfile=/home/centos/ANALYTICS_MODULE/analytics_module/celery/logs/celery-worker-smartcity.log
