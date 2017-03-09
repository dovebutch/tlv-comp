build:
	verilator -Wall --trace --cc counter.v --exe counter_tb.cpp

comp:
	make -C obj_dir -j -f Vcounter.mk Vcounter

run:
	obj_dir/Vcounter

wave:
	gtkwave counter.vcd
