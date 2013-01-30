Summary: Reaction-diffusion simulator
Name: neurord-stochdiff
Version: 2.1.10
Release: 2%{?dist}
License: GPLv2+
Group: Optional
URL: https://github.com/neurord/stochdiff
Source0: %{name}-%{version}.tar.gz
BuildRoot: %{_tmppath}/%{name}-%{version}-%{release}-root

BuildArch:      noarch

BuildRequires:  jpackage-utils

BuildRequires:  java-devel

BuildRequires:  maven

BuildRequires:    maven-compiler-plugin
BuildRequires:    maven-install-plugin
BuildRequires:    maven-jar-plugin
BuildRequires:    maven-javadoc-plugin
BuildRequires:    maven-release-plugin
BuildRequires:    maven-resources-plugin
BuildRequires:    maven-surefire-plugin

Requires:       jpackage-utils
Requires:       java

%description
Simulates reaction-diffusion evolution using stochastic
or deterministic approaches.

%package javadoc
Summary:        Javadocs for %{name}
Group:          Documentation
Requires:       jpackage-utils

%description javadoc
This package contains the API documentation for %{name}.

%prep
%setup -q

%build
mvn-rpmbuild -Dj3ddir=$HOME/neuro/j3d-1_5_2-linux-amd64/lib/ext package javadoc:aggregate

%install

mkdir -p $RPM_BUILD_ROOT%{_javadir}
cp -p target/stochdiff-%{version}.jar $RPM_BUILD_ROOT%{_javadir}/%{name}.jar

mkdir -p $RPM_BUILD_ROOT%{_javadocdir}/%{name}
cp -rp target/site/apidocs/ $RPM_BUILD_ROOT%{_javadocdir}/%{name}

install -d -m 755 $RPM_BUILD_ROOT%{_mavenpomdir}
install -pm 644 pom.xml $RPM_BUILD_ROOT%{_mavenpomdir}/JPP-%{name}.pom

mkdir -p $RPM_BUILD_ROOT%{_bindir}
install -m 0755 util/stochdiff.sh $RPM_BUILD_ROOT%{_bindir}/stochdiff

%add_maven_depmap JPP-%{name}.pom %{name}.jar

%check
mvn-rpmbuild -Dj3ddir=$HOME/neuro/j3d-1_5_2-linux-amd64/lib/ext verify

%files
%{_mavenpomdir}/JPP-%{name}.pom
%{_mavendepmapfragdir}/%{name}
%{_javadir}/%{name}.jar
%{_bindir}/stochdiff
%doc

%files javadoc
%{_javadocdir}/%{name}

%changelog
* Wed Jan 30 2013  <zbyszek@meshugaas.krasnow.gmu.edu> - 2.1.10-2
- New logging setup.

* Wed Jan 30 2013 Zbigniew Jedrzejewski-Szmek <zbyszek@in.waw.pl> - 2.1.10-1
- Initial build.
