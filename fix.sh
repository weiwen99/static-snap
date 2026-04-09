#!/usr/bin/env bash

set -e -x

./mill __.fix
./mill __.reformat
