#!/bin/sh

sudo systemctl stop analytics_module

sudo systemctl disable analytics_module

sudo rm /usr/lib/systemd/system/analytics_module.service

sudo systemctl daemon-reload

