\TLV_version 1a: tl-x.org
\SV
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

`include "ring.vh"

`define STEP(n) repeat(n) @(negedge clk);

`define PACKET(in_stop, packet_number, valid, out_stop, size) \
   packet_valid[in_stop][packet_number] = valid;    \
   packet_size[in_stop][packet_number] = size;      \
   packet_dest[in_stop][packet_number] = out_stop;
   
module ring_tb ();

// Primary inputs
logic clk;
logic reset;

logic [7:0] data_in [RING_STOPS];
logic [RING_STOPS_WIDTH-1:0] dest_in [RING_STOPS];
logic valid_in [RING_STOPS];

// Primary outputs
logic accepted [RING_STOPS];
logic [7:0] data_out [RING_STOPS];
logic valid_out [RING_STOPS];


// Test bench signals
logic                        packet_valid [RING_STOPS][INQ_DEPTH];
logic [7:0]                  packet_size  [RING_STOPS][INQ_DEPTH];
logic [RING_STOPS_WIDTH-1:0] packet_dest  [RING_STOPS][INQ_DEPTH];
logic [2:0] active_packet [RING_STOPS];
logic [15:0] expected_cnt [RING_STOPS];

logic stop_done [RING_STOPS];
logic all_stops_done;
logic passed;

logic [15:0] cyc_cnt;

\TLV
\SV


// The DUT
ring ring (.*);


task step;

   for (int i = 0; i < RING_STOPS; i++) begin
      if (accepted[i] || !packet_valid[i][active_packet[i]]) begin
         packet_size[i][active_packet[i]]--;
         if (packet_size[i][active_packet[i]] == 0) begin
            active_packet[i]++;
         end
      end
      // Drive next.
      valid_in[i] = packet_valid[i][active_packet[i]];
      data_in[i] = valid_in[i] ? {active_packet[i], packet_size[i][active_packet[i]][4:0]} : 'x;
      dest_in[i] = valid_in[i] ? packet_dest[i][active_packet[i]] : 'x;

      // Receive packets
      if (valid_out[i]) begin
         expected_cnt[i]--;
      end
   end
   
   
   `STEP(1)

   if (passed) begin
      SUCCESS: assert(1'b1) begin
         $display("Success!!!  All packets entered ring.");
         $finish;
      end
   end
endtask


// Done?
genvar g_i;
generate
   for (g_i = 0; g_i < RING_STOPS; g_i++) begin : tb_inq1
      always_comb begin
         stop_done[g_i] = 1'b1;
         for (int p = 0; p < INQ_DEPTH; p++) begin
            stop_done[g_i] &= !(packet_valid[g_i][p] && packet_size[g_i][p] > 0);
         end
      end
   end

   always_comb begin
      all_stops_done = 1'b1;
      for (int i = 0; i < RING_STOPS; i++) begin
         all_stops_done &= stop_done[i];
      end
      passed = all_stops_done;
      for (int i = 0; i < RING_STOPS; i++) begin
         passed &= (expected_cnt[i] == 0);
      end
   end
endgenerate


// Clock
initial begin
   clk = 1'b1;
   forever #5 clk = ~clk;
end

initial begin
   reset = 1'b1;
   
   // Initialize packet stream to many long invalid "packets".
   for (int i = 0; i < RING_STOPS; i++) begin
      active_packet[i] = '0;
      for (int p = 0; p < INQ_DEPTH; p++) begin
         packet_valid[i][p] = 0;
         packet_dest[i][p] = 'x;
         packet_size[i][p] = 255;
      end
      expected_cnt[i] = '0;
   end
   
   
   // Populate packets.
   packet_valid[0][0] = 1'b1;
   packet_size[0][0] = 4;
   packet_dest[0][0] = 3;
   `PACKET(0, 0, 1'b1,  3, 10)
   `PACKET(1, 0, 1'b1,  4, 4)
   `PACKET(2, 0, 1'b0, 'x, 3)
   `PACKET(2, 1, 1'b1,  3, 10)
   `PACKET(4, 0, 1'b0, 'x, 1)
   `PACKET(4, 1, 1'b1,  1, 1)

   // expected_cnt
   for (int i = 0; i < RING_STOPS; i++) begin
      for (int p = 0; p < INQ_DEPTH; p++) begin
         if (packet_valid[i][p]) begin
            expected_cnt[packet_dest[i][p]] += packet_size[i][p];
         end
      end
   end


   `STEP(5)
   
   
   reset = 1'b0;
   
   // Run
   
   cyc_cnt = '0;
   for (int cyc = 0; cyc < 100; cyc++) begin
      step();
      cyc_cnt++;
   end

   DIE: assert (1'b1) begin
      $error("Failed!!!  Test did not complete in alloted time.");
      $finish;
   end

end

endmodule  // ring_tb
