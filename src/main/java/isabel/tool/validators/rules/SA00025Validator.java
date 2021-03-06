package isabel.tool.validators.rules;

import isabel.tool.impl.ValidationCollector;
import isabel.tool.imports.ProcessContainer;
import nu.xom.Node;
import nu.xom.Nodes;

import static isabel.tool.impl.Standards.CONTEXT;

public class SA00025Validator extends Validator {

	private static final int MESSAGE_TYPE_AND_TYPE_AND_ELEMENT = 5;
	private static final int TYPE_AND_ELEMENT = 4;
	private static final int MESSAGE_TYPE_AND_ELEMENT = 3;
	private static final int MESSAGE_TYPE_AND_TYPE = 2;
	private static final int MESSAGE_TYPE_OR_TYPE_OR_ELEMENT_MISSING = 1;

	public SA00025Validator(ProcessContainer files,
	                        ValidationCollector violationCollector) {
		super(files, violationCollector);
	}

	@Override
	public void validate() {

		Nodes variables = fileHandler.getBpel().getDocument()
				.query("//bpel:variable", CONTEXT);

		for (Node variable : variables) {

			Nodes messageTypeSet = variable.query("@messageType", CONTEXT);
			Nodes typeSet = variable.query("@type", CONTEXT);
			Nodes elementSet = variable.query("@element", CONTEXT);

			if (messageTypeSet.isEmpty() && typeSet.isEmpty() && elementSet.isEmpty()) {
				addViolation(variable, MESSAGE_TYPE_OR_TYPE_OR_ELEMENT_MISSING);
			} else if (messageTypeSet.hasAny() && typeSet.hasAny()) {
				addViolation(variable, MESSAGE_TYPE_AND_TYPE);
			} else if (messageTypeSet.hasAny() && elementSet.hasAny()) {
				addViolation(variable, MESSAGE_TYPE_AND_ELEMENT);
			} else if (typeSet.hasAny() && elementSet.hasAny()) {
				addViolation(variable, TYPE_AND_ELEMENT);
			} else if (messageTypeSet.hasAny() && typeSet.hasAny() && elementSet.hasAny()) {
				addViolation(variable, MESSAGE_TYPE_AND_TYPE_AND_ELEMENT);
			}
		}
	}

	@Override
	public int getSaNumber() {
		return 25;
	}
}
