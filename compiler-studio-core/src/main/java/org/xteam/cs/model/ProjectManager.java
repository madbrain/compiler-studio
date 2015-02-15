package org.xteam.cs.model;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Map.Entry;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

public class ProjectManager {

	private static final String XML_PROJECT_NS = "http://www.xteam.org/cs/project";
	private Map<String, IResourceFactory> factories = new HashMap<String, IResourceFactory>();
	private List<IBuilder> builders = new ArrayList<IBuilder>();
	
	public ProjectManager() {
		createLoaders();
		createBuilders();
	}
	
	protected void createLoaders() {
		ServiceLoader<IResourceFactory> loader = ServiceLoader.load(IResourceFactory.class);
		Iterator<IResourceFactory> i = loader.iterator();
		while (i.hasNext()) {
			registerResourceFactory(i.next());
		}
	}
	
	protected void createBuilders() {
		ServiceLoader<IBuilder> loader = ServiceLoader.load(IBuilder.class);
		Iterator<IBuilder> i = loader.iterator();
		while (i.hasNext()) {
			builders.add(i.next());
		}
	}

	public void registerResourceFactory(IResourceFactory factory) {
		this.factories.put(factory.getExtension(), factory);
	}
	
	public ProjectResource createResource(Project project, File file) {
		IResourceFactory factory = factories.get(getExtension(file));
		if (factory != null) {
			return factory.create(project, file);
		}
		return null;
	}
	
	private String getExtension(File file) {
		String name = file.getName();
		int pos = name.lastIndexOf('.');
		if (pos < 0)
			return "";
		return name.substring(pos+1);
	}

	public boolean saveProject(Project project, File file) {
		try {
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element projectElem = doc.createElementNS(XML_PROJECT_NS, "project");
			addProperties(doc, projectElem, project.getProperties());
			for (ProjectResource f : project.getResources()) {
				if (f instanceof FileResource) {
					Element resourceElem = doc.createElementNS(XML_PROJECT_NS, "resource");
					resourceElem.setAttribute("path", makeRelative(((FileResource)f).getFile(), file));
					projectElem.appendChild(resourceElem);
					addProperties(doc, resourceElem, f.getProperties());
				}
			}
			DOMImplementationLS ls = (DOMImplementationLS) doc.getImplementation().getFeature("LS", "3.0");
			LSOutput out = ls.createLSOutput();
			Writer writer = new FileWriter(file);
			out.setCharacterStream(writer);
			LSSerializer lsser = ls.createLSSerializer();
			lsser.getDomConfig().setParameter("format-pretty-print", true);
			lsser.write(projectElem, out);
			writer.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private void addProperties(Document doc, Element elem, BaseProperties properties) {
		Properties simpleProperties = properties.getPropertyMap();
		for (Entry<Object, Object> entry : simpleProperties.entrySet()) {
			Element propertyElem = doc.createElementNS(XML_PROJECT_NS, "property");
			propertyElem.setAttribute("name", entry.getKey().toString());
			propertyElem.setAttribute("value", entry.getValue().toString());
			elem.appendChild(propertyElem);
		}
	}

	private String makeRelative(File file, File ref) {
		String[] filePath = file.getParentFile().getAbsolutePath().split(File.separator);
		String[] refPath = ref.getParentFile().getAbsolutePath().split(File.separator);
		int  index = 0;
		while (index < filePath.length && index < refPath.length) {
			if (! filePath[index].equals(refPath[index])) {
				break;
			}
			++index;
		}
		StringBuffer buffer = new StringBuffer();
		for (int i = index; i < refPath.length; ++i) {
			buffer.append("..").append(File.separator);
		}
		for (int i = index; i < filePath.length; ++i) {
			buffer.append(filePath[i]).append(File.separator);
		}
		buffer.append(file.getName());
		return buffer.toString();
	}

	public boolean loadProject(Project project, File file) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setNamespaceAware(true);
			factory.setSchema(SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
					.newSchema(new StreamSource(getClass().getResourceAsStream("/resources/project.xsd"))));
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(file);
			Element projectElement = doc.getDocumentElement();
			project.clear();
			project.setProperties(makeProperties(projectElement));
			NodeList resources = projectElement.getElementsByTagNameNS(XML_PROJECT_NS, "resource");
			for (int i = 0; i < resources.getLength(); ++i) {
				Element res = (Element) resources.item(i);
				ProjectResource f = createResource(project,
						new File(file.getParentFile(), res.getAttribute("path")));
				if (f != null) {
					project.getResources().add(f);
					f.setProperties(makeProperties(res));
				}
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	private Properties makeProperties(Element e) {
		NodeList properties = e.getChildNodes();
		Properties prop = new Properties();
		for (int i = 0; i < properties.getLength(); ++i) {
			if ( ! (properties.item(i) instanceof Element))
				continue;
			Element p = (Element) properties.item(i);
			if (p.getTagName().equals("property")
					&& p.getNamespaceURI().equals(XML_PROJECT_NS)) {
				prop.put(p.getAttribute("name"), p.getAttribute("value"));
			}
		}
		return prop;
	}

	public void buildProject(Project project, IProgressMonitor monitor) {
		for (IBuilder builder : builders) {
			builder.initialize(project, monitor);
			builder.build();
		}
		project.finishBuilding();
	}

	public void generateProject(Project project, IProgressMonitor monitor) {
		for (IBuilder builder : builders) {
			builder.initialize(project, monitor);
			builder.generate();
		}
	}

}
