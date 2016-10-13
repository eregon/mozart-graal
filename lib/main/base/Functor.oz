%%%
%%% Author:
%%%   Leif Kornstaedt <kornstae@ps.uni-sb.de>
%%%
%%% Copyright:
%%%   Leif Kornstaedt, 1998-1999
%%%
%%% Last change:
%%%   $Date$ by $Author$
%%%   $Revision$
%%%
%%% This file is part of Mozart, an implementation of Oz 3:
%%%   http://www.mozart-oz.org
%%%
%%% See the file "LICENSE" or
%%%   http://www.mozart-oz.org/LICENSE.html
%%% for information on usage and redistribution
%%% of this file, and for a DISCLAIMER OF ALL
%%% WARRANTIES.
%%%

local
   FunctorID = {NewUniqueName functorID}

   fun {IsFunctor X}
      {IsChunk X} andthen {HasFeature X FunctorID}
   end

   fun {NewFunctor Import Export Apply}
      % TODO assert that the arguments have the expected types
      {NewChunk f(FunctorID: unit
                  'import': Import
                  'export': Export
                  'apply': Apply)}
   end
in
   Functor = 'functor'(is:  IsFunctor
                       new: NewFunctor)
end
