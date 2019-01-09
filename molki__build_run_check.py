#!/usr/bin/env python3

import os
import sys
from os import walk
from subprocess import *

test_files = []
test_dir   = "./mjtest/tests/exec"
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
        or file_name.endswith(".java")
        or file_name.startswith(".mj")
        or file_name.endswith(".input.java.out")):

        # build + run backend + molki
        run([ "./run"
            , "--compile"
            , test_dir + "/" + file_name ])
        temp, _ = Popen([ "./a.out" ], stdout=PIPE).communicate()
        tempStr = str(temp.decode()).strip()

        file = open(test_dir + "/" + file_name + ".out", "r")
        temp2 = file.read().strip()
        file.close()



        strState = "Success" if (tempStr == temp2) else "Fail"
        print("[ " + strState + " ] " + file_name)

        if (tempStr != temp2):
            exit_code = 1

sys.exit(exit_code)


        # file2 = open(result_file_name, "a")
        # file2.write(file_name + "\n")
        # file2.write(str(tempStr == temp2) + "\n")
        # if tempStr != temp2:
        #     file2.write(test_dir + "/" + file_name + ".out")
        #     file2.write("\n")
        #     file2.write(tempStr + "\n")
        #     file2.write(temp2 + "\n")
        # file2.write("\n")
        # file2.close()

# out_file = "a.out"
# out_s_file = "a.out.s"
# molki_file = "a.molki.s"
# if os.path.isfile(out_file):
#     os.remove(out_file)
# if os.path.isfile(out_s_file):
#     os.remove(out_s_file)
# if os.path.isfile(molki_file):
#     os.remove(molki_file)
