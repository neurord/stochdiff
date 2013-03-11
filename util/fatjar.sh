#!/bin/sh

set -ex

out="$1"
shift

dir=$(mktemp -d /tmp/fatjar.XXXXXX)
for i in "$@"; do
    echo "extracting $i"
    (cd "$dir" && jar -x) < "$i"
done

echo "creating $out"
jar -cf "$out" -C "$dir" .
rm -rf "$dir"
