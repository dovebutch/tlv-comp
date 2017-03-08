
# Copyright (c) 2014, Intel Corporation
# 
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
# 
#     * Redistributions of source code must retain the above copyright notice,
#       this list of conditions and the following disclaimer.
#     * Redistributions in binary form must reproduce the above copyright
#       notice, this list of conditions and the following disclaimer in the
#       documentation and/or other materials provided with the distribution.
#     * Neither the name of Intel Corporation nor the names of its contributors
#       may be used to endorse or promote products derived from this software
#       without specific prior written permission.
# 
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
# FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
# DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
# SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
# CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
# OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
# OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

##################################################################
#
# Comments:
#
#    To build Java projects we need cmake > 2.8.  We can additionally use
#    "add_test" to enable regression testing.
#
#    Here are the steps to build and run:
#        $ /usr/intel/pkgs/cmake/2.8.7/bin/cmake .
#        $ make
#        $ java -jar target/svgen.jar <args>
#
# Author:
# 
#    Stephan Bourduas (stephan.bourduas@intel.com)
#
#
##################################################################

cmake_minimum_required(VERSION 2.8)
enable_testing()
#set(CMAKE_VERBOSE_MAKEFILE 1)
find_package(Java 1.7)
include(UseJava)
project(svgen Java)
set(SVGEN_ROOT ${svgen_BINARY_DIR})
set(CMAKE_INSTALL_PREFIX ${svgen_BINARY_DIR}/target)
set(SVGEN_TARGET_DIR ${svgen_BINARY_DIR}/target)
set(SVGEN_BUILD_DIR ${svgen_BINARY_DIR}/build)


add_subdirectory(src ${SVGEN_BUILD_DIR})

add_subdirectory(regression)

