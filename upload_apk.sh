#!/bin/bash
set -e

TEXT_TYPE=0;
NOTIFY_ALL_TESTERS=2;
AVAILABLE_TO_DOWNLOAD=2;
HOCKEYAPP_API_URL="https://rink.hockeyapp.net/api/2/apps/upload";

token=$2;
environment=$1;
commit_id=$(git rev-parse HEAD);
apk_path=$(find . -name *$environment*.apk -type f);

echo "Uploading $apk_path...BEGIN";

curl -F "status=$AVAILABLE_TO_DOWNLOAD" \
     -F "notify=$NOTIFY_ALL_TESTERS" \
     -F "notes_type=$TEXT_TYPE" \
     -F "note=$commit_id" \
     -F "ipa=@$apk_path" \
     -H "X-HockeyAppToken: $token" \
     $HOCKEYAPP_API_URL

echo "Uploading $apk_path...DONE";
