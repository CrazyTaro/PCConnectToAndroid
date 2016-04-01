package net.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XmlParser {

	public static Document initial() {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			return builder.newDocument();
		} catch (ParserConfigurationException e) {
			CommonUtils.logError(e);
			return null;
		}
	}

	public static boolean createNewXml(XmlNode rootNode, String fileNameWithPath, String charset) {
		Document document = initial();
		if (document == null || rootNode == null) {
			CommonUtils.logError("创建xml文档失败");
			return false;
		} else {
			try {
				File xmlFile = new File(fileNameWithPath);
				if (xmlFile.isDirectory()) {
					return false;
				} else {
					xmlFile.delete();
					xmlFile.createNewFile();
				}
				Element root = createNewElement(document, rootNode);
				if(root==null){
					throw new RuntimeException("发生某些错误,请检查是否数据不存在或者再次尝试!");
				}
				document.appendChild(root);
				TransformerFactory factory = TransformerFactory.newInstance();

				Transformer transformer = factory.newTransformer();
				DOMSource source = new DOMSource(document);
				transformer.setOutputProperty(OutputKeys.ENCODING, charset);
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				PrintWriter pWriter = new PrintWriter(new FileOutputStream(xmlFile));
				StreamResult result = new StreamResult(pWriter);
				transformer.transform(source, result);
				CommonUtils.logInfo("创建XML文件成功,存放路径 : " + fileNameWithPath);
				pWriter.close();
				return true;
			} catch (Exception e) {
				CommonUtils.logError(e);
				return false;
			}
		}
	}

	public static Element createNewElement(Document document, XmlNode parentNode) {
		if (parentNode != null) {
			Element itElement = document.createElement(parentNode.name);
			if(parentNode.getContent()!=null){
				itElement.setTextContent(parentNode.getContent());
			}
			if (parentNode.hasAttribute()) {
				itElement.setAttribute(parentNode.getAttrName(), parentNode.getAttrValue());
			}
			if (parentNode.hasSubNode()) {
				for (XmlNode node : parentNode.getSubNode()) {
					if (node != null) {
						Element childElement = createNewElement(document, node);
						itElement.appendChild(childElement);
					}
				}
			}
			return itElement;
		} else {
			return null;
		}
	}

	public static class XmlNode {
		public String name = null;
		private String attrName = null;
		private String attrValue = null;
		private String content = null;
		private List<XmlNode> subNode = null;

		public XmlNode(String name) {
			this(name, null);
		}

		public XmlNode(String name, String content) {
			this.name = name;
			this.content = content;
		}

		private void checkSubNodeList() {
			if (subNode == null) {
				subNode = new LinkedList<XmlNode>();
			}
		}

		public String getAttrName() {
			return attrName;
		}

		public String getAttrValue() {
			return attrValue;
		}
		
		public String getContent(){
			return content;
		}
		
		public void setContent(String content){
			this.content=content;
		}

		public List<XmlNode> getSubNode() {
			return subNode;
		}

		public boolean hasAttribute() {
			if (attrName != null && attrValue != null) {
				return true;
			} else {
				return false;
			}
		}

		public boolean hasSubNode() {
			if (subNode != null && subNode.size() > 0) {
				return true;
			} else {
				return false;
			}
		}

		public void addAttribute(String name, String value) {
			if (name != null && value != null) {
				attrName = name;
				attrValue = value;
			}
		}

		public void addSubNode(XmlNode node) {
			checkSubNodeList();
			subNode.add(node);
		}

		public XmlNode removeSubNode(int index) {
			if (subNode != null && subNode.size() > index) {
				return subNode.remove(index);
			} else {
				return null;
			}
		}
	}
}
