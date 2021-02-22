%define __jar_repack %{nil}

Name:          open-xchange-pns-impl
BuildArch:     noarch
%if 0%{?rhel_version} && 0%{?rhel_version} >= 700
BuildRequires: ant
%else
BuildRequires: ant-nodeps
%endif
BuildRequires: open-xchange-core
%if 0%{?suse_version}
BuildRequires: java-1_8_0-openjdk-devel
%else
BuildRequires: java-1.8.0-openjdk-devel
%endif
Version:       @OXVERSION@
%define        ox_release 32
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       The implementation bundle for Push Notification Service
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@

%description
The implementation bundle for Push Notification Service

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
%dir /opt/open-xchange/bundles/
/opt/open-xchange/bundles/*
%dir /opt/open-xchange/osgi/bundle.d/
/opt/open-xchange/osgi/bundle.d/*
%dir /opt/open-xchange/etc/
%config(noreplace) %attr(640,root,open-xchange) /opt/open-xchange/etc/pns-apns_http2-options.yml
%config(noreplace) %attr(640,root,open-xchange) /opt/open-xchange/etc/pns-apns-options.yml
%config(noreplace) %attr(640,root,open-xchange) /opt/open-xchange/etc/pns-gcm-options.yml
%config(noreplace) %attr(640,root,open-xchange) /opt/open-xchange/etc/pns-wns-options.yml
/opt/open-xchange/etc/pns-apns-options-defaults.yml
/opt/open-xchange/etc/pns-apns_http2-options-defaults.yml
/opt/open-xchange/etc/pns-gcm-options-defaults.yml
/opt/open-xchange/etc/pns-wns-options-defaults.yml

%changelog
* Mon Feb 01 2021 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2021-02-08 (5944)
* Mon Jan 18 2021 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2021-01-25 (5936)
* Tue Jan 05 2021 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2021-01-11 (5931)
* Wed Dec 09 2020 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2020-12-14 (5923)
* Mon Nov 16 2020 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2020-11-23 (5904)
* Tue Nov 03 2020 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2020-11-09 (5890)
* Tue Oct 20 2020 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2020-10-26 (5887)
* Tue Oct 06 2020 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2020-10-12 (5878)
* Wed Sep 30 2020 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2020-10-09 (5877)
* Tue Sep 22 2020 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2020-09-29 (5868)
* Fri Sep 11 2020 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2020-09-14 (5856)
* Tue Aug 18 2020 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2020-08-24 (5847)
* Tue Aug 04 2020 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2020-08-10 (5833)
* Tue Jul 21 2020 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2020-07-27 (5821)
* Wed Jul 15 2020 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2020-07-17 (5819)
* Thu Jul 09 2020 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2020-07-13 (5804)
* Fri Jun 26 2020 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2020-07-02 (5792)
* Wed Jun 24 2020 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2020-06-30 (5781)
* Mon Jun 15 2020 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2020-06-15 (5765)
* Fri May 15 2020 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2020-05-26 (5742)
* Mon May 04 2020 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2020-05-11 (5720)
* Thu Apr 23 2020 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2020-04-30 (5702)
* Fri Apr 17 2020 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2020-04-02 (5692)
* Mon Apr 06 2020 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2020-04-14 (5677)
* Thu Mar 19 2020 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2020-03-23 (5653)
* Fri Feb 28 2020 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2020-03-02 (5623)
* Wed Feb 12 2020 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2020-02-19 (5588)
* Wed Feb 12 2020 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2020-02-10 (5572)
* Mon Jan 20 2020 Thorben Betten <thorben.betten@open-xchange.com>
Build for patch 2020-01-20 (5547)
* Thu Nov 28 2019 Thorben Betten <thorben.betten@open-xchange.com>
Second candidate for 7.10.3 release
* Thu Nov 21 2019 Thorben Betten <thorben.betten@open-xchange.com>
First candidate for 7.10.3 release
* Thu Oct 17 2019 Thorben Betten <thorben.betten@open-xchange.com>
First preview for 7.10.3 release
* Mon Jun 17 2019 Thorben Betten <thorben.betten@open-xchange.com>
prepare for 7.10.3 release
* Fri May 10 2019 Thorben Betten <thorben.betten@open-xchange.com>
Second candidate for 7.10.2 release
* Fri May 10 2019 Thorben Betten <thorben.betten@open-xchange.com>
First candidate for 7.10.2 release
* Tue Apr 30 2019 Thorben Betten <thorben.betten@open-xchange.com>
Second preview for 7.10.2 release
* Thu Mar 28 2019 Thorben Betten <thorben.betten@open-xchange.com>
First preview for 7.10.2 release
* Thu Oct 18 2018 Thorben Betten <thorben.betten@open-xchange.com>
prepare for 7.10.2 release
* Thu Oct 11 2018 Thorben Betten <thorben.betten@open-xchange.com>
First candidate for 7.10.1 release
* Thu Sep 06 2018 Thorben Betten <thorben.betten@open-xchange.com>
prepare for 7.10.1 release
* Fri Jun 29 2018 Thorben Betten <thorben.betten@open-xchange.com>
Fourth candidate for 7.10.0 release
* Wed Jun 27 2018 Thorben Betten <thorben.betten@open-xchange.com>
Third candidate for 7.10.0 release
* Mon Jun 25 2018 Thorben Betten <thorben.betten@open-xchange.com>
Second candidate for 7.10.0 release
* Mon Jun 11 2018 Thorben Betten <thorben.betten@open-xchange.com>
First candidate for 7.10.0 release
* Fri May 18 2018 Thorben Betten <thorben.betten@open-xchange.com>
Sixth preview of 7.10.0 release
* Thu Apr 19 2018 Thorben Betten <thorben.betten@open-xchange.com>
Fifth preview of 7.10.0 release
* Tue Apr 03 2018 Thorben Betten <thorben.betten@open-xchange.com>
Fourth preview of 7.10.0 release
* Tue Feb 20 2018 Thorben Betten <thorben.betten@open-xchange.com>
Third preview of 7.10.0 release
* Fri Feb 02 2018 Thorben Betten <thorben.betten@open-xchange.com>
Second preview for 7.10.0 release
* Fri Dec 01 2017 Thorben Betten <thorben.betten@open-xchange.com>
First preview for 7.10.0 release
* Thu Oct 12 2017 Thorben Betten <thorben.betten@open-xchange.com>
prepare for 7.10.0 release
* Fri May 19 2017 Thorben Betten <thorben.betten@open-xchange.com>
First candidate for 7.8.4 release
* Thu May 04 2017 Thorben Betten <thorben.betten@open-xchange.com>
Second preview of 7.8.4 release
* Mon Apr 03 2017 Thorben Betten <thorben.betten@open-xchange.com>
First preview of 7.8.4 release
* Fri Nov 25 2016 Thorben Betten <thorben.betten@open-xchange.com>
Second release candidate for 7.8.3 release
* Thu Nov 24 2016 Thorben Betten <thorben.betten@open-xchange.com>
First release candidate for 7.8.3 release
* Thu Nov 24 2016 Thorben Betten <thorben.betten@open-xchange.com>
prepare for 7.8.4 release
* Tue Nov 15 2016 Thorben Betten <thorben.betten@open-xchange.com>
Third preview for 7.8.3 release
* Sat Oct 29 2016 Thorben Betten <thorben.betten@open-xchange.com>
Second preview for 7.8.3 release
* Fri Oct 14 2016 Thorben Betten <thorben.betten@open-xchange.com>
First preview 7.8.3 release
* Tue Sep 06 2016 Thorben Betten <thorben.betten@open-xchange.com>
prepare for 7.8.3 release
* Mon Aug 15 2016 Thorben Betten <thorben.betten@open-xchange.com>
Initial release
