`line 2 "fibo.tlv"// \TLV_version 1a: tl-x.org
`include "tlv_hsx.vh"  //SV
module top(
    input wire clk,
    input wire reset,
    input wire [31:0] cyc_cnt,
    output wire passed,
    output wire failed);

`include "fibo_gen.vs"
   /*_|pipe_*/
      /*_@0_*/
         assign reset_PIPE_00H = reset;
         assign val_PIPE_00H[15:0] = reset_PIPE_00H ? 1: val_PIPE_01H + val_PIPE_02H;

         assign passed = cyc_cnt > 40;
         assign failed = 1'b0; endgenerate
//SV
endmodule;
