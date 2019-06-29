functor
import
   System(showInfo:Show)
define
   A=1
   B=A*2
   proc {Foo}
      A=B
   end
   fun {Bar}
      {Foo}
      42
   end
   {Show {Bar}}
end
