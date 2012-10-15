package de.uniba.wiai.dsg.ss12.isabel.tool.validators;

import static de.uniba.wiai.dsg.ss12.isabel.tool.Standards.CONTEXT;
import nu.xom.Node;
import nu.xom.Nodes;
import de.uniba.wiai.dsg.ss12.isabel.tool.imports.BpelProcessFiles;
import de.uniba.wiai.dsg.ss12.isabel.tool.reports.ViolationCollector;

public class SA00023Validator extends Validator {

	private String fileName;

	public SA00023Validator(BpelProcessFiles files,
			ViolationCollector violationCollector) {
		super(files, violationCollector);
	}

	@Override
	public boolean validate() {

		this.fileName = fileHandler.getBpel().getFilePath();
		Nodes processVariableNames = fileHandler
				.getBpel()
				.getDocument()
				.query("//bpel:process/bpel:variables/bpel:variable/@name",
						CONTEXT);

		checkForDuplicates(processVariableNames, 1);

		Nodes scopes = fileHandler.getBpel().getDocument()
				.query("//bpel:scope", CONTEXT);
		for (Node node : scopes) {
			Nodes variableNames = node.query(
					"bpel:variables/bpel:variable/@name", CONTEXT);
			checkForDuplicates(variableNames, 2);
		}
		return valid;
	}

	private void checkForDuplicates(Nodes nodesToCheck, int errorType) {
		if (nodesToCheck.size() > 1) {
			for (int i = 0; i < nodesToCheck.size(); i++) {
				Node currentNode = nodesToCheck.get(i);

				for (int j = i + 1; j < nodesToCheck.size(); j++) {
					if (nodesToCheck.get(j).getValue()
							.equals(currentNode.getValue())) {
						addViolation(fileName, currentNode, errorType);
					}
				}
			}
		}
	}

	@Override
	public int getSaNumber() {
		return 23;
	}

}
