%%%
%%% Authors:
%%%   Konstantin Popov
%%%
%%% Copyright:
%%%   Konstantin Popov, 1997
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

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%
%%%
%%%   Core window manager;
%%%
%%%
%%%
%%%

%%
class WindowManagerClass from MyClosableObject BatchObject
   %%
   feat
   %% There is a number of object-depended procedures:
      Entry2Path                %  (see beneath;)
      Button2Path               %
      CleanUp                   %

   %%
   attr
      window:      InitValue    %  the window object itself;
   %%
      actionVar:   InitValue    %  tcl's variable;
      actions:     InitValue    %  dictionary of actions;
      nextANumber: InitValue    %  ... and a next free index in it;

   %%
   %%
   meth initWindow
      local Actions in
         Actions = {Dictionary.new}

         %%
         %% This procedure maps (abstract) entries to "full-qualified"
         %% paths to corresponding 'menubar' entries;
         self.Entry2Path =
         fun {$ Entry}
            case Entry
            of break             then browser(break)
            [] unselect          then browser(unselect)
            [] toggleMenus       then browser(toggleMenus)
            [] about             then browser(about)
            [] refineLayout       then browser(refineLayout)
            [] close             then browser(close)
            [] clear             then browser(clear)
            [] clearAllButLast   then browser(clearAllButLast)
            [] expand            then selection(expand)
            [] shrink            then selection(shrink)
            [] deref             then selection(deref)
            [] rebrowse          then selection(rebrowse)
            [] process           then selection(process)
            else InitValue
            end
         end

         %%
         self.Button2Path =
         fun {$ Button}
            case Button
            of break             then break
            else InitValue
            end
         end

         %%
         self.CleanUp = fun {$ A} A \= InitValue end

         %%
         {Dictionary.put Actions 0 r(action:System.show
                                     label:'Show'
                                     number:0)} % must be 0th;
         {Dictionary.put Actions 1 r(action:Browse
                                     label:'Browse'
                                     number:1)} % must be 1st!
         {self.store store(StoreProcessAction Browse)}
         nextANumber <- 2       % two pre-defined actions;
         actions <- Actions
      end
   end

   %%
   %%
   meth createWindow
\ifdef DEBUG_WM
      {System.show 'WindowManagerClass::createWindow is applied'}
\endif
      %%
      if @window == InitValue then
         %%
         %%  This guy produces a top-level window without a menubar;
         window <- {New BrowserWindowClass
                    init(window: {self.store read(StoreOrigWindow $)}
                         screen: {self.store read(StoreScreen $)}
                         browserObj: self.browserObj
                         store: self.store)}

         %%
         {@window [setMinSize expose setWaitCursor]}

         %%
         {self.store store(StoreIsWindow true)}
      end

      %%
\ifdef DEBUG_WM
      {System.show 'WindowManagerClass::createWindow is finished'}
\endif
   end

   %%
   %%
   meth createMenus
\ifdef DEBUG_WM
      {System.show 'WindowManagerClass::createMenus is applied'}
\endif
      %%
      if {self.store read(StoreAreMenus $)} then skip
      else Store BO Window Menus ActionVar Actions CurrAction in
         %%
         Store = self.store
         BO = self.browserObj
         Window = @window
         Actions = @actions
         CurrAction = {Store read(StoreProcessAction $)}

         %%
         %%  All the elements of the menubar
         %%  (The 'TkTools.menubar' is used);
         Menus =
         [menubutton(text: 'Browser'
                     menu: [%%
                            command(label:   'About...'
                                    action:  BO#About
                                    feature: about)
                            separator

                            %%
                            command(label:   'Break'
                                    % key:     ctrl(c)
                                    acc:     '     C-c'
                                    action:  BO#break
                                    feature: break)
                            command(label:   'Deselect'
                                    action:  BO#UnsetSelected
                                    feature: unselect)
                            separator

                            %%
%                           command(label:   'Toggle Menus'
%                                   % key:     ctrl(alt(m))
%                                   acc:     '   C-A-m'
%                                   action:  BO#toggleMenus
%                                   feature: toggleMenus)
%                           separator

                            %%
                            command(label:   'Clear'
                                 % key:     ctrl(u)
                                    acc:     '     C-u'
                                    action:  BO#clear
                                    feature: clear)
                            command(label:   'Clear All But Last'
                                    acc:     '     C-w'
                                    action:  BO#clearAllButLast
                                    feature: clearAllButLast)
                            separator

                            %%
                            command(label:   'Refine Layout'
                                    % key:     ctrl(l)
                                    acc:     '     C-l'
                                    action:  BO#refineLayout
                                    feature: refineLayout)
                            separator

                            %%
                            command(label:   'Close'
                                    % key:     ctrl(x)
                                    acc:     '     C-x'
                                    action:  BO#close
                                    feature: close)]
                     feature: browser)

          %% 'Selection' menu;
          menubutton(text: 'Selection'
                     menu: [%%
                            command(label:   'Expand'
                                    % key:     e
                                    acc:     '       e'
                                    action:  BO#SelExpand
                                    feature: expand)
                            command(label:   'Shrink'
                                    % key:     s
                                    acc:     '       s'
                                    action:  BO#SelShrink
                                    feature: shrink)
                            separator

                            %%
                            command(label:   'Deref'
                                    % key:     d
                                    acc:     '       d'
                                    action:  BO#SelDeref
                                    feature: deref)
                            separator

                            %%
                            command(label:   'Rebrowse'
                                    % key:     ctrl(b)
                                    acc:     '     C-b'
                                    action:  BO#rebrowse
                                    feature: rebrowse)
                            separator

                            %%
                            cascade(label:   'Set Action'
                                    menu:    nil
                                    feature: action)
                            command(label:   'Apply Action'
                                    % key:     ctrl(p)
                                    acc:     '     C-p'
                                    action:  BO#Process
                                    feature: process)]
                     feature: selection)

          %% 'Buffer' menu;
          menubutton(text: 'Options'
                     menu:
                        [%%
                         command(label:'Buffer...'
                                 action: self #
                                 guiOptions(buffer))
                         command(label:'Representation...'
                                 action: self #
                                 guiOptions(representation))
                         command(label:'Display Parameters...'
                                 action: self #
                                 guiOptions(display))
                         command(label:'Layout...'
                                 action: self #
                                 guiOptions(layout))]
                     feature: options)

          %%
         ]

         %%
         %%  create & pack it;
         {Window [createMenuBar(Menus)
                  pushButton(break(%%
                                   % text:   'Break'
                                   action: BO#break
                                   bitmap: IStopBitmap
                                   bd: 0
                                   anchor: center
                                   width: IStopWidth
                                   fg:    IStopFG
                                   activeforeground:IStopAFG)
                             "Break")
                  createTkVar({List.foldL {Dictionary.keys Actions}
                               fun {$ I K}
                                  Action = {Dictionary.get Actions K}
                               in
                                  if Action.action == CurrAction
                                  then Action.number
                                  else I
                                  end
                               end
                               1}       % that's the 'Browse' action;
                              proc {$ V}
                                 Action = {Dictionary.get Actions
                                           {String.toInt V}}.action
                              in
                                 %%
                                 {Store store(StoreProcessAction Action)}
                              end
                              ActionVar)
                  exposeMenuBar]}
         actionVar <- ActionVar

         %%
         %%  everything else is done asynchronously;
         thread
            %%
            %% no "tear-off" menus;
            {Window noTearOff([browser(menu)
                               selection(menu)
                               selection(action(menu))
                               options(menu)])}

            %%
            %% Key bindings;
            {Window [bindKey(key: ctrl(c)      action: BO#break)
                     bindKey(key: ctrl(b)      action: BO#rebrowse)
                     bindKey(key: ctrl(p)      action: BO#Process)
                     bindKey(key: ctrl(l)      action: BO#refineLayout)
                     bindKey(key: ctrl(alt(m)) action: BO#toggleMenus)
                     bindKey(key: ctrl(h)      action: BO#About)
                     bindKey(key: ctrl(x)      action: BO#close)
                     bindKey(key: ctrl(u)      action: BO#clear)
                     bindKey(key: ctrl(w)      action: BO#clearAllButLast)
                     bindKey(key: d            action: BO#SelDeref)
                     bindKey(key: e            action: BO#SelExpand)
                     bindKey(key: s            action: BO#SelShrink)]}

            %%
            {ForAll {Sort {Dictionary.keys Actions} Value.'<'}
             proc {$ K}
                {Window addRadioEntry(selection(action(menu))
                                      {Dictionary.get Actions K}.label
                                      ActionVar K)}
             end}

            %%
         end

         %%
         if @window.standAlone then {@window setMinSize}
         else WindowManagerClass , entriesDisable([close])
         end

         %%
         {self.store store(StoreAreMenus true)}
      end

      %%
\ifdef DEBUG_WM
      {System.show 'WindowManagerClass::createMenus is finished'}
\endif
   end

   %%
   %%
   meth guiOptions(What)
      {Wait {New case What
                 of buffer then BufferDialog
                 [] representation then RepresentationDialog
                 [] display then DisplayDialog
                 [] layout then LayoutDialog
                 end
             init(windowObj: @window)}.closed}
   end

   %%
   %%
   meth resetWindowSize
\ifdef DEBUG_WM
      {System.show 'WindowManagerClass::resetWindowSize is applied'}
\endif
      %%
      if @window \= InitValue then
         {@window [setMinSize setXYSize resetTW]}
      end

      %%
\ifdef DEBUG_WM
      {System.show 'WindowManagerClass::resetWindowSize is finished'}
\endif
   end

   %%
   %%
   meth focusIn
\ifdef DEBUG_WM
      {System.show 'WindowManagerClass::focusIn is applied'}
\endif
      WindowManagerClass , WrapWindow(focusIn)
\ifdef DEBUG_WM
      {System.show 'WindowManagerClass::focusIn is finished'}
\endif
   end

   %%
   %%
   meth closeMenus
\ifdef DEBUG_WM
      {System.show 'WindowManagerClass::closeMenus is applied'}
\endif
      if
         @window \= InitValue andthen
         {self.store read(StoreAreMenus $)}
      then
         {@window [closeMenuBar setMinSize]}
         actions <- InitValue
         actionVar <- InitValue
         {self.store store(StoreAreMenus false)}
      end
\ifdef DEBUG_WM
      {System.show 'WindowManagerClass::closeMenus is finished'}
\endif
   end

   %%
   %%
   meth closeWindow
\ifdef DEBUG_WM
      {System.show 'WindowManagerClass::closeWindow is applied'}
\endif
      %%
      if @window \= InitValue then
         %%
         WindowManagerClass , closeMenus

         %%
         {@window close}
         window <- InitValue
         actions <- InitValue
         actionVar <- InitValue
         {self.store store(StoreIsWindow false)}
      end
\ifdef DEBUG_WM
      {System.show 'WindowManagerClass::closeWindow is finished'}
\endif
   end

   %%
   %%
   meth makeAbout
\ifdef DEBUG_WM
      {System.show 'WindowManagerClass::makeAbout is applied'}
\endif
      {New AboutDialogClass init(windowObj:@window) _}
\ifdef DEBUG_WM
      {System.show 'WindowManagerClass::makeAbout is finished'}
\endif
   end

   %%
   %%  'Arg' is a list of entry names;
   meth entriesEnable(Arg)
\ifdef DEBUG_WM
      {System.show 'WindowManagerClass::entriesEnable is applied'}
\endif
      %%
      local MEs Bs in
         MEs = {Filter {Map Arg self.Entry2Path} self.CleanUp}
         Bs = {Filter {Map Arg self.Button2Path} self.CleanUp}

         %%
         WindowManagerClass , WrapMenuBar(commandEntriesEnable(MEs))
         WindowManagerClass , WrapMenuBar(buttonsEnable(Bs))

         %%
         case Arg of [break] then
            WindowManagerClass , WrapWindow(setWaitCursor)
         else skip
         end
      end
\ifdef DEBUG_WM
      {System.show 'WindowManagerClass::entriesEnable is finished'}
\endif
   end

   %%
   meth entriesDisable(Arg)
\ifdef DEBUG_WM
      {System.show 'WindowManagerClass::entriesDisable is applied'}
\endif
      local MEs Bs in
         MEs = {Filter {Map Arg self.Entry2Path} self.CleanUp}
         Bs = {Filter {Map Arg self.Button2Path} self.CleanUp}

         %%
         WindowManagerClass , WrapMenuBar(commandEntriesDisable(MEs))
         WindowManagerClass , WrapMenuBar(buttonsDisable(Bs))

         %%
         case {Filter Arg fun {$ E} E == break end} of [break] then
            WindowManagerClass , WrapWindow(setDefaultCursor)
         else skip
         end
      end
\ifdef DEBUG_WM
      {System.show 'WindowManagerClass::entriesDisable is finished'}
\endif
   end

   %%
   %%
   meth addAction(Action Label)
\ifdef DEBUG_WM
      {System.show 'WindowManagerClass::addProcessAction is applied'}
\endif
      %%
      local Actions N PA in
         Actions = @actions
         N = @nextANumber
         PA = {Dictionary.get Actions (N-1)}      % cannot be empty;
         {Dictionary.put @actions N r(action:Action
                                      label: Label
                                      number:(PA.number+1))}
         nextANumber <- N + 1

         %%
         if @window \= InitValue andthen {self.store read(StoreAreMenus $)}
         then {@window
               addRadioEntry(selection(action(menu)) Label @actionVar N)}
         end
      end
\ifdef DEBUG_WM
      {System.show 'WindowManagerClass::addProcessAction is finished'}
\endif
   end

   %%
   %%
   meth removeAction(Action)
\ifdef DEBUG_WM
      {System.show 'WindowManagerClass::removeProcessAction is applied'}
\endif
      %%
      local
         Actions = @actions
         WClearProc
         WAddProc
         Store = self.store
         CurrAction = {Store read(StoreProcessAction $)}
      in
         %%
         if @window \= InitValue andthen {self.store read(StoreAreMenus $)}
         then
            Window = @window
            AVar = @actionVar
         in
            proc {WClearProc  N}
               {Window removeRadioEntry(selection(action(menu)) N)}
            end
            proc {WAddProc N L}
               {Window addRadioEntry(selection(action(menu)) L AVar N)}
            end
         else
            proc {WClearProc _} skip end
            proc {WAddProc _ _} skip end
         end

         %%
         {ForAll
          {List.filter {Dictionary.keys Actions}
           fun {$ K}
              case {Value.status Action}
              of free      then false
              [] kinded(_) then false
              else
                 {Dictionary.get Actions K}.action == Action orelse
                 (Action == 'all' andthen K \= 0 andthen K\= 1)
              end
           end}
          proc {$ N}
             {WClearProc N}
             {Dictionary.remove Actions N}
          end}

         %%
         %% Slide numbers - since menu entries could get new
         %% indexes (after executing the code above);
         {ForAll {Sort {Dictionary.keys Actions} Value.'<'} WClearProc}
         nextANumber <-
         {List.foldL
          {Sort {Dictionary.keys Actions} Value.'<'}
          fun {$ I K}
             OldAction = {Dictionary.get Actions K}
          in
             {Dictionary.put Actions I {AdjoinAt OldAction number I}}
             {WAddProc I OldAction.label}
             I + 1
          end
          0}                    % must be 0;

         %%
         %% Set up other action if needed;
         WindowManagerClass ,
         setAction({List.foldL {Dictionary.keys Actions}
                    fun {$ D K}
                       Action = {Dictionary.get Actions K}
                    in
                       if Action.action == CurrAction
                       then CurrAction
                       else D
                       end
                    end
                    Browse})

      end
\ifdef DEBUG_WM
      {System.show 'WindowManagerClass::removeProcessAction is finished'}
\endif
   end

   %%
   %%
   meth setAction(Action)
\ifdef DEBUG_WM
      {System.show 'WindowManagerClass::setAction is applied'}
\endif
      %%
      local
         Actions = @actions
         WSetProc
      in
         if @window \= InitValue andthen {self.store read(StoreAreMenus $)}
         then AVar = @actionVar in
            proc {WSetProc K}
               {AVar tkSet(K)}
            end
         else
            proc {WSetProc _} skip end
         end

         %%
         {ForAll {Dictionary.keys Actions}
          proc {$ K}
             A = {Dictionary.get Actions K}
          in
             if A.action == Action then
                {WSetProc K}
                {self.store store(StoreProcessAction Action)}
             end
          end}
      end
\ifdef DEBUG_WM
      {System.show 'WindowManagerClass::setAction is finished'}
\endif
   end

   %%
   meth setTWFont(Font ?Res)
\ifdef DEBUG_WM
      {System.show 'WindowManagerClass::setTWFont is applied'}
\endif
      if @window \= InitValue then
         {@window setTWFont(Font Res)}
      else
         {self.store store(StoreTWFont Font)}
         Res = true
      end
\ifdef DEBUG_WM
      {System.show 'WindowManagerClass::setTWFont is finished'}
\endif
   end

   %%
   meth unHighlightTerm
\ifdef DEBUG_WM
      {System.show 'WindowManagerClass::unHighlightTerm is applied'}
\endif
      WindowManagerClass , WrapWindow(unHighlightRegion)
\ifdef DEBUG_WM
      {System.show 'WindowManagerClass::unHighlightTerm is finished'}
\endif
   end

   %%
   %%  ... Aux: checks either we have created a window already;
   %% Otherwise, the message ('Meth') is just ignored;
   meth WrapWindow(Meth)
      if @window \= InitValue then {@window Meth}
      end
   end

   %%
   meth WrapMenuBar(Meth)
      if @window \= InitValue andthen {self.store read(StoreAreMenus $)}
      then {@window Meth}
      end
   end

   %%
end
