#!/usr/bin/env bash

bin_dir="$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
java -jar "${bin_dir}/../core/build/libs/krisp-core-all.jar"
