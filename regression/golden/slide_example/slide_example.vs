`line 2 "slide_example.tlv"// \TLV_version 1a: tl-x.org
`include "tlv_hsx.vh"  //SV
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
   ...
`include "slide_example_gen.vs"
   /*_|pipe6_*/
      /*_@0_*/
         assign blah_PIPE6_00H = stuff;
      for (inst = 0; inst <= 3; inst++) begin  /*_>inst[3:0]_*/
         /*_@0_*/
            assign valid_PIPE6_Inst_00H[inst] = valid_U600H;
            assign sv_val = stuff;
         /*_?*sv_val_*/
            /*_@1_*/
               assign raw_inst_PIPE6_Inst_01H[inst] = ...;
               assign mem_op_PIPE6_Inst_01H[inst] = raw_inst_PIPE6_Inst_01H[inst] == 2'b01;
         /*_@3_*/
            assign valid_mem_op_PIPE6_Inst_03H[inst] = valid_PIPE6_Inst_03H[inst] & mem_op_PIPE6_Inst_03H[inst];
         /*_?valid_mem_op_*/
            /*_@3L_*/
               assign mem_addr_PIPE6_Inst_03L[inst][50:0] = op_a_PIPE6_Inst_03L[inst] + raw_inst_PIPE6_Inst_03L[inst];
            /*_@4L_*/
               assign mem_addr_plus1_PIPE6_Inst_04L[inst][50:0] = mem_addr_PIPE6_Inst_04L[inst] | 51'b1;   // BUG: Enable for the clock producing $mem_addr@4L is not created.
         /*_>deep_*/
            /*_@1_*/
               assign test_PIPE6_Inst_Deep_01H[inst] = valid_PIPE6_Inst_Deep_01H[inst]; end
   /*_|pipe7_*/
      /*_@1_*/
         assign foo_PIPE7_01H = valid_PIPE6_Inst_01H[0];
   /*_|pipe3_*/
      /*_@0_*/
         assign add_PIPE3_00H = valid_U400H;
      /*_@1_*/
         assign best_PIPE3_01H = add_PIPE3_01H;
      /*_@2_*/
         assign call_PIPE3_02H = add_PIPE3_02H | best_PIPE3_02H;
         assign dist_PIPE3_02H = best_PIPE3_02H;
      /*_@3_*/
         assign each_PIPE3_03H = call_PIPE3_03H & dist_PIPE3_03H;
      /*_@4_*/
         assign flow_PIPE3_04H = dist_PIPE3_04H;
      /*_@5_*/
         assign git_PIPE3_05H = call_PIPE3_05H | best_PIPE3_05H;
   /*_|pipe4_*/
      /*_@1_*/
         assign flow_PIPE4_01H = jump_PIPE4_01H & add_PIPE3_00H; endgenerate
