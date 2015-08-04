#! /bin/bash
adb pull /data/data/org.openlmis.core/databases/lmis_db
mv lmis_db lmis_db.sqlite
open -a /Applications/SQLPro\ for\ SQLite\ Read-Only.app lmis_db.sqlite
