%%%
%%% Authors:
%%%   Martin Henz (henz@iscs.nus.edu.sg)
%%%   Christian Schulte <schulte@ps.uni-sb.de>
%%%
%%% Copyright:
%%%   Martin Henz, 1997
%%%   Christian Schulte, 1997
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

%%
%% Module
%%
Value = value(wait:            Wait
              waitQuiet:       WaitQuiet
              waitOr:          WaitOr

              '=<':            Boot_Value.'=<'
              '<':             Boot_Value.'<'
              '>=':            Boot_Value.'>='
              '>':             Boot_Value.'>'
              '==':            Boot_Value.'=='
              '=':             proc {$ X Y} X = Y end
              '\\=':           Boot_Value.'\\='
              max:             Max
              min:             Min

              '.':             Boot_Value.'.'
              hasFeature:      HasFeature
              condSelect:      CondSelect

              isFree:          IsFree
              isKinded:        IsKinded
              isFuture:        IsFuture
              isFailed:        IsFailed
              isDet:           IsDet

              status:
                 fun {$ X}
                    R = {Boot_Value.status X}
                 in
                    case R
                    of det(T) then T = {Value.type X}
                    else skip
                    end
                    R
                 end

              type:
                 fun {$ X}
                    R = {Boot_Value.type X}
                 in
                    if R \= chunk then R
                    elseif {IsPort X} then port
                    elseif {IsLock X} then 'lock'
                    elseif {IsClass X} then 'class'
                    elseif {IsBitArray X} then bitArray
                    elseif {IsBitString X} then bitString
                    else R
                    end
                 end

              isNeeded:        IsNeeded
              waitNeeded:      WaitNeeded
              makeNeeded:      MakeNeeded
              byNeed:          ByNeed
              byNeedFuture:    ByNeedFuture
              byNeedDot:       ByNeedDot

              '!!':            Boot_Value.readOnly
              byNeedFail:      FailedValue
              failed:          FailedValue

              toVirtualString:
                 fun {$ X Depth Width}
                    {Boot_System.getRepr X Depth Width}
                 end
             )
