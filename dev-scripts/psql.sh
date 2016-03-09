#!/bin/sh
PGPASSWORD="aipal-adm" psql -h 192.168.50.61 -p 5432 -U aipal_adm -d aipal
