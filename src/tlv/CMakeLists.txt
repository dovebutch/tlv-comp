
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


set(TLV_SRC
	Main.java
	behavioral/Alignment.java
	behavioral/Clock.java
	behavioral/LogicalBehHier.java
	behavioral/LogicalBehScope.java
	behavioral/LogicalBranch.java
	behavioral/LogicalNode.java
	behavioral/LogicalPipeline.java
	behavioral/LogicalStage.java
	behavioral/LogicalWhen.java
	behavioral/PipeSignal.java
	behavioral/TransitionStage.java
	behavioral/range/BitRange.java
	behavioral/range/Range.java
	behavioral/range/TransitionRange.java
	behavioral/range/UsedBitRange.java
	behavioral/range/WildcardedSigRef.java
	config/CommandLineOptions.java
	config/IdentifierType.java
	config/ProjSpecific.java
	parse/ActiveParseContext.java
	parse/Assignment.java
	parse/GeneratedSvFile.java
	parse/NodeType.java
	parse/ParseBehHier.java
	parse/ParseBehScope.java
	parse/ParseBranch.java
	parse/ParseContext.java
	parse/ParseEndSource.java
	parse/ParseIdentifierNode.java
	parse/ParseNode.java
	parse/ParsePipeline.java
	parse/ParseSeq.java
	parse/ParseSource.java
	parse/ParseStage.java
	parse/ParseSV.java
	parse/ParseSvType.java
	parse/ParseTLV.java
	parse/ParseTLVVersion.java
	parse/ParseWhen.java
	parse/Prefix.java
	parse/SourceFile.java
	parse/identifier/Expression.java
	parse/identifier/Identifier.java
	parse/identifier/ParseElement.java
	parse/identifier/ParseMnemonic.java
	utilities/Severity.java
)

#set(CMAKE_JAVA_JAR_ENTRY_POINT tlv/Main) # this only works for cmake 2.8.8
set(CMAKE_JAVA_INCLUDE_PATH ${SVGEN_BUILD_DIR}/joptsimple/joptsimple.jar)
set(CMAKE_JAVA_TARGET_OUTPUT_DIR {$SVGEN_TARGET_DIR})
add_jar(tlv ${TLV_SRC})

add_dependencies(tlv joptsimple)

#add_custom_command(TARGET svgen POST_BUILD COMMAND jar ufe svgen.jar tlv.Main)

#install_jar(tlv ${SVGEN_TARGET_DIR})
