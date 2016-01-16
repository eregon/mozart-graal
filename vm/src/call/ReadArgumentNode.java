package call;

import org.mozartoz.truffle.nodes.OzNode;
import org.mozartoz.truffle.runtime.OzArguments;

import com.oracle.truffle.api.frame.VirtualFrame;

public class ReadArgumentNode extends OzNode {

	private int index;

	public ReadArgumentNode(int index) {
		this.index = index;
	}

	@Override
	public Object execute(VirtualFrame frame) {
		return OzArguments.getArgument(frame, index);
	}

}
