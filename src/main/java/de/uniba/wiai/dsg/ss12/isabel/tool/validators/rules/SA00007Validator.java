package de.uniba.wiai.dsg.ss12.isabel.tool.validators.rules;

import de.uniba.wiai.dsg.ss12.isabel.tool.ValidationResult;
import de.uniba.wiai.dsg.ss12.isabel.tool.helper.NodeHelper;
import de.uniba.wiai.dsg.ss12.isabel.tool.imports.BpelProcessFiles;
import nu.xom.Node;
import nu.xom.Nodes;

import static de.uniba.wiai.dsg.ss12.isabel.tool.impl.Standards.CONTEXT;

public class SA00007Validator extends Validator {

	public SA00007Validator(BpelProcessFiles files,
			ValidationResult violationCollector) {
		super(files, violationCollector);
	}

	@Override
	public void validate() {
        for (Node compensateScope : getAllCompensateScopes()) {
            NodeHelper compensateScopeHelper = new NodeHelper(compensateScope);

            if (!compensateScopeHelper.hasAncestor("bpel:faultHandlers") &&
                    !compensateScopeHelper.hasAncestor("bpel:compensationHandler") &&
                    !compensateScopeHelper.hasAncestor("bpel:terminationHandlers")) {
                addViolation(compensateScope);
            }
		}
	}

    private Nodes getAllCompensateScopes() {
        return fileHandler.getBpel().getDocument()
                .query("//bpel:compensateScope", CONTEXT);
    }

    @Override
	public int getSaNumber() {
		return 7;
	}

}