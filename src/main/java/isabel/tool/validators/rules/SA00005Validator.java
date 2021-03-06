package isabel.tool.validators.rules;

import isabel.tool.helper.NodeHelper;
import isabel.tool.helper.NodesUtil;
import isabel.tool.helper.PrefixHelper;
import isabel.tool.impl.NavigationException;
import isabel.tool.impl.ValidationCollector;
import isabel.tool.imports.ProcessContainer;
import nu.xom.Document;
import nu.xom.Node;

import java.util.LinkedList;
import java.util.List;

import static isabel.tool.impl.Standards.CONTEXT;

public class SA00005Validator extends Validator {

	public SA00005Validator(ProcessContainer files,
	                        ValidationCollector violationCollector) {
		super(files, violationCollector);
	}

	@Override
	public void validate() {
		List<Node> messageActivities = getMessageActivities();

		for (Node messageActivity : messageActivities) {
			try {
				String partnerLinkAttribute = new NodeHelper(messageActivity)
						.getAttribute("partnerLink");

				Node partnerLink = navigator.getPartnerLink(fileHandler
						.getBpel().getDocument(), partnerLinkAttribute);

				Node correspondingPortType = navigator
						.partnerLinkToPortType(partnerLink);

				String localPortTypeDefinition = getLocalPortTypeDefinition(messageActivity);
				String correspondingPortTypeName = new NodeHelper(
						correspondingPortType).getAttribute("name");

				if (!correspondingPortTypeName.equals(localPortTypeDefinition)) {
					addViolation(messageActivity);
				}
			} catch (NavigationException e) {
				// This node could not be validated
			}
		}
	}

	private List<Node> getMessageActivities() {
		Document bpelDom = fileHandler.getBpel().getDocument();

		List<Node> messageActivities = new LinkedList<>();
		messageActivities.addAll(NodesUtil.toList(bpelDom.query(
				"//bpel:receive", CONTEXT)));
		messageActivities.addAll(NodesUtil.toList(bpelDom.query("//bpel:reply",
				CONTEXT)));
		messageActivities.addAll(NodesUtil.toList(bpelDom.query(
				"//bpel:invoke", CONTEXT)));
		messageActivities.addAll(NodesUtil.toList(bpelDom.query(
				"//bpel:onEvent", CONTEXT)));
		messageActivities.addAll(NodesUtil.toList(bpelDom.query(
				"//bpel:onMessage", CONTEXT)));

		return messageActivities;
	}

	private String getLocalPortTypeDefinition(Node messageActivity) {
		String localPortTypeDefinition = new NodeHelper(messageActivity)
				.getAttribute("portType");
		return PrefixHelper.removePrefix(localPortTypeDefinition);
	}

	@Override
	public int getSaNumber() {
		return 5;
	}
}
