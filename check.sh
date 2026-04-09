#!/usr/bin/env bash

set -e -x

./mill $MILL_OPTS __.checkFormat
./mill $MILL_OPTS _.fix --check
./mill $MILL_OPTS __.test
