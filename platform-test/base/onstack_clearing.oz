functor
import
   Debug(onStack:OnStack cleared:Cleared) at 'x-oz://Boot/Debug'
export
   Return
define
   Return='onstack_clearing'([
		      simplelocals(proc {$}
                     local A B C D in
                        1={OnStack 'B'}
                        0={OnStack 'D'}
                        B = D
                        1={Cleared 'D'}
                        0={Cleared 'B'}
                        B = 3
                        1={Cleared 'B'}

                        1={OnStack 'A'}
                        A=3
                        0={Cleared 'A'}
                        A=3
                        1={Cleared 'A'}
                     end
                     skip
				      end
					 keys: [static_analysis]
				     )
           branches(proc {$}
                     local A B C D in
                        {OnStack 'A' 0}
                        {OnStack 'B' 1}
                        {OnStack 'D' 0}
                        {OnStack 'C' 0}
                        if true then
                           {Cleared 'A' 0}
                           {Cleared 'D' 0}
                           proc{B}
                              A = 3
                           end
                           C=4
                           {Cleared 'A' 1}
                           {Cleared 'B' 1}
                           {Cleared 'C' 1}
                           {Cleared 'D' 0}
                        else
                           case x of x then
                              skip
                           [] y then C = 5 end
                           B=5
                           D=4
                        end
                        D=5
                     end
                     skip
                  end
                  keys: [static_analysis])
           exceptions1(proc {$}
                     local A B C D in
                        {OnStack 'A' 0}
                        {OnStack 'B' 0}
                        try
                           Z=3 in
                           A=3
                           D=3
                           C=2
                           {Cleared 'D' 0}
                           {Cleared 'Z' 1}
                           {Cleared 'C' 1}
                           raise x end
                        catch x then
                           {Cleared 'B' 0}
                           B=3
                           D=3
                           {Cleared 'A' 1}
                        end
                        {Cleared 'B' 1}
                     end
                     skip
                  end
                  keys: [static_analysis])
           exceptions2(proc {$}
                     local A B C D in
                        {OnStack 'A' 0}
                        {OnStack 'B' 1}
                        {OnStack 'C' 1}
                        {OnStack 'D' 0}
                        C = 3
                        try X in
                           C = D
                           {Cleared 'C' 1}
                           raise lol end
                           D=3
                           1 = A
                           A = X
                        catch _ then
                           {Cleared 'D' 1}
                           {Cleared 'A' 0}
                           {Cleared 'X' 1}
                           A = 3
                           {Cleared 'A' 1}
                        end
                     end
                     skip
                  end
                  keys: [static_analysis])
           ])
end

