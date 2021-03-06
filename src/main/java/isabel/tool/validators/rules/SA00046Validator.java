package isabel.tool.validators.rules;

import isabel.model.wsdl.OperationElement;
import isabel.tool.impl.NavigationException;
import isabel.tool.impl.Standards;
import isabel.tool.impl.ValidationCollector;
import isabel.tool.imports.ProcessContainer;
import nu.xom.Node;
import nu.xom.Nodes;

import static isabel.tool.impl.Standards.CONTEXT;

public class SA00046Validator extends Validator {
	public SA00046Validator(ProcessContainer files,
	                        ValidationCollector violationCollector) {
		super(files, violationCollector);
	}

	@Override
	public void validate() {
		Nodes invokeCorrelationsNodes = fileHandler.getBpel().getDocument()
				.query("//bpel:invoke/bpel:correlations", CONTEXT);

		for (Node node : invokeCorrelationsNodes) {
			try {
				Node operation = navigator.correspondingOperation(node
						.getParent());

				if (new OperationElement(operation).isRequestResponse()) {
					reportViolation(getCorrelationWithoutPattern(node), 1);
				} else if (new OperationElement(operation).isOneWay()) {
					reportViolation(getCorrelationWithPattern(node), 2);
				}
			} catch (NavigationException e) {
				// This node could not be validated
			}

		}
	}

	private void reportViolation(Nodes correlations, int type) {
		for (Node node : correlations) {
			addViolation(node, type);
		}
	}

	private Nodes getCorrelationWithPattern(Node correlationsElement) {
		return correlationsElement.query(
				"child::bpel:correlation[attribute::pattern]",
				Standards.CONTEXT);
	}

	private Nodes getCorrelationWithoutPattern(Node correlationsElement) {
		return correlationsElement.query(
				"child::bpel:correlation[not(attribute::pattern)]",
				Standards.CONTEXT);
	}

	@Override
	public int getSaNumber() {
		return 46;
	}

}
