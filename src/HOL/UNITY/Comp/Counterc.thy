(*  Title:      HOL/UNITY/Counterc
    ID:         $Id$
    Author:     Sidi O Ehmety, Cambridge University Computer Laboratory
    Copyright   2001  University of Cambridge

A family of similar counters, version with a full use of "compatibility "

From Charpentier and Chandy,
Examples of Program Composition Illustrating the Use of Universal Properties
   In J. Rolim (editor), Parallel and Distributed Processing,
   Spriner LNCS 1586 (1999), pages 1215-1227.
*)

Counterc =  UNITY_Main +

types state
arities state :: type

consts
  C :: "state=>int"
  c :: "state=>nat=>int"

consts  
  sum  :: "[nat,state]=>int"
  sumj :: "[nat, nat, state]=>int"

primrec (* sum I s = sigma_{i<I}. c s i *)
  "sum 0 s = 0"
  "sum (Suc i) s = (c s) i + sum i s"

primrec
  "sumj 0 i s = 0"
  "sumj (Suc n) i s = (if n=i then sum n s else (c s) n + sumj n i s)"
  
types command = "(state*state)set"

constdefs
  a :: "nat=>command"
 "a i == {(s, s'). (c s') i = (c s) i + 1 & (C s') = (C s) + 1}"
 
  Component :: "nat => state program"
  "Component i == mk_total_program({s. C s = 0 & (c s) i = 0},
				   {a i},
 	                           \\<Union>G \\<in> preserves (%s. (c s) i). Acts G)"
end  
