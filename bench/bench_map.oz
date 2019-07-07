functor
import
   Application
   Harness(bench:Bench) at 'harness.ozf'
define
   `$N`=1000000

   fun {BenchMap Xs}
      {Map Xs fun {$ X} X * 11 end}
   end

   L={List.number 1 `$N` 1}

   {Bench fun {$} {BenchMap L} end}

   {Application.exit 0}
end
