/*
Copyright (c) 2014, Intel Corporation

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of Intel Corporation nor the names of its contributors
      may be used to endorse or promote products derived from this software
      without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/



Contents:
--------

This repository includes:

bin/
  certify_as_golden: Accepts most recent regression results as "golden".
  diff_golden:       Shows difference between most recent regression results and "golden" regression
                     results.
  m4_config.m4:      Used by M4-wrapped run.
  make_all:          Build SVGen.
  make_regress_all:  make_all and run regressions.
  post_m4:           Used by M4-wrapped run.
  pre_m4:            Used by M4-wrapped run.
  regress_clean:     Delete results of regression run.
  run:               Runs a regression test.
  run_m4:            Runs M4 wrapper on a .tlv file.
  svgen:             SVGen.  This is a thin wrapper around the SVGen Java application that supports
                     the use of M4.
  test_svgen:        The executable for each regression test.  It runs M4 if needed and SVGen.
  test_vcs:          Some regression tests can run in simulation and could be modified to use this
                     instead of test_svgen to run SVGen (+M4 if needed) and VCS simulation.
  tlv_repo_context.pm: Library file for other executables.
CMakeList:           Build collateral used by cmake.
Docs/:               Documentation, mostly diagrams of SVGen data structures.  Some docs are likely
                     a bit obsolete.
examples/:           A directory containing regression tests (.tlv examples).
                     (Tests are lame, since Intel-proprietary code was stripped.)
m4/:                 Library files of M4 content for M4-extended .tlv files.
README.txt/:         This file.
regression/:         Collateral defining regression tests.
src/:                SVGen source files (including some 3rd party open source code).
verilog/:            Verilog include content used by generated SV.


Requirements:
------------
  For build and run:
    /usr/bin/cmake
    /usr/bin/perl
    /usr/bin/java
    /usr/bin/m4  (for those .tlv files that use it)
  For development:
    /usr/bin/meld



Build:
-----

In {local-clone-dir}
  > make_all
    (which does):
    - /usr/bin/cmake .    (version 2.8.7)
    - make
    - make install


Test:
----

In {local-clone-dir}
  > ctest -R <test-name>

Log written to ./Testing/Temporary/LastTest.log

Tests are defined in ./regression/CMakeLists.txt
Tests run ./bin/test_svgen <test-name> which picks up where ./bin/make_all leaves off.  It runs
SVGen on the .tlv code to produce .sv.  Some generated .sv can be run in simulation.
./bin/test_vcs was created to run test_svgen and then simulate.  This obviously would require
a simulator, so it is not used by default.  test_svgen writes output to
"regress/gen/<test-name>".


Regression:
----------

In {local-clone-dir}
  > /usr/bin/ctest -j10  (10-way parallel)
  (Output written to "run/gen".)
  > ./bin/diff_golden
  > git commit -m "message"
  > git pull
  > ./bin/certify_as_golden (if okay)
  > git commit -m "golden certification"
  > git push


Issues:
------
  - Build process has dependency issues.  Must remove /build to force rebuild.
  - Verilog content is assumed that is not included.  Specifically, there is no
    definition for `GATER(...) macro (which has proprietary heritage).
  - Support for state signals and $ANY is disabled since support for
    TL-X 2a is not complete.
  - Regressions tests are extemely weak (with the loss of proprietary tests).
