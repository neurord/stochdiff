#!/bin/bash
set -ex
set -o nounset

version=$1

dir="$HOME/.m2/repository/org/hdfgroup/hdf5/$version"
mkdir -p "$dir"

[ -e "/usr/share/java/jarhdf5.jar" ] && ln -sv "/usr/share/java/jarhdf5.jar" "$dir/hdf5-$version.jar"
[ -e "/usr/lib/java/hdf5.jar" ] && ln -sv "/usr/lib/java/hdf5.jar" "$dir/hdf5-$version.jar"
[ -e "$dir/hdf5-$version.jar" ] || exit 1

cat >"$dir/hdf5-$version.pom" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd"
    xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <modelVersion>4.0.0</modelVersion>
  <groupId>org.hdfgroup</groupId>
  <artifactId>hdf5</artifactId>
  <version>$version</version>
  <description>TBD</description>
</project>
EOF

touch "$dir/hdf5-$version.jar.lastUpdated"
touch "$dir/hdf5-$version.pom.lastUpdated"
