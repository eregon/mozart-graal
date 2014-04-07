%% Copyright © 2011, Université catholique de Louvain
%% All rights reserved.
%%
%% Redistribution and use in source and binary forms, with or without
%% modification, are permitted provided that the following conditions are met:
%%
%% *  Redistributions of source code must retain the above copyright notice,
%%    this list of conditions and the following disclaimer.
%% *  Redistributions in binary form must reproduce the above copyright notice,
%%    this list of conditions and the following disclaimer in the documentation
%%    and/or other materials provided with the distribution.
%%
%% THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
%% AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
%% IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
%% ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
%% LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
%% CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
%% SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
%% INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
%% CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
%% ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
%% POSSIBILITY OF SUCH DAMAGE.

functor

require
   Boot_VM at 'x-oz://boot/VM'
   Reflection at 'x-oz://boot/Reflection'

import
   Pickle

export
   Ncores
   Current
   New
   GetStream

define
   proc {ReflectiveVM VM S}
      Msg#Ack|Tail = S
   in
      case Msg
      of send(V) then
         VS = {Pickle.pack V}
      in
         {Send VM VS}
         Ack = unit
      else
         Ack = false
      end
      {ReflectiveVM VM Tail}
   end

   fun {NewReflectiveVM VM}
      S
   in
      thread {ReflectiveVM VM S} end
      {Reflection.newReflectiveEntity S}
   end

   Ncores = Boot_VM.ncores

   fun {Current}
      VM = {Boot_VM.current}
   in
      {NewReflectiveVM VM}
   end

   fun {New AppURL}
      VM = {Boot_VM.new AppURL}
   in
      {NewReflectiveVM VM}
   end

   GetStream = Boot_VM.getStream
end
