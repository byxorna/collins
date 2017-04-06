APP_NAME="collins"
APP_HOME="/usr/local/$APP_NAME/current"
LISTEN_PORT=9000
APP_OPTS="-Dconfig.file=$APP_HOME/conf/production.conf -Dhttp.port=${LISTEN_PORT} -Dlogger.file=$APP_HOME/conf/logger.xml"
DNS_OPTS="-Dnetworkaddress.cache.ttl=1 -Dnetworkaddress.cache.negative.ttl=1"
JMX_OPTS="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=3333 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"
GC_OPTS="-XX:+CMSClassUnloadingEnabled"
GC_LOG_OPTS="-XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintTenuringDistribution -XX:+PrintHeapAtGC"
GC_LOG="-Xloggc:/var/log/$APP_NAME/gc.log"
HEAP_OPTS="-XX:MaxPermSize=384m"
DEBUG_OPTS="-XX:ErrorFile=/var/log/$APP_NAME/java_error%p.log -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp/collinsDump.hprof"
EXTRA_OPTS=""
JAVA_OPTS="-server $APP_OPTS $DNS_OPTS $JMX_OPTS $GC_OPTS $GC_LOG_OPTS $GC_LOG $HEAP_OPTS $DEBUG_OPTS $EXTRA_OPTS"