package de.uniba.wiai.dsg.ss12.isabel.tool.helper;

import de.uniba.wiai.dsg.ss12.isabel.tool.impl.Standards;
import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;

public class NodeHelper {

	protected Node node;

	public NodeHelper(Node node) {
		if (node == null) {
			throw new IllegalArgumentException("given node should not be null");
		}

		this.node = node;
	}

	public String getLocalName() {
		Element el = (Element) node;
		return el.getLocalName();
	}

	public boolean hasLocalName(String name) {
		return getLocalName().equals(name);
	}

	public String getTargetNamespace() {
		return new NodeHelper(node.getDocument().getRootElement()).getAttribute("targetNamespace");
	}

    public String getFilePath(){
        return node.getBaseURI();
    }

	public boolean hasTargetNamespace(String targetNamespace) {
		return getTargetNamespace().equals(targetNamespace);
	}

	public boolean hasTargetNamespace(Node otherNode) {
		return hasTargetNamespace(new NodeHelper(otherNode).getTargetNamespace());
	}

	public String getAttribute(String name) {
		Nodes attributes = node.query("@" + name);

		if (attributes.size() > 0) {
			Node attribute = attributes.get(0);
			if (attribute instanceof Attribute) {
				return attribute.getValue();
			}
		}
		return "";
	}

	public Attribute getAttributeNode(String name) {
		Nodes attributes = node.query("@" + name);

		if (attributes.size() > 0) {
			return (Attribute) attributes.get(0);
		}
		return null;
	}

	public boolean hasSameAttribute(NodeHelper otherNode, String attributeName) {
		if (hasAttribute(attributeName) && otherNode.hasAttribute(attributeName)) {

			AttributeHelper otherAttribute = new AttributeHelper(otherNode.getAttributeNode(attributeName));
			AttributeHelper attribute = new AttributeHelper(getAttributeNode(attributeName));
			if (attribute.isEqualTo(otherAttribute.getAttribute())) {
				return true;
			}

		}
		return false;
	}

	public boolean hasAttribute(String name) {
		return !getAttribute(name).isEmpty();
	}

	public boolean hasNoAttribute(String name){
		return !hasAttribute(name);
	}
	
	public boolean hasQueryResult(String query){
		return node.query(query, Standards.CONTEXT).size() > 0;
	}

    public boolean hasEmptyQueryResult(String query){
        return node.query(query, Standards.CONTEXT).size() == 0;
    }

    public boolean hasAncestor(String name){
        return hasQueryResult("ancestor::" + name);
    }

    public Element asElement(){
        return (Element) node;
    }

    public Node getNode() {
        return node;
    }
}
