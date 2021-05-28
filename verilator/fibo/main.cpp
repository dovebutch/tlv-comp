#include <verilated.h>          // Defines common routines
#include <iostream>             // Need std::cout
#include "Vfibo.h"               // From Verilating "top.v"
#include <verilated_vcd_c.h>    // For enabling VCD Tracing

using namespace std;

vluint64_t main_time = 0;       // Current simulation time
// This is a 64-bit integer to reduce wrap over issues and
// allow modulus.  This is in units of the timeprecision
// used in Verilog (or from --timescale-override)

double sc_time_stamp () {       // Called by $time in Verilog
    return main_time;           // converts to double, to match
    // what SystemC does
}

int main(int argc, char** argv) {

    Verilated::traceEverOn(true);
    Verilated::commandArgs(argc, argv);   // Remember args

    Verilated::mkdir("logs");

    auto top = make_unique<Vfibo>();      // Create instance

    VerilatedVcdC *tfp = nullptr;
    const char* flag = Verilated::commandArgsPlusMatch("trace");
    if(flag && 0 == strcmp(flag, "+trace")) {
        cout << "Enabling waves into logs/fibo.vcd...\n";
        tfp = new VerilatedVcdC; //< Create VCD Tracefile
        top->trace(tfp, 99); //< Enable tracing Verilated Module
        tfp->open("logs/fibo.vcd");
    }

    top->reset = 0;           // Set some inputs
    top->cyc_cnt = 0;
    top->clk = 1;

    while (!Verilated::gotFinish()) {
        if (main_time > 10 && main_time < 20) {
            top->reset = 1;   // Deassert reset
        }
        if(main_time > 20) {
            top->reset = 0;
        }

        if ((main_time % 5) == 0) {
            top->clk = !top->clk;
        }

        if(!top->reset && (main_time > 10 && (main_time % 10 == 1))) {
            ++top->cyc_cnt;
        }
        top->eval();            // Evaluate model
        if(tfp)
            tfp->dump(main_time);   // Capture VCD Trace
        cout << top->cyc_cnt << endl;       // Read a output
        ++main_time;            // Time passes...
        if(top->failed) {
            cout << "Simulation FAILED!\n";
            break;
        }
        if(top->passed) {
            cout << "Simulation PASSED!\n";
            break;
        }
    }

    top->final();               // Done simulating

    if(tfp) {
        tfp->close();
    }
    return 0;
}

