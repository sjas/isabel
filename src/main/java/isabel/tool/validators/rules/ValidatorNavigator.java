package isabel.tool.validators.rules;

import isabel.tool.helper.NodeHelper;
import isabel.tool.helper.PrefixHelper;
import isabel.tool.impl.NavigationException;
import isabel.tool.imports.ProcessContainer;
import isabel.tool.imports.XmlFile;
import nu.xom.*;

import java.util.HashMap;
import java.util.List;

import static isabel.tool.impl.Standards.CONTEXT;

public class ValidatorNavigator {

	private final ProcessContainer fileHandler;

	public ValidatorNavigator(ProcessContainer fileHandler) {
		this.fileHandler = fileHandler;
	}

	public HashMap<String, Node> getOperationMessages(
			List<XmlFile> wsdlImports, Node operation)
			throws NavigationException {

		if (operation == null)
			return null;

		Nodes operationChildren = operation.query("child::*", CONTEXT);
		HashMap<String, Node> messages = new HashMap<>();
		for (Node child : operationChildren) {

			NodeHelper childHelper = new NodeHelper(child);
			String messageQName = childHelper.getAttribute("message");
			String childName = childHelper.getLocalName();
			String namespaceURI = operation.getDocument().getRootElement()
					.getNamespaceURI(PrefixHelper.getPrefix(messageQName));
			String messageName = PrefixHelper.removePrefix(messageQName);

			Node message = getMessage(messageName, namespaceURI, wsdlImports);
			if (message != null)
				messages.put(childName, message);
		}

		if (messages.isEmpty())
			throw new NavigationException("Messages are not defined.");

		return messages;
	}

	public Node getMessage(String messageName, String namespaceURI,
	                       List<XmlFile> wsdlImports) {
		Node message = null;
		for (XmlFile wsdlEntry : wsdlImports) {
			String targetNamespace = wsdlEntry.getTargetNamespace();
			if (targetNamespace.equals(namespaceURI)) {
				Nodes messageNodes = wsdlEntry.getDocument().query(
						"//wsdl:message[@name='" + messageName + "']", CONTEXT);

				if (messageNodes.hasAny()) {
					message = messageNodes.get(0);
					break;
				}
			}
		}

		return message;
	}

	public String getPrefixNamespaceURI(Document document,
	                                    String namespacePrefix) throws NavigationException {
		if (namespacePrefix.isEmpty()) {
			throw new NavigationException(
					"Document has no namespace for this prefix");
		}

		return document.getRootElement().getNamespaceURI(namespacePrefix);
	}

	public Node correspondingOperation(Node messageActivity)
			throws NavigationException {
		Element invokeElement = (Element) messageActivity;
		String partnerLinkName = invokeElement.getAttributeValue("partnerLink");
		String operationName = invokeElement.getAttributeValue("operation");
		Node partnerLink = getPartnerLink(messageActivity.getDocument(),
				partnerLinkName);

		Node portType = partnerLinkToPortType(partnerLink);

		return portTypeToOperation(portType, operationName);
	}

	public Node getPartnerLink(Document document, String partnerLinkName)
			throws NavigationException {
		Nodes partnerLink = document.query(
				"//bpel:partnerLinks/bpel:partnerLink[@name='"
						+ partnerLinkName + "']", CONTEXT);

		if (partnerLink.hasAny())
			return partnerLink.get(0);

		throw new NavigationException("PartnerLink not defined");
	}

	public Node partnerLinkToPortType(Node partnerLinkNode)
			throws NavigationException {
		Element partnerLink = (Element) partnerLinkNode;

		String partnerLinkTypeAttribute = partnerLink
				.getAttributeValue("partnerLinkType");
		String wsdlImportNamespace = getPrefixNamespaceURI(
				partnerLink.getDocument(),
				PrefixHelper.getPrefix(partnerLinkTypeAttribute));
		Document correspondingWsdlDom = fileHandler
				.getWsdlByTargetNamespace(wsdlImportNamespace);

		if (correspondingWsdlDom != null) {
			String partnerLinkTypeName = PrefixHelper
					.removePrefix(partnerLinkTypeAttribute);
			String partnerRoleName = partnerLink
					.getAttributeValue("partnerRole");
			String myRoleName = partnerLink.getAttributeValue("myRole");

			Nodes partnerRolePortType = correspondingWsdlDom.query(
					"//plink:partnerLinkType[@name='" + partnerLinkTypeName
							+ "']/" + "plink:role[@name='" + partnerRoleName
							+ "']/@portType", CONTEXT);

			if (partnerRolePortType.hasAny()) {
				String portTypeQName = partnerRolePortType.get(0).getValue();
				String portTypeNamespaceURI = getPrefixNamespaceURI(
						correspondingWsdlDom,
						PrefixHelper.getPrefix(portTypeQName));
				return getPortType(portTypeQName, portTypeNamespaceURI);
			} else {
				Nodes myRolePortType = correspondingWsdlDom.query(
						"//plink:partnerLinkType[@name='" + partnerLinkTypeName
								+ "']/" + "plink:role[@name='" + myRoleName
								+ "']/@portType", CONTEXT);
				if (myRolePortType.hasAny()) {
					String portTypeQName = myRolePortType.get(0).getValue();
					String portTypeNamespaceURI = getPrefixNamespaceURI(
							correspondingWsdlDom,
							PrefixHelper.getPrefix(portTypeQName));
					return getPortType(portTypeQName, portTypeNamespaceURI);
				}
			}
		}

		throw new NavigationException("PortType not defined");
	}

	public Node portTypeToOperation(Node portType, String operationName)
			throws NavigationException {
		Nodes operations = portType.query(
				"child::wsdl:operation[attribute::name='" + operationName
						+ "']", CONTEXT);
		if (operations.hasAny())
			return operations.get(0);

		throw new NavigationException("Operation not defined");
	}

	public Node getPortType(String portTypeQName, String portTypeNamespaceURI)
			throws NavigationException {
		String portTypeName = PrefixHelper.removePrefix(portTypeQName);
		for (XmlFile wsdlEntry : fileHandler.getWsdls()) {
			String targetNamespace = wsdlEntry.getTargetNamespace();
			if (targetNamespace.equals(portTypeNamespaceURI)) {
				Nodes portTypes = wsdlEntry.getDocument().query(
						"//wsdl:portType[@name='" + portTypeName + "']",
						CONTEXT);
				if (portTypes.hasAny()) {
					return portTypes.get(0);
				}
			}
		}

		throw new NavigationException("portType not defined");
	}

	public Node operationToMessage(List<XmlFile> wsdlImports,
	                               Node operation) throws NavigationException {

		if (operation == null)
			throw new NavigationException("Operation not defined");

		Nodes inputNodes = operation.query("child::wsdl:input/@message",
				CONTEXT);

		if (inputNodes.isEmpty()) {
			return null;
		}

		String messageQName = inputNodes.get(0).getValue();
		String qNamePrefix = PrefixHelper.getPrefix(messageQName);
		String messageName = PrefixHelper.removePrefix(messageQName);
		String messageNamespaceURI = getPrefixNamespaceURI(
				operation.getDocument(), qNamePrefix);

		Node message = null;
		for (XmlFile wsdlEntry : wsdlImports) {
			String targetNamespace = wsdlEntry.getTargetNamespace();
			if (targetNamespace.equals(messageNamespaceURI)) {
				Nodes messageNodes = wsdlEntry.getDocument().query(
						"//wsdl:message[@name='" + messageName + "']", CONTEXT);

				if (messageNodes.hasAny()) {
					message = messageNodes.get(0);
					break;
				}

			}
		}

		return message;
	}

	public Node getCorrespondingOutgoingMessage(Node invoke)
			throws NavigationException {

		Node operation = correspondingOperation(invoke);
		String messageAttribute = getAttributeValue(operation.query(
				"wsdl:output/@message", CONTEXT));
		Document correspondingWsdl = operation.getDocument();
		Nodes messages = correspondingWsdl.query("//wsdl:message", CONTEXT);
		for (Node message : messages) {
			String messageName = new NodeHelper(message).getAttribute("name");
			if (messageName.equals(PrefixHelper.removePrefix(messageAttribute))) {
				return message;
			}
		}
		throw new NavigationException("corresponding <message> is not defined");
	}

	public Node getCorrespondingIncomingMessage(Node invoke)
			throws NavigationException {

		Node operation = correspondingOperation(invoke);
		String messageAttribute = getAttributeValue(operation.query(
				"wsdl:input/@message", CONTEXT));
		Document correspondingWsdl = operation.getDocument();
		Nodes messages = correspondingWsdl.query("//wsdl:message", CONTEXT);
		for (Node message : messages) {
			String messageName = new NodeHelper(message).getAttribute("name");
			if (messageName.equals(PrefixHelper.removePrefix(messageAttribute))) {
				return message;
			}
		}
		throw new NavigationException("corresponding <message> is not defined");
	}

	public boolean hasInputVariable(Node msgActivity) {
		Nodes inputVar = msgActivity.query("attribute::inputVariable", CONTEXT);
		return inputVar.hasAny();
	}

	public boolean hasOutputVariable(Node msgActivity) {
		Nodes outVar = msgActivity.query("attribute::outputVariable", CONTEXT);
		return outVar.hasAny();
	}

	public XmlFile getCorrespondingWsdlToCorrelationSet(
			Node correlationSet) throws NavigationException {
		String namespacePrefix = getCorrelationPropertyAliasPrefix(correlationSet);
		String correspondingTargetNamespace = getImportNamespace(correlationSet, namespacePrefix);

		for (XmlFile wsdlFile : fileHandler.getWsdls()) {
			if (wsdlFile.getTargetNamespace().equals(correspondingTargetNamespace)) {
				return wsdlFile;
			}
		}
		throw new NavigationException("Referenced WSDL File does not exist.");
	}

	public String getCorrelationPropertyAliasPrefix(Node correlationSet)
			throws NavigationException {

		String propertyAliasName = new NodeHelper(correlationSet).getAttribute("properties");
		String namespacePrefix = PrefixHelper.getPrefix(propertyAliasName);

		if (namespacePrefix != null) {
			return namespacePrefix;
		}

		throw new NavigationException(
				"<correlationSet>@properties prefix does not exist.");
	}

	public String getImportNamespace(Node node, String namespacePrefix) {
		Element rootElement = node.getDocument().getRootElement();

		try {
			return rootElement.getNamespaceURI(namespacePrefix);
		} catch (NullPointerException e) {
			/*
			 * if the prefix is undefined in the root element, getNamespaceURI
			 * will throw a nullPointerException.
			 */
			return "";
		}
	}

	public Node getCorrespondingPropertyAlias(Node correlationSet,
	                                          Document wsdlFile) throws NavigationException {
		Nodes propertyAliases = wsdlFile
				.query("//vprop:propertyAlias", CONTEXT);
		String propertyAliasAttribute = PrefixHelper
				.removePrefix(new NodeHelper(correlationSet)
						.getAttribute("properties"));

		return navigateFromPropertyAliasNameToPropertyAlias(propertyAliases,
				propertyAliasAttribute);
	}

	public Node navigateFromPropertyAliasNameToPropertyAlias(Nodes aliases,
	                                                         String propertyAliasAttribute) throws NavigationException {
		for (Node propertyAlias : aliases) {
			String propertyAliasName = PrefixHelper
					.removePrefix(new NodeHelper(propertyAlias)
							.getAttribute("propertyName"));

			if (propertyAliasName.equals(propertyAliasAttribute)) {
				return propertyAlias;
			}
		}
		throw new NavigationException(
				"Referenced <propertyAlias> does not exist.");
	}

	public Node getCorrespondingProperty(Node propertyAlias)
			throws NavigationException {
		Document wsdlDom = propertyAlias.getDocument();
		String propertyAttribute = new NodeHelper(propertyAlias)
				.getAttribute("propertyName");
		Nodes properties = wsdlDom.query("//vprop:property", CONTEXT);

		for (Node property : properties) {
			String propertyName = new NodeHelper(property).getAttribute("name");

			if (propertyName.equals(PrefixHelper
					.removePrefix(propertyAttribute))) {
				return property;
			}
		}
		throw new NavigationException("Referenced <property> does not exist.");
	}

	public static String getAttributeValue(Nodes attributes) {
		if (attributes.hasAny()) {
			Node attribute = attributes.get(0);
			if (attribute instanceof Attribute) {
				return attribute.getValue();
			}
		}
		return "";
	}

}
