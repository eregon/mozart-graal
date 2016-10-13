%%%
%%% Authors:
%%%   kennytm
%%%
%%% Copyright:
%%%   Kenny Chan, 2014
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
define
    FunctionFailureLineNumber = 31
    ClassFailureLineNumber = 39

    proc {EnsureFailure}
        2 + 2 = 5
    end

    class ClassFailure
        prop locking

        meth something
            lock
                2 + 2 = 5
            end
        end
    end

    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

    fun {CreateTestCase P LineNumber Key}
        Key(
            proc {$}
                try
                    {P}
                catch E then
                    Stack = E.debug.stack
                in
                    Stack.1.line = LineNumber
                end
            end
            keys: [Key stacktraceLineNum]
        )
    end

    Return = stacktraceLineNum([
        {CreateTestCase EnsureFailure
                        FunctionFailureLineNumber
                        simpleFunctionCall}

        {CreateTestCase (proc {$} _ = {New ClassFailure something} end)
                        ClassFailureLineNumber
                        simpleMethodCall}
    ])
end

