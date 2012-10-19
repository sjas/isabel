package de.uniba.wiai.dsg.ss12.isabel.tool.validators;

import de.uniba.wiai.dsg.ss12.isabel.tool.ValidationResult;
import de.uniba.wiai.dsg.ss12.isabel.tool.helper.NodeHelper;
import de.uniba.wiai.dsg.ss12.isabel.tool.impl.Standards;
import de.uniba.wiai.dsg.ss12.isabel.tool.imports.BpelProcessFiles;
import nu.xom.Node;
import nu.xom.Nodes;

public class SA00076Validator extends Validator {

    public SA00076Validator(BpelProcessFiles files, ValidationResult validationResult) {
        super(files, validationResult);
    }

    @Override
    public void validate() {
        Nodes forEachLoops = fileHandler.getBpel().getDocument().query("//bpel:forEach", Standards.CONTEXT);
        for(Node forEach : forEachLoops){
            NodeHelper forEachHelper = new NodeHelper(forEach);

            Nodes variables = forEach.query("bpel:scope/bpel:variables/bpel:variable", Standards.CONTEXT);
            for(Node variable : variables){
                NodeHelper variableHelper = new NodeHelper(variable);

                if(forEachHelper.getAttribute("counterName").equals(variableHelper.getAttribute("name"))){
                    addViolation(variable);
                }
            }
        }
    }

    @Override
    public int getSaNumber() {
        return 76;
    }
}