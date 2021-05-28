\TLV_version 1a: tl-x.org
\SV
module top(
    input wire clk,
    input wire reset,
    input wire [31:0] cyc_cnt,
    output wire passed,
    output wire failed);

\TLV
   |pipe
      @0
!        $reset = *reset;
         $val[15:0] = $reset ? 1: $val#+1 + $val#+2;

!        *passed = *cyc_cnt > 40;
!        *failed = 1'b0;
\SV
endmodule;

