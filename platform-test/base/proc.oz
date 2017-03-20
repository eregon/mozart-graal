functor
export
   Return
define
   fun {Closure N}
      if N==0 then
         nil
      else
         fun {$} N end|{Closure N-1}
      end
   end

   Return='proc'([
         closure(proc {$}
            Xs={Closure 3}
            Applied={Map Xs fun {$ X} {X} end}
         in
            Applied = [3 2 1]
         end
         keys: [procedure capture])

         apply(proc {$}
            R
         in
            {Procedure.apply Number.'+' [1 2 R]}
            R = 3
         end
         keys: [procedure apply])
      ])
end
