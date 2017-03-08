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

some sv code

\TLV
   |pipe1
      @0
         $signal3 = ..;
\SV_plus

*something = |pipe0$siga#10 | withA;


\TLV
   |pipe0
      @0
         $valid = >top|pipe1$signal3#5;
      ?valid
         @1
            \always_comb
               $$sigc.field = >top|pipe1$signal3#1;
            \SV_plus
               $$sigk = special_operation(>top|pipe1$signal3#0);
            $siga[A:0] = ...;
         @4L
            $sigb = $siga[B]#2 && $sigc#4;
         @9
            *struct.field1 = $siga[4:2];
            *struct.field2 = $sigb | $sigk;
//\SV_plus
//
//somesignal = >top|pipe0$sigG#10 | withA;
//if(somethingelse)
//some signal = this;