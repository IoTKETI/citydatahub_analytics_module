#!/bin/sh

sudo cp   /home/centos/ANALYTICS_MODULE/analytics_module/service/analytics_module.service /usr/lib/systemd/system/analytics_module.service

sudo systemctl daemon-reload

sudo systemctl enable analytics_module

sudo systemctl start analytics_module
