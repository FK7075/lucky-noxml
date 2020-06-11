package com.lucky.jacklamb.sqlcore.mapper;

import com.lucky.jacklamb.annotation.orm.Id;
import com.lucky.jacklamb.annotation.orm.mapper.*;
import com.lucky.jacklamb.cglib.CglibProxy;
import com.lucky.jacklamb.conversion.proxy.ConversionProxy;
import com.lucky.jacklamb.enums.JOIN;
import com.lucky.jacklamb.enums.PrimaryType;
import com.lucky.jacklamb.enums.Sort;
import com.lucky.jacklamb.exception.NotFindFlieException;
import com.lucky.jacklamb.file.ini.INIConfig;
import com.lucky.jacklamb.ioc.config.AppConfig;
import com.lucky.jacklamb.query.QueryBuilder;
import com.lucky.jacklamb.query.SqlAndObject;
import com.lucky.jacklamb.query.SqlFragProce;
import com.lucky.jacklamb.sqlcore.abstractionlayer.abstcore.SqlCore;
import com.lucky.jacklamb.sqlcore.abstractionlayer.util.PojoManage;
import com.lucky.jacklamb.sqlcore.exception.NotFindSqlConfigFileException;
import com.lucky.jacklamb.sqlcore.exception.NotFoundInterfacesGenericException;
import com.lucky.jacklamb.utils.LuckyUtils;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

@SuppressWarnings("all")
public class LuckyMapperProxy {

    private static final Logger log = LogManager.getLogger(LuckyMapperProxy.class);
    private SqlCore sqlCore;
    private Map<String, String> sqlMap;
    private Class<?> LuckyMapperGeneric;

    public LuckyMapperProxy(SqlCore sql) {
        sqlCore = sql;
        sqlMap = new HashMap<>();
    }

    /**
     * 初始化sqlMap,将配置类中的SQl语句加载到sqlMap中
     *
     * @param clazz 配置类的Class
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    private <T> void initClassSqlMap(Class<T> clazz) throws InstantiationException, IllegalAccessException {
        if (clazz.isAnnotationPresent(Mapper.class)) {
            Mapper map = clazz.getAnnotation(Mapper.class);
            Class<?> sqlClass = map.value();
            if (!sqlClass.isAssignableFrom(Void.class)) {
                Object sqlPo = sqlClass.newInstance();
                Field[] fields = sqlClass.getDeclaredFields();
                for (Field fi : fields) {
                    fi.setAccessible(true);
                    sqlMap.put(fi.getName().toUpperCase(), (String) fi.get(sqlPo));
                }
            }
        }
    }

    /**
     * 加载写在.ini配置文件中的Sql
     *
     * @param clazz
     * @throws IOException
     */
    private <T> void initIniMap(Class<T> clazz) throws IOException {
        String iniSql = AppConfig.getAppConfig().getScanConfig().getSqlIniPath();
        InputStream ini = this.getClass().getResourceAsStream("/" + iniSql);
        if (ini != null) {
            INIConfig app = new INIConfig(iniSql);
            Map<String, String> classMap = app.getSectionMap(clazz.getName());
            if (classMap != null) {
                for (String key : classMap.keySet()) {
                    sqlMap.put(key.toUpperCase(), classMap.get(key));
                }
            }
        }
    }

    /**
     * 加载写在.properties配置文件中Sql语句
     *
     * @param clzz
     */
    private <T> void initSqlMapProperty(Class<T> clzz) {
        if (clzz.isAnnotationPresent(Mapper.class)) {
            Mapper mapper = clzz.getAnnotation(Mapper.class);
            String[] propertys = mapper.properties();
            String coding = mapper.codedformat();
            for (String path : propertys) {
                InputStream resource = this.getClass().getResourceAsStream("/" + path);
                if (resource == null) {
                    log.error("找不到" + clzz.getName() + "的Sql配置文件" + path + "!请检查@Mapper注解上的properties配置信息！");
                    throw new NotFindSqlConfigFileException("找不到" + clzz.getName() + "的Sql配置文件" + path + "!请检查@Mapper注解上的properties配置信息！");
                }
                loadProperty(clzz, resource, coding);

            }
        }
    }

    /**
     * 加载写在.properties配置文件中Sql语句
     *
     * @param clzz
     * @param propertyPath
     * @param coding
     */
    private void loadProperty(Class<?> clzz, InputStream propertyPath, String coding) {
        try {
            Properties p = new Properties();
            p.load(new BufferedReader(new InputStreamReader(propertyPath, coding)));
            Method[] methods = clzz.getDeclaredMethods();
            for (Method method : methods) {
                if (!method.isAnnotationPresent(Select.class) && !method.isAnnotationPresent(Insert.class)
                        && !method.isAnnotationPresent(Update.class) && !method.isAnnotationPresent(Delete.class)
                        && !method.isAnnotationPresent(Query.class) && !method.isAnnotationPresent(Count.class)) {
                    String key = method.getName();
                    String value = p.getProperty(key);
                    if (value != null)
                        sqlMap.put(key.toUpperCase(), value);
                }
            }
        } catch (UnsupportedEncodingException e) {
            throw new NotFindFlieException("找不到文件:" + propertyPath + "，无法加载SQL.... 错误位置(Mapper):" + clzz.getName());
        } catch (FileNotFoundException e) {
            throw new NotFindFlieException("找不到文件:" + propertyPath + "，无法加载SQL.... 错误位置(Mapper):" + clzz.getName());
        } catch (IOException e) {
            throw new NotFindFlieException("找不到文件:" + propertyPath + "，无法加载SQL.... 错误位置(Mapper):" + clzz.getName());
        }


    }

    /**
     * 递归方法：找到Mapper接口继承的LuckyMapper接口中的泛型类型，如果Mapper接口没有继承LuckyMapper则返回null。(结束！)
     * 1.判断Mapper是否为LuckyMapper接口的子接口，如果不是直接返回null。
     * 2.使用Class的getGenericInterfaces()方法得到当前接口的所有ParameterizedType。
     * 3.判断ParameterizedType[]是否含有LuckyMapper,如果已经包含，侧直接返回该ParameterizedType对应的泛型。(结束！)
     * 4.不包含，说明Mapper没有直接继承LuckyMapper,需要使用用此流程操作Mapper接口的直接父接口
     * ----4.1.使用Class的getInterfaces()方法得到Mapper接口所有的直接父接口的Class
     * ----4.2.递归操作这些父接口的Class
     * ----4.3.判断递归结果,如果不为null，则表示当前操作的Class已经直接继承的LuckyMapper接口，返回递归结果。(结束！)
     * @param mapperClass
     * @return
     */
    private Class<?> getLuckyMapperGeneric(Class<?> mapperClass){
        if(LuckyMapper.class.isAssignableFrom(mapperClass)){
            Type[] genericInterfaces = mapperClass.getGenericInterfaces();
            String typeName;
            for (Type anInterface : genericInterfaces) {
                typeName=anInterface.getTypeName();
                typeName=typeName.indexOf("<")!=-1?typeName.substring(0,typeName.indexOf("<")):typeName;
                if(typeName.equals(LuckyMapper.class.getTypeName())){
                    ParameterizedType interfacePtype;
                    try{
                        interfacePtype = (ParameterizedType)anInterface;
                    }catch (ClassCastException e){
                        throw new NotFoundInterfacesGenericException(mapperClass,e);
                    }
                    return (Class<?>) interfacePtype.getActualTypeArguments()[0];
                }
            }
            Class<?>[] interfaces = mapperClass.getInterfaces();
            for (Class<?> anInterface : interfaces) {
                Class<?> result=getLuckyMapperGeneric(anInterface);
                if(result!=null)
                    return result;
            }
            return null;
        }else{
            return null;
        }
    }

    /**
     * 返回接口的代理对象
     *
     * @param clazz 接口的Class
     * @return T
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IOException
     */
    public <T> T getMapperProxyObject(Class<T> clazz) throws InstantiationException, IllegalAccessException, IOException {
        LuckyMapperGeneric=getLuckyMapperGeneric(clazz);
        initIniMap(clazz);
        initClassSqlMap(clazz);
        initSqlMapProperty(clazz);
        return CglibProxy.getCglibProxyObject(clazz,new LuckyMapperMethodInterceptor(LuckyMapperGeneric,sqlCore,sqlMap));
    }


}
