functor
import
   Application
   Harness(bench:Bench) at 'harness.ozf'
define
   `$N`=1000

   fun {BenchFlatten Xs}
      {Flatten Xs}
   end

   fun {GenList Elements Depth}
      if Depth==1 then
         Elements
      else
         {Map Elements fun {$ X} {GenList Elements Depth-1} end}
      end
   end

   L={Map {List.number 1 `$N` 1} fun {$ X} {GenList [1 2 3] 5} end}
   % L={Map {List.number 1 `$N`*100 1} fun {$ X} {GenList [X X X X X] 1} end}

   {Bench fun {$} {BenchFlatten L} end}

   {Application.exit 0}
end
