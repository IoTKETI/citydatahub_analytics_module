cd /home/centos/ANALYTICS_MODULE/analytics_module

/home/centos/ANALYTICS_MODULE/bin/celery multi start beat -A smartcity --beat \
--loglevel=INFO \
--workdir=/home/centos/ANALYTICS_MODULE/analytics_module \
--pidfile=/home/centos/ANALYTICS_MODULE/analytics_module/celery/run/celery-beat-smartcity.pid \
--logfile=/home/centos/ANALYTICS_MODULE/analytics_module/celery/logs/celery-beat-smartcity.log
