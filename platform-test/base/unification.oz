functor
import
   System(show:Show)
export
   Return
define
   proc{MustFail X P}
      skip
      try
         {P}
         raise {VirtualString.toAtom '  '#X#' should have failed'} end
      catch failure(...) then
         skip
      end
   end
   Return='unification'([
      cyclic( % unification should work with cyclic structures
         proc{$}
            A = 1|2|A
            B = 1|2|1|2|B
            C = '|'(1 '|'(2 C))
            
            D = rec(D d)
            E = rec(E e)

            M = rec(a rec(b rec(m)))
            N = rec(a rec(b rec(n)))
            
            F = rec(G F)
            G = rec(F G)
         in
            A = B
            A = C
            B = C
            F = G

            (A==B) = true
            (A==C) = true
            (B==C) = true
            (F==G) = true

            {MustFail 'D=E' proc{$} D=E end}
            (D==E) = false

            {MustFail 'M=N' proc{$} M=N end}
            (M==N) = false
         end
         keys: [cyclic equality unification cons]
      )
      pattern_matching( % Cyclic patterns are only possible using variables which
         % are handled through equality, so it should not cause problems.
         proc{$}
            A = rec(A 1)
            B = rec(B 2)

            C = 1|C
            D = 1|D
            X = 1|nil
         in
            case rec(B 1) of !A then
               raise '  B not equal to A' end
            else
               case rec(B 2) of !B then
                  skip
               else
                  raise '  recursive pattern fails' end
               end
            end
            
            case A of rec(!A 1) then
               case B of rec(!B 2) then
                  skip
               else
                  raise '  !B in pattern fails' end
               end
            else
               raise '  !A in pattern fails' end
            end

            case C of 1|!C then
               skip
            else
               raise '  !C in cons pattern fails' end
            end
         end
         keys: [pattern_matching cyclic]
      )
      respecialization( % unification site should guard equality call correctly
         % Equality only if A and B are not variables and A and B are not both records
         proc{$}
            proc{Unify A B}
               A = B
            end
            A
         in
            {Unify 2 2}
            {MustFail 'equality specialization' proc{$} {Unify rec(A) 2} end}
         end
         keys: [truffle unification]
      )
   ])
end
