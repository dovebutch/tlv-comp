
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


package Context;

use strict;

use FindBin ();
use File::Path qw(make_path);



#
# Configuration
#

my $expected_java = '/usr/bin/java';
my $expected_cmake = '/usr/bin/cmake';

# This structure is used to characterize the examples and define regression tests.
# TODO: I took the wrong approach with this.  Build/run of examples should be
#       considered build, not regression.  Need to rework this whole build
#       infrastructure w/ cmake.
  # If these are touched, rebuild everything:
@Context::lib_files = ('m4/generic.tlvm4',
                       'm4/pipeflow_lib.tlvm4',
                       'target/svgen.jar');
#my $aplr_repo = (exists $ENV{APLR_REPO}) ? $ENV{APLR_REPO} : "<UNDEFINED>";
%Context::tests = (
  beh_hier           => {tlv => ['examples/beh_hier/beh_hier.tlv'],
                         exit_code => [4]
                        },
  users_guide1       => {tlv => ['examples/doc_examples/users_guide1.tlv'],
                         exit_code => [4]
                        },
  operand_mux        => {tlv => ['examples/dttc_2012/operand3.tlv',
                                'examples/dttc_2012/operand4.tlv'],
                         exit_code => [4, 4]
                        },
  slide_example      => {tlv => ['examples/slide_example/slide_example.tlv'],
                         exit_code => [4]
                        },
  yuras_presentation => {tlv => ['examples/yuras_presentation/yuras_presentation.tlv']},
  ring               => {tlv => ['examples/ring/ring.tlvm4',
                                'examples/ring/ring_tb.tlv'],
                         pp_args => [" --xinj --xclk", " --xinj --xclk"],
                         exit_code => [0, 0],
                         f => ['examples/ring/ring.f'],
                         simv => 2  # Self-checking.
                        },

  #
  # These are source controlled elsewhere, but can be run from this environment.
  # They are not part of the regression.
  #
  aplr               => {path_env => 'APLR_REPO',   # An environment variable that must exist to use this test and is used as an include path for m4.
                         # TODO: Need to expose aplr.m4 as a source file for dependence analysis.
                         tlv => [# "src/subswitch.tlv",
                                # "src/subswitch_tb.tlv",
                                "src/aplr.tlvm4",
                                "src/aplr_tb.tlv"],
                         pp_args => [' -noline --xinj --xclk',
                                     ' -noline --xinj --xclk'],
                         exit_code => [0, 0],
                         f => ["src/aplr.f"]
                         #simv => 2  # Self-checking.
                        },

  instr_pipe         => {path_env => 'APLR_REPO',   # An environment variable that must exist to use this test and is used as an include path for m4.
                         # TODO: Need to expose aplr.m4 as a source file for dependence analysis.
                         tlv => [#"src/subswitch.tlv",
                                #"src/subswitch_tb.tlv",
                                "src/instr_pipe.tlvm4",
                                "src/instr_pipe_tb.tlv"],
                         pp_args => [' -noline --xinj --xclk', " --xinj --xclk"],
                         exit_code => [1, 0],
                         f => [#"src/subswitch.f",
                               "src/instr_pipe.f"]
                         #simv => 2  # Self-checking.
                        },

#  instr_pipe         => {path_env => 'APLR_REPO',   # An environment variable that must exist to use this test and is used as an include path for m4.
#                         # TODO: Need to expose aplr.m4 as a source file for dependence analysis.
#                         tlv => [#"src/subswitch.tlv",
#                                #"src/subswitch_tb.tlv",
#                                "src/instr_pipe.tlvm4",
#                                "src/instr_pipe_tb.tlv"],
#                         pp_args => [' -noline --xinj --xclk', " --xinj --xclk"],
#                         exit_code => [1, 0],
#                         f => [#"src/subswitch.f",
#                               "src/instr_pipe.f"]
#                         #simv => 2  # Self-checking.
#                        }
  fibo               => {
                         tlv => ['examples/fibo/fibo.tlv'],
                         exit_code => [0]
                        }
);



#
# Subroutines
#


# Init.
#   Chdir to repository dir.
#   Check Java version.
# Args:
#   bool: svgen required
#   bool: (default 1) VCS required
sub init {
  my $svgen_needed = shift;
  my $vcs_required = shift;
  if (!defined($vcs_required)) {$vcs_required = 1;}

  $ENV{VCS_HOME} = '/SOMEWHERE';
  ((-e $ENV{VCS_HOME}) || !$vcs_required) or die "Can't find VCS";
  $ENV{SNPSLMD_LICENSE_FILE} = '/SOMEWHERE/ELSE';

  # Check environment.
  $Context::java = `which java`;
  chomp $Context::java;
  if ($Context::java ne $expected_java) {
    print "\nWarning: Expecting \$PATH to find java at: '$expected_java', not '$Context::java'\n\n";
  }
  $Context::cmake = `which cmake`;
  chomp $Context::cmake;
  if ($Context::cmake ne $expected_cmake) {
    print "\nWarning: Expecting \$PATH to find cmake at: '$expected_cmake', not '$Context::cmake'\n\n";
  }


  # cd into repo dir.

  my $bin_dir = $FindBin::Bin;

  # Get the path to this executable (which is in bin dir).
  chdir $bin_dir or die "Couldn't cd to bin dir: $bin_dir.";

  # Validate dir.
  $bin_dir = `pwd`;
  chomp $bin_dir;
  my $dir = $bin_dir;
  ($dir =~ s|/bin$||) or die "Bug: /bin dir $dir doesn't look right.";

  # cd to requested dir (from /bin dir).
  chdir $dir or die "Couldn't chdir: $dir.";

  $Context::repo_dir = $dir;


  # Check for svgen.
  if ($svgen_needed) {
    # Make sure SVGen exists.
    (-e "target/svgen.jar") or die "SVGen has not been built.";
  }
}


# Get a test.
# Returns the structure from %Context::tests.
sub get_test {
  my $test_name = shift;
  if (!exists $Context::tests{$test_name}) { 
    print STDERR "Invalid test name: $test_name.\n";
    print STDERR "Recognized test names are:\n";
    foreach my $test (keys %Context::tests) {
      print STDERR "\t$test\n";
    }
    exit(2);
  }
  my $test_ref = $Context::tests{$test_name};
  if ( exists $$test_ref{path_env} &&
      !exists $ENV{$$test_ref{path_env}}) {
    print STDERR "Test $test_name cannot be accessed because environment variable $$test_ref{path_env} is not defined.\n";
    exit(2);
  }

  $Context::tests{$test_name};
}

# Make a directory if it doesn't already exist.
sub old_require_dir {
  my $dir = shift;
  if (!((-e $dir) or (mkdir $dir))) {
    my $tmp = `pwd`;
    print $tmp;
    die "Couldn't mkdir $dir: $!";
  }
}
sub require_dir {
  my $dir = shift;
  make_path($dir);
}



1;
