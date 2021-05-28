#!/bin/bash

###############################################
# Manipulate Generated sources

# Replace "node" with "logic"
sed 's/^node/logic/g' fibo_gen.vs > fibo_gen.sv

# Fix clk signal name from 'Clk_H' to 'clk'
sed -i 's/posedge(Clk_H)/(posedge clk)/g' fibo_gen.sv


# Fix the `include statement
sed 's/fibo_gen.vs/fibo_gen.sv/g' fibo.vs > fibo.sv

# Comment the `line in generated sources.
sed -i 's/^`line/\/\/&/g' fibo.sv
# Comment the `include for non-existent "tlv_hsx.vh" source file.
sed -i 's/^`include "tlv_hsx.vh"/\/\/&/g' fibo.sv

#cp fibo.vs fibo.sv
##############################################
if [ ! -d build ]; then
    mkdir build
fi

pushd $PWD
cd build
# One needs to set VERILATOR_HOME where the Verilator is installed.
cmake ..

make


ctest

popd

