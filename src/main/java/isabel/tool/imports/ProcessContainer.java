package isabel.tool.imports;

import isabel.tool.helper.NodesUtil;
import isabel.tool.impl.NavigationException;
import nu.xom.Document;
import nu.xom.Node;
import nu.xom.Nodes;
import org.pmw.tinylog.Logger;

import java.io.File;
import java.util.*;

import static isabel.tool.impl.Standards.CONTEXT;
import static isabel.tool.impl.Standards.XSD_NAMESPACE;

public class ProcessContainer {

	private XmlFile bpel;

	private List<XmlFile> allFiles = new ArrayList<>();

	private List<XmlFile> wsdls = new ArrayList<>();
	private List<XmlFile> xsds = new ArrayList<>();

	private List<XmlFile> directlyImportedWsdls = new ArrayList<>();
	private List<XmlFile> directlyImportedXsds = new ArrayList<>();

	private List<Node> schemas = new ArrayList<>();

	private void addWsdl(XmlFile xmlFile) {
		Objects.requireNonNull(xmlFile, "a xmlFile reference is required");
		xmlFile.failUnlessWsdl();
		wsdls.add(xmlFile);
		allFiles.add(xmlFile);
	}

	private void addXsd(XmlFile xmlFile) {
		Objects.requireNonNull(xmlFile, "a xmlFile reference is required");
		xmlFile.failUnlessXsd();
		xsds.add(xmlFile);
		allFiles.add(xmlFile);
	}

	public void add(XmlFile xmlFile) {
		if(xmlFile.isWsdl()){
			addWsdl(xmlFile);
		} else if(xmlFile.isXsd()){
			addXsd(xmlFile);
		} else {
			throw new IllegalArgumentException("Expected WSDL or XSD file, got something else for " + xmlFile);
		}
	}

	public void addDirectlyImported(XmlFile xmlFile) {
		if(xmlFile.isWsdl()){
			addWsdl(xmlFile);
			directlyImportedWsdls.add(xmlFile);
		} else if(xmlFile.isXsd()){
			addXsd(xmlFile);
			directlyImportedXsds.add(xmlFile);
		} else {
			throw new IllegalArgumentException("Expected WSDL or XSD file, got something else for " + xmlFile);
		}
	}

	public void addSchema(Node schema) {
		schemas.add(Objects.requireNonNull(schema, "a schema reference is required"));
	}

	public void setBpel(XmlFile xmlFile) {
		Objects.requireNonNull(xmlFile, "a bpel reference is required");
		xmlFile.failUnlessBpel();
		this.bpel = xmlFile;
		allFiles.add(xmlFile);
	}

	public boolean contains(XmlFile xmlFile){
		return allFiles.contains(xmlFile);
	}

	public String getAbsoluteBpelFolder() {
		return new File(this.bpel.getFilePath()).getParent();
	}

	public XmlFile getBpel() {
		return bpel;
	}

	public List<XmlFile> getWsdls() {
		return wsdls;
	}

	public List<XmlFile> getXsds() {
		return xsds;
	}

	public List<XmlFile> getDirectlyImportedWsdls() {
		return directlyImportedWsdls;
	}

	public List<XmlFile> getDirectlyImportedXsds() {
		return directlyImportedXsds;
	}

	public List<Node> getSchemas() {
		List<Node> xsdSchema = new ArrayList<>(schemas);
		for (XmlFile xsdNode : getXsds())
			xsdSchema.add(xsdNode.getDocument().getChild(0));

		return xsdSchema;
	}

	public Document getXmlSchema() throws NavigationException {
		for (XmlFile xmlFile : getXsds())
			if (XSD_NAMESPACE.equals(xmlFile.getTargetNamespace()))
				return xmlFile.getDocument();

		throw new NavigationException("XMLSchema should have been imported, but haven't.");
	}

	public Document getWsdlByTargetNamespace(String searchedTargetNamespace)
			throws NavigationException {
		for (XmlFile wsdlEntry : getWsdls())
			if (wsdlEntry.getTargetNamespace().equals(searchedTargetNamespace))
				return wsdlEntry.getDocument();

		throw new NavigationException("Document does not exist");
	}

	public List<Node> getPropertyAliases() {
		List<Node> propertyAliases = new LinkedList<>();
		for (XmlFile xmlFile : getDirectlyImportedWsdls()) {
			propertyAliases.addAll(NodesUtil.toList(xmlFile.getDocument().query("//vprop:propertyAlias", CONTEXT)));
		}
		return propertyAliases;
	}

	public List<Node> getProperties() {
		List<Node> propertyAliases = new LinkedList<>();
		for (XmlFile xmlFile : getDirectlyImportedWsdls()) {
			propertyAliases.addAll(NodesUtil.toList(xmlFile.getDocument().query("//vprop:property", CONTEXT)));
		}
		return propertyAliases;
	}

	public Nodes getCorrelationSets() {
		return getBpel().getDocument().query("//bpel:correlationSet", CONTEXT);
	}

	public ProcessContainer validateAndFinalize() {
		Logger.info("Creating immutable ProcessContainer {0}", this);

		validate();

		wsdls = Collections.unmodifiableList(wsdls);
		xsds = Collections.unmodifiableList(xsds);
		schemas = Collections.unmodifiableList(schemas);

		return this;
	}

	public Nodes getImports() {
		return getBpel().getDocument().query("//bpel:import", CONTEXT);
	}

	void validate() {
		// assertion
		if (getWsdls().isEmpty()) {
			throw new IllegalStateException("At least one WSDL file is required");
		}
	}

	public String toString() {
		return new ProcessContainerPrinter(this).toString();
	}

}
