%%%
%%% Authors:
%%%   Christian Schulte <schulte@ps.uni-sb.de>
%%%
%%% Copyright:
%%%   Christian Schulte, 1998
%%%
%%% Last change:
%%%   $Date$ by $Author$
%%%   $Revision$
%%%
%%% This file is part of Mozart, an implementation
%%% of Oz 3
%%%    http://www.mozart-oz.org/
%%%
%%% See the file "LICENSE" or
%%%    http://www.mozart-oz.org/LICENSE
%%% for information on usage and redistribution
%%% of this file, and for a DISCLAIMER OF ALL
%%% WARRANTIES.
%%%


functor

export
   worker: Process

import
   Remote(manager)
   Module(manager)

define

   class Process
      feat name id process
      meth init(name:Name fork:Fork id:Id)
         thread
            self.process = if Name==same then
                              {New Module.manager init}
                           else
                              {New Remote.manager init(host:Name fork:Fork)}
                           end
            self.name    = Name
            self.id      = Id
         end
      end
      meth plain(logger:L manager:M script:SF $)
         Id=self.id
      in
         {Wait self.name}
         {self.process apply(functor
                             import
                                Module
                                Worker(plain) at 'x-oz://system/ParWorker.ozf'
                             export
                                worker: W
                             define
                                %% Get the script module
                                [S] = if {Functor.is SF} then
                                         {Module.apply [SF]}
                                      else
                                         {Module.link [SF]}
                                      end
                                %% Start worker
                                W = {Worker.plain
                                     init(logger:  L
                                          manager: M
                                          id:      Id
                                          script:  S.script)}
                             end $)}.worker
      end
      meth best(logger:L manager:M script:SF $)
         Id=self.id
      in
         {Wait self.name}
         {self.process apply(functor
                             import
                                Module
                                Worker(best) at 'x-oz://system/ParWorker.ozf'
                             export
                                worker: W
                             define
                                %% Get the script module
                                [S] = if {Functor.is SF} then
                                         {Module.apply [SF]}
                                      else
                                         {Module.link [SF]}
                                      end
                                %% Start worker
                                W = {Worker.best
                                     init(logger:  L
                                          manager: M
                                          id:      Id
                                          order:   S.order
                                          script:  S.script)}
                             end $)}.worker
      end
      meth close
         if self.name\=same then
            {self.process close}
         end
      end
   end

end
