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

package tlv.utilities;

/**
 * Encoding for error/warning/info severity.
 * Error methodology:
 *    o Parse messages either represent errors which must be fixed, have a mechanism to be waived which is reported with the message,
 *      or are from a very limited report.
 *    o \TLV context syntax is very specific.  Different compilers should adhere to it.  Clean code may report WARNINGs that
 *      differ across compilers.  A compiler switch should be available to disable them, but this should not commonly be used.
 *      Instead, the build process should support a mechanism to compare the baseline report vs. the new report and report diffs.
 *      Release should be gated by an increase in warnings.  This avoids warnings getting lost in the clutter and it gives an
 *      incremental path to eliminating warnings from code leveraged from a project using a different compiler or compiler switches.
 */
public enum Severity
{
	DEBUG(),          // A debug message.  These are for development only and should not exist in production.
	CONTEXT(),        // This is reported prior to another, providing an additional related context.
	INFORM(),         // A routine informational message.  These are limited to a small core set, or are
	                  // specifically requested.
	WARNING(),        // A situation warranting review.  For parsing, the message must contain
	                  // information about how to waive the report.
	FIXED_ERROR(),    // Input is wrong, but there is a reasonable interpretation that is made and a correction
	                  // available in echoed TLV code.
	SYNTAX_ERROR(),   // Input is wrong; an interpretation is assumed and reported.  (This is similar to
	                  // RECOV_ERROR, but is specifically for design parsing and indicates that echoed TLV code
	                  // will differ from original.)
	RECOV_ERROR(),    // Design is, or might be, broken, but program execution can be trusted otherwise.
	                  // Design error or assumption is reported.
	LOGIC_ERROR(),    // Code can be generated and valid (compilable) SV can be produced, but the logic may not be correct.
	GEN_ERROR(),      // SV pre-processor will run to completion safely, but the generated code will not be correct and may not compile.
	RISKY_ERROR(),    // Best effort to recover, but no promises.
	DEFERRED_ERROR(), // Error, but the current stage of execution can be completed safely, and then an error reported.
	                  // TODO: Add a notion of stages of execution (like parsing, etc.), and a callback to step to a new phase.
	ERROR(),          // Execution continues, but downstream behavior is questionable.
	FATAL_ERROR(),    // Can't recover.  Exits immediately.
	BUG(),            // Bug, but we will continue execution and hope for the best.
	FATAL_BUG();      // Bug from which we cannot recover.
	
	
	public int getExitCode()
	{
		return ((ordinal() <= 2) ? 0 : (ordinal() - 2) /* -2 compensates for CONTEXT */);
	}
	
	private Severity()
	{
	}
}
