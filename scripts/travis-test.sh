#!/bin/bash

case "$TESTTYPE" in
  checkstyle)
    ./mini-java-compiler/gradlew -b ./mini-java-compiler/build.gradle checkstyleMain checkstyleTest
    ;;
  frontend)
    ./build --no-swift && \
    echo "" && echo "" && echo "#### LEXER TESTS" && \
    ./mjtest/mjt.py lexer --ci_testing --parallel && \
    echo "" && echo "" && echo "#### PARSER TESTS" && \
    ./mjtest/mjt.py syntax --ci_testing --parallel && \
    echo "" && echo "" && echo "#### AST GENERATION TESTS" && \
    ./mjtest/mjt.py ast --ci_testing --parallel && \
    echo "" && echo "" && echo "#### SEMANTIC TESTS" && \
    ./mjtest/mjt.py semantic --ci_testing --parallel && \
    echo "" && echo "" && echo "#### FIRM COMPILATION TESTS" && \
    ./mjtest/mjt.py compile-firm --ci_testing --parallel --all_exec_tests
    ;;
  backend)
    ./build && \
    echo "" && echo "" && echo "#### COMPILATION TESTS" &&
    ./mjtest/mjt.py compile --ci_testing --parallel --all_exec_tests
    echo "" && echo "" && echo "#### COMPILATION TESTS (NO OPTIMIZATIONS)" &&
    ./mjtest/mjt.py no-optimization --ci_testing --parallel --all_exec_tests
    ;;
  *)
    echo "Unknown test type! Quitting ..."; return 1
    ;;
esac
