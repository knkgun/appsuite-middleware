
Name:          open-xchange-admin-contextrestore
BuildArch:     noarch
#!BuildIgnore: post-build-checks
BuildRequires: ant
BuildRequires: ant-nodeps
BuildRequires: open-xchange-core
BuildRequires: open-xchange-admin
BuildRequires: java-devel >= 1.6.0
Version:       @OXVERSION@
%define        ox_release 57
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       Extension to restore context data from a database dump
Autoreqprov:   no
Requires:      open-xchange-core >= @OXVERSION@
Requires:      open-xchange-admin >= @OXVERSION@
Provides:      open-xchange-admin-plugin-contextrestore = %{version}
Obsoletes:     open-xchange-admin-plugin-contextrestore < %{version}

%description
This package adds the OSGi bundle that allows to restore a complete context from a MySQL database dump file. Only the table rows for the
given context are extracted from the database dump file and inserted into the currently registered database servers. This can be used to
restore accidentially deleted contexts.

Authors:
--------
    Open-Xchange

%prep

%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
ant -lib build/lib -Dbasedir=build -DdestDir=%{buildroot} -DpackageName=%{name} -f build/build.xml clean build

%post
. /opt/open-xchange/lib/oxfunctions.sh
ox_move_config_file /opt/open-xchange/etc/admindaemon /opt/open-xchange/etc plugin/contextrestore.properties

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root)
%dir /opt/open-xchange/bundles
/opt/open-xchange/bundles/*
%dir /opt/open-xchange/osgi/bundle.d
/opt/open-xchange/osgi/bundle.d/*
%dir /opt/open-xchange/sbin
/opt/open-xchange/sbin/*
%dir /opt/open-xchange/lib
/opt/open-xchange/lib/*
%dir /opt/open-xchange/etc/plugin
%config(noreplace) /opt/open-xchange/etc/plugin/*
%doc com.openexchange.admin.contextrestore/ChangeLog

%changelog
* Tue Jan 12 2021 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2021-01-15 (5932)
* Mon Nov 23 2020 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2020-11-23 (5916)
* Thu Sep 17 2020 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2020-09-22 (5867)
* Wed Jul 01 2020 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2020-07-10 (5794)
* Mon Jun 08 2020 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2020-06-12 (5762)
* Mon May 18 2020 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2020-05-22 (5739)
* Thu Apr 02 2020 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2020-04-07 (5685)
* Tue Mar 03 2020 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2020-03-06 (5637)
* Wed Jan 08 2020 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2020-01-13 (5537)
* Tue Jul 09 2019 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2019-07-12 (5315)
* Tue Jun 18 2019 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2019-07-01 (5288)
* Fri May 03 2019 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2019-05-13 (5231)
* Wed Mar 13 2019 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2019-03-12 (5165)
* Fri Feb 01 2019 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2019-02-11 (5104)
* Mon Nov 12 2018 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2018-11-19 (4895)
* Wed Aug 29 2018 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2018-08-30 (4876)
* Tue Aug 14 2018 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2018-08-20 (4860)
* Thu Aug 02 2018 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2018-08-08 (4856)
* Thu Jun 21 2018 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2018-06-25 (4789)
* Fri May 11 2018 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2018-05-04 (4695)
* Fri Apr 20 2018 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2018-04-23 (4667)
* Tue Jan 30 2018 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2018-02-05 (4552)
* Fri Dec 08 2017 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for Patch 2017-12-11 (4470)
* Thu Nov 16 2017 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2017-11-30 (4438)
* Mon Oct 23 2017 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2017-10-30 (4423)
* Fri Oct 13 2017 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for Patch 2017-10-16 (4391)
* Mon Aug 14 2017 Jan Bauerdick <jan.bauerdick@open-xchange.com>
2017-08-21 (4315)
* Wed Aug 02 2017 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2017-08-01 (4308)
* Mon Jul 03 2017 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2017-07-10 (4254)
* Mon May 08 2017 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2017-05-15 (4133)
* Tue Apr 18 2017 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2017-04-21 (4079)
* Fri Mar 31 2017 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2017-04-03 (4047)
* Fri Feb 24 2017 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2017-02-24 (3991)
* Wed Feb 08 2017 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2017-02-20 (3949)
* Thu Jan 26 2017 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2017-01-26 (3922)
* Thu Jan 19 2017 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2017-01-23 (3875)
* Tue Jan 03 2017 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2017-01-06 (3833)
* Fri Nov 11 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2016-11-21 (3728)
* Fri Nov 04 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2016-11-10 (3712)
* Thu Oct 13 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2016-10-24 (3627)
* Tue Sep 20 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2016-09-26 (3569)
* Thu Sep 01 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2016-09-07 (3527)
* Fri Aug 19 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2016-08-29 (3519)
* Thu Jul 21 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2016-08-01 (3464)
* Thu Jun 30 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2016-07-04 (3358)
* Wed Jun 01 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2016-06-06 (3315)
* Tue May 03 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2016-05-09 (3270)
* Tue Apr 19 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2016-04-25 (3237)
* Mon Mar 21 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2016-03-29 (3187)
* Tue Mar 08 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2016-03-14 (3147)
* Mon Feb 22 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2016-02-29 (3120)
* Wed Feb 03 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2016-02-08 (3072)
* Tue Jan 19 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2016-01-25 (3030)
* Fri Jan 15 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2016-01-14 (3023)
* Thu Jan 07 2016 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2016-01-13 (2972)
* Tue Dec 01 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Second candidate for 7.6.3 release
* Mon Oct 26 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
First candidate for 7.6.3 release
* Tue Oct 20 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2015-10-26 (2813)
* Mon Oct 19 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2015-10-30 (2818)
* Mon Oct 12 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2015-10-23 (2806)
* Wed Sep 30 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2015-10-12 (2784)
* Fri Sep 25 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2015-09-28 (2767)
* Tue Sep 08 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2015-09-14 (2732)
* Wed Sep 02 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2015-09-01 (2726)
* Mon Aug 24 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2015-08-24 (2674)
* Mon Aug 17 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2015-08-12 (2671)
* Thu Aug 06 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2015-08-17 (2666)
* Tue Aug 04 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2015-08-10 (2655)
* Mon Aug 03 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2015-08-03 (2650)
* Thu Jul 23 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2015-07-27 (2626)
* Wed Jul 15 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2015-07-20 (2614)
* Fri Jul 03 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2015-07-10
* Fri Jul 03 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2015-07-02 (2611)
* Fri Jul 03 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2015-06-29 (2578)
* Fri Jul 03 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2015-06-29 (2542)
* Wed Jun 24 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2015-06-29 (2569)
* Wed Jun 24 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2015-06-26 (2573)
* Wed Jun 10 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2015-06-08 (2539)
* Wed Jun 10 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2015-06-08 (2540)
* Mon May 18 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2015-05-26 (2521)
* Fri May 15 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2015-05-15 (2529)
* Fri May 08 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2015-05-12 (2478)
* Thu Apr 30 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2015-05-04 (2496)
* Thu Apr 30 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2015-05-04 (2497)
* Tue Apr 28 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2015-05-04 (2505)
* Fri Apr 24 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2015-09-09 (2495)
* Tue Apr 14 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2015-04-13 (2473)
* Wed Apr 08 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2015-04-13 (2474)
* Tue Apr 07 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-04-09 (2486)
* Thu Mar 26 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2015-03-30 (2459)
* Wed Mar 25 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
prepare for 7.6.3
* Mon Mar 23 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2015-03-20
* Tue Mar 17 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2015-03-18
* Fri Mar 13 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Twelfth candidate for 7.6.2 release
* Fri Mar 06 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2015-03-16
* Fri Mar 06 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Eleventh candidate for 7.6.2 release
* Wed Mar 04 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Tenth candidate for 7.6.2 release
* Tue Mar 03 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Nineth candidate for 7.6.2 release
* Thu Feb 26 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2015-02-23
* Tue Feb 24 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Eighth candidate for 7.6.2 release
* Mon Feb 23 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2015-02-25
* Thu Feb 12 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2015-02-23
* Thu Feb 12 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2015-02-23
* Wed Feb 11 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Seventh candidate for 7.6.2 release
* Fri Feb 06 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2015-02-10
* Fri Feb 06 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2015-02-09
* Fri Jan 30 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Sixth candidate for 7.6.2 release
* Wed Jan 28 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Fifth candidate for 7.6.2 release
* Mon Jan 26 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-10-27
* Mon Jan 26 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2015-01-26
* Wed Jan 21 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2015-01-29
* Mon Jan 12 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2015-01-09
* Wed Jan 07 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2015-01-12
* Mon Jan 05 2015 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2015-01-12
* Tue Dec 30 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2015-01-12
* Tue Dec 16 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-12-10
* Fri Dec 12 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Fourth candidate for 7.6.2 release
* Mon Dec 08 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-12-15
* Mon Dec 08 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-12-10
* Mon Dec 08 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-12-15
* Fri Dec 05 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Third candidate for 7.6.2 release
* Thu Dec 04 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-12-09
* Tue Dec 02 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-12-03
* Tue Nov 25 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-12-01
* Mon Nov 24 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-12-01
* Mon Nov 24 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-12-01
* Fri Nov 21 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Second candidate for 7.6.2 release
* Thu Nov 20 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-12-01
* Wed Nov 19 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-11-21
* Tue Nov 18 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-11-20
* Mon Nov 10 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-11-17
* Mon Nov 10 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-11-17
* Mon Nov 10 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-11-17
* Tue Nov 04 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-11-10
* Fri Oct 31 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
First candidate for 7.6.2 release
* Tue Oct 28 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-11-03
* Mon Oct 27 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-10-30
* Fri Oct 24 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-11-04
* Fri Oct 24 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-11-03
* Fri Oct 24 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-10-22
* Fri Oct 17 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-10-24
* Tue Oct 14 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Fifth candidate for 7.6.1 release
* Fri Oct 10 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-10-20
* Fri Oct 10 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Fourth candidate for 7.6.1 release
* Fri Oct 10 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-10-20
* Thu Oct 09 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-10-13
* Tue Oct 07 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-10-09
* Tue Oct 07 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-10-09
* Tue Oct 07 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-10-10
* Thu Oct 02 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Third release candidate for 7.6.1
* Tue Sep 30 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-10-06
* Fri Sep 26 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-09-29
* Fri Sep 26 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-10-06
* Tue Sep 23 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-10-02
* Thu Sep 18 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-09-23
* Wed Sep 17 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
prepare for 7.6.2 release
* Tue Sep 16 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Second release candidate for 7.6.1
* Mon Sep 08 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-09-15
* Mon Sep 08 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-09-15
* Fri Sep 05 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
First release candidate for 7.6.1
* Thu Aug 21 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-08-25
* Wed Aug 20 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-08-25
* Mon Aug 18 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-08-25
* Wed Aug 13 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-08-15
* Tue Aug 05 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-08-06
* Mon Aug 04 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-08-11
* Mon Aug 04 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-08-11
* Mon Jul 28 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-07-30
* Mon Jul 21 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-07-28
* Tue Jul 15 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-07-21
* Mon Jul 14 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-07-24
* Thu Jul 10 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-07-15
* Mon Jul 07 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-07-14
* Mon Jul 07 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-07-07
* Tue Jul 01 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-07-07
* Thu Jun 26 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
prepare for 7.6.1
* Mon Jun 23 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Seventh candidate for 7.6.0 release
* Fri Jun 20 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Sixth release candidate for 7.6.0
* Wed Jun 18 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-06-30
* Fri Jun 13 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Fifth release candidate for 7.6.0
* Fri Jun 13 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-06-23
* Thu Jun 05 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-06-16
* Fri May 30 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Fourth release candidate for 7.6.0
* Thu May 22 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-05-26
* Fri May 16 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-05-26
* Fri May 16 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Third release candidate for 7.6.0
* Wed May 07 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-05-05
* Mon May 05 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Second release candidate for 7.6.0
* Fri Apr 25 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-04-29
* Tue Apr 15 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-04-22
* Fri Apr 11 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
First release candidate for 7.6.0
* Thu Apr 10 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-04-11
* Thu Apr 03 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-04-07
* Mon Mar 31 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-03-31
* Wed Mar 19 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-03-21
* Mon Mar 17 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-03-24
* Thu Mar 13 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-03-13
* Mon Mar 10 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-03-12
* Fri Mar 07 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-03-07
* Tue Mar 04 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-03-05
* Tue Feb 25 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-03-10
* Tue Feb 25 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-02-26
* Fri Feb 21 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-02-28
* Fri Feb 21 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-02-26
* Tue Feb 18 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-02-20
* Wed Feb 12 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
prepare for 7.6.0
* Fri Feb 07 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Sixth release candidate for 7.4.2
* Thu Feb 06 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Fifth release candidate for 7.4.2
* Thu Feb 06 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-02-11
* Tue Feb 04 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Fourth release candidate for 7.4.2
* Fri Jan 31 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-02-03
* Thu Jan 30 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-02-03
* Wed Jan 29 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-01-30
* Tue Jan 28 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-01-31
* Tue Jan 28 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-01-30
* Tue Jan 28 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-01-30
* Mon Jan 27 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-01-30
* Fri Jan 24 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-12-17
* Thu Jan 23 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Third release candidate for 7.4.2
* Wed Jan 22 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-01-22
* Mon Jan 20 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-01-20
* Thu Jan 16 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-01-16
* Mon Jan 13 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-01-14
* Fri Jan 10 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Second release candidate for 7.4.2
* Fri Jan 10 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-12-17
* Fri Jan 03 2014 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2014-01-06
* Mon Dec 23 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-12-09
* Mon Dec 23 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
First release candidate for 7.4.2
* Thu Dec 19 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-12-23
* Thu Dec 19 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-12-23
* Thu Dec 19 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-12-23
* Wed Dec 18 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
prepare for 7.4.2
* Tue Dec 17 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-12-19
* Tue Dec 17 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-12-18
* Tue Dec 17 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-12-16
* Thu Dec 12 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-12-12
* Thu Dec 12 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-12-12
* Mon Dec 09 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-12-09
* Fri Dec 06 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-11-29
* Fri Dec 06 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-12-10
* Tue Dec 03 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-11-28
* Wed Nov 20 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Fifth candidate for 7.4.1 release
* Tue Nov 19 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Fourth candidate for 7.4.1 release
* Mon Nov 11 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-11-12
* Mon Nov 11 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-11-12
* Fri Nov 08 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-11-11
* Thu Nov 07 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-11-08
* Thu Nov 07 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Third candidate for 7.4.1 release
* Tue Nov 05 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-11-12
* Wed Oct 30 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-10-28
* Thu Oct 24 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-10-30
* Thu Oct 24 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-10-30
* Wed Oct 23 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Second candidate for 7.4.1 release
* Tue Oct 22 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-10-23
* Mon Oct 21 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-10-21
* Thu Oct 17 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-10-21
* Tue Oct 15 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-10-11
* Mon Oct 14 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-10-21
* Mon Oct 14 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-10-15
* Thu Oct 10 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
First sprint increment for 7.4.0 release
* Wed Oct 09 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-10-09
* Wed Oct 09 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-10-07
* Thu Sep 26 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-09-23
* Tue Sep 24 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Eleventh candidate for 7.4.0 release
* Fri Sep 20 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
prepare for 7.4.1 release
* Fri Sep 20 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Tenth candidate for 7.4.0 release
* Thu Sep 12 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Ninth candidate for 7.4.0 release
* Wed Sep 11 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-09-12
* Wed Sep 11 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-09-12
* Thu Sep 05 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-09-05
* Mon Sep 02 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-09-26
* Mon Sep 02 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Eighth candidate for 7.4.0 release
* Fri Aug 30 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-08-30
* Wed Aug 28 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-09-03
* Tue Aug 27 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Seventh candidate for 7.4.0 release
* Fri Aug 23 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Sixth candidate for 7.4.0 release
* Thu Aug 22 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-08-22
* Thu Aug 22 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-08-22
* Tue Aug 20 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-08-19
* Mon Aug 19 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-08-21
* Mon Aug 19 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Fifth release candidate for 7.4.0
* Tue Aug 13 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Fourth release candidate for 7.4.0
* Tue Aug 06 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Third release candidate for 7.4.0
* Mon Aug 05 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-08-09
* Fri Aug 02 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Second release candidate for 7.4.0
* Wed Jul 17 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
First release candidate for 7.4.0
* Tue Jul 16 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
prepare for 7.4.0
* Mon Jul 15 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Second build for patch 2013-07-18
* Mon Jul 15 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-07-18
* Fri Jul 12 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-07-18
* Fri Jul 12 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-07-18
* Thu Jul 11 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-07-10
* Wed Jul 03 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-06-27
* Mon Jul 01 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Third candidate for 7.2.2 release
* Fri Jun 28 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Second candidate for 7.2.2 release
* Wed Jun 26 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Release candidate for 7.2.2 release
* Fri Jun 21 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Second feature freeze for 7.2.2 release
* Mon Jun 17 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Feature freeze for 7.2.2 release
* Tue Jun 11 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-06-13
* Mon Jun 10 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-06-11
* Fri Jun 07 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-06-20
* Mon Jun 03 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
First sprint increment for 7.2.2 release
* Wed May 29 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
First candidate for 7.2.2 release
* Tue May 28 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Second build for patch 2013-05-28
* Mon May 27 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
prepare for 7.2.2
* Thu May 23 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Third candidate for 7.2.1 release
* Wed May 22 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-05-22
* Wed May 22 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-05-22
* Wed May 22 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-05-22
* Wed May 15 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Second candidate for 7.2.1 release
* Wed May 15 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-05-10
* Mon May 13 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-05-09
* Mon May 13 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-05-09
* Mon May 13 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-05-09
* Mon May 13 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-05-09
* Mon May 13 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-05-09
* Fri May 03 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-04-23
* Tue Apr 30 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-04-17
* Mon Apr 22 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
First candidate for 7.2.1 release
* Mon Apr 15 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
prepare for 7.2.1
* Fri Apr 12 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-04-12
* Wed Apr 10 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Fourth candidate for 7.2.0 release
* Tue Apr 09 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Third candidate for 7.2.0 release
* Tue Apr 02 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Second candidate for 7.2.0 release
* Tue Apr 02 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-04-04
* Tue Apr 02 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-04-04
* Tue Apr 02 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-04-04
* Tue Apr 02 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-04-04
* Tue Mar 26 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
First release candidate for 7.2.0
* Fri Mar 15 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
prepare for 7.2.0
* Tue Mar 12 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Sixth release candidate for 6.22.2/7.0.2
* Mon Mar 11 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Fifth release candidate for 6.22.2/7.0.2
* Fri Mar 08 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Fourth release candidate for 6.22.2/7.0.2
* Fri Mar 08 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Third release candidate for 6.22.2/7.0.2
* Thu Mar 07 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Second release candidate for 6.22.2/7.0.2
* Mon Mar 04 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-03-07
* Mon Mar 04 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-03-08
* Fri Mar 01 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-03-07
* Wed Feb 27 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
First release candidate for 6.22.2/7.0.2
* Tue Feb 26 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-02-22
* Mon Feb 25 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-02-22
* Tue Feb 19 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Fourth release candidate for 7.0.1
* Tue Feb 19 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Third release candidate for 7.0.1
* Tue Feb 19 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
prepare for 7.0.2 release
* Fri Feb 15 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-02-13
* Thu Feb 14 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Second release candidate for 7.0.1
* Fri Feb 01 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
First release candidate for 7.0.1
* Tue Jan 29 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-01-28
* Mon Jan 21 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-01-24
* Tue Jan 15 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2013-01-23
* Thu Jan 10 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
prepare for 7.0.1
* Thu Jan 03 2013 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for public patch 2013-01-15
* Fri Dec 28 2012 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for public patch 2012-12-31
* Fri Dec 21 2012 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for public patch 2012-12-21
* Tue Dec 18 2012 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Third release candidate for 7.0.0
* Mon Dec 17 2012 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Second release candidate for 7.0.0
* Wed Dec 12 2012 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for public patch 2012-12-04
* Tue Dec 04 2012 Jan Bauerdick <jan.bauerdick@open-xchange.com>
First release candidate for 7.0.0
* Tue Dec 04 2012 Jan Bauerdick <jan.bauerdick@open-xchange.com>
prepare for 7.0.0 release
* Mon Nov 26 2012 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Build for patch 2012-11-28
* Wed Nov 14 2012 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Sixth release candidate for 6.22.1
* Tue Nov 13 2012 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Fifth release candidate for 6.22.1
* Tue Nov 13 2012 Jan Bauerdick <jan.bauerdick@open-xchange.com>
First release candidate for EDP drop #6
* Tue Nov 06 2012 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Fourth release candidate for 6.22.1
* Fri Nov 02 2012 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Third release candidate for 6.22.1
* Wed Oct 31 2012 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Second release candidate for 6.22.1
* Fri Oct 26 2012 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Third release build for EDP drop #5
* Fri Oct 26 2012 Jan Bauerdick <jan.bauerdick@open-xchange.com>
First release candidate for 6.22.1
* Fri Oct 26 2012 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Second release build for EDP drop #5
* Fri Oct 26 2012 Jan Bauerdick <jan.bauerdick@open-xchange.com>
prepare for 6.22.1
* Thu Oct 11 2012 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Release build for EDP drop #5
* Wed Oct 10 2012 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Fifth release candidate for 6.22.0
* Tue Oct 09 2012 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Fourth release candidate for 6.22.0
* Fri Oct 05 2012 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Third release candidate for 6.22.0
* Thu Oct 04 2012 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Second release candidate for 6.22.0
* Tue Sep 04 2012 Jan Bauerdick <jan.bauerdick@open-xchange.com>
First release candidate for 6.23.0
* Mon Sep 03 2012 Jan Bauerdick <jan.bauerdick@open-xchange.com>
prepare for next EDP drop
* Tue Aug 21 2012 Jan Bauerdick <jan.bauerdick@open-xchange.com>
First release candidate for 6.22.0
* Mon Aug 20 2012 Jan Bauerdick <jan.bauerdick@open-xchange.com>
prepare for 6.22.0
* Fri Jun 15 2012 Jan Bauerdick <jan.bauerdick@open-xchange.com>
Initial packaging
