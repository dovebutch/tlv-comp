`line 2 "users_guide1.tlv"// \TLV_version 1a: tl-x.org
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
// Inputs to TLV
node [4:0] opcode_u101H [3:0];
// Outputs from TLV
node [3:0] arith_err_u104H;

`include "users_guide1_gen.vs"
   /*_|pipe4_*/
      for (inst = 0; inst <= 3; inst++) begin  /*_>inst[3:0]_*/
         /*_@0_*/
            assign valid_PIPE4_Inst_00H[inst] = ...;
         /*_?valid_*/
            /*_@1_*/
               assign opcode_PIPE4_Inst_01H[inst][4:0] = opcode_u101H[inst];
               assign add_PIPE4_Inst_01H[inst] = opcode_PIPE4_Inst_01H[inst] == 5'b00000;
       end//...
      for (inst = 0; inst <= 3; inst++) begin  /*_>inst[*]_*/
         /*_?valid_*/ //?bogus
            /*_@3_*/
               assign write_PIPE4_Inst_03H[inst] = opcode_PIPE4_Inst_03H[inst] == 5'b11000;
            /*_@4_*/
               assign arith_err_u104H[inst] = (add_PIPE4_Inst_04H[inst] && overflow_PIPE4_Inst_04H[inst]) || (sub_PIPE4_Inst_04H[inst] && underflow_PIPE4_Inst_04H[inst]); end endgenerate
