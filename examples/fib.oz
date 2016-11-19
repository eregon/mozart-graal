functor
import
   System(showInfo:Show)
   Boot_Time at 'x-oz://boot/Time'
define
   fun {Fib N}
      if N =< 1 then
         N
      else
         {Fib N-1} + {Fib N-2}
      end
   end

   for I in 1..10 do
      local
         T0={Boot_Time.getMonotonicTime}
         {Show {Fib 30}}
         T1={Boot_Time.getMonotonicTime}
      in
         {Show (T1-T0) div 1000000}
      end
   end
end
