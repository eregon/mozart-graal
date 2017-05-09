functor
import System
define
   S = pong|_

   proc {Ping S I}
      case S of pong|A then
         A = ping|_
         {Ping A.2 I+1}
      end
   end

   proc {Pong S I}
      case S of ping|A then
         A = pong|_
         if I mod 1000 == 0 then
            {System.showInfo I}
         end
         {Pong A.2 I+1}
      end
   end

   thread {Ping S 0} end
   thread {Pong S.2 1} end

   {Wait _}
end
