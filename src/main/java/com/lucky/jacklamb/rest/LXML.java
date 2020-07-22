package com.lucky.jacklamb.rest;

import com.lucky.jacklamb.utils.base.LuckyUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;

public class LXML {

    private String xmlStr;


    public String getXmlStr() {
        return xmlStr;
    }

    /**
     * 将pojo转化为XML字符
     *
     * @param pojo
     */
    public LXML(Object pojo) {
        xmlStr= XmlFileUtil.createXmlDocument(pojo);
//        if (pojo == null)
//            xmlStr = "<null/>";
//        else {
//            Class<?> clzz = pojo.getClass();
//            if (Collection.class.isAssignableFrom(clzz)) {
//                try {
//                    xmlStr = collectionToxmlStr((Collection<?>) pojo);
//                } catch (IllegalArgumentException e) {
//                    e.printStackTrace();
//                } catch (IllegalAccessException e) {
//                    e.printStackTrace();
//                }
//            } else if (clzz.isEnum()) {
//                xmlStr = pojo.toString();
//            } else if (clzz.isArray()) {
//                try {
//                    xmlStr = arrayToxmlStr((Object[]) pojo);
//                } catch (IllegalArgumentException e) {
//                    e.printStackTrace();
//                } catch (IllegalAccessException e) {
//                    e.printStackTrace();
//                }
//            } else if (Map.class.isAssignableFrom(clzz)) {
//                xmlStr = mapToxmlStr((Map<Object, Object>) pojo);
//            } else if (clzz.getClassLoader() != null) {
//                try {
//                    xmlStr = objectToxmlStr(pojo);
//                } catch (IllegalArgumentException e) {
//                    e.printStackTrace();
//                } catch (IllegalAccessException e) {
//                    e.printStackTrace();
//                }
//            } else {
//                xmlStr = pojo.toString();
//            }
//        }

    }

    private String mapToxmlStr(Map<Object, Object> map) {
        if (map == null || map.isEmpty()) {
            return "<map/>";
        }
        StringBuilder str = new StringBuilder("<map>");
        for (Entry<Object, Object> entry : map.entrySet()) {
            str.append("<").append(entry.getKey()).append(">").append(new LXML(entry.getValue()).getXmlStr()).append("</").append(entry.getKey()).append(">");
        }
        return str.toString() + "</map>";
    }

    private String arrayToxmlStr(Object[] objects) throws IllegalArgumentException, IllegalAccessException {
        if (objects == null || objects.length == 0) {
            return "<array/>";
        }
        StringBuilder arrayxmlStr = new StringBuilder("<array>");
        List<String> field_json_copy = new ArrayList<>();
        List<String> field_json = new ArrayList<>();
        for (Object objStr : objects) {
            field_json_copy.add(objectToxmlStr(objStr));
        }
        field_json_copy.stream().filter(str -> !"".equals(str)).forEach(field_json::add);
        for (int i = 0; i < field_json.size(); i++) {
            arrayxmlStr.append("<element>").append(field_json.get(i)).append("</element>");
        }
        return arrayxmlStr.toString() + "</array>";
    }

    /**
     * 将List集合形式的Pojo转化为Json格式
     *
     * @param list pojo格式的数据
     * @return
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    private <T> String collectionToxmlStr(Collection<T> list) throws IllegalArgumentException, IllegalAccessException {
        if (list.isEmpty() || list == null) {
            return "<collection/>";
        }
        StringBuilder listxmlStr = new StringBuilder("<collection>");
        List<String> field_json_copy = new ArrayList<>();
        List<String> field_json = new ArrayList<>();
        for (T objStr : list) {
            field_json_copy.add(objectToxmlStr(objStr));
        }
        field_json_copy.stream().filter(str -> !"".equals(str)).forEach(field_json::add);
        for (int i = 0; i < field_json.size(); i++) {
            String string = field_json.get(i);
            if (string.startsWith("<") && string.endsWith(">"))
                listxmlStr.append(string);
            else
                listxmlStr.append("<element>").append(string).append("</element>");
        }
        return listxmlStr.toString() + "</collection>";
    }

    /**
     * 将pojo对象转化为JSON格式的数据
     *
     * @param object pojo对象
     * @return
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    private String objectToxmlStr(Object object) throws IllegalArgumentException, IllegalAccessException {
        if (object == null) {
            return "";
        }
        if (object.getClass().getClassLoader() != null) {
            String className = LuckyUtils.TableToClass1(object.getClass().getSimpleName());
            StringBuilder objxmlStr = new StringBuilder("<").append(className).append(">");
            List<String> field_json_copy = new ArrayList<>();
            List<String> field_json = new ArrayList<>();
            Field[] fields = object.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (!field.getType().getName().equals(object.getClass().getName()))
                    field_json_copy.add(fieldToxmlStr(object, field));
            }
            field_json_copy.stream().filter(str -> !"".equals(str)).forEach(field_json::add);
            for (int i = 0; i < field_json.size(); i++) {
                objxmlStr.append(field_json.get(i));
            }
            return objxmlStr.toString() + "</" + className + ">";
        } else {
            return object.toString();
        }

    }

    /**
     * 将不为null的属性转变为json格式
     *
     * @param field_Obj 目标对象
     * @param field     目标属性
     * @return
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    private String fieldToxmlStr(Object field_Obj, Field field)
            throws IllegalArgumentException, IllegalAccessException {
        StringBuilder fieldxmlStr = new StringBuilder();
        field.setAccessible(true);
        Object obj = field.get(field_Obj);
        Class<?> fieldClass = field.getType();
        if (obj != null) {
            String fieldXmlStr = new LXML(obj).getXmlStr();
            if (fieldClass.isArray()) {
                if (!"<array/>".equals(fieldXmlStr) && !"<array></array>".equals(fieldXmlStr)) {
                    fieldXmlStr = fieldXmlStr.replaceAll("<array>", "<" + AttrUtil.getField(field) + ">").replaceAll("</array>", "</" + AttrUtil.getField(field) + ">");
                    fieldxmlStr.append(fieldXmlStr);
                    return "";
                } else {
                    return fieldxmlStr.toString();
                }
            }
            if (Collection.class.isAssignableFrom(fieldClass)) {
                if (!"<collection/>".equals(fieldXmlStr) && !"<collection></collection>".equals(fieldXmlStr)) {
                    fieldXmlStr = fieldXmlStr.replaceAll("<collection>", "<" + AttrUtil.getField(field) + ">").replaceAll("</collection>", "</" + AttrUtil.getField(field) + ">");
                    fieldxmlStr.append(fieldXmlStr);
                    return fieldxmlStr.toString();
                } else {
                    return "";
                }
            }
            if (Map.class.isAssignableFrom(fieldClass)) {
                if (!"<map/>".equals(fieldXmlStr) && !"<map></map>".equals(fieldXmlStr)) {
                    fieldXmlStr = fieldXmlStr.replaceAll("<map>", "<" + AttrUtil.getField(field) + ">").replaceAll("</map>", "</" + AttrUtil.getField(field) + ">");
                    fieldxmlStr.append(fieldXmlStr);
                    return fieldxmlStr.toString();
                } else {
                    return "";
                }
            }
            return "<" + AttrUtil.getField(field) + ">" + fieldXmlStr + "</" + AttrUtil.getField(field) + ">";
        }
        return fieldxmlStr.toString();
    }

    public static void main(String[] args) {
        TT object = new TT();
        object.setStr("String属性");
        List<Double> list = new ArrayList<>();
        list.add(12.5);
        list.add(55.7);
        list.add(99.999);
        object.setList(list);
        Map<String, Integer> map = new HashMap<>();
        map.put("key1", 111);
        map.put("key2", 222);
        object.setMap(map);
        BB bb = new BB();
        bb.setBname("BNAME");
        String[] arr = {};
        bb.setArray(arr);
        object.setBb(bb);
        List<BB> list_bb = new ArrayList<>();
        list_bb.add(bb);
        list_bb.add(new BB("BB2"));
        object.setList_BB(list_bb);
        Map<String, BB> map_bb = new HashMap<>();
        map_bb.put("map1", bb);
        map_bb.put("map2", bb);
        map_bb.put("map3", new BB("MAPBB"));
        object.setMap_BB(map_bb);
        LXML l = new LXML(object);
        System.out.println(l.getXmlStr());

    }

}

class TT {

    @Attr("TT-STR")
    private String str;

    @Attr("TT-DOUBLE-LIST")
    private List<Double> doublelist;

    @Attr("TT-STRING-BB-MAP")
    private Map<String, BB> map_BB;

    @Attr("TT-BB-LIST")
    private List<BB> list_BB;

    @Attr("TT-STRING-INTEGER-MAP")
    private Map<String, Integer> map;

    @Attr("TT-BB")
    private BB bb;

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }

    public List<Double> getList() {
        return doublelist;
    }

    public void setList(List<Double> list) {
        this.doublelist = list;
    }

    public Map<String, BB> getMap_BB() {
        return map_BB;
    }

    public void setMap_BB(Map<String, BB> map_BB) {
        this.map_BB = map_BB;
    }

    public List<BB> getList_BB() {
        return list_BB;
    }

    public void setList_BB(List<BB> list_BB) {
        this.list_BB = list_BB;
    }

    public Map<String, Integer> getMap() {
        return map;
    }

    public void setMap(Map<String, Integer> map) {
        this.map = map;
    }

    public BB getBb() {
        return bb;
    }

    public void setBb(BB bb) {
        this.bb = bb;
    }


}

class BB {

    public BB() {
    }

    public BB(String bname) {
        this.bname = bname;
    }

    @Attr("BB-BNAME")
    private String bname;

    @Attr("BB-ARRAY")
    private String[] stringarray;

    public String getBname() {
        return bname;
    }

    public void setBname(String bname) {
        this.bname = bname;
    }

    public String[] getArray() {
        return stringarray;
    }

    public void setArray(String[] array) {
        this.stringarray = array;
    }


}


