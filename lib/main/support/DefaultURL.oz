%%% -*- oz -*-
%%%
%%% Author:
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
%%%    http://www.mozart-oz.org
%%%
%%% See the file "LICENSE" or
%%%    http://www.mozart-oz.org/LICENSE.html
%%% for information on usage and redistribution
%%% of this file, and for a DISCLAIMER OF ALL
%%% WARRANTIES.
%%%

functor

require
   URL(make:    UrlMake
       resolve: UrlResolve)

prepare

   PickleExt   = '.ozp'
   FunctorExt  = '.ozf'
   OzScheme    = 'x-oz'
   % OzVersion = {Property.get 'oz.version'}

   HomeUrl    = {UrlMake 'x-oz://system'}
   ContribUrl = {UrlMake 'x-oz://contrib'}
   SystemUrl  = {UrlMake OzScheme#'://system/'}
   BootUrl    = {UrlMake OzScheme#'://system/'}

   proc {MakeUrlTable Xs ?R}
      R={MakeRecord table
         {Map Xs fun {$ X}
                    case X of A#_ then A else X end
                 end}}
      {ForAll Xs proc {$ X}
         case X of A#SubNamespace then
            R.A={UrlResolve {UrlMake OzScheme#'://system/'#SubNamespace#'/'}
                 {UrlMake A#FunctorExt}}
         else
            R.X={UrlResolve SystemUrl {UrlMake X#FunctorExt}}
         end
      end}
   end

   local
      Libs      = ['Application'
                   'Search' 'FD' 'Schedule' 'FS' 'Combinator'
                   'RecordC'
                   'Error' 'ErrorRegistry' 'Finalize' 'Service'
                   'Fault' 'Connection' 'Remote' 'VirtualSite' 'URL'
                   'DPStatistics' 'DP' 'Site'
                   'Discovery'
                   'Open'
                   'Tk' 'TkTools' 'Tix'
                   'Compiler' 'Macro'
                   'Type' 'Narrator' 'Listener' 'ErrorListener'
                   'DefaultURL' 'ObjectSupport'
\ifdef DENYS_EVENTS
                   'Timer' 'Perdio'
\endif
                  ]
      NatLibs   = ['Space' 'OsTime']
      Volatiles = ['Module'
                   'Resolve' 'OS' 'Property' 'Pickle' 'System'
\ifdef DENYS_EVENTS
                   'Event'
\endif
                  ]
      Tools     = ['OPI' 'OPIEnv' 'Panel' 'Browser' 'Explorer' 'CompilerPanel'
                   'EvalDialog'
                   'Emacs' 'Ozcar' 'OzcarClient'
                   'Profiler' 'Gump' 'GumpScanner'
                   'GumpParser' 'ProductionTemplates'
                   'Inspector' 'OPIServer' 'DistributionPanel'
                  ]
   in
      LibFuncs     = {MakeUrlTable Libs}
      NatLibFuncs  = {MakeUrlTable NatLibs}
      VolFuncs     = {MakeUrlTable Volatiles}
      ToolFuncs    = {MakeUrlTable Tools}
      SystemFuncs  = {FoldL [LibFuncs NatLibFuncs VolFuncs] Adjoin ToolFuncs}
      FunctorNames = {Map {FoldR [Libs NatLibs Volatiles Tools] Append nil}
                      fun {$ X} case X of A#_ then A else X end end}
   end




   local
      VS2A = VirtualString.toAtom

      fun {NewNameTest Table}
         fun {$ Name}
            {HasFeature Table {VS2A Name}}
         end
      end
   in
      fun {NameToUrl Name}
         NameA = {VS2A Name}
      in
         if {HasFeature SystemFuncs NameA} then
            SystemFuncs.NameA
         else
            {UrlMake Name#FunctorExt}
         end
      end

      IsLibName      = {NewNameTest LibFuncs}
      IsNatLibName   = {NewNameTest NatLibFuncs}
      IsVolatileName = {NewNameTest VolFuncs}
      IsToolsName    = {NewNameTest ToolFuncs}
      IsSystemName   = {NewNameTest SystemFuncs}
   end


export
   NameToUrl

   IsSystemName
   IsLibName
   IsNatLibName
   IsVolatileName
   IsToolsName

   HomeUrl
   ContribUrl
   SystemUrl
   BootUrl

   OzScheme
   % OzVersion

   PickleExt
   FunctorExt

   FunctorNames
end
