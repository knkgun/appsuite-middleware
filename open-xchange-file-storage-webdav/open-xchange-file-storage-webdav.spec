%define __jar_repack %{nil}

Name:           open-xchange-file-storage-webdav
BuildArch:      noarch
BuildRequires:  ant
BuildRequires:  open-xchange-core
BuildRequires:  java-1.8.0-openjdk-devel
Version:        @OXVERSION@
%define         ox_release 18
Release:        %{ox_release}_<CI_CNT>.<B_CNT>
Group:          Applications/Productivity
License:        GPL-2.0
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
URL:            http://www.open-xchange.com/
Source:         %{name}_%{version}.orig.tar.bz2
Summary:        The Open Xchange backend webdav file storage extension
Autoreqprov:    no
Requires:       open-xchange-core >= @OXVERSION@
Provides:       open-xchange-file-storage-webdav = %{version}

%description
Adds webdav file storage services like ownCloud to the backend installation.

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

%changelog
* Tue Feb 02 2021 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Build for patch 2021-02-08 (5945)
* Tue Jan 19 2021 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Build for patch 2021-01-25 (5937)
* Tue Jan 05 2021 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Build for patch 2021-01-11 (5930)
* Wed Dec 09 2020 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Build for patch 2020-12-14 (5924)
* Mon Nov 16 2020 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Build for patch 2020-11-23 (5905)
* Wed Nov 04 2020 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Build for patch 2020-11-09 (5891)
* Tue Oct 20 2020 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Build for patch 2020-10-26 (5888)
* Tue Oct 06 2020 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Build for patch 2020-10-12 (5879)
* Wed Sep 23 2020 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Build for patch 2020-09-29 (5869)
* Fri Sep 11 2020 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Build for patch 2020-09-14 (5857)
* Mon Aug 24 2020 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Build for patch 2020-08-24 (5842)
* Wed Aug 05 2020 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Fifth candidate for 7.10.4 release
* Tue Aug 04 2020 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Fourth candidate for 7.10.4 release
* Tue Aug 04 2020 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Third candidate for 7.10.4 release
* Fri Jul 31 2020 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Second candidate for 7.10.4 release
* Tue Jul 28 2020 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
First candidate for 7.10.4 release
* Tue Jun 30 2020 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
Second preview of 7.10.4 release
* Wed May 20 2020 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
First preview of 7.10.4 release
* Tue Jan 07 2020 Kevin Ruthmann <kevin.ruthmann@open-xchange.com>
first candidate for 7.10.4
