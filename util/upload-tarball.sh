#!/bin/sh

set -e

tag=$(git describe)
version=${tag#v}
jar=target/neurord-${version}-all-deps.jar

echo "Checking that $jar show the correct version:"
java -jar $jar --version | grep "NeuroRD $tag"

echo "Press enter to continue"
read

echo "Signing $jar"
gpg2 --armor --detach-sign $jar

url=https://api.github.com/repos/neurord/stochdiff/releases/tags/$tag
curl $url > target/tag.json
url2=$(jq -r .upload_url < target/tag.json)
url2=$(echo $url2 | sed -r 's/\{.*\}//')

echo "Retrieving github authentication token"
auth=$(git config github.auth)

echo "Uploading $jar to $url2"
curl -u $auth -X POST -H 'Content-Type: application/zip' "$url2?name=neurord-$version-all-deps.jar" --data-binary @$jar | jq

echo "Uploading $jar.sig to $url2"
curl -u $auth -X POST -H 'Content-Type: text/plain' "$url2?name=neurord-$version-all-deps.jar.asc" --data-binary @$jar.asc |jq
