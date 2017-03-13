// <Legal notice here>


`include "tlv.vh"


genvar inst;


//
// Scope: |pipe3
//

// For signal "add"
node add_PIPE3_00H;
node add_PIPE3_01H;
node add_PIPE3_02H;

// For signal "best"
node best_PIPE3_01H;
node best_PIPE3_02H;
node best_PIPE3_03H;
node best_PIPE3_04H;
node best_PIPE3_05H;

// For signal "call"
node call_PIPE3_02H;
node call_PIPE3_03H;
node call_PIPE3_04H;
node call_PIPE3_05H;

// For signal "dist"
node dist_PIPE3_02H;
node dist_PIPE3_03H;
node dist_PIPE3_04H;

// For signal "each"
node each_PIPE3_03H;

// For signal "flow"
node flow_PIPE3_04H;

// For signal "git"
node git_PIPE3_05H;


//
// Scope: |pipe4
//

// For signal "flow"
node flow_PIPE4_01H;

// For signal "jump"
node jump_PIPE4_01H;


//
// Scope: |pipe6
//

// For signal "blah"
node blah_PIPE6_00H;


//
// Scope: >inst[3:0]
//

// For signal "mem_addr"
node [3:0] [50:0] mem_addr_PIPE6_Inst_03L;
node [3:0] [50:0] mem_addr_PIPE6_Inst_04L;

// For signal "mem_addr_plus1"
node [3:0] [50:0] mem_addr_plus1_PIPE6_Inst_04L;

// For signal "mem_op"
node [3:0] mem_op_PIPE6_Inst_01H;
node [3:0] mem_op_PIPE6_Inst_02H;
node [3:0] mem_op_PIPE6_Inst_03H;

// For signal "op_a"
node [3:0] op_a_PIPE6_Inst_03L;

// For signal "raw_inst"
node [3:0] raw_inst_PIPE6_Inst_01H;
node [3:0] raw_inst_PIPE6_Inst_02H;
node [3:0] raw_inst_PIPE6_Inst_03H;
node [3:0] raw_inst_PIPE6_Inst_03L;

// For signal "valid"
node [3:0] valid_PIPE6_Inst_00H;
node [3:0] valid_PIPE6_Inst_01H;
node [3:0] valid_PIPE6_Inst_02H;
node [3:0] valid_PIPE6_Inst_03H;

// For signal "valid_mem_op"
node [3:0] valid_mem_op_PIPE6_Inst_03H;
node [3:0] valid_mem_op_PIPE6_Inst_03L;

// Clock signals.
logic Clk_valid_mem_op_PIPE6_Inst_04L [3:0];

//
// Scope: >deep
//

// For signal "test"
node [3:0] test_PIPE6_Inst_Deep_01H;

// For signal "valid"
node [3:0] valid_PIPE6_Inst_Deep_01H;


//
// Scope: |pipe7
//

// For signal "foo"
node foo_PIPE7_01H;


generate



   //
   // Scope: |pipe3
   //


   // For signal "add"
   always_ff @posedge(Clk_H) add_PIPE3_01H <= add_PIPE3_00H;
   always_ff @posedge(Clk_H) add_PIPE3_02H <= add_PIPE3_01H;

   // For signal "best"
   always_ff @posedge(Clk_H) best_PIPE3_02H <= best_PIPE3_01H;
   always_ff @posedge(Clk_H) best_PIPE3_03H <= best_PIPE3_02H;
   always_ff @posedge(Clk_H) best_PIPE3_04H <= best_PIPE3_03H;
   always_ff @posedge(Clk_H) best_PIPE3_05H <= best_PIPE3_04H;

   // For signal "call"
   always_ff @posedge(Clk_H) call_PIPE3_03H <= call_PIPE3_02H;
   always_ff @posedge(Clk_H) call_PIPE3_04H <= call_PIPE3_03H;
   always_ff @posedge(Clk_H) call_PIPE3_05H <= call_PIPE3_04H;

   // For signal "dist"
   always_ff @posedge(Clk_H) dist_PIPE3_03H <= dist_PIPE3_02H;
   always_ff @posedge(Clk_H) dist_PIPE3_04H <= dist_PIPE3_03H;

   // For signal "each"

   // For signal "flow"

   // For signal "git"



   //
   // Scope: |pipe4
   //


   // For signal "flow"

   // For signal "jump"



   //
   // Scope: |pipe6
   //


   // For signal "blah"


      //
      // Scope: >inst[3:0]
      //

      for (inst = 0; inst <= 3; inst++) begin
      // For signal "mem_addr"
      always_ff @posedge(Clk_valid_mem_op_PIPE6_Inst_04L[inst]) mem_addr_PIPE6_Inst_04L[inst][50:0] <= mem_addr_PIPE6_Inst_03L[inst][50:0];

      // For signal "mem_addr_plus1"

      // For signal "mem_op"
      always_ff @posedge(Clk_V_sv_valH) mem_op_PIPE6_Inst_02H[inst] <= mem_op_PIPE6_Inst_01H[inst];
      always_ff @posedge(Clk_V_sv_valH) mem_op_PIPE6_Inst_03H[inst] <= mem_op_PIPE6_Inst_02H[inst];

      // For signal "op_a"

      // For signal "raw_inst"
      always_ff @posedge(Clk_V_sv_valH) raw_inst_PIPE6_Inst_02H[inst] <= raw_inst_PIPE6_Inst_01H[inst];
      always_ff @posedge(Clk_V_sv_valH) raw_inst_PIPE6_Inst_03H[inst] <= raw_inst_PIPE6_Inst_02H[inst];
      always_latch if Clk_V_sv_valL raw_inst_PIPE6_Inst_03L[inst] <= raw_inst_PIPE6_Inst_03H[inst];

      // For signal "valid"
      always_ff @posedge(Clk_H) valid_PIPE6_Inst_01H[inst] <= valid_PIPE6_Inst_00H[inst];
      always_ff @posedge(Clk_H) valid_PIPE6_Inst_02H[inst] <= valid_PIPE6_Inst_01H[inst];
      always_ff @posedge(Clk_H) valid_PIPE6_Inst_03H[inst] <= valid_PIPE6_Inst_02H[inst];

      // For signal "valid_mem_op"
      always_latch if Clk_L valid_mem_op_PIPE6_Inst_03L[inst] <= valid_mem_op_PIPE6_Inst_03H[inst];


         //
         // Scope: >deep
         //


         // For signal "test"

         // For signal "valid"


      end


   //
   // Scope: |pipe7
   //


   // For signal "foo"




endgenerate



//
// Gated clocks.
//

generate


`GATER(Clk_V_sv_valH, Clk_H, 1'b1, sv_val)
`GATER(Clk_V_sv_valL, Clk_L, 1'b1, sv_val)

   //
   // Scope: |pipe3
   //




   //
   // Scope: |pipe4
   //




   //
   // Scope: |pipe6
   //



      //
      // Scope: >inst[3:0]
      //

      for (inst = 0; inst <= 3; inst++) begin
      `GATER(Clk_valid_mem_op_PIPE6_Inst_04L[inst], Clk_L, 1'b1, valid_mem_op_PIPE6_Inst_03L[inst])

         //
         // Scope: >deep
         //



      end


   //
   // Scope: |pipe7
   //





endgenerate



generate   // This is awkward, but we need to go into 'generate' context in the line that `includes the declarations file.
