package de.uniba.wiai.dsg.ss12.isabel;

import de.uniba.wiai.dsg.ss12.isabel.tool.imports.LocationAwareNodeFactory;
import nu.xom.*;

import java.io.File;
import java.io.IOException;

public class XOMTest {

	public static void main(String[] args) throws IOException, ParsingException {

		Builder builder = new Builder(new LocationAwareNodeFactory());
		Document document = builder.build(new File("Testcases/betsy/TestInterface.wsdl"));
		Nodes result = document.getRootElement().query("//wsdl:service", new XPathContext("wsdl","http://schemas.xmlsoap.org/wsdl/"));
		for(Node node : result){
			System.out.println(node.toXML());
			Element element = (Element) node;
			System.out.println("Column: " + element.getUserData("columnNumber"));
			System.out.println("Line: " + element.getUserData("lineNumber"));
		}

	}
}
