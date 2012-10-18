package de.uniba.wiai.dsg.ss12.isabel.tool.validators;

import de.uniba.wiai.dsg.ss12.isabel.tool.ValidationResult;
import de.uniba.wiai.dsg.ss12.isabel.tool.helper.NodeHelper;
import de.uniba.wiai.dsg.ss12.isabel.tool.helper.PrefixHelper;
import de.uniba.wiai.dsg.ss12.isabel.tool.impl.NavigationException;
import de.uniba.wiai.dsg.ss12.isabel.tool.imports.BpelProcessFiles;
import de.uniba.wiai.dsg.ss12.isabel.tool.imports.DocumentEntry;
import nu.xom.Document;
import nu.xom.Node;
import nu.xom.Nodes;

import static de.uniba.wiai.dsg.ss12.isabel.tool.impl.Standards.CONTEXT;
import static de.uniba.wiai.dsg.ss12.isabel.tool.validators.ValidatorNavigator.getAttributeValue;

public class SA00021Validator extends Validator {

	public SA00021Validator(BpelProcessFiles files,
	                        ValidationResult violationCollector) {
		super(files, violationCollector);
	}

	@Override
	public void validate() {
		hasEachCorrelationSetExistingProperty();
		validateFor("//bpel:from[@property]");
		validateFor("//bpel:to[@property]");
	}

	private void validateFor(String toOrFrom) {
		Nodes fromToSet = fileHandler.getBpel().getDocument()
				.query(toOrFrom, CONTEXT);
		for (Node fromTo : fromToSet) {
			String property = new NodeHelper(fromTo).getAttributeByName("property");
			if (!property.isEmpty()) {
				try {
					Node variable = getCorrespondingScopeVariable(fromTo);
					hasCorrespondingPropertyAlias(variable, property, fromTo);
				} catch (NavigationException e) {
					addViolation(fromTo);
				}
			}
		}
	}

	private Node getCorrespondingScopeVariable(Node fromTo)
			throws NavigationException {
		String variableName = new NodeHelper(fromTo).getAttributeByName("variable");
		return checkParent(fromTo, variableName);
	}

	private Node checkParent(Node node, String variableName)
			throws NavigationException {
		if ("onEvent".equals(toElement(node).getLocalName())) {
			NodeHelper nodeHelper = new NodeHelper(node);
			String variableAttribute = nodeHelper.getAttributeByName("variable");
			if (variableAttribute.equals(variableName))
				if (nodeHelper.hasAttribute("messageType") || nodeHelper.hasAttribute("element"))
					return node;
		} else if ("scope".equals(toElement(node).getLocalName())) {
			Nodes scopeVariableSet = node.query("bpel:variable", CONTEXT);
			for (Node variable : scopeVariableSet) {
				String name = new NodeHelper(variable).getAttributeByName("name");
				if (name.equals(variableName))
					return variable;
			}
		} else if ("process".equals(toElement(node).getLocalName())) {
			Nodes scopeVariableSet = node.query("bpel:variable", CONTEXT);
			for (Node variable : scopeVariableSet) {
				String name = new NodeHelper(variable).getAttributeByName("name");
				if (name.equals(variableName))
					return variable;
			}

			throw new NavigationException("Variable not in process");
		}

		return checkParent(
				node.query("parent::*").get(node.query("parent::*").size() - 1),
				variableName);
	}

	private void hasCorrespondingPropertyAlias(Node node, String property,
	                                           Node partHolder) throws NavigationException {

		String type = "";
		if (toElement(node).getLocalName().equals("onEvent")) {
			type = getAttributeValue(getOnEventVariableType(node));
		} else if (toElement(node).getLocalName().equals("scope")) {
			type = getAttributeValue(getEnclosingScopeVariableType(node));
		} else if (toElement(node).getLocalName().equals("process")) {
			type = getAttributeValue(getEnclosingScopeVariableType(node));
		}

		if (!type.isEmpty()) {
			Document wsdl = getCorrespondingWsdl(property, node);
			Nodes wsdlPropertyAliasSet = wsdl.query("//vprop:propertyAlias",
					CONTEXT);
			for (Node propertyAlias : wsdlPropertyAliasSet) {
				String propertyName = PrefixHelper.removePrefix(new NodeHelper(propertyAlias).getAttributeByName("propertyName"));

				if (PrefixHelper.removePrefix(property).equals(propertyName)) {
					if (!isOfThisMessageType(type, propertyAlias, partHolder)
							&& !isOfThisType(type, propertyAlias)
							&& !isOfThisElement(type, propertyAlias)) {
						addViolation(node);
					}
				} else {
					addViolation(node);
				}
			}
		}
	}

	private boolean isOfThisElement(String type, Node propertyAlias) {
		return new NodeHelper(propertyAlias).getAttributeByName("element").equals(
				PrefixHelper.removePrefix(type));
	}

	private boolean isOfThisType(String type, Node propertyAlias) {
		return new NodeHelper(propertyAlias).getAttributeByName("type").equals(
				PrefixHelper.removePrefix(type));
	}

	private boolean isOfThisMessageType(String type, Node propertyAlias,
	                                    Node partHolder) {
		NodeHelper nodeHelper = new NodeHelper(propertyAlias);
		boolean isMessageType = nodeHelper.getAttributeByName("messageType").equals(PrefixHelper.removePrefix(type));

		return isMessageType
				&& nodeHelper.getAttributeByName("part")
				.equals(PrefixHelper.removePrefix(new NodeHelper(partHolder).getAttributeByName("part")));
	}

	private Document getCorrespondingWsdl(String property, Node node)
			throws NavigationException {
		String targetNamespace = navigator.getPrefixNamespaceURI(
				node.getDocument(), PrefixHelper.getPrefix(property));
		return fileHandler.getWsdlByTargetNamespace(targetNamespace);
	}

	private void hasEachCorrelationSetExistingProperty() {
		Nodes correlationSets = fileHandler.getBpel().getDocument()
				.query("//bpel:correlationSet", CONTEXT);

		for (Node correlationSet : correlationSets) {
			try {
				DocumentEntry wsdl = navigator
						.getCorrespondingWsdlToCorrelationSet(correlationSet);
				Node propertyAlias = navigator.getCorrespondingPropertyAlias(
						correlationSet, wsdl.getDocument());
				navigator.getCorrespondingProperty(propertyAlias);
			} catch (NavigationException e) {
				addViolation(correlationSet);
			}
		}
	}

	private Nodes getEnclosingScopeVariableType(Node scope)
			throws NavigationException {
		try {
			return getVariableType(scope, "@messageType");
		} catch (NavigationException e) {
			try {
				return getVariableType(scope, "@type");
			} catch (NavigationException f) {
				return getVariableType(scope, "@element");
			}
		}
	}

	private Nodes getVariableType(Node scope, String type)
			throws NavigationException {
		Nodes scopeMessageTypeSet = scope.query(type, CONTEXT);
		if (scopeMessageTypeSet.size() > 0) {
			return scopeMessageTypeSet;
		}

		throw new NavigationException("This is not the correct variable Type");
	}

	private Nodes getOnEventVariableType(Node scope) throws NavigationException {
		try {
			return getVariableType(scope, "@messageType");
		} catch (NavigationException e) {
			return getVariableType(scope, "@element");
		}
	}

	@Override
	public int getSaNumber() {
		return 21;
	}

}