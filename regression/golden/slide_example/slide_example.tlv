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
   ...
\TLV
   |pipe6
      @0
         $blah = stuff;
      >inst[3:0]
         @0
            $valid = *valid_U600H;
            *sv_val = stuff;
         ?*sv_val
            @1
               $raw_inst = ...;
               $mem_op = $raw_inst == 2'b01;
         @3
            $valid_mem_op = $valid & $mem_op;
         ?valid_mem_op
            @3L
               $mem_addr[50:0] = $op_a + $raw_inst;
            @4L
               $mem_addr_plus1[50:0] = $mem_addr | 51'b1;   // BUG: Enable for the clock producing $mem_addr@4L is not created.
         >deep
            @1
               $test = $valid;
   |pipe7
      @1
         $foo = >top|pipe6>inst[0]$valid#0;
   |pipe3
      @0
         $add = *valid_U400H;
      @1
         $best = $add;
      @2
         $call = $add | $best;
         $dist = $best;
      @3
         $each = $call & $dist;
      @4
         $flow = $dist;
      @5
         $git = $call | $best;
   |pipe4
      @1
         $flow = $jump & >top|pipe3$add#-1;

