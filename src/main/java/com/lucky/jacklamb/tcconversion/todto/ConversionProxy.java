package com.lucky.jacklamb.tcconversion.todto;

import com.lucky.jacklamb.annotation.conversion.Conversion;
import com.lucky.jacklamb.annotation.conversion.Mapping;
import com.lucky.jacklamb.annotation.conversion.Mappings;
import com.lucky.jacklamb.utils.FieldUtils;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 得到一个LuckyConversion接口的实现类
 */
public class ConversionProxy {
    /*
        一、Entity与Dto之间的转换模式：
        1.编写转换接口，该接口必须继承LuckyConversion<E,D>接口，并指定接口的泛型！
        2.如果待转换的Entity与Dto之间的属性完全相同则不需要配置！
        3.指定属性之间相互转换需要借助@Mappings或者@Mapping注解，设置source和target属性指定原字段与目标字段！
        4.如果带转化的对象之中包含其他带转化对象，可以使用@Conversion注解将该对象的LuckyConversion接口实现类注入，该对象的转化交由此Conversion来转换

        二、Conversion接口代理对象的获取
        1.传入一个LuckyConversion接口的子接口的Class
        2.方法内部会获取到该接口的父接口LuckyConversion中的两个泛型
        3.在执行toDto和toEntity方法时会分别做不同的代理
        4.以toDto方法的执行过程为例
            1.找出该接口上@Conversion中配置的LuckyConversion数组
            2.根据这个数组得到一个EntityAndDao集合，EntityAndDao对象中封装的是LuckyConversion的代理对象、Dto泛型和Entity泛型
            3.从传入的参数中获取待转换的Entity对象，并将这个对象转化为一个"全属性名"和属性值组成的Map<String,Object>
            注:原对象中每个自定义类型都会特别生成一个“全类名”=“对象值”的K-V，每个泛型为自定义类型的集合也会特别生成一个“Collection<全类名>”=“集合值”的K-V
            eg：
                Map ==>{
                            name=Jack,(普通属性)
                            age=24,
                            type.name=TYPE-NAME,（嵌套在对象中的属性）
                            com.lucky.Type=com.lucky.Type@a09ee92,(自定义的类型)
                            Collection<com.lucky.Type>=[com.lucky.Type@a04eg12]（泛型为自定义类型的集合）
                        }
            4.检查方法上@Mapping注解或者@Mappings注解中配置的转换映射（source->target），并使用映射Value代替Map中的映射Key，以达到特殊映射的目的
            5.通过反射创建一个空的Dto对象，遍历这个对象的所有属性，并使用属性的“全属性名”去Map中拿到该属性的值，
            首先会检查这个类型的LuckyConversion是否已被配置在@Conversion注解中，如果存在，则调用该LuckyConversion对象执行对这个entity的转换,
            否则创建一个空对象，继续遍历这个属性对象的所有属性
            6.如果是泛型为定义类型集合，则去找对应的LuckyConversion，找不到则会抛出异常！
     */


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
     * @param luckyConversionClasses @Conversion注解中的LuckyConversion的Class[]
     * @param toDto 当前执行的方法是否为toDto
     * @param targetClass 目标对象的CLass
     * @return
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


    /**
     *
     * @param sourceObject 原对象
     * @param initialName 初始属性名，用于递归时获取带层级的属性名
     * @return
     * @throws IllegalAccessException
     */
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
            }else if(Collection.class.isAssignableFrom(field.getType())){
                Class<?> genericClass=FieldUtils.getGenericType(field)[0];
                sourceNameValueMap.put("Collection<"+genericClass.getName()+">",fieldValue);
            }
        }
        return sourceNameValueMap;
    }

    /**
     * 为目标对象设置属性
     * @param targetObject 目标对象
     * @param sourceMap 原对象的属性名与属性值所组成的Map
     * @param eds EntityAndDto对象集合
     * @param toDto 当前执行的方法是否为toDto
     * @param initialName 初始属性名，用于递归时获取带层级的属性名
     * @return
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
            }if(Collection.class.isAssignableFrom(field.getType())){
                Class<?> genericClass=FieldUtils.getGenericType(field)[0];
                EntityAndDao ed;
                String classKey;
                LuckyConversion conversion;
                if(toDto){
                    ed=EntityAndDao.getEntityAndDtoByDaoClass(eds,genericClass);
                    if(ed==null)
                        throw new RuntimeException("在@Conversion注解中找不到"+genericClass+"类相对应的LuckyConversion，无法转换属性（"+field.getType()+"）"+field.getName());
                    conversion=ed.getConversion();
                    classKey="Collection<"+ed.getEntityClass().getName()+">";
                    if(sourceMap.containsKey(classKey)){
                        Collection coll=(Collection) sourceMap.get(classKey);
                        Object collect = coll.stream().map(a -> conversion.toDto(a)).collect(Collectors.toList());
                        if(List.class.isAssignableFrom(field.getType()))
                            field.set(targetObject,collect);
                        else if(Set.class.isAssignableFrom(field.getType()))
                            field.set(targetObject,new HashSet((List)collect));
                    }
                }else{
                    ed=EntityAndDao.getEntityAndDtoByEntityClass(eds,genericClass);
                    conversion=ed.getConversion();
                    classKey="Collection<"+ed.getDtoClass().getName()+">";
                    if(sourceMap.containsKey(classKey)){
                        Collection coll=(Collection) sourceMap.get(classKey);
                        Object collect = coll.stream().map(a -> conversion.toEntity(a)).collect(Collectors.toList());
                        if(List.class.isAssignableFrom(field.getType()))
                            field.set(targetObject,collect);
                        else if(Set.class.isAssignableFrom(field.getType()))
                            field.set(targetObject,new HashSet((List)collect));
                    }
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
        List<TypeO> list=new ArrayList<>();
        list.add(t);
        u.setId(1);
        u.setStringList(Arrays.asList(array));
        u.setArray(array);
        u.setName("Jack");
        u.setMath(22.5);
        u.setType(t);
        u.setType0list(list);
        Map<String, Object> user = c.getSourceNameValueMap(u, "");
        System.out.println(u.getClass()==User.class);
        System.out.println(user);
        System.out.println(user.get("Collection<"+TypeO.class.getName()+">"));
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
    private List<TypeO> type0list;

    public List<TypeO> getType0list() {
        return type0list;
    }

    public void setType0list(List<TypeO> type0list) {
        this.type0list = type0list;
    }

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