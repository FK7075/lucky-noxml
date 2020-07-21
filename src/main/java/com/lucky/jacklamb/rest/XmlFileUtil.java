package com.lucky.jacklamb.rest;

import com.lucky.jacklamb.utils.reflect.ClassUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;


public class XmlFileUtil {

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static void toElement(Object object, Element root) {
        if (object != null) {
            if ((object instanceof Number) || (object instanceof Boolean) || (object instanceof String) || (object instanceof Double) || (object instanceof Float)) {
                root.setText(object.toString());
            } else if (object instanceof Map) {
                mapToElement((Map) object, root);
            } else if (object instanceof Collection) {
                collToElement((Collection) object, root);
            } else {
                pojoToElement(object, root);
            }
        } else {
            root.setText("");
        }
    }

    /**
     * list中存放的数据类型有基本类型、用户自定对象 map、list
     *
     * @param coll
     * @param root
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static void collToElement(Collection<?> coll, Element root) {
        for (Iterator<?> it = coll.iterator(); it.hasNext();) {
            Object value = it.next();
            if (coll == value) {
                continue;
            }
            if ((value instanceof Number) || (value instanceof Boolean) || (value instanceof String) || (value instanceof Double) || (value instanceof Float)) {
                Class<?> classes = value.getClass();
                String objName = classes.getName();
                String elementName = objName.substring(objName.lastIndexOf(".") + 1, objName.length());
                Element elementOfObject = root.addElement(elementName);
                elementOfObject.setText(value.toString());
            } else if (value instanceof Map) {
                Class<?> classes = value.getClass();
                String objName = classes.getName();
                String elementName = objName.substring(objName.lastIndexOf(".") + 1, objName.length());
                Element elementOfObject = root.addElement(elementName);
                mapToElement((Map) value, elementOfObject);
            } else if (value instanceof Collection) {
                Class<?> classes = value.getClass();
                String objName = classes.getName();
                String elementName = objName.substring(objName.lastIndexOf(".") + 1, objName.length());
                Element elementOfObject = root.addElement(elementName);
                collToElement((Collection) value, elementOfObject);
            } else {
                toElement(value, root);
            }

        }
    };

    /**
     * map中存放的数据类型有基本类型、用户自定对象 map、list
     *
     * @param map
     * @param root
     */

    @SuppressWarnings("rawtypes")
    private static void mapToElement(Map<String, Object> map, Element root) {
        for (Iterator<?> it = map.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            String name = (String) entry.getKey();
            if (null == name)
                continue;
            Object value = entry.getValue();
            if (value == map) {

            }
            Element elementValue = root.addElement(name);
            toElement(value, elementValue);
        }
    }

    /**
     *
     * @param obj
     * @param root
     */
    private static void pojoToElement(Object obj, Element root) {
        Class<?> classes = obj.getClass();
        String elementName = classes.getSimpleName();
        /** 该类为一个节点 */
        Element elementOfObject = null;
        if(!root.getName().equals(elementName)){
            elementOfObject = root.addElement(elementName);
        }else{
            elementOfObject = root;
        }
        Field[] fields = ClassUtils.getAllFields(classes);
        for (Field f : fields) {
            if (Modifier.isStatic(f.getModifiers()))
                continue;
            String name = f.getName();
            f.setAccessible(true);
            Object value = null;
            try {
                value = f.get(obj);
            } catch (Exception e) {
                value = null;
            }
            Element elementValue = elementOfObject.addElement(name);
            toElement(value, elementValue);
        }
    }

    public static String createXmlDocument(Object obj) {
        Class<?> classes = obj.getClass();
        String elementName = classes.getSimpleName();
         return createXmlDocument(obj,elementName);
    }

    public static String createXmlDocument(Object obj,String xmlRootName) {
        Document xmlDoc = DocumentHelper.createDocument();
        Element root = xmlDoc.addElement(xmlRootName);
        toElement(obj, root);
        String xmlStr = "";
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("UTF-8"); // 指定XML编码
        StringWriter writerStr = new StringWriter();
        try {
            XMLWriter xmlw = new XMLWriter(writerStr,format);
//            XMLWriter xmlw = new XMLWriter(new OutputStreamWriter(new FileOutputStream(new File(filePath)), "UTF-8"), format);
            xmlw.write(xmlDoc);
            xmlw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return writerStr.getBuffer().toString();
    }
}
