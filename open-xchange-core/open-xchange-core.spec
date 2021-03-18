%define        configfiles     configfiles.list

Name:          open-xchange-core
BuildArch:     noarch
#!BuildIgnore: post-build-checks
BuildRequires: ant
BuildRequires: ant-nodeps
BuildRequires: open-xchange-osgi
BuildRequires: open-xchange-xerces
BuildRequires: java-devel >= 1.6.0
Version:       @OXVERSION@
%define        ox_release 57
Release:       %{ox_release}_<CI_CNT>.<B_CNT>
Group:         Applications/Productivity
License:       GPL-2.0
BuildRoot:     %{_tmppath}/%{name}-%{version}-build
URL:           http://www.open-xchange.com/
Source:        %{name}_%{version}.orig.tar.bz2
Summary:       The essential core of an Open-Xchange backend
Autoreqprov:   no
Requires:      open-xchange-osgi >= @OXVERSION@
Requires:      open-xchange-xerces
Requires:      open-xchange-system
Provides:      open-xchange-cache = %{version}
Obsoletes:     open-xchange-cache < %{version}
Provides:      open-xchange-calendar = %{version}
Obsoletes:     open-xchange-calendar < %{version}
Provides:      open-xchange-charset = %{version}
Obsoletes:     open-xchange-charset < %{version}
Provides:      open-xchange-common = %{version}
Obsoletes:     open-xchange-common < %{version}
Provides:      open-xchange-config-cascade = %{version}
Obsoletes:     open-xchange-config-cascade < %{version}
Provides:      open-xchange-config-cascade-context = %{version}
Obsoletes:     open-xchange-config-cascade-context < %{version}
Provides:      open-xchange-config-cascade-user = %{version}
Obsoletes:     open-xchange-config-cascade-user < %{version}
Provides:      open-xchange-configread = %{version}
Obsoletes:     open-xchange-configread < %{version}
Provides:      open-xchange-contactcollector = %{version}
Obsoletes:     open-xchange-contactcollector < %{version}
Provides:      open-xchange-control = %{version}
Obsoletes:     open-xchange-control < %{version}
Provides:      open-xchange-conversion = %{version}
Obsoletes:     open-xchange-conversion < %{version}
Provides:      open-xchange-conversion-engine = %{version}
Obsoletes:     open-xchange-conversion-engine < %{version}
Provides:      open-xchange-conversion-servlet = %{version}
Obsoletes:     open-xchange-conversion-servlet < %{version}
Provides:      open-xchange-crypto = %{version}
Obsoletes:     open-xchange-crypto < %{version}
Provides:      open-xchange-data-conversion-ical4j = %{version}
Obsoletes:     open-xchange-data-conversion-ical4j < %{version}
Provides:      open-xchange-dataretention = %{version}
Obsoletes:     open-xchange-dataretention < %{version}
Provides:      open-xchange-genconf = %{version}
Obsoletes:     open-xchange-genconf < %{version}
Provides:      open-xchange-genconf-mysql = %{version}
Obsoletes:     open-xchange-genconf-mysql < %{version}
Provides:      open-xchange-file-storage = %{version}
Obsoletes:     open-xchange-file-storage < %{version}
Provides:      open-xchange-file-storage-composition = %{version}
Obsoletes:     open-xchange-file-storage-composition < %{version}
Provides:      open-xchange-file-storage-config = %{version}
Obsoletes:     open-xchange-file-storage-config < %{version}
Provides:      open-xchange-file-storage-generic = %{version}
Obsoletes:     open-xchange-file-storage-generic < %{version}
Provides:      open-xchange-file-storage-infostore = %{version}
Obsoletes:     open-xchange-file-storage-infostore < %{version}
Provides:      open-xchange-file-storage-json = %{version}
Obsoletes:     open-xchange-file-storage-json < %{version}
Provides:      open-xchange-folder-json = %{version}
Obsoletes:     open-xchange-folder-json < %{version}
Provides:      open-xchange-frontend-uwa = %{version}
Obsoletes:     open-xchange-frontend-uwa < %{version}
Provides:      open-xchange-frontend-uwa-json = %{version}
Obsoletes:     open-xchange-frontend-uwa-json < %{version}
Provides:      open-xchange-global = %{version}
Obsoletes:     open-xchange-global < %{version}
Provides:      open-xchange-html = %{version}
Obsoletes:     open-xchange-html < %{version}
Provides:      open-xchange-i18n = %{version}
Obsoletes:     open-xchange-i18n < %{version}
Provides:      open-xchange-itip-json = %{version}
Obsoletes:     open-xchange-itip-json < %{version}
Provides:      open-xchange-jcharset = %{version}
Obsoletes:     open-xchange-jcharset < %{version}
Provides:      open-xchange-logging = %{version}
Obsoletes:     open-xchange-logging < %{version}
Provides:      open-xchange-management = %{version}
Obsoletes:     open-xchange-management < %{version}
Provides:      open-xchange-modules-json = %{version}
Obsoletes:     open-xchange-modules-json < %{version}
Provides:      open-xchange-modules-model = %{version}
Obsoletes:     open-xchange-modules-model < %{version}
Provides:      open-xchange-modules-storage = %{version}
Obsoletes:     open-xchange-modules-storage < %{version}
Provides:      open-xchange-monitoring = %{version}
Obsoletes:     open-xchange-monitoring < %{version}
Provides:      open-xchange-proxy = %{version}
Obsoletes:     open-xchange-proxy < %{version}
Provides:      open-xchange-proxy-servlet = %{version}
Obsoletes:     open-xchange-proxy-servlet < %{version}
Provides:      open-xchange-publish-basic = %{version}
Obsoletes:     open-xchange-publish-basic < %{version}
Provides:      open-xchange-publish-infostore-online = %{version}
Obsoletes:     open-xchange-publish-infostore-online < %{version}
Provides:      open-xchange-push = %{version}
Obsoletes:     open-xchange-push < %{version}
Provides:      open-xchange-push-udp = %{version}
Obsoletes:     open-xchange-push-udp < %{version}
Provides:      open-xchange-secret = %{version}
Obsoletes:     open-xchange-secret < %{version}
Provides:      open-xchange-secret-recovery = %{version}
Obsoletes:     open-xchange-secret-recovery < %{version}
Provides:      open-xchange-secret-recovery-json = %{version}
Obsoletes:     open-xchange-secret-recovery-json < %{version}
Provides:      open-xchange-secret-recovery-mail = %{version}
Obsoletes:     open-xchange-secret-recovery-mail < %{version}
Provides:      open-xchange-server = %{version}
Obsoletes:     open-xchange-server < %{version}
Provides:      open-xchange-sessiond = %{version}
Obsoletes:     open-xchange-sessiond < %{version}
Provides:      open-xchange-settings-extensions = %{version}
Obsoletes:     open-xchange-settings-extensions < %{version}
Provides:      open-xchange-sql = %{version}
Obsoletes:     open-xchange-sql < %{version}
Provides:      open-xchange-templating = %{version}
Obsoletes:     open-xchange-templating < %{version}
Provides:      open-xchange-templating-base = %{version}
Obsoletes:     open-xchange-templating-base < %{version}
Provides:      open-xchange-threadpool = %{version}
Obsoletes:     open-xchange-threadpool < %{version}
Provides:      open-xchange-tx = %{version}
Obsoletes:     open-xchange-tx < %{version}
Provides:      open-xchange-user-json = %{version}
Obsoletes:     open-xchange-user-json < %{version}
Provides:      open-xchange-xml = %{version}
Obsoletes:     open-xchange-xml < %{version}
Provides:      open-xchange-passwordchange-servlet = %{version}
Obsoletes:     open-xchange-passwordchange-servlet < %{version}
Provides:      open-xchange-file-storage-webdav = %{version}
Obsoletes:     open-xchange-file-storage-webdav < %{version}
Provides:      open-xchange-cluster-discovery-mdns = %{version}
Obsoletes:     open-xchange-cluster-discovery-mdns < %{version}
Provides:      open-xchange-cluster-discovery-static = %{version}
Obsoletes:     open-xchange-cluster-discovery-static < %{version}
Provides:      open-xchange-log4j = %{version}
Obsoletes:     open-xchange-log4j <= %{version}

%description
This package installs all essential bundles that are necessary to get a working backend installation. This are the bundles for the main
modules of Open-Xchange: Mail, Calendar, Contacts, Tasks and InfoStore. Additionally the following functionalities are installed with this
package:
* the main caching system using the Java Caching System (JCS)
* the config cascade allowing administrators to selectively override configuration parameters on context and user level
* the contact collector storing every contact of read or written emails in a special collected contacts folder
* the conversion engine converting vCard or iCal email attachments to contacts or appointments
* the import and export module to import or export complete contact or appointment folders
* the iMIP implementation to handle invitations with participants through emails
* auto configuration for external email accounts
* encrypted storing of passwords for integrated social accounts
* and a lot more

Authors:
--------
    Open-Xchange

%prep

%setup -q

%build

%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
ant -lib build/lib -Dbasedir=build -DdestDir=%{buildroot} -DpackageName=%{name} -f build/build.xml clean build
mkdir -p %{buildroot}/var/log/open-xchange
mkdir -p %{buildroot}/var/spool/open-xchange/uploads
rm -f %{configfiles}
find %{buildroot}/opt/open-xchange/etc \
     %{buildroot}/opt/open-xchange/importCSV \
        -type f \
        -printf "%%%config(noreplace) %p\n" > %{configfiles}
perl -pi -e 's;%{buildroot};;' %{configfiles}
perl -pi -e 's;^(.*?)\s+(.*/paths.perfMap)$;$2;' %{configfiles}
perl -pi -e 's;(^.*?)\s+(.*/(mail|configdb|server|filestorage|management|oauth-provider|secret|sessiond)\.properties)$;$1 %%%attr(640,root,open-xchange) $2;' %{configfiles}
perl -pi -e 's;(^.*?)\s+(.*/(secrets|tokenlogin-secrets))$;$1 %%%attr(640,root,open-xchange) $2;' %{configfiles}

%pre
if [ ${1:-0} -eq 2 ]; then
    # only when updating
    # prevent bash from expanding, see bug 13316
    GLOBIGNORE='*'

    . /opt/open-xchange/lib/oxfunctions.sh

    # SoftwareChange_Request-1564
    VALUE="empty"
    if [ -e /opt/open-xchange/bundles/com.openexchange.cluster.discovery.mdns.jar ]; then
        VALUE="multicast"
    elif [ -e /opt/open-xchange/bundles/com.openexchange.cluster.discovery.static.jar ]; then
        VALUE="static"
    fi
    pfile=/opt/open-xchange/etc/hazelcast.properties
    if [ -e $pfile ] && ! ox_exists_property com.openexchange.hazelcast.network.join $pfile; then
        ox_set_property com.openexchange.hazelcast.network.join "$VALUE" $pfile
    fi
fi

%post
. /opt/open-xchange/lib/oxfunctions.sh
ox_move_config_file /opt/open-xchange/etc/common /opt/open-xchange/etc i18n.properties
ox_move_config_file /opt/open-xchange/etc/groupware /opt/open-xchange/etc push.properties push-udp.properties
CONFFILES="management.properties templating.properties mail-push.properties filestorage.properties folderjson.properties messaging.properties publications.properties secret.properties secrets threadpool.properties settings/themes.properties settings/ui.properties meta/ui.yml attachment.properties cache.ccf calendar.properties configdb.properties contact.properties event.properties file-logging.properties HTMLEntities.properties importerExporter.xml import.properties infostore.properties javamail.properties ldap.properties login.properties mailcache.ccf mail.properties mime.types noipcheck.cnf notification.properties ox-scriptconf.sh participant.properties passwordchange.properties server.properties sessioncache.ccf sessiond.properties smtp.properties system.properties user.properties whitelist.properties folder-reserved-names"
for FILE in $CONFFILES; do
    ox_move_config_file /opt/open-xchange/etc/groupware /opt/open-xchange/etc $FILE
done
COCONFFILES="excludedupdatetasks.properties foldercache.properties transport.properties"
for FILE in ${COCONFFILES}; do
    ox_move_config_file /opt/open-xchange/etc/common /opt/open-xchange/etc $FILE
done

# SoftwareChange_Request-1297
rm -f /opt/open-xchange/etc/sessioncache.ccf

# SoftwareChange_Request-1094
rm -f /opt/open-xchange/etc/groupware/mailjsoncache.properties
# SoftwareChange_Request-1091
rm -f /opt/open-xchange/etc/groupware/TidyConfiguration.properties
rm -f /opt/open-xchange/etc/groupware/TidyMessages.properties

pfile=/opt/open-xchange/etc/ox-scriptconf.sh
if grep COMMONPROPERTIESDIR $pfile >/dev/null; then
    ox_remove_property COMMONPROPERTIESDIR $pfile
    # without original values, we're lost...
    if [ -e ${pfile}.rpmnew ]; then
       CHECKPROPS="LIBPATH PROPERTIESDIR LOGGINGPROPERTIES OSGIPATH"
       grep JAVA_OXCMD_OPTS $pfile > /dev/null || CHECKPROPS="$CHECKPROPS JAVA_OXCMD_OPTS" && true
       for prop in $CHECKPROPS; do
           oval=$(ox_read_property $prop ${pfile}.rpmnew)
           if [ -n "$oval" ]; then
               ox_set_property $prop "$oval" $pfile
           fi
       done
    fi
fi

# SoftwareChange_Request-1559
pfile=/opt/open-xchange/etc/mail.properties
VALUE=$(ox_read_property com.openexchange.mail.mailAccessCacheIdleSeconds $pfile)
if [ "$VALUE" == "7" ]; then
    ox_set_property com.openexchange.mail.mailAccessCacheIdleSeconds 4 $pfile
fi

# SoftwareChange_Request-1557
pfile=/opt/open-xchange/etc/mail.properties
if ! ox_exists_property com.openexchange.mail.maxForwardCount $pfile; then
    ox_set_property com.openexchange.mail.maxForwardCount 8 $pfile
fi

# SoftwareChange_Request-1518
pfile=/opt/open-xchange/etc/mail.properties
if ! ox_exists_property com.openexchange.mail.hideDetailsForDefaultAccount $pfile; then
    ox_set_property com.openexchange.mail.hideDetailsForDefaultAccount false $pfile
fi

# SoftwareChange_Request-1497
pfile=/opt/open-xchange/etc/hazelcast.properties
if ! ox_exists_property com.openexchange.hazelcast.logging.enabled $pfile; then
    ox_set_property com.openexchange.hazelcast.logging.enabled true $pfile
fi

# SoftwareChange_Request-1492
pfile=/opt/open-xchange/etc/server.properties
for key in com.openexchange.json.poolEnabled com.openexchange.json.poolSize com.openexchange.json.poolCharArrayLength; do
    if ox_exists_property $key $pfile; then
       ox_remove_property $key $pfile
    fi
done

# SoftwareChange_Request-1483
pfile=/opt/open-xchange/etc/server.properties
if ! ox_exists_property com.openexchange.servlet.maxRateTimeWindow $pfile; then
    ox_set_property com.openexchange.servlet.maxRateTimeWindow 300000 $pfile
fi
if ! ox_exists_property com.openexchange.servlet.maxRate $pfile; then
    ox_set_property com.openexchange.servlet.maxRate 1500 $pfile
fi
if ! ox_exists_property com.openexchange.servlet.maxRateLenientClients $pfile; then
    ox_set_property com.openexchange.servlet.maxRateLenientClients '"Open-Xchange .NET HTTP Client*", "Open-Xchange USM HTTP Client*", "Jakarta Commons-HttpClient*"' $pfile
fi
if ! ox_exists_property com.openexchange.servlet.maxRateKeyPartProviders $pfile; then
    ox_set_property com.openexchange.servlet.maxRateKeyPartProviders '' $pfile
fi

# SoftwareChange_Request-1459
pfile=/opt/open-xchange/etc/mail.properties
if ! ox_exists_property com.openexchange.mail.supportMsisdnAddresses $pfile; then
    ox_set_property com.openexchange.mail.supportMsisdnAddresses false $pfile
fi

# SoftwareChange_Request-1458
pfile=/opt/open-xchange/etc/mail.properties
if ! ox_exists_property com.openexchange.mail.maxMailSize $pfile; then
    ox_set_property com.openexchange.mail.maxMailSize -1 $pfile
fi

# SoftwareChange_Request-1455
pfile=/opt/open-xchange/etc/sessiond.properties
if ! ox_exists_property com.openexchange.sessiond.asyncPutToSessionStorage $pfile; then
    ox_set_property com.openexchange.sessiond.asyncPutToSessionStorage false $pfile
fi

# SoftwareChange_Request-1448
ox_set_property com.openexchange.push.udp.pushEnabled false /opt/open-xchange/etc/push-udp.properties
ox_set_property com.openexchange.push.udp.registerDistributionEnabled false /opt/open-xchange/etc/push-udp.properties
ox_set_property com.openexchange.push.udp.eventDistributionEnabled false /opt/open-xchange/etc/push-udp.properties
ox_set_property com.openexchange.push.udp.multicastEnabled false /opt/open-xchange/etc/push-udp.properties

# SoftwareChange_Request-1446
pfile=/opt/open-xchange/etc/server.properties
VALUE=$(ox_read_property MAX_UPLOAD_SIZE $pfile)
if [ "$VALUE" == "0" ]; then
    ox_set_property MAX_UPLOAD_SIZE 104857600 $pfile
fi
VALUE=$(ox_read_property com.openexchange.defaultMaxConcurrentAJAXRequests $pfile)
if [ "$VALUE" == "250" ]; then
    ox_set_property com.openexchange.defaultMaxConcurrentAJAXRequests 100 $pfile
fi
VALUE=$(ox_read_property com.openexchange.servlet.maxActiveSessions $pfile)
if [ "$VALUE" == "-1" ]; then
    ox_set_property com.openexchange.servlet.maxActiveSessions 250000 $pfile
fi
pfile=/opt/open-xchange/etc/sessiond.properties
VALUE=$(ox_read_property com.openexchange.sessiond.maxSession $pfile)
if [ "$VALUE" == "5000" ]; then
    ox_set_property com.openexchange.sessiond.maxSession 50000 $pfile
fi
VALUE=$(ox_read_property com.openexchange.sessiond.randomTokenTimeout $pfile)
if [ "$VALUE" == "1M" ]; then
    ox_set_property com.openexchange.sessiond.randomTokenTimeout 30000 $pfile
fi

# SoftwareChange_Request-1445
pfile=/opt/open-xchange/etc/hazelcast.properties
if ! ox_exists_property com.openexchange.hazelcast.maxOperationTimeout $pfile; then
    ox_set_property com.openexchange.hazelcast.maxOperationTimeout 300000 $pfile
fi

# SoftwareChange_Request-1426
pfile=/opt/open-xchange/etc/server.properties
if ! ox_exists_property com.openexchange.log.maxMessageLength $pfile; then
    ox_set_property com.openexchange.log.maxMessageLength -1 $pfile
fi

# SoftwareChange_Request-1365
pfile=/opt/open-xchange/etc/configdb.properties
if ! ox_exists_property com.openexchange.database.replicationMonitor $pfile; then
    ox_set_property com.openexchange.database.replicationMonitor true $pfile
fi

# SoftwareChange_Request-1389
# -----------------------------------------------------------------------
pfile=/opt/open-xchange/etc/foldercache.properties
if ! ox_exists_property com.openexchange.folderstorage.database.preferDisplayName $pfile; then
    ox_set_property com.openexchange.folderstorage.database.preferDisplayName false $pfile
fi

# SoftwareChange_Request-1335
pfile=/opt/open-xchange/etc/paths.perfMap
if ! grep "modules/mail/defaultaddress" $pfile > /dev/null; then
   ptmp=${pfile}.$$
   cp $pfile $ptmp
   cat<<EOF >> $ptmp
modules/mail/defaultaddress > io.ox/mail//defaultaddress
modules/mail/sendaddress > io.ox/mail//sendaddress
EOF
   if [ -s $ptmp ]; then
      cp $ptmp $pfile
   fi
   rm -f $ptmp
fi

# SoftwareChange_Request-1330
pfile=/opt/open-xchange/etc/mime.types
if ! grep docm $pfile > /dev/null; then
   ptmp=${pfile}.$$
   cp $pfile $ptmp
   cat<<EOF >> $ptmp
application/vnd.ms-word.document.macroEnabled.12 docm
application/vnd.ms-word.template dotm
application/vnd.openxmlformats-officedocument.wordprocessingml.template dotx
application/vnd.ms-powerpoint.presentation.macroEnabled.12 potm
application/vnd.openxmlformats-officedocument.presentationml.template potx
application/vnd.ms-excel.sheet.binary.macroEnabled.12 xlsb
application/vnd.ms-excel.sheet.macroEnabled.12 xlsm
application/vnd.openxmlformats-officedocument.spreadsheetml.sheet xlsx
EOF
   if [ -s $ptmp ]; then
      cp $ptmp $pfile
   fi
   rm -f $ptmp
fi

# SoftwareChange_Request-1324
# -----------------------------------------------------------------------
pfile=/opt/open-xchange/etc/hazelcast.properties
if ! ox_exists_property com.openexchange.hazelcast.enableIPv6Support $pfile; then
    ox_set_property com.openexchange.hazelcast.enableIPv6Support false $pfile
fi

# SoftwareChange_Request-1308
# -----------------------------------------------------------------------
pfile=/opt/open-xchange/etc/hazelcast.properties
if ! ox_exists_property com.openexchange.hazelcast.networkConfig.port $pfile; then
    ox_set_property com.openexchange.hazelcast.networkConfig.port 5701 $pfile
fi
if ! ox_exists_property com.openexchange.hazelcast.networkConfig.portAutoIncrement $pfile; then
    ox_set_property com.openexchange.hazelcast.networkConfig.portAutoIncrement true $pfile
fi
if ! ox_exists_property com.openexchange.hazelcast.networkConfig.outboundPortDefinitions $pfile; then
    ox_set_property com.openexchange.hazelcast.networkConfig.outboundPortDefinitions '' $pfile
fi

# SoftwareChange_Request-1307
pfile=/opt/open-xchange/etc/server.properties
if ! ox_exists_property com.openexchange.server.considerXForwards $pfile; then
    ox_set_property com.openexchange.server.considerXForwards false $pfile
fi
if ! ox_exists_property com.openexchange.server.forHeader $pfile; then
    ox_set_property com.openexchange.server.forHeader X-Forwarded-For $pfile
fi
if ! ox_exists_property com.openexchange.server.knownProxies $pfile; then
    ox_set_property com.openexchange.server.knownProxies '' $pfile
fi

# SoftwareChange_Request-1296
# -----------------------------------------------------------------------
pfile=/opt/open-xchange/etc/cache.properties
if ! ox_exists_property com.openexchange.caching.jcs.eventInvalidation $pfile; then
    ox_set_property com.openexchange.caching.jcs.eventInvalidation true $pfile
fi

# SoftwareChange_Request-1302
# -----------------------------------------------------------------------
pfile=/opt/open-xchange/etc/user.properties
if ! ox_exists_property com.openexchange.user.maxClientCount $pfile; then
    ox_set_property com.openexchange.user.maxClientCount -1 $pfile
fi

# SoftwareChange_Request-1252
# -----------------------------------------------------------------------
pfile=/opt/open-xchange/etc/whitelist.properties
if ! grep -E '^html.tag.div.*bgcolor' $pfile > /dev/null; then
    oval=$(ox_read_property html.tag.div ${pfile})
    oval=${oval//\"/}
    ox_set_property html.tag.div \""${oval}bgcolor,"\" $pfile
fi

# SoftwareChange_Request-1247
# -----------------------------------------------------------------------
pfile=/opt/open-xchange/etc/hazelcast.properties
if ! ox_exists_property com.openexchange.hazelcast.enabled $pfile; then
    ox_set_property com.openexchange.hazelcast.enabled true $pfile
fi

# SoftwareChange_Request-1223
# SoftwareChange_Request-1237
# SoftwareChange_Request-1243
# SoftwareChange_Request-1245
# SoftwareChange_Request-1392
# SoftwareChange_Request-1468
# SoftwareChange_Request-1498
# -----------------------------------------------------------------------
pfile=/opt/open-xchange/etc/ox-scriptconf.sh
jopts=$(eval ox_read_property JAVA_XTRAOPTS $pfile)
jopts=${jopts//\"/}
nopts=$jopts
# -----------------------------------------------------------------------
permval=$(echo $nopts | sed 's;^.*MaxPermSize=\([0-9]*\).*$;\1;')
if [ $permval -lt 256 ]; then
    nopts=$(echo $nopts | sed "s;\(^.*MaxPermSize=\)[0-9]*\(.*$\);\1256\2;")
fi
# -----------------------------------------------------------------------
for opt in "-XX:+DisableExplicitGC" "-server" "-Djava.awt.headless=true" \
    "-XX:+UseConcMarkSweepGC" "-XX:+UseParNewGC" "-XX:CMSInitiatingOccupancyFraction=" \
    "-XX:+UseCMSInitiatingOccupancyOnly" "-XX:NewRatio=" "-XX:+UseTLAB" \
    "-XX:-OmitStackTraceInFastThrow"; do
    if ! echo $nopts | grep -- $opt > /dev/null; then
        if [ "$opt" = "-XX:CMSInitiatingOccupancyFraction=" ]; then
            opt="-XX:CMSInitiatingOccupancyFraction=75"
        elif [ "$opt" = "-XX:NewRatio=" ]; then
            opt="-XX:NewRatio=3"
        fi
        if [ "$opt" == "-XX:+UseConcMarkSweepGC" -o "$opt" == "-XX:+UseParNewGC" -o "$opt" == "-XX:CMSInitiatingOccupancyFraction=75" -o "$opt" == "-XX:+UseCMSInitiatingOccupancyOnly" ]; then
            if ! echo $nopts | grep -- "-XX:+UseParallelGC" > /dev/null && ! echo $nopts | grep -- "-XX:+UseParallelOldGC" > /dev/null; then
                nopts="$nopts $opt"
            fi
        else
            nopts="$nopts $opt"
        fi
    fi
done
# -----------------------------------------------------------------------
for opt in "-XX:+UnlockExperimentalVMOptions" "-XX:+UseG1GC" "-XX:+CMSClassUnloadingEnabled"; do
    if echo $nopts | grep -- $opt > /dev/null; then
        nopts=$(echo $nopts | sed "s;$opt;;")
    fi
done
if [ "$jopts" != "$nopts" ]; then
   ox_set_property JAVA_XTRAOPTS \""$nopts"\" $pfile
fi

# SoftwareChange_Request-1141
pfile=/opt/open-xchange/etc/mime.types
if ! grep font-woff $pfile > /dev/null; then
   ptmp=${pfile}.$$
   cp $pfile $ptmp
   cat<<EOF >> $ptmp
application/font-woff woff
text/cache-manifest appcache
text/javascript js
EOF
   if [ -s $ptmp ]; then
      cp $ptmp $pfile
   fi
   rm -f $ptmp
fi
if grep -E "application\/javascript.*js" $pfile > /dev/null; then
   ptmp=${pfile}.$$
   grep -vE "^application\/.*javascript" $pfile > $ptmp
   cat<<EOF >> $ptmp
application/javascript
application/x-javascript
EOF
   if [ -s $ptmp ]; then
      cp $ptmp $pfile
   fi
   rm -f $ptmp
fi

# SoftwareChange_Request-1212
pfile=/opt/open-xchange/etc/foldercache.properties
if ! ox_exists_property com.openexchange.folderstorage.outlook.showPersonalBelowInfoStore $pfile; then
    ox_set_property com.openexchange.folderstorage.outlook.showPersonalBelowInfoStore true $pfile
fi

# SoftwareChange_Request-1196
pfile=/opt/open-xchange/etc/import.properties
if ! ox_exists_property com.openexchange.import.ical.limit $pfile; then
    ox_set_property com.openexchange.import.ical.limit 10000 $pfile
fi

# SoftwareChange_Request-1220
# obsoletes SoftwareChange_Request-1068
# -----------------------------------------------------------------------
pfile=/opt/open-xchange/etc/ox-scriptconf.sh
jopts=$(eval ox_read_property JAVA_XTRAOPTS $pfile)
jopts=${jopts//\"/}
if echo $jopts | grep "osgi.compatibility.bootdelegation" > /dev/null; then
    jopts=$(echo $jopts | sed 's;-Dosgi.compatibility.bootdelegation=true;-Dosgi.compatibility.bootdelegation=false;')
    ox_set_property JAVA_XTRAOPTS \""$jopts"\" $pfile
fi

# SoftwareChange_Request-1135
pfile=/opt/open-xchange/etc/contact.properties
for key in scale_images scale_image_width scale_image_height; do
    if ox_exists_property $key $pfile; then
       ox_remove_property $key $pfile
    fi
done

# SoftwareChange_Request-1124
pfile=/opt/open-xchange/etc/server.properties
if ! ox_exists_property com.openexchange.ajax.response.includeStackTraceOnError $pfile; then
    ox_set_property com.openexchange.ajax.response.includeStackTraceOnError false $pfile
fi

# SoftwareChange_Request-1117
pfile=/opt/open-xchange/etc/server.properties
if ! ox_exists_property com.openexchange.webdav.disabled $pfile; then
    ox_set_property com.openexchange.webdav.disabled false $pfile
fi

# SoftwareChange_Request-1105
pfile=/opt/open-xchange/etc/cache.ccf
ptmp=${pfile}.$$
if grep -E "^jcs.region.OXIMAPConCache" $pfile > /dev/null; then
    grep -vE "^jcs.region.OXIMAPConCache" $pfile > $ptmp
    if [ -s $ptmp ]; then
        cp $ptmp $pfile
    fi
    rm -f $ptmp
fi
# SoftwareChange_Request-1028
pfile=/opt/open-xchange/etc/contact.properties
if ! ox_exists_property com.openexchange.carddav.tree $pfile; then
    ox_set_property com.openexchange.carddav.tree "0" $pfile
fi
if ! ox_exists_property com.openexchange.carddav.combinedRequestTimeout $pfile; then
    ox_set_property com.openexchange.carddav.combinedRequestTimeout "20000" $pfile
fi
if ! ox_exists_property com.openexchange.carddav.exposedCollections $pfile; then
    ox_set_property com.openexchange.carddav.exposedCollections "0" $pfile
fi
# SoftwareChange_Request-1091
pfile=/opt/open-xchange/etc/contact.properties
if ox_exists_property contactldap.configuration.path $pfile; then
    ox_remove_property contactldap.configuration.path $pfile
fi

# SoftwareChange_Request-1101
pfile=/opt/open-xchange/etc/configdb.properties
if ox_exists_property writeOnly $pfile; then
    ox_remove_property writeOnly $pfile
fi
# SoftwareChange_Request-1091
if ox_exists_property useSeparateWrite $pfile; then
    ox_remove_property useSeparateWrite $pfile
fi

# SoftwareChange_Request-1091
pfile=/opt/open-xchange/etc/system.properties
for prop in Calendar Infostore Attachment Notification ServletMappingDir CONFIGPATH \
    AJPPROPERTIES IMPORTEREXPORTER LDAPPROPERTIES EVENTPROPERTIES PUSHPROPERTIES \
    UPDATETASKSCFG HTMLEntities MailCacheConfig TidyMessages TidyConfiguration Whitelist; do
    if ox_exists_property $prop $pfile; then
       ox_remove_property $prop $pfile
    fi
done
if grep -E '^com.openexchange.caching.configfile.*/' $pfile >/dev/null; then
    ox_set_property com.openexchange.caching.configfile cache.ccf $pfile
fi
if ox_exists_property MimeTypeFile $pfile; then
    ox_set_property MimeTypeFileName mime.types $pfile
    ox_remove_property MimeTypeFile $pfile
fi
pfile=/opt/open-xchange/etc/import.properties
if ! ox_exists_property com.openexchange.import.mapper.path $pfile; then
    ox_set_property com.openexchange.import.mapper.path /opt/open-xchange/importCSV $pfile
fi
pfile=/opt/open-xchange/etc/mail.properties
if ! ox_exists_property com.openexchange.mail.JavaMailProperties $pfile || grep -E '^com.openexchange.mail.JavaMailProperties.*/' $pfile >/dev/null; then
    ox_set_property com.openexchange.mail.JavaMailProperties javamail.properties $pfile
fi
pfile=/opt/open-xchange/etc/sessiond.properties
if ox_exists_property com.openexchange.sessiond.sessionCacheConfig $pfile; then
    ox_remove_property com.openexchange.sessiond.sessionCacheConfig $pfile
fi

# SoftwareChange_Request-1024
pfile=/opt/open-xchange/etc/server.properties
if ! ox_exists_property com.openexchange.IPMaskV4 $pfile; then
    ox_set_property com.openexchange.IPMaskV4 "" $pfile
fi
if ! ox_exists_property com.openexchange.IPMaskV6 $pfile; then
    ox_set_property com.openexchange.IPMaskV6 "" $pfile
fi

# SoftwareChange_Request-1027
pfile=/opt/open-xchange/etc/server.properties
if ! ox_exists_property com.openexchange.dispatcher.prefix $pfile; then
    ox_set_property com.openexchange.dispatcher.prefix "/ajax/" $pfile
fi


# SoftwareChange_Request-1167
pfile=/opt/open-xchange/etc/contact.properties
if ! ox_exists_property "com.openexchange.contact.scaleVCardImages" $pfile; then
   ox_set_property "com.openexchange.contact.scaleVCardImages" "200x200" $pfile
fi

# SoftwareChange_Request-1148
pfile=/opt/open-xchange/etc/whitelist.properties
if ! ox_exists_property "html.style.word-break" $pfile; then
   ox_set_property "html.style.word-break" '"break-all"' $pfile
fi
if ! ox_exists_property "html.style.word-wrap" $pfile; then
   ox_set_property "html.style.word-wrap" '"break-word"' $pfile
fi

# SoftwareChange_Request-1125
pfile=/opt/open-xchange/etc/contactcollector.properties
if ! ox_exists_property com.openexchange.contactcollector.folder.deleteDenied $pfile; then
   ox_set_property com.openexchange.contactcollector.folder.deleteDenied false $pfile
fi

# SoftwareChange_Request-1540
pfile=/opt/open-xchange/etc/permissions.properties
if ! grep "com.openexchange.capability.boring" >/dev/null $pfile; then
    echo -e "\n# Mark this installation as boring, i.e. disable an easter egg\n" >> $pfile
    echo "# com.openexchange.capability.boring=true" >> $pfile
fi

# SoftwareChange_Request-1556
pfile=/opt/open-xchange/etc/excludedupdatetasks.properties
if ! grep "com.openexchange.groupware.tasks.database.TasksModifyCostColumnTask" >/dev/null $pfile; then
    echo -e "\n# v7.4.0 update tasks start here\n" >> $pfile
    echo "# Changes the columns actual_costs and target_costs for tasks from float to NUMERIC(12, 2)" >> $pfile
    echo "!com.openexchange.groupware.tasks.database.TasksModifyCostColumnTask" >> $pfile
fi

# SoftwareChange_Request-1558
pfile=/opt/open-xchange/etc/import.properties
if ! grep "com.openexchange.import.contacts.limit" >/dev/null $pfile; then
    echo -e "\n# sets the limit on how many contacts can be imported at once\n" >> $pfile
    echo "# -1 means unlimited, defaults to -1" >> $pfile
    echo "# com.openexchange.import.contacts.limit=-1" >> $pfile
fi

# SoftwareChange_Request-1564
VALUE=""
[ -e /opt/open-xchange/etc/cluster.properties ] && VALUE=$(ox_read_property com.openexchange.cluster.name /opt/open-xchange/etc/cluster.properties)
TOVALUE=$(ox_read_property com.openexchange.hazelcast.group.name /opt/open-xchange/etc/hazelcast.properties)
if [ -n "$VALUE" -a -z "$TOVALUE" ]; then
    ox_set_property com.openexchange.hazelcast.group.name "$VALUE" /opt/open-xchange/etc/hazelcast.properties
fi
rm -f /opt/open-xchange/etc/cluster.properties
[ -e /opt/open-xchange/etc/static-cluster-discovery.properties ] && VALUE=$(ox_read_property com.openexchange.cluster.discovery.static.nodes /opt/open-xchange/etc/static-cluster-discovery.properties)
TOVALUE=$(ox_read_property com.openexchange.hazelcast.network.join.static.nodes /opt/open-xchange/etc/hazelcast.properties)
if [ -n "$VALUE" -a -z "$TOVALUE" ]; then
    ox_set_property com.openexchange.hazelcast.network.join.static.nodes "$VALUE" /opt/open-xchange/etc/hazelcast.properties
fi
pfile=/opt/open-xchange/etc/hazelcast.properties
OLDNAMES=( com.openexchange.hazelcast.interfaces com.openexchange.hazelcast.mergeFirstRunDelay com.openexchange.hazelcast.mergeRunDelay com.openexchange.hazelcast.networkConfig.port com.openexchange.hazelcast.networkConfig.portAutoIncrement com.openexchange.hazelcast.networkConfig.outboundPortDefinitions com.openexchange.hazelcast.enableIPv6Support )
NEWNAMES=( com.openexchange.hazelcast.network.interfaces com.openexchange.hazelcast.merge.firstRunDelay com.openexchange.hazelcast.merge.runDelay com.openexchange.hazelcast.network.port com.openexchange.hazelcast.network.portAutoIncrement com.openexchange.hazelcast.network.outboundPortDefinitions com.openexchange.hazelcast.network.enableIPv6Support )
DEFAULTS=( 127.0.0.1 120s 120s 5701 true "" false )
for I in $(seq 1 ${#OLDNAMES[@]}); do
    OLDNAME=${OLDNAMES[$I-1]}
    NEWNAME=${NEWNAMES[$I-1]}
    VALUE=$(ox_read_property $OLDNAME $pfile)
    if ox_exists_property $OLDNAME $pfile; then
        ox_remove_property $OLDNAME $pfile
    fi
    if [ -z "$VALUE" ]; then
        VALUE="${DEFAULTS[$I-1]}"
    fi
    if ! ox_exists_property $NEWNAME $pfile; then
        ox_set_property $NEWNAME "$VALUE" $pfile
    fi
done
NEWPROPS=( com.openexchange.hazelcast.jmxDetailed com.openexchange.hazelcast.network.join.multicast.group com.openexchange.hazelcast.network.join.multicast.port com.openexchange.hazelcast.group.password com.openexchange.hazelcast.memcache.enabled com.openexchange.hazelcast.rest.enabled com.openexchange.hazelcast.socket.bindAny )
DEFAULTS=( false 224.2.2.3 54327 'wtV6$VQk8#+3ds!a' false false false )
for I in $(seq 1 ${#NEWPROPS[@]}); do
    NEWPROP=${NEWPROPS[$I-1]}
    DEFAULT=${DEFAULTS[$I-1]}
    if ! ox_exists_property $NEWPROP $pfile; then
        ox_set_property $NEWPROP "$DEFAULT" $pfile
    fi
done

# SoftwareChange_Request-1601
ox_set_property com.openexchange.server.considerXForwards "true" /opt/open-xchange/etc/server.properties

# SoftwareChange_Request-1607
pfile=/opt/open-xchange/etc/preview.properties
VALUE=$(ox_read_property com.openexchange.preview.cache.quota $pfile)
if [ "$VALUE" == "0" ]; then
    ox_set_property com.openexchange.preview.cache.quota 10485760 $pfile
fi
VALUE=$(ox_read_property com.openexchange.preview.cache.quotaPerDocument $pfile)
if [ "$VALUE" == "0" ]; then
    ox_set_property com.openexchange.preview.cache.quotaPerDocument 524288 $pfile
fi
if ! ox_exists_property com.openexchange.preview.cache.type $pfile; then
    ox_set_property com.openexchange.preview.cache.type "FS" $pfile
fi
if ! ox_exists_property com.openexchange.preview.cache.quotaAware $pfile; then
    ox_set_property com.openexchange.preview.cache.quotaAware false $pfile
fi

# SoftwareChange_Request-1610
pfile=/opt/open-xchange/etc/templating.properties
if ! ox_exists_property com.openexchange.templating.trusted $pfile; then
    ox_set_property com.openexchange.templating.trusted server $pfile
fi

# SoftwareChange_Request-1635
PFILE=/opt/open-xchange/etc/permissions.properties
if ! ox_exists_property com.openexchange.capability.filestore $PFILE; then
    ox_set_property com.openexchange.capability.filestore true $PFILE
fi

# SoftwareChange_Request-1643
PFILE=/opt/open-xchange/etc/login.properties
if ! ox_exists_property com.openexchange.ajax.login.randomToken $PFILE; then
    ox_set_property com.openexchange.ajax.login.randomToken false $PFILE
fi

# SoftwareChange_Request-1645
PFILE=/opt/open-xchange/etc/server.properties
if ! ox_exists_property com.openexchange.cookie.hash.salt $PFILE; then
    ox_set_property com.openexchange.cookie.hash.salt replaceMe1234567890 $PFILE
fi

# SoftwareChange_Request-1646
PFILE=/opt/open-xchange/etc/configdb.properties
if ! ox_exists_property com.openexchange.database.checkWriteCons $PFILE; then
    ox_set_property com.openexchange.database.checkWriteCons false $PFILE
fi

# SoftwareChange_Request-1648
PFILE=/opt/open-xchange/etc/server.properties
if ! ox_exists_property com.openexchange.servlet.maxRateLenientModules $PFILE; then
    ox_set_property com.openexchange.servlet.maxRateLenientModules "rt, system" $PFILE
fi

# SoftwareChange_Request-1667
ox_add_property com.openexchange.html.css.parse.timeout 4 /opt/open-xchange/etc/server.properties

# SoftwareChange_Request-1684
ox_add_property com.openexchange.templating.usertemplating false /opt/open-xchange/etc/templating.properties

# SoftwareChange_Request-1702
ox_add_property com.openexchange.mail.transport.removeMimeVersionInSubParts false /opt/open-xchange/etc/transport.properties

# SoftwareChange_Request-1707
ox_add_property com.openexchange.servlet.contentSecurityPolicy '""' /opt/open-xchange/etc/server.properties

PFILE=/opt/open-xchange/etc/excludedupdatetasks.properties
if ! grep "com.openexchange.groupware.update.tasks.FolderExtendNameTask" >/dev/null $PFILE; then
    cat >> $PFILE <<EOF

# v7.4.2 update tasks start here

# Extends the size of the 'fname' column in the 'oxfolder_tree' table, as well as the 'name' column in the 'virtualTree' table.
!com.openexchange.groupware.update.tasks.FolderExtendNameTask
EOF
fi

# SoftwareChange_Request-1747
ox_add_property com.openexchange.log.suppressedCategories USER_INPUT /opt/open-xchange/etc/server.properties

# SoftwareChange_Request-1760
ox_add_property com.openexchange.mail.account.blacklist "" /opt/open-xchange/etc/mail.properties

# SoftwareChange_Request-1772
if [ \( -e /opt/open-xchange/etc/file-logging.properties \) -a \( ! \( -e /opt/open-xchange/etc/log4j.xml \) \) ]; then
    cat <<EOF | /opt/open-xchange/sbin/xmlModifier -i /opt/open-xchange/etc/logback.xml -o /opt/open-xchange/etc/logback.xml.new -x /configuration/appender[@name=\'ASYNC\']/appender-ref -r -
<configuration>
    <appender name="ASYNC">
        <appender-ref ref="FILE_COMPAT"/>
    </appender>
</configuration>
EOF
    cat /opt/open-xchange/etc/logback.xml.new >/opt/open-xchange/etc/logback.xml
    rm -f /opt/open-xchange/etc/logback.xml.new
    MODIFIED=$(rpm --verify open-xchange-core | grep file-logging.properties | grep 5 | wc -l)
    if [ $MODIFIED -eq 1 ]; then
        # Configuration has been modified after installation. Try to migrate.
        TMPFILE=$(mktemp)
        /opt/open-xchange/sbin/extractJULModifications -i /opt/open-xchange/etc/file-logging.properties | /opt/open-xchange/sbin/convertJUL2Logback -o $TMPFILE
        /opt/open-xchange/sbin/xmlModifier -i /opt/open-xchange/etc/logback.xml -o /opt/open-xchange/etc/logback.xml.new -x /configuration/logger -r $TMPFILE -d @name
        cat /opt/open-xchange/etc/logback.xml.new >/opt/open-xchange/etc/logback.xml
        /opt/open-xchange/sbin/xmlModifier -i /opt/open-xchange/etc/logback.xml -o /opt/open-xchange/etc/logback.xml.new -x /configuration/root -r $TMPFILE
        cat /opt/open-xchange/etc/logback.xml.new >/opt/open-xchange/etc/logback.xml
        rm -f /opt/open-xchange/etc/logback.xml.new $TMPFILE
    fi
fi
rm -f /opt/open-xchange/etc/file-logging.properties
if [ -e /opt/open-xchange/etc/log4j.xml ]; then
    cat <<EOF | /opt/open-xchange/sbin/xmlModifier -i /opt/open-xchange/etc/logback.xml -o /opt/open-xchange/etc/logback.xml.new -x /configuration/appender[@name=\'ASYNC\']/appender-ref -r -
<configuration>
    <appender name="ASYNC">
        <appender-ref ref="SYSLOG"/>
    </appender>
</configuration>
EOF
    cat /opt/open-xchange/etc/logback.xml.new >/opt/open-xchange/etc/logback.xml
    MODIFIED=$(rpm --verify open-xchange-log4j | grep log4j.xml | grep 5 | wc -l)
    if [ $MODIFIED -eq 1 ]; then
        # Configuration has been modified after installation. Try to migrate.
        TMPFILE=$(mktemp)
        /opt/open-xchange/sbin/extractLog4JModifications -i /opt/open-xchange/etc/log4j.xml | /opt/open-xchange/sbin/convertJUL2Logback -o $TMPFILE
        /opt/open-xchange/sbin/xmlModifier -i /opt/open-xchange/etc/logback.xml -o /opt/open-xchange/etc/logback.xml.new -x /configuration/logger -r $TMPFILE -d @name
        cat /opt/open-xchange/etc/logback.xml.new >/opt/open-xchange/etc/logback.xml
        /opt/open-xchange/sbin/xmlModifier -i /opt/open-xchange/etc/logback.xml -o /opt/open-xchange/etc/logback.xml.new -x /configuration/root -r $TMPFILE
        cat /opt/open-xchange/etc/logback.xml.new >/opt/open-xchange/etc/logback.xml
        rm -f /opt/open-xchange/etc/logback.xml.new $TMPFILE
    fi
fi
rm -f /opt/open-xchange/etc/log4j.xml

# SoftwareChange_Request-1773
ox_add_property com.openexchange.hazelcast.network.symmetricEncryption false /opt/open-xchange/etc/hazelcast.properties
ox_add_property com.openexchange.hazelcast.network.symmetricEncryption.algorithm PBEWithMD5AndDES /opt/open-xchange/etc/hazelcast.properties
ox_add_property com.openexchange.hazelcast.network.symmetricEncryption.salt 2mw67LqNDEb3 /opt/open-xchange/etc/hazelcast.properties
ox_add_property com.openexchange.hazelcast.network.symmetricEncryption.password D2xhL8mPkjsF /opt/open-xchange/etc/hazelcast.properties
ox_add_property com.openexchange.hazelcast.network.symmetricEncryption.iterationCount 19 /opt/open-xchange/etc/hazelcast.properties

# SoftwareChange_Request-1786
ox_add_property com.openexchange.threadpool.keepAliveThreshold 1000 /opt/open-xchange/etc/threadpool.properties

# SoftwareChange_Request-1823
ox_add_property com.openexchange.preview.cache.enabled true /opt/open-xchange/etc/preview.properties

# SoftwareChange_Request-1828
ox_add_property com.openexchange.capability.alone false /opt/open-xchange/etc/permissions.properties

# SoftwareChange_Request-1832
ox_set_property readProperty.5 autoReconnect=false /opt/open-xchange/etc/configdb.properties
ox_set_property writeProperty.5 autoReconnect=false /opt/open-xchange/etc/configdb.properties

# Change jolokia.properties comment by reloading properties
pfile=/opt/open-xchange/etc/jolokia.properties
VALUE=$(ox_read_property com.openexchange.jolokia.user $pfile)
if [ -n "$VALUE" ]; then
    ox_set_property com.openexchange.jolokia.user "$VALUE" $pfile
else
    ox_set_property com.openexchange.jolokia.user "" $pfile
fi
VALUE=$(ox_read_property com.openexchange.jolokia.password $pfile)
if [ -n "$VALUE" ]; then
    ox_set_property com.openexchange.jolokia.password "$VALUE" $pfile
else
    ox_set_property com.openexchange.jolokia.password "" $pfile
fi

# SoftwareChange_Request-1865
PFILE=/opt/open-xchange/etc/whitelist.properties
if [ -n "$(ox_read_property html.tag.base $PFILE)" ]; then
    ox_comment html.tag.base= add /opt/open-xchange/etc/whitelist.properties
fi
if [ -n "$(ox_read_property html.tag.meta $PFILE)" ]; then
    ox_comment html.tag.meta= add $PFILE
fi

# SoftwareChange_Request-1886
PFILE=/opt/open-xchange/etc/server.properties
if ox_exists_property com.openexchange.server.fullPrimaryKeySupport $PFILE; then
    ox_remove_property com.openexchange.server.fullPrimaryKeySupport $PFILE
fi

# SoftwareChange_Request-1956
ox_add_property com.openexchange.ajax.login.formatstring.login "" /opt/open-xchange/etc/login.properties
ox_add_property com.openexchange.ajax.login.formatstring.logout "" /opt/open-xchange/etc/login.properties

# SoftwareChange_Request-1959
VALUE=$(ox_read_property com.openexchange.servlet.maxInactiveInterval /opt/open-xchange/etc/server.properties)
ox_set_property com.openexchange.servlet.maxInactiveInterval "$VALUE" /opt/open-xchange/etc/server.properties

# SoftwareChange_Request-1968
VALUE=$(ox_read_property com.openexchange.sessiond.sessionDefaultLifeTime /opt/open-xchange/etc/sessiond.properties)
ox_set_property com.openexchange.sessiond.sessionDefaultLifeTime "$VALUE" /opt/open-xchange/etc/sessiond.properties

# SoftwareChange_Request-1980
VALUE=$(ox_read_property com.openexchange.jolokia.restrict.to.localhost /opt/open-xchange/etc/jolokia.properties)
ox_set_property com.openexchange.jolokia.restrict.to.localhost "$VALUE" /opt/open-xchange/etc/jolokia.properties

# SoftwareChange_Request-1985
ox_remove_property com.openexchange.log.propertyNames /opt/open-xchange/etc/server.properties

# SoftwareChange_Request-1987
cat <<EOF | /opt/open-xchange/sbin/xmlModifier -i /opt/open-xchange/etc/logback.xml -o /opt/open-xchange/etc/logback.xml.new -x /configuration/define -d @name -r -
<configuration>
    <define name="syslogPatternLayoutActivator" class="com.openexchange.logback.extensions.SyslogPatternLayoutActivator"/>
</configuration>
EOF
cat /opt/open-xchange/etc/logback.xml.new >/opt/open-xchange/etc/logback.xml
rm -f /opt/open-xchange/etc/logback.xml.new

# SoftwareChange_Request-1990
ox_add_property com.openexchange.quota.attachment -1 /opt/open-xchange/etc/quota.properties

# SoftwareChagne_Request-2002
ox_add_property com.openexchange.infostore.zipDocumentsCompressionLevel -1 /opt/open-xchange/etc/infostore.properties

# SoftwareChange_Request-2027
pfile=/opt/open-xchange/etc/whitelist.properties
if ! grep -E '^html.tag.table.*height' $pfile > /dev/null; then
    oval=$(ox_read_property html.tag.table ${pfile})
    oval=${oval//\"/}
    ox_set_property html.tag.table \""${oval}height,"\" $pfile
fi
if ! grep -E '^html.style.combimap.background.*radial-gradient' $pfile > /dev/null; then
    oval=$(ox_read_property html.style.combimap.background ${pfile})
    oval=${oval//\"/}
    ox_set_property html.style.combimap.background \""${oval}radial-gradient,"\" $pfile
fi

# SoftwareChange_Request-2036
VALUE=$(ox_read_property com.openexchange.hazelcast.network.symmetricEncryption /opt/open-xchange/etc/hazelcast.properties)
ox_set_property com.openexchange.hazelcast.network.symmetricEncryption "$VALUE" /opt/open-xchange/etc/hazelcast.properties

# SoftwareChange_Request-2037
PFILE=/opt/open-xchange/etc/sessiond.properties
if ! grep "com.openexchange.sessiond.remoteParameterNames" >/dev/null $PFILE; then
    echo -e "\n# Specifies the colon-separated names of such parameters that are supposed to be taken over from session to stored session representation." >> $PFILE
    echo "# The parameter names MUST NOT contain the ':' colon character that serves as a delimiter." >> $PFILE
    echo "# E.g.    com.openexchange.sessiond.remoteParameterNames=remoteParameter1:remoteParameter2:...:remoteParameterN" >> $PFILE
    echo "#" >> $PFILE
    echo "# By default this setting is empty." >> $PFILE
    echo "#com.openexchange.sessiond.remoteParameterNames=" >> $PFILE
fi

# SoftwareChange_Request-2055
ox_add_property com.openexchange.rest.services.basic-auth.login "" /opt/open-xchange/etc/server.properties
ox_add_property com.openexchange.rest.services.basic-auth.password "" /opt/open-xchange/etc/server.properties

# SoftwareChange_Request-2079
ox_add_property com.openexchange.passwordchange.allowedPattern "" /opt/open-xchange/etc/passwordchange.properties
ox_add_property com.openexchange.passwordchange.allowedPatternHint "" /opt/open-xchange/etc/passwordchange.properties

# SoftwareChange_Request-2081
PFILE=/opt/open-xchange/etc/configdb.properties
KEY=minIdle
if ox_exists_property $KEY $PFILE; then
    ox_remove_property $KEY $PFILE
fi

# SoftwareChange_Request-2094
PFILE=/opt/open-xchange/etc/mail.properties
KEY=com.openexchange.mail.imageHost
ox_add_property $KEY "" $PFILE

# SoftwareChange_Request-2108
pfile=/opt/open-xchange/etc/mime.types
if ! grep vnd.openxmlformats-officedocument.spreadsheetml.template $pfile > /dev/null; then
   ptmp=${pfile}.$$
   cp $pfile $ptmp
   cat<<EOF >> $ptmp
application/vnd.openxmlformats-officedocument.spreadsheetml.template xltx
application/vnd.openxmlformats-officedocument.presentationml.slideshow ppsx
application/vnd.openxmlformats-officedocument.presentationml.presentation pptx
application/vnd.openxmlformats-officedocument.presentationml.slide sldx
application/vnd.ms-excel.addin.macroEnabled.12 xlam
application/vnd.ms-excel.sheet.binary.macroEnabled.12 xlsb
EOF
   if [ -s $ptmp ]; then
      cp $ptmp $pfile
   fi
   rm -f $ptmp
fi

# SoftwareChagne_Request-2110
ox_add_property html.tag.strike '""' /opt/open-xchange/etc/whitelist.properties

# SoftwareChange_Request-2148
ox_add_property com.openexchange.mail.enforceSecureConnection false /opt/open-xchange/etc/mail.properties

# SoftwareChange_Request-2171
PFILE=/opt/open-xchange/etc/server.properties
VALUE=$(ox_read_property com.openexchange.rest.services.basic-auth.login $PFILE)
if [ "open-xchange" = "$VALUE" ]; then
    ox_set_property com.openexchange.rest.services.basic-auth.login "" $PFILE
fi
VALUE=$(ox_read_property com.openexchange.rest.services.basic-auth.password $PFILE)
if [ "secret" = "$VALUE" ]; then
    ox_set_property com.openexchange.rest.services.basic-auth.password "" $PFILE
fi

# SoftwareChange_Request-2177
ox_add_property com.openexchange.preview.thumbnail.blockingWorker false /opt/open-xchange/etc/server.properties

# SoftwareChange_Request-2190
pfile=/opt/open-xchange/etc/cache.ccf
if ! grep "jcs.region.UserPermissionBits=LTCP" > /dev/null $pfile; then
    echo -e "\n# Pre-defined cache for user configuration" >> $pfile
    echo "jcs.region.UserPermissionBits=LTCP" >> $pfile
    echo "jcs.region.UserPermissionBits.cacheattributes=org.apache.jcs.engine.CompositeCacheAttributes" >> $pfile
    echo "jcs.region.UserPermissionBits.cacheattributes.MaxObjects=20000" >> $pfile
    echo "jcs.region.UserPermissionBits.cacheattributes.MemoryCacheName=org.apache.jcs.engine.memory.lru.LRUMemoryCache" >> $pfile
    echo "jcs.region.UserPermissionBits.cacheattributes.UseMemoryShrinker=true" >> $pfile
    echo "jcs.region.UserPermissionBits.cacheattributes.MaxMemoryIdleTimeSeconds=360" >> $pfile
    echo "jcs.region.UserPermissionBits.cacheattributes.ShrinkerIntervalSeconds=60" >> $pfile
    echo "jcs.region.UserPermissionBits.cacheattributes.MaxSpoolPerRun=500" >> $pfile
    echo "jcs.region.UserPermissionBits.elementattributes=org.apache.jcs.engine.ElementAttributes" >> $pfile
    echo "jcs.region.UserPermissionBits.elementattributes.IsEternal=false" >> $pfile
    echo "jcs.region.UserPermissionBits.elementattributes.MaxLifeSeconds=-1" >> $pfile
    echo "jcs.region.UserPermissionBits.elementattributes.IdleTime=360" >> $pfile
    echo "jcs.region.UserPermissionBits.elementattributes.IsSpool=false" >> $pfile
    echo "jcs.region.UserPermissionBits.elementattributes.IsRemote=false" >> $pfile
    echo -e "jcs.region.UserPermissionBits.elementattributes.IsLateral=false\n" >> $pfile
fi

# SoftwareChange_Request-2197
PFILE=/opt/open-xchange/etc/cache.ccf
NAMES=( jcs.region.OXFolderCache.cacheattributes.MaxMemoryIdleTimeSeconds jcs.region.OXFolderCache.elementattributes.MaxLifeSeconds jcs.region.OXFolderCache.elementattributes.IdleTime jcs.region.OXFolderQueryCache.cacheattributes.MaxMemoryIdleTimeSeconds jcs.region.OXFolderQueryCache.elementattributes.MaxLifeSeconds jcs.region.OXFolderQueryCache.elementattributes.IdleTime jcs.region.UserSettingMail.cacheattributes.MaxMemoryIdleTimeSeconds jcs.region.UserSettingMail.elementattributes.MaxLifeSeconds jcs.region.UserSettingMail.elementattributes.IdleTime jcs.region.MailAccount.cacheattributes.MaxMemoryIdleTimeSeconds jcs.region.MailAccount.elementattributes.MaxLifeSeconds jcs.region.MailAccount.elementattributes.IdleTime jcs.region.GlobalFolderCache.cacheattributes.MaxMemoryIdleTimeSeconds jcs.region.GlobalFolderCache.elementattributes.MaxLifeSeconds jcs.region.GlobalFolderCache.elementattributes.IdleTime )
OLDDEFAULTS=( 180 300 180 150 300 150 180 300 180 180 300 180 180 300 180 )
NEWDEFAULTS=( 360 -1 360 360 -1 360 360 -1 360 360 -1 360 360 -1 360 )
for I in $(seq 1 ${#NAMES[@]}); do
    VALUE=$(ox_read_property ${NAMES[$I-1]} $PFILE)
    if [ "${VALUE}" = "${OLDDEFAULTS[$I-1]}" ]; then
        ox_set_property ${NAMES[$I-1]} "${NEWDEFAULTS[$I-1]}" $PFILE
    fi
done

# SoftwareChange_Request-2199
ox_add_property com.openexchange.servlet.maxRateLenientRemoteAddresses "" /opt/open-xchange/etc/server.properties

# SoftwareChange_Request-2204
ox_add_property com.openexchange.webdav.recursiveMarshallingLimit 250000 /opt/open-xchange/etc/server.properties

# SoftwareChange_Request-2206
NAMES=( com.openexchange.quota.calendar com.openexchange.quota.task com.openexchange.quota.contact com.openexchange.quota.infostore com.openexchange.quota.attachment )
for I in "${NAMES[@]}"; do
    VALUE=$(ox_read_property $I /opt/open-xchange/etc/quota.properties)
    ox_set_property $I "$VALUE" /opt/open-xchange/etc/quota.properties
done

# SoftwareChange_Request-2224
ox_add_property com.openexchange.webdav.recursiveMarshallingLimit 250000 /opt/open-xchange/etc/server.properties

# SoftwareChange_Request-2235
ox_add_property com.openexchange.ajax.login.maxRateTimeWindow 300000 /opt/open-xchange/etc/login.properties
ox_add_property com.openexchange.ajax.login.maxRate 50 /opt/open-xchange/etc/login.properties

# SoftwareChange_Request-2243
if [ ${1:-0} -eq 2 ]; then
    VALUE=$(ox_read_property com.openexchange.servlet.maxRate /opt/open-xchange/etc/server.properties)
    if [ "500" = "$VALUE" ]; then
        ox_set_property com.openexchange.servlet.maxRate 1500 /opt/open-xchange/etc/server.properties
    fi
fi

# SoftwareChange_Request-2245
ox_add_property com.openexchange.sessiond.useDistributedTokenSessions false /opt/open-xchange/etc/sessiond.properties

# SoftwareChange_Request-2249
ox_add_property com.openexchange.requestwatcher.usm.ignore.path /syncUpdate /opt/open-xchange/etc/requestwatcher.properties

# SoftwareChange_Request-2250
ox_add_property com.openexchange.requestwatcher.eas.ignore.cmd sync,ping /opt/open-xchange/etc/requestwatcher.properties

# SoftwareChange_Request-2270
ox_add_property html.tag.center '""' /opt/open-xchange/etc/whitelist.properties

# SoftwareChange_Request-2335
PFILE=/opt/open-xchange/etc/ox-scriptconf.sh
JOPTS=$(eval ox_read_property JAVA_XTRAOPTS $PFILE)
JOPTS=${JOPTS//\"/}
if ! echo $JOPTS | grep "logback.threadlocal.put.duplicate" > /dev/null; then
    JOPTS="$JOPTS -Dlogback.threadlocal.put.duplicate=false"
    ox_set_property JAVA_XTRAOPTS \""$JOPTS"\" $PFILE
fi

# SoftwareChange_Request-2342
PFILE=/opt/open-xchange/etc/excludedupdatetasks.properties
if ! grep "com.openexchange.groupware.update.tasks.CheckForPresetMessageFormatInJSLob" >/dev/null $PFILE; then
    cat >> $PFILE <<EOF

# Check for possibly preset message format preference in JSLob and aligns the DB value accordingly
!com.openexchange.groupware.update.tasks.CheckForPresetMessageFormatInJSLob
EOF
fi

# SoftwareChange_Request-2350
ox_add_property com.openexchange.mail.signature.maxImageSize 1 /opt/open-xchange/etc/mail.properties
ox_add_property com.openexchange.mail.signature.maxImageLimit 3 /opt/open-xchange/etc/mail.properties

# SoftwareChange_Request-2353
ox_add_property com.openexchange.infostore.trash.retentionDays -1 /opt/open-xchange/etc/infostore.properties

# SoftwareChange_Request-2429
ox_add_property com.openexchange.guard.endpoint "" /opt/open-xchange/etc/guard.properties

# SoftwareChange_Request-2442
VALUE=$(ox_read_property html.style.background-position /opt/open-xchange/etc/whitelist.properties)
if [ "\",top,bottom,center,left,right,\"" = "$VALUE" ]; then
    ox_set_property html.style.background-position "\",N,top,bottom,center,left,right,\"" /opt/open-xchange/etc/whitelist.properties
fi

# SoftwareChange_Request-2444
PFILE=/opt/open-xchange/etc/excludedupdatetasks.properties
if ! grep "com.openexchange.groupware.update.tasks.DeleteFacebookContactSubscriptionRemnantsTask" >/dev/null $PFILE; then
    cat >> $PFILE <<EOF

# v7.6.2 update tasks start here

# Deletes remnants for removed Facebook subscription
!com.openexchange.groupware.update.tasks.DeleteFacebookContactSubscriptionRemnantsTask
EOF
fi

# SoftwareChange_Request-2456
ox_add_property com.openexchange.caching.jcs.remoteInvalidationForPersonalFolders false /opt/open-xchange/etc/cache.properties

# SoftwareChange_Request-2530
ox_add_property com.openexchange.mail.autoconfig.ispdb.proxy "" /opt/open-xchange/etc/autoconfig.properties
ox_add_property com.openexchange.mail.autoconfig.ispdb.proxy.login "" /opt/open-xchange/etc/autoconfig.properties
ox_add_property com.openexchange.mail.autoconfig.ispdb.proxy.password "" /opt/open-xchange/etc/autoconfig.properties

# SoftwareChange_Request-2546
VALUE=$(ox_read_property com.openexchange.push.allowedClients /opt/open-xchange/etc/mail-push.properties)
if [ "\"USM-EAS*\", \"USM-JSON*\"" = "$VALUE" ]; then
    ox_set_property com.openexchange.push.allowedClients "\"USM-EAS*\", \"USM-JSON*\", \"open-xchange-mailapp\"" /opt/open-xchange/etc/mail-push.properties
fi
ox_add_property com.openexchange.push.allowPermanentPush true /opt/open-xchange/etc/mail-push.properties
ox_add_property com.openexchange.push.credstorage.enabled false /opt/open-xchange/etc/mail-push.properties
ox_add_property com.openexchange.push.credstorage.passcrypt "" /opt/open-xchange/etc/mail-push.properties
ox_add_property com.openexchange.push.credstorage.rdb false /opt/open-xchange/etc/mail-push.properties

# SoftwareChange_Request-2549
VALUE=$(ox_read_property com.openexchange.IPCheckWhitelist /opt/open-xchange/etc/server.properties)
if [ "" = "$VALUE" ]; then
    ox_set_property com.openexchange.IPCheckWhitelist "\"open-xchange-mailapp\"" /opt/open-xchange/etc/server.properties
fi

# SoftwareChange_Request-2575
ox_add_property com.openexchange.capability.mobile_mail_app false /opt/open-xchange/etc/permissions.properties

# SoftwareChange_Request-2665
ox_add_property com.openexchange.calendar.notify.poolenabled true /opt/open-xchange/etc/notification.properties

#SoftwareChange_Request-2698
ox_add_property com.openexchange.mail.rateLimitDisabledRange "" /opt/open-xchange/etc/mail.properties

# SoftwareChange_Request-2831
ox_add_property com.openexchange.tools.images.transformations.maxSize 5242880 /opt/open-xchange/etc/server.properties
ox_add_property com.openexchange.tools.images.transformations.maxResolution 12087962 /opt/open-xchange/etc/server.properties
ox_add_property com.openexchange.tools.images.transformations.waitTimeoutSeconds 10 /opt/open-xchange/etc/server.properties

# SoftwareChange_Request-2849
PFILE=/opt/open-xchange/etc/permissions.properties
if ! ox_exists_property com.openexchange.capability.archive_emails $PFILE; then
    ox_set_property com.openexchange.capability.archive_emails true $PFILE
fi

# SoftwareChange_Request-3034
ox_add_property com.openexchange.mail.bodyDisplaySizeLimit 10485760 /opt/open-xchange/etc/mail.properties

# SoftwareChange_Request-3254
VALUE=$(ox_read_property com.openexchange.mail.account.blacklist /opt/open-xchange/etc/mail.properties)
if [ "" = "$VALUE" ]; then
    ox_set_property com.openexchange.mail.account.blacklist "127.0.0.1-127.255.255.255,localhost" /opt/open-xchange/etc/mail.properties
fi
ox_add_property com.openexchange.mail.account.whitelist.ports "143,993, 25,465,587, 110,995" /opt/open-xchange/etc/mail.properties

# SoftwareChange_Request-3406
TMPFILE=$(mktemp)
rm -f $TMPFILE
cat <<EOF | /opt/open-xchange/sbin/xmlModifier -i /opt/open-xchange/etc/logback.xml -o $TMPFILE -x /configuration/appender[@name=\'FILE\']/encoder/pattern -r -
<configuration>
    <appender name="FILE">
        <encoder>
            <pattern>%date{"yyyy-MM-dd'T'HH:mm:ss,SSSZ"} %-5level [%thread] %class.%method\(%class{0}.java:%line\)%n%sanitisedMessage%n%lmdc%exception{full}</pattern>
        </encoder>
    </appender>
</configuration>
EOF
if [ -e $TMPFILE ]; then
    cat $TMPFILE > /opt/open-xchange/etc/logback.xml
    rm -f $TMPFILE
fi
cat <<EOF | /opt/open-xchange/sbin/xmlModifier -i /opt/open-xchange/etc/logback.xml -o $TMPFILE -x /configuration/appender[@name=\'FILE_COMPAT\']/encoder/pattern -r -
<configuration>
    <appender name="FILE_COMPAT">
        <encoder>
            <pattern>%d{"MMM dd, yyyy H:mm:ss a"} %class.%method\(%class{0}.java:%line\)%n%level: %sanitisedMessage%n%lmdc%exception{full}</pattern>
        </encoder>
    </appender>
</configuration>
EOF
if [ -e $TMPFILE ]; then
    cat $TMPFILE > /opt/open-xchange/etc/logback.xml
    rm -f $TMPFILE
fi
cat <<EOF | /opt/open-xchange/sbin/xmlModifier -i /opt/open-xchange/etc/logback.xml -o $TMPFILE -x /configuration/appender[@name=\'SYSLOG\']/suffixPattern -r -
<configuration>
    <appender name="SYSLOG">
        <suffixPattern>%date open-xchange %-5level [%logger][%thread]: %class.%method\(%class{0}.java:%line\)%n%lmdc %n %sanitisedMessage%n</suffixPattern>
    </appender>
</configuration>
EOF
if [ -e $TMPFILE ]; then
    cat $TMPFILE > /opt/open-xchange/etc/logback.xml
    rm -f $TMPFILE
fi

# SoftwareChange_Request-3862
ox_comment html.tag.form add /opt/open-xchange/etc/whitelist.properties
ox_comment html.tag.input add /opt/open-xchange/etc/whitelist.properties

# SoftwareChange_Request-3934
ox_comment html.style.list-style-image add /opt/open-xchange/etc/whitelist.properties

# SoftwareChange_Request-77
PFILE=/opt/open-xchange/etc/cache.ccf
NAMES=( jcs.region.User.cacheattributes.MaxObjects jcs.region.UserConfiguration.cacheattributes.MaxObjects jcs.region.UserPermissionBits.cacheattributes.MaxObjects jcs.region.UserSettingMail.cacheattributes.MaxObjects jcs.region.Context.cacheattributes.MaxObjects )
OLDDEFAULTS=( 40000 20000 20000 20000 10000 )
NEWDEFAULTS=( 4000000 4000000 4000000 4000000 1000000 )
for I in $(seq 1 ${#NAMES[@]}); do
  VALUE=$(ox_read_property ${NAMES[$I-1]} $PFILE)
  if [ "${VALUE}" = "${OLDDEFAULTS[$I-1]}" ]; then
    ox_set_property ${NAMES[$I-1]} "${NEWDEFAULTS[$I-1]}" $PFILE
  fi
done

# SoftwareChange_Request-175
ox_add_property com.openexchange.server.migrationRedirectURL "" /opt/open-xchange/etc/server.properties

PROTECT="configdb.properties mail.properties management.properties oauth-provider.properties secret.properties secrets sessiond.properties tokenlogin-secrets"
for FILE in $PROTECT
do
    ox_update_permissions "/opt/open-xchange/etc/$FILE" root:open-xchange 640
done
ox_update_permissions "/opt/open-xchange/etc/ox-scriptconf.sh" root:root 644
ox_update_permissions "/opt/open-xchange/osgi" open-xchange:root 750
ox_update_permissions "/var/spool/open-xchange/uploads" open-xchange:root 750
ox_update_permissions "/var/log/open-xchange" open-xchange:root 750
exit 0

%clean
%{__rm} -rf %{buildroot}


%files -f %{configfiles}
%defattr(-,root,root)
%dir /opt/open-xchange/bundles/
/opt/open-xchange/bundles/*
%dir /opt/open-xchange/etc/
%dir /opt/open-xchange/i18n/
%dir /opt/open-xchange/importCSV/
%dir /opt/open-xchange/lib/
/opt/open-xchange/lib/*
%dir /opt/open-xchange/osgi/bundle.d/
/opt/open-xchange/osgi/bundle.d/*
/opt/open-xchange/osgi/config.ini.template
%dir /opt/open-xchange/sbin/
/opt/open-xchange/sbin/*
%dir /opt/open-xchange/templates/
/opt/open-xchange/templates/*
%dir /opt/open-xchange/etc/hazelcast/
%config(noreplace) /opt/open-xchange/etc/hazelcast/*
%dir %attr(750, open-xchange, root) /var/log/open-xchange
%dir %attr(750, open-xchange, root) /var/spool/open-xchange/uploads
%doc docs/
%doc com.openexchange.server/doc/examples
%doc com.openexchange.server/ChangeLog

%changelog
* Tue Jan 12 2021 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2021-01-15 (5932)
* Mon Nov 23 2020 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2020-11-23 (5916)
* Thu Sep 17 2020 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2020-09-22 (5867)
* Wed Jul 01 2020 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2020-07-10 (5794)
* Mon Jun 08 2020 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2020-06-12 (5762)
* Mon May 18 2020 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2020-05-22 (5739)
* Thu Apr 02 2020 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2020-04-07 (5685)
* Tue Mar 03 2020 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2020-03-06 (5637)
* Wed Jan 08 2020 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2020-01-13 (5537)
* Tue Jul 09 2019 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2019-07-12 (5315)
* Tue Jun 18 2019 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2019-07-01 (5288)
* Fri May 03 2019 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2019-05-13 (5231)
* Wed Mar 13 2019 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2019-03-12 (5165)
* Fri Feb 01 2019 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2019-02-11 (5104)
* Mon Nov 12 2018 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2018-11-19 (4895)
* Wed Aug 29 2018 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2018-08-30 (4876)
* Tue Aug 14 2018 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2018-08-20 (4860)
* Thu Aug 02 2018 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2018-08-08 (4856)
* Thu Jun 21 2018 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2018-06-25 (4789)
* Fri May 11 2018 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2018-05-04 (4695)
* Fri Apr 20 2018 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2018-04-23 (4667)
* Tue Jan 30 2018 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2018-02-05 (4552)
* Fri Dec 08 2017 Marcus Klein <marcus.klein@open-xchange.com>
Build for Patch 2017-12-11 (4470)
* Thu Nov 16 2017 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2017-11-30 (4438)
* Mon Oct 23 2017 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2017-10-30 (4423)
* Fri Oct 13 2017 Marcus Klein <marcus.klein@open-xchange.com>
Build for Patch 2017-10-16 (4391)
* Mon Aug 14 2017 Marcus Klein <marcus.klein@open-xchange.com>
2017-08-21 (4315)
* Wed Aug 02 2017 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2017-08-01 (4308)
* Mon Jul 03 2017 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2017-07-10 (4254)
* Mon May 08 2017 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2017-05-15 (4133)
* Tue Apr 18 2017 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2017-04-21 (4079)
* Fri Mar 31 2017 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2017-04-03 (4047)
* Fri Feb 24 2017 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2017-02-24 (3991)
* Wed Feb 08 2017 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2017-02-20 (3949)
* Thu Jan 26 2017 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2017-01-26 (3922)
* Thu Jan 19 2017 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2017-01-23 (3875)
* Tue Jan 03 2017 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2017-01-06 (3833)
* Fri Nov 11 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-11-21 (3728)
* Fri Nov 04 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-11-10 (3712)
* Thu Oct 13 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-10-24 (3627)
* Tue Sep 20 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-09-26 (3569)
* Thu Sep 01 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-09-07 (3527)
* Fri Aug 19 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-08-29 (3519)
* Thu Jul 21 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-08-01 (3464)
* Thu Jun 30 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-07-04 (3358)
* Wed Jun 01 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-06-06 (3315)
* Tue May 03 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-05-09 (3270)
* Tue Apr 19 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-04-25 (3237)
* Mon Mar 21 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-03-29 (3187)
* Tue Mar 08 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-03-14 (3147)
* Mon Feb 22 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-02-29 (3120)
* Wed Feb 03 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-02-08 (3072)
* Tue Jan 19 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-01-25 (3030)
* Fri Jan 15 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-01-14 (3023)
* Thu Jan 07 2016 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2016-01-13 (2972)
* Tue Dec 01 2015 Marcus Klein <marcus.klein@open-xchange.com>
Second candidate for 7.6.3 release
* Mon Oct 26 2015 Marcus Klein <marcus.klein@open-xchange.com>
First candidate for 7.6.3 release
* Tue Oct 20 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-10-26 (2813)
* Mon Oct 19 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-10-30 (2818)
* Mon Oct 12 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-10-23 (2806)
* Wed Sep 30 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-10-12 (2784)
* Fri Sep 25 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-09-28 (2767)
* Tue Sep 08 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-09-14 (2732)
* Wed Sep 02 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-09-01 (2726)
* Mon Aug 24 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-08-24 (2674)
* Mon Aug 17 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-08-12 (2671)
* Thu Aug 06 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-08-17 (2666)
* Tue Aug 04 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-08-10 (2655)
* Mon Aug 03 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-08-03 (2650)
* Thu Jul 23 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-07-27 (2626)
* Wed Jul 15 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-07-20 (2614)
* Fri Jul 03 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-07-10
* Fri Jul 03 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-07-02 (2611)
* Fri Jul 03 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-06-29 (2578)
* Fri Jul 03 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-06-29 (2542)
* Wed Jun 24 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-06-29 (2569)
* Wed Jun 24 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-06-26 (2573)
* Wed Jun 10 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-06-08 (2539)
* Wed Jun 10 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-06-08 (2540)
* Mon May 18 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-05-26 (2521)
* Fri May 15 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-05-15 (2529)
* Fri May 08 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-05-12 (2478)
* Thu Apr 30 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-05-04 (2496)
* Thu Apr 30 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-05-04 (2497)
* Tue Apr 28 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-05-04 (2505)
* Fri Apr 24 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-09-09 (2495)
* Tue Apr 14 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-04-13 (2473)
* Wed Apr 08 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-04-13 (2474)
* Tue Apr 07 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-04-09 (2486)
* Thu Mar 26 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-03-30 (2459)
* Wed Mar 25 2015 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.6.3
* Mon Mar 23 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-03-20
* Tue Mar 17 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-03-18
* Fri Mar 13 2015 Marcus Klein <marcus.klein@open-xchange.com>
Twelfth candidate for 7.6.2 release
* Fri Mar 06 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-03-16
* Fri Mar 06 2015 Marcus Klein <marcus.klein@open-xchange.com>
Eleventh candidate for 7.6.2 release
* Wed Mar 04 2015 Marcus Klein <marcus.klein@open-xchange.com>
Tenth candidate for 7.6.2 release
* Tue Mar 03 2015 Marcus Klein <marcus.klein@open-xchange.com>
Nineth candidate for 7.6.2 release
* Thu Feb 26 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-02-23
* Tue Feb 24 2015 Marcus Klein <marcus.klein@open-xchange.com>
Eighth candidate for 7.6.2 release
* Mon Feb 23 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-02-25
* Thu Feb 12 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-02-23
* Thu Feb 12 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-02-23
* Wed Feb 11 2015 Marcus Klein <marcus.klein@open-xchange.com>
Seventh candidate for 7.6.2 release
* Fri Feb 06 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-02-10
* Fri Feb 06 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-02-09
* Fri Jan 30 2015 Marcus Klein <marcus.klein@open-xchange.com>
Sixth candidate for 7.6.2 release
* Wed Jan 28 2015 Marcus Klein <marcus.klein@open-xchange.com>
Fifth candidate for 7.6.2 release
* Mon Jan 26 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-10-27
* Mon Jan 26 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-01-26
* Wed Jan 21 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-01-29
* Mon Jan 12 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-01-09
* Wed Jan 07 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-01-12
* Mon Jan 05 2015 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-01-12
* Tue Dec 30 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2015-01-12
* Tue Dec 16 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-12-10
* Fri Dec 12 2014 Marcus Klein <marcus.klein@open-xchange.com>
Fourth candidate for 7.6.2 release
* Mon Dec 08 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-12-15
* Mon Dec 08 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-12-10
* Mon Dec 08 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-12-15
* Fri Dec 05 2014 Marcus Klein <marcus.klein@open-xchange.com>
Third candidate for 7.6.2 release
* Thu Dec 04 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-12-09
* Tue Dec 02 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-12-03
* Tue Nov 25 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-12-01
* Mon Nov 24 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-12-01
* Mon Nov 24 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-12-01
* Fri Nov 21 2014 Marcus Klein <marcus.klein@open-xchange.com>
Second candidate for 7.6.2 release
* Thu Nov 20 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-12-01
* Wed Nov 19 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-11-21
* Tue Nov 18 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-11-20
* Mon Nov 10 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-11-17
* Mon Nov 10 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-11-17
* Mon Nov 10 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-11-17
* Tue Nov 04 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-11-10
* Fri Oct 31 2014 Marcus Klein <marcus.klein@open-xchange.com>
First candidate for 7.6.2 release
* Tue Oct 28 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-11-03
* Mon Oct 27 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-10-30
* Fri Oct 24 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-11-04
* Fri Oct 24 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-11-03
* Fri Oct 24 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-10-22
* Fri Oct 17 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-10-24
* Tue Oct 14 2014 Marcus Klein <marcus.klein@open-xchange.com>
Fifth candidate for 7.6.1 release
* Fri Oct 10 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-10-20
* Fri Oct 10 2014 Marcus Klein <marcus.klein@open-xchange.com>
Fourth candidate for 7.6.1 release
* Fri Oct 10 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-10-20
* Thu Oct 09 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-10-13
* Tue Oct 07 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-10-09
* Tue Oct 07 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-10-09
* Tue Oct 07 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-10-10
* Thu Oct 02 2014 Marcus Klein <marcus.klein@open-xchange.com>
Third release candidate for 7.6.1
* Tue Sep 30 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-10-06
* Fri Sep 26 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-09-29
* Fri Sep 26 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-10-06
* Tue Sep 23 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-10-02
* Thu Sep 18 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-09-23
* Wed Sep 17 2014 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.6.2 release
* Tue Sep 16 2014 Marcus Klein <marcus.klein@open-xchange.com>
Second release candidate for 7.6.1
* Mon Sep 08 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-09-15
* Mon Sep 08 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-09-15
* Fri Sep 05 2014 Marcus Klein <marcus.klein@open-xchange.com>
First release candidate for 7.6.1
* Thu Aug 21 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-08-25
* Wed Aug 20 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-08-25
* Mon Aug 18 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-08-25
* Wed Aug 13 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-08-15
* Tue Aug 05 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-08-06
* Mon Aug 04 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-08-11
* Mon Aug 04 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-08-11
* Mon Jul 28 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-07-30
* Mon Jul 21 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-07-28
* Tue Jul 15 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-07-21
* Mon Jul 14 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-07-24
* Thu Jul 10 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-07-15
* Mon Jul 07 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-07-14
* Mon Jul 07 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-07-07
* Tue Jul 01 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-07-07
* Thu Jun 26 2014 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.6.1
* Mon Jun 23 2014 Marcus Klein <marcus.klein@open-xchange.com>
Seventh candidate for 7.6.0 release
* Fri Jun 20 2014 Marcus Klein <marcus.klein@open-xchange.com>
Sixth release candidate for 7.6.0
* Wed Jun 18 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-06-30
* Fri Jun 13 2014 Marcus Klein <marcus.klein@open-xchange.com>
Fifth release candidate for 7.6.0
* Fri Jun 13 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-06-23
* Thu Jun 05 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-06-16
* Fri May 30 2014 Marcus Klein <marcus.klein@open-xchange.com>
Fourth release candidate for 7.6.0
* Thu May 22 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-05-26
* Fri May 16 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-05-26
* Fri May 16 2014 Marcus Klein <marcus.klein@open-xchange.com>
Third release candidate for 7.6.0
* Wed May 07 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-05-05
* Mon May 05 2014 Marcus Klein <marcus.klein@open-xchange.com>
Second release candidate for 7.6.0
* Fri Apr 25 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-04-29
* Tue Apr 15 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-04-22
* Fri Apr 11 2014 Marcus Klein <marcus.klein@open-xchange.com>
First release candidate for 7.6.0
* Thu Apr 10 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-04-11
* Thu Apr 03 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-04-07
* Mon Mar 31 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-03-31
* Wed Mar 19 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-03-21
* Mon Mar 17 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-03-24
* Thu Mar 13 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-03-13
* Mon Mar 10 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-03-12
* Fri Mar 07 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-03-07
* Tue Mar 04 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-03-05
* Tue Feb 25 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-03-10
* Tue Feb 25 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-02-26
* Fri Feb 21 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-02-28
* Fri Feb 21 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-02-26
* Tue Feb 18 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-02-20
* Wed Feb 12 2014 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.6.0
* Fri Feb 07 2014 Marcus Klein <marcus.klein@open-xchange.com>
Sixth release candidate for 7.4.2
* Thu Feb 06 2014 Marcus Klein <marcus.klein@open-xchange.com>
Fifth release candidate for 7.4.2
* Thu Feb 06 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-02-11
* Tue Feb 04 2014 Marcus Klein <marcus.klein@open-xchange.com>
Fourth release candidate for 7.4.2
* Fri Jan 31 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-02-03
* Thu Jan 30 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-02-03
* Wed Jan 29 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-01-30
* Tue Jan 28 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-01-31
* Tue Jan 28 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-01-30
* Tue Jan 28 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-01-30
* Mon Jan 27 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-01-30
* Fri Jan 24 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-12-17
* Thu Jan 23 2014 Marcus Klein <marcus.klein@open-xchange.com>
Third release candidate for 7.4.2
* Wed Jan 22 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-01-22
* Mon Jan 20 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-01-20
* Thu Jan 16 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-01-16
* Mon Jan 13 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-01-14
* Fri Jan 10 2014 Marcus Klein <marcus.klein@open-xchange.com>
Second release candidate for 7.4.2
* Fri Jan 10 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-12-17
* Fri Jan 03 2014 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2014-01-06
* Mon Dec 23 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-12-09
* Mon Dec 23 2013 Marcus Klein <marcus.klein@open-xchange.com>
First release candidate for 7.4.2
* Thu Dec 19 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-12-23
* Thu Dec 19 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-12-23
* Thu Dec 19 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-12-23
* Wed Dec 18 2013 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.4.2
* Tue Dec 17 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-12-19
* Tue Dec 17 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-12-18
* Tue Dec 17 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-12-16
* Thu Dec 12 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-12-12
* Thu Dec 12 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-12-12
* Mon Dec 09 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-12-09
* Fri Dec 06 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-11-29
* Fri Dec 06 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-12-10
* Tue Dec 03 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-11-28
* Wed Nov 20 2013 Marcus Klein <marcus.klein@open-xchange.com>
Fifth candidate for 7.4.1 release
* Tue Nov 19 2013 Marcus Klein <marcus.klein@open-xchange.com>
Fourth candidate for 7.4.1 release
* Mon Nov 11 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-11-12
* Mon Nov 11 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-11-12
* Fri Nov 08 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-11-11
* Thu Nov 07 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-11-08
* Thu Nov 07 2013 Marcus Klein <marcus.klein@open-xchange.com>
Third candidate for 7.4.1 release
* Tue Nov 05 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-11-12
* Wed Oct 30 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-10-28
* Thu Oct 24 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-10-30
* Thu Oct 24 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-10-30
* Wed Oct 23 2013 Marcus Klein <marcus.klein@open-xchange.com>
Second candidate for 7.4.1 release
* Tue Oct 22 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-10-23
* Mon Oct 21 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-10-21
* Thu Oct 17 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-10-21
* Tue Oct 15 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-10-11
* Mon Oct 14 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-10-21
* Mon Oct 14 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-10-15
* Thu Oct 10 2013 Marcus Klein <marcus.klein@open-xchange.com>
First sprint increment for 7.4.0 release
* Wed Oct 09 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-10-09
* Wed Oct 09 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-10-07
* Thu Sep 26 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-09-23
* Tue Sep 24 2013 Marcus Klein <marcus.klein@open-xchange.com>
Eleventh candidate for 7.4.0 release
* Fri Sep 20 2013 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.4.1 release
* Fri Sep 20 2013 Marcus Klein <marcus.klein@open-xchange.com>
Tenth candidate for 7.4.0 release
* Thu Sep 12 2013 Marcus Klein <marcus.klein@open-xchange.com>
Ninth candidate for 7.4.0 release
* Wed Sep 11 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-09-12
* Wed Sep 11 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-09-12
* Thu Sep 05 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-09-05
* Mon Sep 02 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-09-26
* Mon Sep 02 2013 Marcus Klein <marcus.klein@open-xchange.com>
Eighth candidate for 7.4.0 release
* Fri Aug 30 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-08-30
* Wed Aug 28 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-09-03
* Tue Aug 27 2013 Marcus Klein <marcus.klein@open-xchange.com>
Seventh candidate for 7.4.0 release
* Fri Aug 23 2013 Marcus Klein <marcus.klein@open-xchange.com>
Sixth candidate for 7.4.0 release
* Thu Aug 22 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-08-22
* Thu Aug 22 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-08-22
* Tue Aug 20 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-08-19
* Mon Aug 19 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-08-21
* Mon Aug 19 2013 Marcus Klein <marcus.klein@open-xchange.com>
Fifth release candidate for 7.4.0
* Tue Aug 13 2013 Marcus Klein <marcus.klein@open-xchange.com>
Fourth release candidate for 7.4.0
* Tue Aug 06 2013 Marcus Klein <marcus.klein@open-xchange.com>
Third release candidate for 7.4.0
* Mon Aug 05 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-08-09
* Fri Aug 02 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-08-02
* Fri Aug 02 2013 Marcus Klein <marcus.klein@open-xchange.com>
Second release candidate for 7.4.0
* Fri Jul 26 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-07-26
* Wed Jul 24 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-08-02
* Wed Jul 17 2013 Marcus Klein <marcus.klein@open-xchange.com>
First release candidate for 7.4.0
* Tue Jul 16 2013 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.4.0
* Mon Jul 15 2013 Marcus Klein <marcus.klein@open-xchange.com>
Second build for patch 2013-07-18
* Mon Jul 15 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-07-18
* Fri Jul 12 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-07-18
* Fri Jul 12 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-07-18
* Thu Jul 11 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-07-10
* Wed Jul 03 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-06-27
* Mon Jul 01 2013 Marcus Klein <marcus.klein@open-xchange.com>
Third candidate for 7.2.2 release
* Fri Jun 28 2013 Marcus Klein <marcus.klein@open-xchange.com>
Second candidate for 7.2.2 release
* Wed Jun 26 2013 Marcus Klein <marcus.klein@open-xchange.com>
Release candidate for 7.2.2 release
* Tue Jun 25 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-07-05
* Mon Jun 24 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-06-21
* Fri Jun 21 2013 Marcus Klein <marcus.klein@open-xchange.com>
Second feature freeze for 7.2.2 release
* Mon Jun 17 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-06-11
* Mon Jun 17 2013 Marcus Klein <marcus.klein@open-xchange.com>
Feature freeze for 7.2.2 release
* Tue Jun 11 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-06-13
* Mon Jun 10 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-06-11
* Fri Jun 07 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-06-20
* Mon Jun 03 2013 Marcus Klein <marcus.klein@open-xchange.com>
First sprint increment for 7.2.2 release
* Wed May 29 2013 Marcus Klein <marcus.klein@open-xchange.com>
First candidate for 7.2.2 release
* Tue May 28 2013 Marcus Klein <marcus.klein@open-xchange.com>
Second build for patch 2013-05-28
* Mon May 27 2013 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.2.2
* Thu May 23 2013 Marcus Klein <marcus.klein@open-xchange.com>
Third candidate for 7.2.1 release
* Wed May 22 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-05-22
* Wed May 22 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-05-22
* Wed May 22 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-05-22
* Wed May 15 2013 Marcus Klein <marcus.klein@open-xchange.com>
Second candidate for 7.2.1 release
* Wed May 15 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-05-10
* Mon May 13 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-05-09
* Mon May 13 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-05-09
* Mon May 13 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-05-09
* Mon May 13 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-05-09
* Mon May 13 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-05-09
* Tue May 07 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-05-08
* Fri May 03 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-04-23
* Tue Apr 30 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-04-17
* Sun Apr 28 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-04-25
* Mon Apr 22 2013 Marcus Klein <marcus.klein@open-xchange.com>
First candidate for 7.2.1 release
* Thu Apr 18 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-04-30
* Wed Apr 17 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-04-09
* Mon Apr 15 2013 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.2.1
* Fri Apr 12 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-04-12
* Wed Apr 10 2013 Marcus Klein <marcus.klein@open-xchange.com>
Fourth candidate for 7.2.0 release
* Tue Apr 09 2013 Marcus Klein <marcus.klein@open-xchange.com>
Third candidate for 7.2.0 release
* Tue Apr 02 2013 Marcus Klein <marcus.klein@open-xchange.com>
Second candidate for 7.2.0 release
* Tue Apr 02 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-04-04
* Tue Apr 02 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-04-04
* Tue Apr 02 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-04-04
* Tue Apr 02 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-04-04
* Tue Mar 26 2013 Marcus Klein <marcus.klein@open-xchange.com>
First release candidate for 7.2.0
* Mon Mar 18 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-03-18
* Fri Mar 15 2013 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.2.0
* Tue Mar 12 2013 Marcus Klein <marcus.klein@open-xchange.com>
Sixth release candidate for 6.22.2/7.0.2
* Mon Mar 11 2013 Marcus Klein <marcus.klein@open-xchange.com>
Fifth release candidate for 6.22.2/7.0.2
* Fri Mar 08 2013 Marcus Klein <marcus.klein@open-xchange.com>
Fourth release candidate for 6.22.2/7.0.2
* Fri Mar 08 2013 Marcus Klein <marcus.klein@open-xchange.com>
Third release candidate for 6.22.2/7.0.2
* Thu Mar 07 2013 Marcus Klein <marcus.klein@open-xchange.com>
Second release candidate for 6.22.2/7.0.2
* Mon Mar 04 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-03-07
* Mon Mar 04 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-03-08
* Fri Mar 01 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-03-07
* Wed Feb 27 2013 Marcus Klein <marcus.klein@open-xchange.com>
First release candidate for 6.22.2/7.0.2
* Tue Feb 26 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-02-22
* Mon Feb 25 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-02-22
* Tue Feb 19 2013 Marcus Klein <marcus.klein@open-xchange.com>
Fourth release candidate for 7.0.1
* Tue Feb 19 2013 Marcus Klein <marcus.klein@open-xchange.com>
Third release candidate for 7.0.1
* Tue Feb 19 2013 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.0.2 release
* Fri Feb 15 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-02-13
* Thu Feb 14 2013 Marcus Klein <marcus.klein@open-xchange.com>
Second release candidate for 7.0.1
* Fri Feb 01 2013 Marcus Klein <marcus.klein@open-xchange.com>
First release candidate for 7.0.1
* Tue Jan 29 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-01-28
* Mon Jan 21 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-01-24
* Tue Jan 15 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-01-23
* Thu Jan 10 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2013-01-10
* Thu Jan 10 2013 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.0.1
* Thu Jan 03 2013 Marcus Klein <marcus.klein@open-xchange.com>
Build for public patch 2013-01-15
* Fri Dec 28 2012 Marcus Klein <marcus.klein@open-xchange.com>
Build for public patch 2012-12-31
* Fri Dec 21 2012 Marcus Klein <marcus.klein@open-xchange.com>
Build for public patch 2012-12-21
* Tue Dec 18 2012 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2012-12-27
* Tue Dec 18 2012 Marcus Klein <marcus.klein@open-xchange.com>
Third release candidate for 7.0.0
* Mon Dec 17 2012 Marcus Klein <marcus.klein@open-xchange.com>
Second release candidate for 7.0.0
* Wed Dec 12 2012 Marcus Klein <marcus.klein@open-xchange.com>
Build for public patch 2012-12-04
* Tue Dec 04 2012 Marcus Klein <marcus.klein@open-xchange.com>
First release candidate for 7.0.0
* Tue Dec 04 2012 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 7.0.0 release
* Mon Nov 26 2012 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2012-11-28
* Wed Nov 14 2012 Marcus Klein <marcus.klein@open-xchange.com>
Sixth release candidate for 6.22.1
* Tue Nov 13 2012 Marcus Klein <marcus.klein@open-xchange.com>
Fifth release candidate for 6.22.1
* Tue Nov 13 2012 Marcus Klein <marcus.klein@open-xchange.com>
First release candidate for EDP drop #6
* Mon Nov 12 2012 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2012-11-08
* Thu Nov 08 2012 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2012-11-08
* Tue Nov 06 2012 Marcus Klein <marcus.klein@open-xchange.com>
Fourth release candidate for 6.22.1
* Mon Nov 05 2012 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2012-10-31
* Fri Nov 02 2012 Marcus Klein <marcus.klein@open-xchange.com>
Third release candidate for 6.22.1
* Wed Oct 31 2012 Marcus Klein <marcus.klein@open-xchange.com>
Second release candidate for 6.22.1
* Wed Oct 31 2012 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2012-10-31
* Tue Oct 30 2012 Marcus Klein <marcus.klein@open-xchange.com>
Build for patch 2012-10-29
* Fri Oct 26 2012 Marcus Klein <marcus.klein@open-xchange.com>
Third release build for EDP drop #5
* Fri Oct 26 2012 Marcus Klein <marcus.klein@open-xchange.com>
First release candidate for 6.22.1
* Fri Oct 26 2012 Marcus Klein <marcus.klein@open-xchange.com>
Second release build for EDP drop #5
* Fri Oct 26 2012 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 6.22.1
* Thu Oct 11 2012 Marcus Klein <marcus.klein@open-xchange.com>
Release build for EDP drop #5
* Wed Oct 10 2012 Marcus Klein <marcus.klein@open-xchange.com>
Fifth release candidate for 6.22.0
* Tue Oct 09 2012 Marcus Klein <marcus.klein@open-xchange.com>
Fourth release candidate for 6.22.0
* Fri Oct 05 2012 Marcus Klein <marcus.klein@open-xchange.com>
Third release candidate for 6.22.0
* Thu Oct 04 2012 Marcus Klein <marcus.klein@open-xchange.com>
Second release candidate for 6.22.0
* Tue Sep 04 2012 Marcus Klein <marcus.klein@open-xchange.com>
First release candidate for 6.23.0
* Mon Sep 03 2012 Marcus Klein <marcus.klein@open-xchange.com>
prepare for next EDP drop
* Tue Aug 21 2012 Marcus Klein <marcus.klein@open-xchange.com>
First release candidate for 6.22.0
* Mon Aug 20 2012 Marcus Klein <marcus.klein@open-xchange.com>
prepare for 6.22.0
* Tue Jul 03 2012 Marcus Klein <marcus.klein@open-xchange.com>
Release build for EDP drop #2
* Mon Jun 04 2012 Marcus Klein <marcus.klein@open-xchange.com>
Release build for EDP drop #2
* Tue May 22 2012 Marcus Klein <marcus.klein@open-xchange.com>
Internal release build for EDP drop #2
* Mon Apr 16 2012 Marcus Klein <marcus.klein@open-xchange.com>
Internal release build for EDP drop #1
* Wed Apr 04 2012 Marcus Klein <marcus.klein@open-xchange.com>
Internal release build for EDP drop #0
* Mon Oct 17 2011 Marcus Klein <marcus.klein@open-xchange.com>
Initial release
