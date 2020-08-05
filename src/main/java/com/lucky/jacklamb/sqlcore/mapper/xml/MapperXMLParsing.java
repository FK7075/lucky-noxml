package com.lucky.jacklamb.sqlcore.mapper.xml;

import com.lucky.jacklamb.sqlcore.mapper.LuckyMapper;
import com.lucky.jacklamb.sqlcore.mapper.exception.XMLParsingException;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2020/8/5 11:05 下午
 */
public class MapperXMLParsing {

    private static final String MAPPER_INTERFACE="MapperInterface";
    private static final String CLASS="class";
    private static final String NAME="name";
    private static final String METHOD="Method";

    private Map<String, Map<String,String>> xmlMap=new HashMap<>();

    public Map<String, Map<String, String>> getXmlMap() {
        return xmlMap;
    }

    public boolean isExistClass(Class<?> mapperClass){
        return xmlMap.containsKey(mapperClass.getName());
    }

    public Map<String, String> getMapperSql(Class<?> mapperClass){
        return xmlMap.get(mapperClass.getName());
    }

    private BufferedReader xmlReader;

    public MapperXMLParsing(String xmlPath) throws IOException {
        xmlReader=new BufferedReader(new InputStreamReader(new FileInputStream(xmlPath),"UTF-8"));
        xmlPars();
    }

    public MapperXMLParsing(InputStream xmlInputStream) throws IOException {
        xmlReader=new BufferedReader(new InputStreamReader(xmlInputStream,"UTF-8"));
        xmlPars();
    }

    public MapperXMLParsing(Reader xmlReader){
        this.xmlReader=new BufferedReader(xmlReader);
        xmlPars();
    }

    public void xmlPars(){
        try{
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(xmlReader);
            Element root = document.getRootElement();
            List<Element> mapperInterfaces = root.elements(MAPPER_INTERFACE);
            for (Element mapInter : mapperInterfaces) {
                String mapperClass=mapInter.attributeValue(CLASS);
                Map<String,String> mapperSql=new HashMap<>();
                List<Element> methodElements = mapInter.elements(METHOD);
                for (Element mElement : methodElements) {
                    mapperSql.put(mElement.attributeValue(NAME),mElement.getText().replaceAll("\r\n"," ").replaceAll("\n"," ").trim().replaceAll(" +"," "));
                }
                xmlMap.put(mapperClass,mapperSql);
            }
        }catch (DocumentException e){
            throw new XMLParsingException(e);
        }
    }

    public static void main(String[] args) throws IOException {
        MapperXMLParsing xml=new MapperXMLParsing("/Users/fukang/IDEA-WORK/lucky-noxml/src/main/java/com/lucky/jacklamb/sqlcore/mapper/xml/Mapper.xml");
        System.out.println(xml.getMapperSql(LuckyMapper.class));
        System.out.println(xml.getMapperSql(MapperXMLParsing.class));
    }

}
