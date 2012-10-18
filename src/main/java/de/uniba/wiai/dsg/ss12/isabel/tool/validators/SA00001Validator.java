package de.uniba.wiai.dsg.ss12.isabel.tool.validators;

import de.uniba.wiai.dsg.ss12.isabel.tool.ValidationResult;
import de.uniba.wiai.dsg.ss12.isabel.tool.helper.OperationHelper;
import de.uniba.wiai.dsg.ss12.isabel.tool.imports.BpelProcessFiles;
import de.uniba.wiai.dsg.ss12.isabel.tool.imports.DocumentEntry;
import nu.xom.Node;
import nu.xom.Nodes;

import static de.uniba.wiai.dsg.ss12.isabel.tool.impl.Standards.CONTEXT;

public class SA00001Validator extends Validator {

	private static final int SOLICIT_RESPONSE_TYPE = 2;
	private static final int NOTIFICATION_FAULT = 1;

	public SA00001Validator(BpelProcessFiles files,
	                        ValidationResult violationCollector) {
		super(files, violationCollector);
	}

	@Override
	public void validate() {
		for (DocumentEntry wsdlEntry : fileHandler.getAllWsdls()) {
			String filePath = wsdlEntry.getFilePath();
			for (Node currentOperation : getOperations(wsdlEntry)) {
				OperationHelper operationHelper = new OperationHelper(currentOperation);
				if (operationHelper.isNotification()) {
					addViolation(filePath, currentOperation, NOTIFICATION_FAULT);
				}
				if (operationHelper.isSolicitResponse()) {
					addViolation(filePath, currentOperation, SOLICIT_RESPONSE_TYPE);
				}
			}
		}
	}

	private Nodes getOperations(DocumentEntry wsdlEntry) {
		return wsdlEntry.getDocument().query(
				"//wsdl:portType/wsdl:operation", CONTEXT);
	}

	@Override
	public int getSaNumber() {
		return 1;
	}

}