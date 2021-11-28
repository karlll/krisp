#!/usr/bin/env bash

bin_dir="$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
java -jar "${bin_dir}/../build/libs/krisp-0.0.1-SNAPSHOT.jar"
