#!/bin/bash
set -e

TEXT_TYPE=0;
NOTIFY_ALL_TESTERS=2;
AVAILABLE_TO_DOWNLOAD=2;
HOCKEYAPP_API_URL="https://rink.hockeyapp.net/api/2/apps/upload";

token=$2;
commit_id=$(git rev-parse HEAD);
apk_name=$(ls *$1*.apk);

echo "Uploading $apk_name...BEGIN"

curl -F "status=$AVAILABLE_TO_DOWNLOAD" \
     -F "notify=$NOTIFY_ALL_TESTERS" \
     -F "notes_type=$TEXT_TYPE" \
     -F "note=$commit_id" \
     -F "ipa=@$apk_name" \
     -H "X-HockeyAppToken: $token" \
     $HOCKEYAPP_API_URL

echo "Uploading $apk_name...DONE"
