#!/bin/sh

APP_BASE_NAME=`basename "$0"`
GRADLE_USER_HOME="${GRADLE_USER_HOME:-$HOME/.gradle}"

warn () {
    echo "$*"
} >&2

die () {
    echo
    echo "$*"
    echo
    exit 1
} >&2

case "`uname`" in
  Darwin* )
    darwin=true
    ;;
  MINGW* )
    msys=true
    ;;
esac

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

exec "$JAVACMD" $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS \
    -classpath "$CLASSPATH" \
    org.gradle.wrapper.GradleWrapperMain \
    "$@"
