#!/bin/sh
cd /home/centos/ANALYTICS_MODULE/analytics_module

/usr/bin/nohup /home/centos/ANALYTICS_MODULE/bin/python3.6 /home/centos/ANALYTICS_MODULE/analytics_module/manage.py runserver --insecure 0:8000 &

