#!/bin/sh
version=2.1.10
output=${2:-$HOME/rpmbuild/SOURCES/neurord-stochdiff-${version}.tar.gz}
git archive --format=tar.gz --prefix=neurord-stochdiff-${version}/ ${1:-HEAD} >${output}
rpmbuild -bb neurord-stochdiff.spec
