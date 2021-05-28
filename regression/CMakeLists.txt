
# Copyright (c) 2014, Intel Corporation
# 
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
# 
#     * Redistributions of source code must retain the above copyright notice,
#       this list of conditions and the following disclaimer.
#     * Redistributions in binary form must reproduce the above copyright
#       notice, this list of conditions and the following disclaimer in the
#       documentation and/or other materials provided with the distribution.
#     * Neither the name of Intel Corporation nor the names of its contributors
#       may be used to endorse or promote products derived from this software
#       without specific prior written permission.
# 
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
# FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
# DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
# SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
# CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
# OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
# OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.


# Regression suite.


# Preprocess and check results.
add_test(beh_hier           ${SVGEN_ROOT}/bin/test_svgen beh_hier)
add_test(users_guide1       ${SVGEN_ROOT}/bin/test_svgen users_guide1)
add_test(operand_mux        ${SVGEN_ROOT}/bin/test_svgen operand_mux)
add_test(slide_example      ${SVGEN_ROOT}/bin/test_svgen slide_example)
add_test(yuras_presentation ${SVGEN_ROOT}/bin/test_svgen yuras_presentation)
#add_test(instr_pipe ${SVGEN_ROOT}/bin/test_svgen instr_pipe) # Disabled due to dependence on APLR_REPO
#add_test(aplr ${SVGEN_ROOT}/bin/test_svgen aplr) # Disabled due to dependence on APLR_REPO
#add_test(ring               ${SVGEN_ROOT}/bin/test_svgen test_vcs)
#add_test(ring               ${SVGEN_ROOT}/bin/test_svgen ring) # Disabled due to usage of $ANY not supported on TLV 1a
add_test(fibo               ${SVGEN_ROOT}/bin/test_svgen fibo)
