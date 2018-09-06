#!/bin/sh
echo "===== begin to startup binlog server ===="

PRGNAME=binlog-server
ADATE=`date +%Y%m%d%H%M%S`

echo "$ADATE"

PRGDIR=`pwd`
dirname $0|grep "^/" >/dev/null

if [ $? -eq 0 ];then
   PRGDIR=`dirname $0`
else
    dirname $0|grep "^\." >/dev/null
    retval=$?
    if [ $retval -eq 0 ];then
        PRGDIR=`dirname $0|sed "s#^.#$PRGDIR#"`
    else
        PRGDIR=`dirname $0|sed "s#^#$PRGDIR/#"`
    fi
fi


LOGDIR=$PRGDIR/logs
if [ ! -d "$LOGDIR" ]; then
        mkdir "$LOGDIR"
fi





