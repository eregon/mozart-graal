functor
export
   Return
define
   % An upper bound on the stack size so a StackOverflow would occur if tail calls were not optimized
   StackDepthUpperBound = 100000
   Return='tailrec'([
		     tailProc(proc {$}
				 proc {Rec I}
				    if I == 0 then
				       {Rec I-1}
				    end
				 end
			      in
				 {Rec StackDepthUpperBound}
			      end
			      keys: [tailrec procedure]
			     )
		     tailFun(proc {$}
				fun {Rec D A}
				   case D of 0 then
				      A
				   else
				      {Rec D-1 A+1}
				   end
				end
			     in
				StackDepthUpperBound = {Rec StackDepthUpperBound 0}
			     end
			     keys: [tailrec function]
			    )
		     tailFunList(proc {$}
				    fun {Rec I}
				       case I of 0 then
					  nil
				       else
					  I|{Rec I-1}
				       end
				    end
				 in
				    StackDepthUpperBound = {List.length {Rec StackDepthUpperBound}}
				 end
				 keys: [tailrec function list]
				)
		     tailFunEmbRecords(proc {$}
					  fun {Rec I}
					     case I of 0 then
						b
					     else
						top(a down(a {Rec I-1} c) c)
					     end
					  end
				       in
					  _ = {Rec StackDepthUpperBound}
				       end
				       keys: [tailrec embbeded_records]
				      )
		     tailRecordsOrder(proc {$}
					 fun {Gen}
					    A B
					    proc {GenA R}
					       A = 1
					       R = A
					    end
					    proc {GenB R}
					       B = A + 1
					       R = B
					    end
					 in
					    ret({GenA} {GenB})
					 end
				      in
					 ret(1 2) = {Gen}
				      end
					 keys: [tailrec record param_order]
				     )
		    ])
end

