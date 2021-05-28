cmake_minimum_required(VERSION 3.5)

project(fibo)

find_package(verilator HINTS $ENV{VERILATOR_HOME})

set (CMAKE_CXX_STANDARD "17")
set (CMAKE_CXX_EXTENSIONS OFF)
set (CMAKE_EXPORT_COMPILE_COMMANDS ON)

add_executable(
        ${PROJECT_NAME}
        main.cpp
        )

verilate(
        ${PROJECT_NAME}
        SOURCES fibo.sv #top_gen.sv
        VERILATOR_ARGS --debug --trace
        )

include (CTest)

add_test(
        NAME ${PROJECT_NAME}
        COMMAND ${PROJECT_NAME} +trace
        )
