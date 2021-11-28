#!/usr/bin/env bash
SCRIPT_DIR="$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

steps=(step2_eval.mal step3_env.mal step4_if_fn_do.mal step5_tco.mal step6_file.mal step7_quote.mal step8_macros.mal step9_try.mal stepA_mal.mal)

for i in "${steps[@]}"
do
   "${SCRIPT_DIR}"/runtest.py  --rundir "${SCRIPT_DIR}" --hard --deferrable --optional "$i" -- sh "${SCRIPT_DIR}"/../../../bin/krisp-repl.sh
done
