// <Legal notice here>


`include "tlv.vh"





//
// Scope: |pipe0
//

// For signal "sig_a"
node sig_a_PIPE0_01H;
node sig_a_PIPE0_02H;
node sig_a_PIPE0_03H;
node sig_a_PIPE0_04H;
node sig_a_PIPE0_05H;
node sig_a_PIPE0_06H;
node sig_a_PIPE0_06L;

// For signal "sig_b"
node sig_b_PIPE0_04L;
node sig_b_PIPE0_05L;
node sig_b_PIPE0_06L;
node sig_b_PIPE0_07L;
node sig_b_PIPE0_08L;
node sig_b_PIPE0_09H;

// For signal "sig_c"
node sig_c_PIPE0_00H;
node sig_c_PIPE0_01H;
node sig_c_PIPE0_02H;
node sig_c_PIPE0_03H;
node sig_c_PIPE0_04H;
node sig_c_PIPE0_05H;
node sig_c_PIPE0_06H;
node sig_c_PIPE0_07H;
node sig_c_PIPE0_08H;
node sig_c_PIPE0_08L;

// For signal "sig_f"
type_t sig_f_PIPE0_01H;
type_t sig_f_PIPE0_02H;
type_t sig_f_PIPE0_03H;
type_t sig_f_PIPE0_04H;
type_t sig_f_PIPE0_04L;

// For signal "sig_g"
node sig_g_PIPE0_01H;
node sig_g_PIPE0_02H;
node sig_g_PIPE0_03H;
node sig_g_PIPE0_04H;
node sig_g_PIPE0_04L;

// For signal "valid"
node valid_PIPE0_00H;
node valid_PIPE0_01H;
node valid_PIPE0_02H;
node valid_PIPE0_03H;
node valid_PIPE0_03L;
node valid_PIPE0_04H;
node valid_PIPE0_04L;
node valid_PIPE0_05H;
node valid_PIPE0_05L;
node valid_PIPE0_06L;
node valid_PIPE0_07L;
node valid_PIPE0_08H;

// Clock signals.
logic Clk_valid_PIPE0_02H ;
logic Clk_valid_PIPE0_03H ;
logic Clk_valid_PIPE0_04H ;
logic Clk_valid_PIPE0_04L ;
logic Clk_valid_PIPE0_05H ;
logic Clk_valid_PIPE0_05L ;
logic Clk_valid_PIPE0_06H ;
logic Clk_valid_PIPE0_06L ;
logic Clk_valid_PIPE0_07L ;
logic Clk_valid_PIPE0_08L ;
logic Clk_valid_PIPE0_09H ;

generate



   //
   // Scope: |pipe0
   //


   // For signal "sig_a"
   always_ff @posedge(Clk_valid_PIPE0_02H) sig_a_PIPE0_02H <= sig_a_PIPE0_01H;
   always_ff @posedge(Clk_valid_PIPE0_03H) sig_a_PIPE0_03H <= sig_a_PIPE0_02H;
   always_ff @posedge(Clk_valid_PIPE0_04H) sig_a_PIPE0_04H <= sig_a_PIPE0_03H;
   always_ff @posedge(Clk_valid_PIPE0_05H) sig_a_PIPE0_05H <= sig_a_PIPE0_04H;
   always_ff @posedge(Clk_valid_PIPE0_06H) sig_a_PIPE0_06H <= sig_a_PIPE0_05H;
   always_latch if Clk_valid_PIPE0_06L sig_a_PIPE0_06L <= sig_a_PIPE0_06H;

   // For signal "sig_b"
   always_ff @posedge(Clk_valid_PIPE0_05L) sig_b_PIPE0_05L <= sig_b_PIPE0_04L;
   always_ff @posedge(Clk_valid_PIPE0_06L) sig_b_PIPE0_06L <= sig_b_PIPE0_05L;
   always_ff @posedge(Clk_valid_PIPE0_07L) sig_b_PIPE0_07L <= sig_b_PIPE0_06L;
   always_ff @posedge(Clk_valid_PIPE0_08L) sig_b_PIPE0_08L <= sig_b_PIPE0_07L;
   always_latch if Clk_valid_PIPE0_09H sig_b_PIPE0_09H <= sig_b_PIPE0_08L;

   // For signal "sig_c"
   always_ff @posedge(Clk_H) sig_c_PIPE0_01H <= sig_c_PIPE0_00H;
   always_ff @posedge(Clk_H) sig_c_PIPE0_02H <= sig_c_PIPE0_01H;
   always_ff @posedge(Clk_H) sig_c_PIPE0_03H <= sig_c_PIPE0_02H;
   always_ff @posedge(Clk_H) sig_c_PIPE0_04H <= sig_c_PIPE0_03H;
   always_ff @posedge(Clk_H) sig_c_PIPE0_05H <= sig_c_PIPE0_04H;
   always_ff @posedge(Clk_H) sig_c_PIPE0_06H <= sig_c_PIPE0_05H;
   always_ff @posedge(Clk_H) sig_c_PIPE0_07H <= sig_c_PIPE0_06H;
   always_ff @posedge(Clk_H) sig_c_PIPE0_08H <= sig_c_PIPE0_07H;
   always_latch if Clk_L sig_c_PIPE0_08L <= sig_c_PIPE0_08H;

   // For signal "sig_f"
   always_ff @posedge(Clk_valid_PIPE0_02H) sig_f_PIPE0_02H <= sig_f_PIPE0_01H;
   always_ff @posedge(Clk_valid_PIPE0_03H) sig_f_PIPE0_03H <= sig_f_PIPE0_02H;
   always_ff @posedge(Clk_valid_PIPE0_04H) sig_f_PIPE0_04H <= sig_f_PIPE0_03H;
   always_latch if Clk_valid_PIPE0_04L sig_f_PIPE0_04L <= sig_f_PIPE0_04H;

   // For signal "sig_g"
   always_ff @posedge(Clk_valid_PIPE0_02H) sig_g_PIPE0_02H <= sig_g_PIPE0_01H;
   always_ff @posedge(Clk_valid_PIPE0_03H) sig_g_PIPE0_03H <= sig_g_PIPE0_02H;
   always_ff @posedge(Clk_valid_PIPE0_04H) sig_g_PIPE0_04H <= sig_g_PIPE0_03H;
   always_latch if Clk_valid_PIPE0_04L sig_g_PIPE0_04L <= sig_g_PIPE0_04H;

   // For signal "valid"
   always_ff @posedge(Clk_H) valid_PIPE0_01H <= valid_PIPE0_00H;
   always_ff @posedge(Clk_H) valid_PIPE0_02H <= valid_PIPE0_01H;
   always_ff @posedge(Clk_H) valid_PIPE0_03H <= valid_PIPE0_02H;
   always_latch if Clk_L valid_PIPE0_03L <= valid_PIPE0_03H;
   always_latch if Clk_H valid_PIPE0_04H <= valid_PIPE0_03L;
   always_latch if Clk_L valid_PIPE0_04L <= valid_PIPE0_04H;
   always_latch if Clk_H valid_PIPE0_05H <= valid_PIPE0_04L;
   always_latch if Clk_L valid_PIPE0_05L <= valid_PIPE0_05H;
   always_ff @posedge(Clk_L) valid_PIPE0_06L <= valid_PIPE0_05L;
   always_ff @posedge(Clk_L) valid_PIPE0_07L <= valid_PIPE0_06L;
   always_latch if Clk_H valid_PIPE0_08H <= valid_PIPE0_07L;




endgenerate



//
// Gated clocks.
//

generate



   //
   // Scope: |pipe0
   //


   `GATER(Clk_valid_PIPE0_02H, Clk_H, 1'b1, valid_PIPE0_01H)
   `GATER(Clk_valid_PIPE0_03H, Clk_H, 1'b1, valid_PIPE0_02H)
   `GATER(Clk_valid_PIPE0_04H, Clk_H, 1'b1, valid_PIPE0_03H)
   `GATER(Clk_valid_PIPE0_04L, Clk_L, 1'b1, valid_PIPE0_03L)
   `GATER(Clk_valid_PIPE0_05H, Clk_H, 1'b1, valid_PIPE0_04H)
   `GATER(Clk_valid_PIPE0_05L, Clk_L, 1'b1, valid_PIPE0_04L)
   `GATER(Clk_valid_PIPE0_06H, Clk_H, 1'b1, valid_PIPE0_05H)
   `GATER(Clk_valid_PIPE0_06L, Clk_L, 1'b1, valid_PIPE0_05L)
   `GATER(Clk_valid_PIPE0_07L, Clk_L, 1'b1, valid_PIPE0_06L)
   `GATER(Clk_valid_PIPE0_08L, Clk_L, 1'b1, valid_PIPE0_07L)
   `GATER(Clk_valid_PIPE0_09H, Clk_H, 1'b1, valid_PIPE0_08H)



endgenerate



generate   // This is awkward, but we need to go into 'generate' context in the line that `includes the declarations file.
