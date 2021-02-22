%define __jar_repack %{nil}

Name:          open-xchange-saml-ucs
BuildArch:     noarch
%if 0%{?rhel_version} && 0%{?rhel_version} >= 700
BuildRequires: ant
%else
BuildRequires: ant-nodeps
%endif
%if 0%{?suse_version}
BuildRequires: java-1_8_0-openjdk-devel
%else
BuildRequires: java-1.8.0-openjdk-devel
%endif
BuildRequires: open-xchange-authentication-ucs-common
BuildRequires: open-xchange-saml-core
Version:       @OXVERSION@
%define        ox_release 32
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Module for authenticating users on a Univention Corporate Server installation via SAML
Autoreqprov:   no
Requires:      open-xchange-authentication-ucs-common >= @OXVERSION@
Requires:      open-xchange-saml-core >= @OXVERSION@

%description
This package installs the OSGi bundle implementing the OSGi SamlBackend for the backend. The implementation uses Univention
Corporate Server to authenticate login requests.

Authors:
--------
    Open-Xchange

%prep

%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
ant -lib build/lib -Dbasedir=build -DdestDir=%{buildroot} -DpackageName=%{name} -f build/build.xml clean build

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
/opt/open-xchange/bundles/com.openexchange.saml.ucs.jar
/opt/open-xchange/osgi/bundle.d/com.openexchange.saml.ucs.ini

%changelog
* Mon Feb 01 2021 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2021-02-08 (5944)
* Mon Jan 18 2021 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2021-01-25 (5936)
* Tue Jan 05 2021 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2021-01-11 (5931)
* Wed Dec 09 2020 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2020-12-14 (5923)
* Mon Nov 16 2020 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2020-11-23 (5904)
* Tue Nov 03 2020 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2020-11-09 (5890)
* Tue Oct 20 2020 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2020-10-26 (5887)
* Tue Oct 06 2020 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2020-10-12 (5878)
* Wed Sep 30 2020 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2020-10-09 (5877)
* Tue Sep 22 2020 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2020-09-29 (5868)
* Fri Sep 11 2020 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2020-09-14 (5856)
* Tue Aug 18 2020 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2020-08-24 (5847)
* Tue Aug 04 2020 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2020-08-10 (5833)
* Tue Jul 21 2020 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2020-07-27 (5821)
* Wed Jul 15 2020 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2020-07-17 (5819)
* Thu Jul 09 2020 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2020-07-13 (5804)
* Fri Jun 26 2020 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2020-07-02 (5792)
* Wed Jun 24 2020 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2020-06-30 (5781)
* Mon Jun 15 2020 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2020-06-15 (5765)
* Fri May 15 2020 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2020-05-26 (5742)
* Mon May 04 2020 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2020-05-11 (5720)
* Thu Apr 23 2020 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2020-04-30 (5702)
* Fri Apr 17 2020 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2020-04-02 (5692)
* Mon Apr 06 2020 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2020-04-14 (5677)
* Thu Mar 19 2020 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2020-03-23 (5653)
* Fri Feb 28 2020 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2020-03-02 (5623)
* Wed Feb 12 2020 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2020-02-19 (5588)
* Wed Feb 12 2020 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2020-02-10 (5572)
* Mon Jan 20 2020 Felix Marx <felix.marx@open-xchange.com>
Build for patch 2020-01-20 (5547)
* Thu Nov 28 2019 Felix Marx <felix.marx@open-xchange.com>
Second candidate for 7.10.3 release
* Thu Nov 21 2019 Felix Marx <felix.marx@open-xchange.com>
First candidate for 7.10.3 release
* Thu Oct 17 2019 Felix Marx <felix.marx@open-xchange.com>
First preview for 7.10.3 release
* Mon Jun 17 2019 Felix Marx <felix.marx@open-xchange.com>
prepare for 7.10.3 release
* Fri May 10 2019 Felix Marx <felix.marx@open-xchange.com>
Second candidate for 7.10.2 release
* Fri May 10 2019 Felix Marx <felix.marx@open-xchange.com>
First candidate for 7.10.2 release
* Tue Apr 30 2019 Felix Marx <felix.marx@open-xchange.com>
Second preview for 7.10.2 release
* Thu Mar 28 2019 Felix Marx <felix.marx@open-xchange.com>
First preview for 7.10.2 release
* Thu Oct 18 2018 Felix Marx <felix.marx@open-xchange.com>
prepare for 7.10.2 release
* Mon Oct 08 2018 Felix Marx <felix.marx@open-xchange.com>
prepare for 7.10.1 release
