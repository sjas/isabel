package isabel.model.bpel;

import isabel.tool.helper.NodeHelper;
import nu.xom.Node;

public class ReceiveElement extends NodeHelper {

	public ReceiveElement(Node receive) {
		super(receive);

		if (!getLocalName().equals("receive")) {
			throw new IllegalArgumentException(
					"receive helper only works for bpel:receive elements");
		}
	}

	public boolean hasFromParts() {
		return hasQueryResult("bpel:fromParts");
	}

	public boolean hasVariable() {
		return hasAttribute("variable");
	}
}
