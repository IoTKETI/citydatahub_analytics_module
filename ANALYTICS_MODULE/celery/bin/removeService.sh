#!/bin/sh

sudo systemctl stop celery_beat
sudo systemctl stop celery_worker

sudo systemctl disable celery_worker
sudo systemctl disable celery_beat

sudo rm /usr/lib/systemd/system/celery_worker.service
sudo rm /usr/lib/systemd/system/celery_beat.service

sudo systemctl daemon-reload

