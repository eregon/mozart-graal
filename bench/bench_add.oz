functor
import
   Application
   Harness(bench:Bench) at 'harness.ozf'
define
   `$N`=2000000

   fun {BenchAdd A B}
      if A==0 then
         B
      else
         {Add A-1 B+1}
      end
   end

   {Bench fun {$} {BenchAdd `$N` 0} end}

   {Application.exit 0}
end
