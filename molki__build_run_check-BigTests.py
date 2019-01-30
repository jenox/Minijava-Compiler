#!/usr/bin/env python3

import os
import sys
from os import walk
from subprocess import *

test_files = []
test_dir   = "./mjtest/tests/exec/big"
run_cmd    = "run"
molki_cmd  = "./molki/molki.py"

exit_code = 0 

result_file_name = "molki_test_results.txt"
if os.path.isfile(result_file_name):
    os.remove(result_file_name)

for (_, _, file_names) in walk(test_dir):
    test_files = file_names
    break

for file_name in test_files:
    if not (   file_name.endswith(".mj.out") 
        or file_name.endswith(".inputc")
        or file_name.endswith(".mjtest_correct_testcases_compile-firm")
        or file_name.endswith(".out")
        or file_name.startswith(".java")
        or file_name.startswith(".mj")
        or file_name.endswith(".input.java.out")):

        # build + run backend + molki
        run([ "./run"
            , "--compile"
            , test_dir + "/" + file_name ])
        temp, _ = Popen([ "./a.out" ], stdout=PIPE).communicate()
        tempStr = str(temp.decode()).strip()

        if os.path.isfile(test_dir + "/" + file_name + ".out"):
            file = open(test_dir + "/" + file_name + ".out", "r")
        else:
            assert os.path.isfile(test_dir + "/" + file_name[:-3] + ".out"), "output file is missing"
            file = open(test_dir + "/" + file_name[:-3] + ".out", "r")

        temp2 = file.read().strip()
        file.close()



        strState = "Success" if (tempStr == temp2) else "Fail"
        print("[ " + strState + " ] " + file_name)

        if (tempStr != temp2):
            exit_code = 1

sys.exit(exit_code)
