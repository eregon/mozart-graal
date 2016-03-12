%%%
%%% Authors:
%%%   Michael Mehl (mehl@dfki.de)
%%%
%%% Copyright:
%%%   Michael Mehl, 1998
%%%
%%% Last change:
%%%   $Date$ by $Author$
%%%   $Revision$
%%%
%%% This file is part of Mozart, an implementation
%%% of Oz 3
%%%    http://www.mozart-oz.org
%%%
%%% See the file "LICENSE" or
%%%    http://www.mozart-oz.org/LICENSE.html
%%% for information on usage and redistribution
%%% of this file, and for a DISCLAIMER OF ALL
%%% WARRANTIES.
%%%

functor

export
   Return
import
   System RecordC(width: WidthC)

define
   Return =

   records(proc {$}
              proc {Eq X Y} if X==Y then skip else {System.show eq(X Y)} fail end end
           in
% ^
              {Eq f(a)^1 a}

              local X=f(...) in
                 X^1=67
                 {Eq X.1 67}
                 {Eq {Label X} f}
              end

% `.` suspends for Open
              local X=f(...) in
                 thread {Eq X.a i} end
                 X^a=i
              end

% bigint feature
              local
                 X = {Pow 100 10}
                 XX = {Pow 100 10}
                 Y=X+1
              in
                 {Eq a(X:a ...).X a}
                 local
                    R = a(X:a Y:b ...)
                 in
                    {Eq R.X a}
                    {Eq R.Y b}
                 end
                 {Eq a(X:a ...).XX a}
                 {Eq a(X:a ...).X a}
                 {Eq a(XX:a ...).X a}

                 local
                    R1=a(X:a ...) R2=a(XX:a ...)
                 in
                    {WidthC R1 1}
                    {WidthC R2 1}
                    {Eq R1 R2}
                 end
              end
           end
           keys:[module record])
end
