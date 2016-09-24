functor
import
   Browser(browse:Browse)
define
   {Browse 42}
   {Browse a(b:c d:[1 2 3])}

   S
   P={NewPort S}
   {Browse S}

   for I in {List.number 1 10 1} do
      {Delay 1000}
      {Send P I}
   end
end
