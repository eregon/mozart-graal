%%%
%%% Authors:
%%%   Benoit Daloze
%%%
%%% Copyright:
%%%   Benoit Daloze, 2014
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
import
   Application
   System(showInfo:Info show:Show)
   Module
define
   proc {NewLine}
      {Info ''}
   end

   fun {TestProcedure TestDesc}
      Test = TestDesc.1
      Keys = TestDesc.keys
   in
      if {Member space Keys} then
         proc {$} raise skipped(space) end end
      elseif {IsProcedure Test} then
         case {Procedure.arity Test}
         of 0 then Test
         [] 1 then proc {$} {Test} = true end
         end
      else
         equal(F Expected) = Test
      in
         proc {$}
            {F} = Expected
         end
      end
   end

   TestFiles = {Application.getArgs plain}
   ExitCode = {NewCell 0}

   for File in TestFiles do
      Applied = {Module.link [File]}.1
      Return = Applied.return
      TestCase = {Label Return}
      Tests = if {IsList Return.1} then Return.1 else [Return] end
   in
      {Info 'Testing '#TestCase}

      for Test in Tests do
         {Info {Label Test}}
         ActualTest = {TestProcedure Test}
      in
         try
            {ActualTest}
            {Info '  OK'}
         catch E then
            case E
            of skipped(Key) then
               {Info '  skipped ('#Key#')'}
            else
               ExitCode := 1
               {Show E}
            end
         end
      end
      {NewLine}
   end

   {Application.exit @ExitCode}
end
