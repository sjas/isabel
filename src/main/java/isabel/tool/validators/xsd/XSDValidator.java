package isabel.tool.validators.xsd;

import isabel.tool.ValidationException;
import org.pmw.tinylog.Logger;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.validation.SchemaFactory;
import java.io.File;

class XSDValidator {

	private static SchemaFactory sFactory = SchemaFactory
			.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

	public void validate(String file) throws ValidationException {
		// Load the main schema
		try {
			sFactory.newSchema(new File(file));
			Logger.debug("File " + file + " is a valid XSD file");
		} catch (SAXException e) {
			throw new ValidationException("File " + file + " is no XML Schema",
					e);
		}
	}
}
