package cn.com.wysha.config_Manager.manger.xml;

import cn.com.wysha.config_Manager.manger.Manager;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.FileWriter;
import java.util.Map;
import java.util.Set;

public class XmlManager extends Manager {
    private Document getXmlFile(String path) {
        File file = new File(path);
        try {
            if (file.exists()) {
                return new SAXReader().read(file);
            }else {
                return DocumentHelper.createDocument();
            }
        } catch (DocumentException e) {
            if (file.exists()) {
                file.delete();
            }
            return DocumentHelper.createDocument();
        }
    }
    @Override
    protected void read(Object obj, String filePath, Map<String, Set<FieldElement>> map) {
        try {
            Document document = getXmlFile(filePath);

            Element rootElement = document.getRootElement();
            if (rootElement == null){
                return;
            }

            map.forEach((sectionPath, set) -> {
                Element element = rootElement;
                String[] sections = sectionPath.split("\\.");
                for (String string : sections){
                    element = element.element(string);
                    if (element == null){
                        return;
                    }
                }

                Element finalElement = element;
                set.forEach((fieldElement) -> {
                    String value = finalElement.elementText(fieldElement.key());

                    if (value != null){
                        try {
                            fieldElement.field().set(obj, fieldElement.converter().stringToObj(value));
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void write(Object obj, String filePath, Map<String, Set<FieldElement>> map) {
        try {
            Document document = getXmlFile(filePath);

            Element rootElement = document.getRootElement();
            if (rootElement == null){
                rootElement = document.addElement("DATA");
            }

            Element finalRootElement = rootElement;
            map.forEach((sectionPath, set) -> {
                Element element = finalRootElement;
                String[] sections = sectionPath.split("\\.");
                for (String string : sections){
                    Element tmp = element.element(string);
                    if (tmp == null){
                        tmp = element.addElement(string);
                    }
                    element = tmp;
                }

                Element finalElement = element;
                set.forEach((fieldElement) -> {
                    try {
                        Object value = fieldElement.field().get(obj);

                        Element tmp = finalElement.element(fieldElement.key());
                        tmp = tmp == null ? finalElement.addElement(fieldElement.key()) : tmp;
                        tmp.setText(fieldElement.converter().objToString(value));
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                });
            });

            XMLWriter writer = new XMLWriter(new FileWriter(filePath));
            writer.write(document);
            writer.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
