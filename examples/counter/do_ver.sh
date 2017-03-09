#!/bin/bash                                                                                   

# cleanup
rm -rf obj_dir
rm -f  counter.vcd


# run Verilator to translate Verilog into C++, include C++ testbench
echo make build
make build

# build C++ project
echo make comp
make comp

# run executable simulation
echo make run
make run
