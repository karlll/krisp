#!/usr/bin/env bash -x
test_dir="$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

steps=(step2_eval.mal step3_env.mal step4_if_fn_do.mal step5_tco.mal step6_file.mal step7_quote.mal step8_macros.mal step9_try.mal stepA_mal.mal)

for i in "${steps[@]}"
do
   "${test_dir}"/runtest.py  --rundir "${test_dir}" --hard --deferrable --optional "$i" -- sh "${test_dir}"/../../../bin/krisp-repl.sh
done
