BIN_PATH=$(dirname $(realpath $0))

pg_restore -v --no-owner --username=smartcity --dbname=ANALYTICS_MANAGER2 --password /home/centos/ANALYTICS_MANAGER/db_schema/ANALYTICS_MANAGER.dump

psql -U smartcity -f /home/centos/ANALYTICS_MANAGER/db_schema/AlgorihtmAndPreprocessFunction.sql ANALYTICS_MANAGER2

