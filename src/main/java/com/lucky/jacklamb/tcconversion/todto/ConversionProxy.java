package com.lucky.jacklamb.tcconversion.todto;

import com.lucky.jacklamb.annotation.conversion.Conversion;
import com.lucky.jacklamb.annotation.conversion.Mapping;
import com.lucky.jacklamb.annotation.conversion.Mappings;
import com.lucky.jacklamb.ioc.ApplicationBeans;
import com.lucky.jacklamb.utils.FieldUtils;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

import java.lang.reflect.*;
import java.util.*;

/**
 * 得到一个LuckyConversion接口的实现类
 */
@Conversion({LuckyConversion.class})
public class ConversionProxy {


    /**
     * 得到一个LuckyConversion接口子接口的代理对象
     * @param childInterfaceClass LuckyConversion子接口的Class
     * @param <T>
     * @return
     */
    public static<T extends LuckyConversion> T getLuckyConversion(Class<T> childInterfaceClass){
        Type[] luckyConversionGenericClass=childInterfaceClass.getGenericInterfaces();
        ParameterizedType interfaceType=(ParameterizedType) luckyConversionGenericClass[0];
        Class<?> entityClass =(Class<?>) interfaceType.getActualTypeArguments()[0];
        Class<?> dtoClass =(Class<?>) interfaceType.getActualTypeArguments()[1];
        Class<? extends LuckyConversion>[] luckyConversionClasses=childInterfaceClass.getAnnotation(Conversion.class).value();
        final Enhancer enhancer=new Enhancer();
        enhancer.setSuperclass(childInterfaceClass);
        MethodInterceptor interceptor=(object, method, params, methodProxy)->{
            String methodName=method.getName();
            if("toEntity".equals(methodName)){
                return change(method,params[0],luckyConversionClasses,false,entityClass);
            }else if("toDto".equals(methodName)){
                return change(method,params[0],luckyConversionClasses,true,dtoClass);
            }else {
                return methodProxy.invokeSuper(object,params);
            }
        };
        enhancer.setCallback(interceptor);
        return  (T)enhancer.create();
    }

    public static List<EntityAndDao> getEntityAndDaoByConversion(Class<? extends LuckyConversion>[] conversionClasses){
        List<EntityAndDao> eds=new ArrayList<>();
        LuckyConversion luckyConversion;
        Type[] conversionGenericTypes;
        ParameterizedType interfaceType;
        Class<?> dtoClass;
        Class<?> entityClass;
        for(Class<? extends LuckyConversion> conversionClass:conversionClasses){
            if(conversionClass==LuckyConversion.class)
                continue;
            luckyConversion=ConversionProxy.getLuckyConversion(conversionClass);
            conversionGenericTypes=conversionClass.getGenericInterfaces();
            interfaceType=(ParameterizedType) conversionGenericTypes[0];
            entityClass=(Class<?>) interfaceType.getActualTypeArguments()[0];
            dtoClass=(Class<?>) interfaceType.getActualTypeArguments()[1];
            eds.add(new EntityAndDao(luckyConversion,entityClass,dtoClass));
        }
        return eds;
    }

    /**
     * 原对象转目标对象
     * @param method 调用的方法
     * @param sourceObj 原对象
     * @param targetClass 目标类的Class对象
     * @return 目标对象
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     * @throws NoSuchMethodException
     */
    private static Object change(Method method, Object sourceObj, Class<? extends LuckyConversion>[] luckyConversionClasses,boolean toDto,Class<?> targetClass) throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        List<EntityAndDao> eds=getEntityAndDaoByConversion(luckyConversionClasses);
        Constructor<?> constructor = targetClass.getConstructor();
        constructor.setAccessible(true);
        Object targetObj=constructor.newInstance();
        Map<String,Object> sourceFieldNameValueMap =getSourceNameValueMap(sourceObj,"");
        Map<String,String> changeMap=new HashMap<>();
        if(method.isAnnotationPresent(Mapping.class)){
            Mapping mapping=method.getAnnotation(Mapping.class);
            changeMap.put(mapping.source(),mapping.target());
        }else if(method.isAnnotationPresent(Mappings.class)){
            Mapping[] mappings=method.getAnnotation(Mappings.class).value();
            for(Mapping mapping:mappings)
                changeMap.put(mapping.source(),mapping.target());
        }

        Set<String> keySet=changeMap.keySet();
        for(String key:keySet){
            if(sourceFieldNameValueMap.containsKey(key)){
                Object changeValue=sourceFieldNameValueMap.get(key);
                sourceFieldNameValueMap.remove(key);
                sourceFieldNameValueMap.put(changeMap.get(key),changeValue);
            }
        }
        return setTargetObject(targetObj,sourceFieldNameValueMap,eds,toDto,"");
    }


    public static Map<String,Object> getSourceNameValueMap(Object sourceObject, String initialName) throws IllegalAccessException {
        Map<String,Object> sourceNameValueMap=new HashMap<>();
        Class<?> sourceClass=sourceObject.getClass();
        Field[] fields=sourceClass.getDeclaredFields();
        Object fieldValue;
        String fieldName;
        for(Field field:fields){
            field.setAccessible(true);
            fieldValue=field.get(sourceObject);
            fieldName="".equals(initialName)?field.getName():initialName+"."+field.getName();
            if(FieldUtils.isBasicSimpleType(field)){
                sourceNameValueMap.put(fieldName,fieldValue);
            }else if(field.getType().getClassLoader()!=null){
                sourceNameValueMap.put(field.getType().getName(),fieldValue);
                Map<String, Object> fieldNameValueMap = getSourceNameValueMap(fieldValue, fieldName);
                for(String key:fieldNameValueMap.keySet()){
                    sourceNameValueMap.put(key,fieldNameValueMap.get(key));
                }
            }
        }
        return sourceNameValueMap;
    }


    /**
     * 为目标对象设置属性
     * @param targetObject 目标对象
     * @param sourceMap 原对象的属性名与属性值所组成的Map
     * @param initialName 属性的原始属性名
     * @return 返回一个目标对象
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public static Object setTargetObject(Object targetObject, Map<String, Object> sourceMap,List<EntityAndDao> eds,boolean toDto, String initialName) throws IllegalAccessException, InstantiationException {
        Class<?> targetClass = targetObject.getClass();
        Field[] targetFields=targetClass.getDeclaredFields();
        Class<?> fieldClass;
        String fieldName;
        for(Field field:targetFields){
            fieldClass=field.getType();
            field.setAccessible(true);
            fieldName="".equals(initialName)?field.getName():initialName+"."+field.getName();
            if(FieldUtils.isBasicSimpleType(field)){
                if(sourceMap.containsKey(fieldName)){
                    field.set(targetObject,sourceMap.get(fieldName));
                }
            }else if(fieldClass.getClassLoader()!=null){
                Object fieldValue=field.getType().newInstance();
                EntityAndDao ed=toDto?EntityAndDao.getEntityAndDtoByDaoClass(eds,field.getType()):EntityAndDao.getEntityAndDtoByEntityClass(eds,field.getType());
                if(ed!=null){
                    String classKey;
                    LuckyConversion conversion=ed.getConversion();
                    if(toDto){//Entity转Dto
                        classKey=ed.getEntityClass().getName();
                        if(sourceMap.containsKey(classKey)){
                            field.set(targetObject,conversion.toDto(sourceMap.get(classKey)));
                        }else {
                            field.set(targetObject,setTargetObject(fieldValue,sourceMap,eds,true,fieldName));
                        }
                    }else{//Dto转Entity
                        classKey=ed.getDtoClass().getName();
                        if(sourceMap.containsKey(classKey)){
                            field.set(targetObject,conversion.toEntity(sourceMap.get(classKey)));
                        }else {
                            field.set(targetObject,setTargetObject(fieldValue,sourceMap,eds,false,fieldName));
                        }
                    }
                }else{
                    field.set(targetObject,setTargetObject(fieldValue,sourceMap,eds,toDto,fieldName));
                }
            }
        }
        return targetObject;
    }

    public static void main(String[] args) throws IllegalAccessException {
        ConversionProxy c=new ConversionProxy();
        User u=new User();
        String[] array={"w","d","r","fd"};
        Map<String,Double> map=new HashMap<>();
        map.put("ss",12.5);
        map.put("cc",33.3);
        TypeO t=new TypeO();
        t.setTypeID(34);
        t.setMap(map);
        t.setTypeName("高效");
        u.setId(1);
        u.setStringList(Arrays.asList(array));
        u.setArray(array);
        u.setName("Jack");
        u.setMath(22.5);
        u.setType(t);
        Map<String, Object> user = c.getSourceNameValueMap(u, "");
        System.out.println(u.getClass()==User.class);
        System.out.println(user);
        System.out.println(user.get(TypeO.class.getName()));
    }
}

class EntityAndDao{

    private LuckyConversion conversion;

    private Class<?> entityClass;

    private Class<?> dtoClass;

    public EntityAndDao(LuckyConversion conversion, Class<?> entityClass, Class<?> dtoClass) {
        this.conversion = conversion;
        this.entityClass = entityClass;
        this.dtoClass = dtoClass;
    }

    public LuckyConversion getConversion() {
        return conversion;
    }

    public void setConversion(LuckyConversion conversion) {
        this.conversion = conversion;
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(Class<?> entityClass) {
        this.entityClass = entityClass;
    }

    public Class<?> getDtoClass() {
        return dtoClass;
    }

    public void setDtoClass(Class<?> dtoClass) {
        this.dtoClass = dtoClass;
    }

    public static EntityAndDao getEntityAndDtoByDaoClass(List<EntityAndDao> eds,Class<?> dtoClass){
        for(EntityAndDao ed:eds){
            if(dtoClass==ed.getDtoClass())
                return ed;
        }
        return null;
    }

    public static EntityAndDao getEntityAndDtoByEntityClass(List<EntityAndDao> eds,Class<?> entityClass){
        for(EntityAndDao ed:eds){
            if(ed.getEntityClass()==entityClass)
                return ed;
        }
        return null;
    }
}


class User{
    private int id;
    private String name;
    private Double math;
    private List<String> stringList;
    private String[] array;

    public String[] getArray() {
        return array;
    }

    public void setArray(String[] array) {
        this.array = array;
    }

    private TypeO type;


    public List<String> getStringList() {
        return stringList;
    }

    public void setStringList(List<String> stringList) {
        this.stringList = stringList;
    }

    public TypeO getType() {
        return type;
    }

    public void setType(TypeO type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getMath() {
        return math;
    }

    public void setMath(Double math) {
        this.math = math;
    }
}

class TypeO{

    private int typeID;
    private String typeName;
    private Map<String,Double> map;

    public Map<String, Double> getMap() {
        return map;
    }

    public void setMap(Map<String, Double> map) {
        this.map = map;
    }

    public int getTypeID() {
        return typeID;
    }

    public void setTypeID(int typeID) {
        this.typeID = typeID;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
}