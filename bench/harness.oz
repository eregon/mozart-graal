functor
import
   OS
   Boot_Time at 'x-oz://boot/Time'
   System(showInfo:ShowInfo)
export
   measure: Measure
   bench: Bench
define
   GetTime
   Diff
   if {HasFeature Boot_Time getMonotonicTime} then % Mozart 2, Mozart-Graal
      GetTime = Boot_Time.getMonotonicTime
      fun {Diff X Y}
         Y - X
      end
   else
      % For Mozart 1, precise to around ~1ms.
      % Better than {GetProperty 'time.total'}, which is only precise to 10ms.
      fun {GetTime}
         Stdout
      in
         {OS.pipe date ["+%s%N"] _ Stdout#_}
         Stdout
      end
      fun {Diff StdoutX StdoutY}
         OutX OutY
      in
         {OS.wait _ _}
         {OS.wait _ _}
         {OS.read StdoutX 30 OutX nil _}
         {OS.read StdoutY 30 OutY nil _}
         {OS.close StdoutX}
         {OS.close StdoutY}
         {StringToInt OutY} - {StringToInt OutX}
      end
   end

   fun {Measure F}
      T0 = {GetTime}
      R={F}
      T1 = {GetTime}
   in
      {ShowInfo ({Diff T0 T1} div 1000000)}
      R
   end

   Iterations=50
   proc {Bench F}
      R={NewCell nil}
   in
      for I in 1..Iterations do
         R := nil % Let last result GC
         R := {Measure F}
      end
   end
end
