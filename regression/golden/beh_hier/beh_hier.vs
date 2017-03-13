`line 2 "beh_hier.tlv"// \TLV_version 1a: tl-x.org
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
`include "beh_hier_gen.vs"
   for (core = 0; core <= 1; core++) begin  /*_>core[1:0]_*/
      for (inst = 0; inst <= 3; inst++) begin  /*_>inst[3:0]_*/
         /*_|pipe1_*/
            /*_@1_*/
               // \source "beh_hier.tlvm4" 100
                  assign valid_Core_Inst_PIPE1_01H[core][inst] = test_sig;
            /*_?$valid_*/
               /*_@3_*/
                  assign op_a_Core_Inst_PIPE1_03H[core][inst][63:0] =
                     (op_a_src_Core_Inst_PIPE1_03H[core][inst] == IMM) ? imm_data_Core_Inst_PIPE1_03H[core][inst]    :
                     (op_a_src_Core_Inst_PIPE1_03H[core][inst] == BYP) ? rslt_Core_Inst_PIPE1_04H[core][inst]     :
                     (op_a_src_Core_Inst_PIPE1_03H[core][inst] == REG) ? reg_data_Core_Inst_PIPE1_05H[core][inst] :
                     (op_a_src_Core_Inst_PIPE1_03H[core][inst] == MEM) ? mem_data_M320H :
                                          64'b0;
               /*_@4_*/
                  assign rslt_Core_Inst_PIPE1_04H[core][inst][63:0] = f_ALU(opcode_Core_Inst_PIPE1_04H[core][inst], op_a_Core_Inst_PIPE1_04H[core][inst], op_b_Core_Inst_PIPE1_04H[core][inst]);
               /*_@5_*/
                  assign reg_data_Core_Inst_PIPE1_05H[core][inst][63:0] = ...;
         /*_|pipe2_*/
            /*_@5_*/
               assign rslt_Core_Inst_PIPE2_05H[core][inst][63:0] = rslt_Core_Inst_PIPE1_05H[core][inst];
            /*_@6_*/
               assign out_Core_Inst_PIPE2_06H[core][inst][63:0] = rslt_Core_Inst_PIPE2_06H[core][inst];
               `BOGUS_USE(out_Core_Inst_PIPE2_06H[core][inst]); end end endgenerate
