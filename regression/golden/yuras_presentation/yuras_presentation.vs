`line 2 "yuras_presentation.tlv"// \TLV_version 1a: tl-x.org
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

   
`include "yuras_presentation_gen.vs"
   /*_|pipe0_*/
      /*_@0_*/
         assign valid_PIPE0_00H = ...;
         assign sig_c_PIPE0_00H = ...;
      /*_?$valid_*/
         /*_@1_*/

            always_comb begin
               sig_f_PIPE0_01H.field = ...; end
            assign sig_f_PIPE0_01H.other_field = sig_c_PIPE0_01H ? 1'b1 : sig_f_PIPE0_02H.other_field;
            assign sig_a_PIPE0_01H = ...;
            assign sig_g_PIPE0_01H = ..;
         /*_@4L_*/
            assign sig_b_PIPE0_04L = sig_a_PIPE0_06L && sig_c_PIPE0_08L && sig_g_PIPE0_04L && sig_f_PIPE0_04L.field;
         /*_@9_*/
            assign structure.field = sig_b_PIPE0_09H; endgenerate
