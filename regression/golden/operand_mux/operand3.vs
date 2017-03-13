`line 2 "operand3.tlv"// \TLV_version 1a: tl-x.org
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
`include "operand3_gen.vs"
   // Context:
   /*_|pipe1_*/
      /*_@2_*/
         assign valid_PIPE1_02H = ...;
      /*_@3_*/
         assign op_a_src_PIPE1_03H[3:0] = ...;
         assign imm_data_PIPE1_03H[63:0] = ...;
      /*_@5_*/
         assign reg_data_PIPE1_05H[63:0] = ...;
      //|pipe1
      /*_?valid_*/
         /*_@3_*/
            assign op_a_PIPE1_03H[63:0] =
               (op_a_src_PIPE1_03H == IMM) ? imm_data_PIPE1_03H    :
               (op_a_src_PIPE1_03H == BYP) ? rslt_PIPE1_04H     :
               (op_a_src_PIPE1_03H == REG) ? reg_data_PIPE1_05H :
               (op_a_src_PIPE1_03H == MEM) ? mem_data_M320H :
                                    64'b0;
         /*_@4_*/
            assign rslt_PIPE1_04H[63:0] = f_ALU(opcode_PIPE1_04H, op_a_PIPE1_04H, op_b_PIPE1_04H);
       endgenerate//...
