#!/bin/sh
sudo cp   /home/centos/ANALYTICS_MODULE/analytics_module/celery/service/celery_worker.service /usr/lib/systemd/system/celery_worker.service
sudo cp   /home/centos/ANALYTICS_MODULE/analytics_module/celery/service/celery_beat.service /usr/lib/systemd/system/celery_beat.service

sudo systemctl daemon-reload

sudo systemctl enable celery_worker
sudo systemctl enable celery_beat

sudo systemctl start celery_worker
sudo systemctl start celery_beat
