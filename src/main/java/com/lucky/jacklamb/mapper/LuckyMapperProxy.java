package com.lucky.jacklamb.mapper;

import com.lucky.jacklamb.annotation.orm.Id;
import com.lucky.jacklamb.annotation.orm.mapper.*;
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
    private <T> void initSqlMap(Class<T> clazz) throws InstantiationException, IllegalAccessException {
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
     * 执行带有SQL的接口方法
     *
     * @param method 接口方法
     * @param args   参数列表
     * @param sql_fp SQl片段化
     * @param sql    sql语句
     * @return
     * @throws NoSuchFieldException
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    private boolean updateSql(Method method, Object[] args, SqlFragProce sql_fp, String sql) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        if (sql.contains("#{")) {
            Class<?> obc = args[0].getClass();
            SqlAndArray sqlArr = noSqlTo(obc, sql);
            if (method.isAnnotationPresent(Change.class)) {
                return dynamicUpdateSql(sql_fp, method, sqlArr.getSql(), sqlArr.getArray());
            } else {
                return sqlCore.updateMethod(sqlArr.getSql(), method, sqlArr.getArray());
            }
        } else {
            if (method.isAnnotationPresent(Change.class)) {
                return dynamicUpdateSql(sql_fp, method, sql, args);
            } else {
                return sqlCore.updateMethod(sql, method, args);
            }
        }
    }

    /**
     * 将含有#{}的sql转化为预编译的sql
     *
     * @param obj   上下文对象
     * @param noSql 包含#{}的sql
     * @return SqlAndArray对象包含预编译sql和执行参数
     * @throws NoSuchFieldException
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    private SqlAndArray noSqlTo(Object obj, String noSql) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        SqlAndArray sqlArr = new SqlAndArray();
        List<String> fieldname = LuckyUtils.getSqlField(noSql);
        Map<String, Object> fieldNameValueMap = ConversionProxy.getSourceNameValueMap(obj, "");
        //得到预编译的SQL语句
        noSql = LuckyUtils.getSqlStatem(noSql);
        List<Object> fields = new ArrayList<>();
        for (String fieldName : fieldname)
            if (fieldNameValueMap.containsKey(fieldName))
                fields.add(fieldNameValueMap.get(fieldName));
        sqlArr.setSql(noSql);
        sqlArr.setArray(fields.toArray());
        return sqlArr;
    }


    /**
     * 基于非空检查的SQL语句的执行
     *
     * @param sql_fp SQL片段化
     * @param sql    sql语句(预编译)
     * @param args   执行参数
     * @return true/false
     */
    private boolean dynamicUpdateSql(SqlFragProce sql_fp, Method method, String sql, Object[] args) {
        SqlAndObject so = sql_fp.filterSql(sql, args);
        return sqlCore.updateMethod(so.getSqlStr(), method, so.getObjects());
    }

    /**
     * 得到List的泛型的Class
     *
     * @param method 接口方法
     * @return List的泛型类型的Class
     */
    private Class<?> getListGeneric(Method method) {
        ParameterizedType type = (ParameterizedType) method.getGenericReturnType();
        Type[] entry = type.getActualTypeArguments();
        Class<?> cla = (Class<?>) entry[0];
        return cla;
    }

    /**
     * 处理被@Select注解标注的接口方法
     *
     * @param method 接口方法
     * @param args   参数列表
     * @param sql_fp SQl片段化类
     * @return Object
     * @throws NoSuchFieldException
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    private <T> Object select(Method method, Object[] args, SqlFragProce sql_fp) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Class<?> c = method.getReturnType();
        Select sel = method.getAnnotation(Select.class);
        if (sel.byid()) {
            if (args.length == 2) {
                return sqlCore.getOne((Class<?>) args[0], args[1]);
            } else if (args.length == 1) {
                return sqlCore.getOne(method.getReturnType(), args[0]);
            } else {
                return false;
            }
        } else {
            String sql = sel.value();
            if ("".equals(sql)) {
                if (sel.sResults().length == 0 && sel.hResults().length == 0) {
                    if (List.class.isAssignableFrom(c)) {
                        return sqlCore.getList(args[0]);
                    } else {
                        return sqlCore.getObject(args[0]);
                    }
                } else {// 有指定列的标注
                    if (sel.hResults().length != 0 && sel.sResults().length != 0)
                        throw new RuntimeException("@Select注解的\"hResults\"属性和\"sResults\"属性不可以同时使用！错误位置：" + method);
                    Parameter[] parameters = method.getParameters();
                    QueryBuilder query = new QueryBuilder();
                    query.addObject(args);
                    query.setJoin(JOIN.INNER_JOIN);
                    if (sel.sResults().length != 0)
                        query.addResult(sel.sResults());
                    if (sel.hResults().length != 0)
                        query.hiddenResult(sel.hResults());
                    if (List.class.isAssignableFrom(c)) {
                        Class<?> listGeneric = getListGeneric(method);
                        return sqlCore.query(query, listGeneric);
                    } else {
                        List<?> list = sqlCore.query(query, c);
                        if (list == null || list.isEmpty()) {
                            return null;
                        } else {
                            return list.get(0);
                        }
                    }
                }
            } else {
                if (sql.contains("#{")) {
                    if (method.getParameterCount() == 3)
                        pageParam(method, args);
                    SqlAndArray sqlArr = noSqlTo(args[0], sql);
                    if (List.class.isAssignableFrom(c)) {
                        Class<?> listGeneric = getListGeneric(method);
                        if (method.isAnnotationPresent(Change.class)) {
                            SqlAndObject so = sql_fp.filterSql(sqlArr.getSql(), sqlArr.getArray());
                            if (method.getParameterCount() == 3) {
                                List<Object> list = new ArrayList<>();
                                list.addAll(Arrays.asList(so.getObjects()));
                                list.add(args[1]);
                                list.add(args[2]);
                                return (List<T>) sqlCore.getListMethod(listGeneric, method, so.getSqlStr(), list.toArray());
                            }
                            return (List<T>) sqlCore.getListMethod(listGeneric, method, so.getSqlStr(), so.getObjects());
                        } else {
                            if (method.getParameterCount() == 3) {
                                List<Object> list = new ArrayList<>();
                                list.addAll(Arrays.asList(sqlArr.getArray()));
                                list.add(args[1]);
                                list.add(args[2]);
                                return (List<T>) sqlCore.getListMethod(listGeneric, method, sqlArr.getSql(), list.toArray());
                            }
                            return (List<T>) sqlCore.getListMethod(listGeneric, method, sqlArr.getSql(), sqlArr.getArray());
                        }
                    } else {
                        List<T> list = new ArrayList<>();
                        if (method.isAnnotationPresent(Change.class)) {
                            SqlAndObject so = sql_fp.filterSql(sqlArr.getSql(), sqlArr.getArray());
                            if (method.getParameterCount() == 3) {
                                List<Object> lists = new ArrayList<>();
                                lists.addAll(Arrays.asList(so.getObjects()));
                                lists.add(args[1]);
                                lists.add(args[2]);
                                return (List<T>) sqlCore.getListMethod(c, method, so.getSqlStr(), lists.toArray());
                            }
                            return (T) sqlCore.getObjectMethod(c, method, so.getSqlStr(), so.getObjects());
                        } else {
                            if (method.getParameterCount() == 3) {
                                List<Object> lists = new ArrayList<>();
                                lists.addAll(Arrays.asList(sqlArr.getArray()));
                                lists.add(args[1]);
                                lists.add(args[2]);
                                return (List<T>) sqlCore.getListMethod(c, method, sqlArr.getSql(), list.toArray());
                            }
                            return (T) sqlCore.getObjectMethod(c, method, sqlArr.getSql(), sqlArr.getArray());
                        }
                    }
                } else {
                    pageParam(method, args);
                    if (List.class.isAssignableFrom(c)) {
                        Class<?> listGeneric = getListGeneric(method);
                        if (method.isAnnotationPresent(Change.class)) {
                            SqlAndObject so = sql_fp.filterSql(sql, args);
                            return (List<T>) sqlCore.getListMethod(listGeneric, method, so.getSqlStr(), so.getObjects());
                        } else {
                            return (List<T>) sqlCore.getListMethod(listGeneric, method, sql, args);
                        }
                    } else {
                        List<T> list = new ArrayList<>();
                        if (method.isAnnotationPresent(Change.class)) {
                            SqlAndObject so = sql_fp.filterSql(sql, args);
                            return (T) sqlCore.getObjectMethod(c, method, so.getSqlStr(), so.getObjects());
                        } else {
                            return (T) sqlCore.getObjectMethod(c, method, sql, args);
                        }
                    }
                }
            }
        }
    }


    /**
     * 处理被@Update注解标注的接口方法
     *
     * @param method 接口方法
     * @param args   参数列表
     * @param sql_fp SQl片段化类
     * @return true/false
     * @throws NoSuchFieldException
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    private <T> boolean update(Method method, Object[] args, SqlFragProce sql_fp) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Update upd = method.getAnnotation(Update.class);
        if (upd.batch())
            return sqlCore.updateBatchByCollection((Collection<T>) args[0]);
        String sql = upd.value();
        if ("".equals(sql)) {
            List<String> list = new ArrayList<>();
            String[] array;
            Object pojo = null;
            Parameter[] parameters = method.getParameters();
            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i].isAnnotationPresent(X.class)) {
                    if (List.class.isAssignableFrom(parameters[i].getType())) {
                        list.addAll((List<String>) args[i]);
                    } else if (String.class.isAssignableFrom(parameters[i].getType())) {
                        list.add((String) args[i]);
                    } else {
                        throw new RuntimeException("@Update更新操作中意外的标注类型：" + parameters[i].getType().getName() + "!@X注解只能标注String和List<String>类型的参数.错误位置：" + method);
                    }
                } else {
                    pojo = args[i];
                }
            }
            array = new String[list.size()];
            list.toArray(array);
            if (pojo == null)
                throw new RuntimeException("@Update更新操作异常：没有找到用于更新操作的实体类对象!错误位置：" + method);
            return sqlCore.updateByPojo(pojo, array);
        } else
            return updateSql(method, args, sql_fp, sql);
    }

    /**
     * 处理被@Delete注解标注的接口方法
     *
     * @param method 接口方法
     * @param args   参数列表
     * @param sql_fp SQl片段化类
     * @return true/false
     * @throws NoSuchFieldException
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    private <T> boolean delete(Method method, Object[] args, SqlFragProce sql_fp) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Delete del = method.getAnnotation(Delete.class);
        if (del.byid())
            return sqlCore.delete((Class<?>) args[0], args[1]);
        if (del.batch())
            return sqlCore.deleteBatchByCollection((Collection<T>) args[0]);
        String sql = del.value();
        if ("".equals(sql))
            return sqlCore.delete(args[0]);
        else
            return updateSql(method, args, sql_fp, sql);
    }


    /**
     * 处理被@Insert注解标注的接口方法
     *
     * @param method 接口方法
     * @param args   参数列表
     * @param sql_fp SQl片段化类
     * @return true/false
     * @throws NoSuchFieldException
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    private <T> boolean insert(Method method, Object[] args, SqlFragProce sql_fp) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Insert ins = method.getAnnotation(Insert.class);
        String sql = ins.value();
        if ("".equals(sql)) {
            if (ins.batch()) {
                return sqlCore.insertBatchByCollection((Collection<T>) args[0]);
            } else {
                if (ins.setautoId()) {
                    return sqlCore.insertSetId(args[0]);
                } else {
                    return sqlCore.insert(args[0]);
                }
            }
        } else {
            return updateSql(method, args, sql_fp, sql);
        }
    }

    /**
     * 处理被@Query注解标注的接口方法
     *
     * @param method 接口方法
     * @param args   参数列表
     * @return Object
     */
    private Object join(Method method, Object[] args) {
        Query query = method.getAnnotation(Query.class);
        Parameter[] parameters = method.getParameters();
        ParameterizedType type = (ParameterizedType) method.getGenericReturnType();
        Type[] entry = type.getActualTypeArguments();
        Class<?> cla;
        if (LuckyMapperGeneric != null && ("query".equals(method.getName()) || "selectLimit".equals(method.getName())))
            cla = LuckyMapperGeneric;
        else
            cla = (Class<?>) entry[0];
        if (query.queryBuilder()) {
            if (parameters.length != 1)
                throw new RuntimeException("@Query参数数量溢出异常  size:" + parameters.length + "！@Query注解的\"queryBuilder\"模式下的参数只能是唯一，而且类型必须是 com.lucky.jacklamb.query.QueryBuilder！错误位置：" + method);
            if (!QueryBuilder.class.isAssignableFrom(parameters[0].getType()))
                throw new RuntimeException("@Query参数类型异常  错误类型:" + parameters[0].getType().getName() + "！@Query注解的\"queryBuilder\"模式下的参数只能是唯一，而且类型必须是 com.lucky.jacklamb.query.QueryBuilder！错误位置：" + method);
            return sqlCore.query((QueryBuilder) args[0], cla, query.expression());
        }
        QueryBuilder queryBuilder = new QueryBuilder();
        setQueryBuilder(query, parameters, method, args, queryBuilder);
        return sqlCore.query(queryBuilder, cla, query.expression());
    }

    /**
     * 处理被没有被注解标注的接口方法
     *
     * @param method 接口方法
     * @param args   参数列表
     * @param sql_fp SQl片段化类
     * @return Object
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws SecurityException
     * @throws NoSuchFieldException
     */
    private Object notHave(Method method, Object[] args, SqlFragProce sql_fp) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        if (sqlMap.containsKey(method.getName().toUpperCase())) {
            pageParam(method, args);
            String methodName = method.getName().toUpperCase();
            String sqlStr = sqlMap.get(methodName);
            String sqlCopy = sqlStr.toUpperCase();
            if (sqlCopy.contains("#{")) {
                if (method.isAnnotationPresent(AutoId.class)) {
                    Field idField = PojoManage.getIdField(args[0].getClass());
                    Id id = idField.getAnnotation(Id.class);
                    if (id.type() == PrimaryType.AUTO_INT)
                        sqlCore.setNextId(args[0]);
                    else if (id.type() == PrimaryType.AUTO_UUID) {
                        idField.setAccessible(true);
                        idField.set(args[0], UUID.randomUUID().toString());
                    }
                }
                SqlAndArray sqlArr = noSqlTo(args[0], sqlStr);
                sqlStr = sqlArr.getSql();
                if (method.getParameterCount() == 3) {
                    List<Object> list = new ArrayList<>();
                    list.addAll(Arrays.asList(sqlArr.getArray()));
                    list.add(args[1]);
                    list.add(args[2]);
                    args = list.toArray();
                } else
                    args = sqlArr.getArray();
            }
            if (sqlCopy.contains("SELECT")) {
                if ("C:".equalsIgnoreCase(sqlCopy.substring(0, 2))) {
                    sqlStr = sqlStr.substring(2, sqlStr.length());
                    SqlAndObject so = sql_fp.filterSql(sqlStr, args);
                    if (List.class.isAssignableFrom(method.getReturnType())) {
                        ParameterizedType type = (ParameterizedType) method.getGenericReturnType();
                        Type[] entry = type.getActualTypeArguments();
                        Class<?> cla = (Class<?>) entry[0];
                        return sqlCore.getListMethod(cla, method, so.getSqlStr(), so.getObjects());
                    } else {
                        return sqlCore.getObjectMethod(method.getReturnType(), method, so.getSqlStr(), so.getObjects());
                    }
                } else {
                    if (List.class.isAssignableFrom(method.getReturnType())) {
                        ParameterizedType type = (ParameterizedType) method.getGenericReturnType();
                        Type[] entry = type.getActualTypeArguments();
                        Class<?> cla = (Class<?>) entry[0];
                        return sqlCore.getListMethod(cla, method, sqlStr, args);
                    } else {
                        return sqlCore.getObjectMethod(method.getReturnType(), method, sqlStr, args);
                    }
                }
            } else {
                if ("C:".equalsIgnoreCase(sqlCopy.substring(0, 2))) {
                    sqlStr = sqlStr.substring(2, sqlStr.length());
                    return dynamicUpdateSql(sql_fp, method, sqlStr, args);
                } else {
                    return sqlCore.updateMethod(sqlStr, method, args);
                }
            }
        } else {
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
        Type[] genericInterfaces = clazz.getGenericInterfaces();
        if (LuckyMapper.class.isAssignableFrom(clazz) && genericInterfaces.length == 1) {
            ParameterizedType interfacePtype = (ParameterizedType) genericInterfaces[0];
            LuckyMapperGeneric = (Class<?>) interfacePtype.getActualTypeArguments()[0];
        }
        initIniMap(clazz);
        initSqlMap(clazz);
        initSqlMapProperty(clazz);
        final Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        MethodInterceptor interceptor = (object, method, params, methodProxy) -> {
            log.debug("Run ==> " + object.getClass().getName() + "." + method.getName() + "\n params=" + Arrays.toString(params));

			/*
			  用户自定义的Mapper如果继承了LuckyMapper<T>,代理selectById,deleteById,count,selectList,createTable,deleteByIdIn,selectByIdIn方法
			 这两个方法的执行依赖LuckyMapper接口的泛型类型，所以需要特殊处理
			*/
            boolean isExtendLM = LuckyMapperGeneric != null;
            if (isExtendLM && "selectById".equals(method.getName())) {
                return sqlCore.getOne(LuckyMapperGeneric, params[0]);
            }
            if (isExtendLM && "deleteById".equals(method.getName())) {
                return (Object) sqlCore.delete(LuckyMapperGeneric, params[0]);
            }
            if (isExtendLM && "count".equals(method.getName()) && params.length == 0) {
                return (Object) sqlCore.count(LuckyMapperGeneric);
            }
            if (isExtendLM && "selectList".equals(method.getName()) && params.length == 0) {
                return sqlCore.getList(LuckyMapperGeneric);
            }
            if (isExtendLM && "createTable".equals(method.getName()) && params.length == 0) {
                sqlCore.createTable(LuckyMapperGeneric);
                return void.class;
            }
            if (isExtendLM && "deleteByIdIn".equals(method.getName()) && params.length == 1) {
                 return (Object) sqlCore.deleteByIdIn(LuckyMapperGeneric, (List<?>)params[0]);

            }
            if (isExtendLM && "selectByIdIn".equals(method.getName()) && params.length == 1) {
                return sqlCore.getByIdIn(LuckyMapperGeneric, (List<?>)params[0]);
            }

            //用户自定义Mapper接口方法的代理
            SqlFragProce sql_fp = SqlFragProce.getSqlFP();
            if (method.isAnnotationPresent(Select.class))
                return select(method, params, sql_fp);
            else if (method.isAnnotationPresent(Update.class))
                return (Object) update(method, params, sql_fp);
            else if (method.isAnnotationPresent(Delete.class))
                return (Object) delete(method, params, sql_fp);
            else if (method.isAnnotationPresent(Insert.class))
                return (Object) insert(method, params, sql_fp);
            else if (method.isAnnotationPresent(Query.class))
                return join(method, params);
            else if (method.isAnnotationPresent(Count.class))
                return (Object) sqlCore.count(params[0]);
            else
                return notHave(method, params, sql_fp);
        };
        enhancer.setCallback(interceptor);
        return (T) enhancer.create();
    }

    /**
     * 根据配置设置QueryBuilder对象
     *
     * @param query        Query注解对象
     * @param parameters   参数类型数组
     * @param args         参数值数组
     * @param queryBuilder QueryBuilder对象
     */
    private void setQueryBuilder(Query query, Parameter[] parameters, Method method, Object[] args, QueryBuilder queryBuilder) {
        /*
         * queryBuilder对象的设置有一定的顺序，addObjects()方法必须优先执行，
         * 所以必须先找到接口中用于查询的对象，之后才能设置查询的细节
         */
        queryBuilder.setJoin(query.join());
        int end = parameters.length;//用于记录非模糊查询参数的索引
        List<Integer> indexs = new ArrayList<>();
        List<Object> objectlist = new ArrayList<>();
        Object[] objectarray;
        if (query.limit()) {//分页模式，优先过滤掉两个分页参数
            if (parameters.length < 3)
                throw new RuntimeException("@Query参数缺失异常！@Query注解的\"Like\"模式下的参数至少为3个，而且最后两个参数必须为int类型的分页参数(page,rows)！错误位置：" + method.getName());
            indexs.add(end - 1);
            indexs.add(end - 2);
            for (int i = 0; i < end - 2; i++) {
                if (!parameters[i].isAnnotationPresent(Like.class)) {
                    objectlist.add(args[i]);
                    indexs.add(i);
                }
            }
            objectarray = new Object[objectlist.size()];
            objectlist.toArray(objectarray);
            queryBuilder.addObject(objectarray);
            queryBuilder.limit((int) args[end - 2], (int) args[end - 1]);
            setLike(parameters, queryBuilder, method, args, indexs, end - 2);
            setSort(query, queryBuilder);
            setResults(method, query, queryBuilder);
        } else {//非分页模式
            for (int i = 0; i < end; i++) {
                if (!parameters[i].isAnnotationPresent(Like.class)) {
                    objectlist.add(args[i]);
                    indexs.add(i);
                }
            }
            objectarray = new Object[objectlist.size()];
            objectlist.toArray(objectarray);
            queryBuilder.addObject(objectarray);
            setLike(parameters, queryBuilder, method, args, indexs, end);
            setSort(query, queryBuilder);
            setResults(method, query, queryBuilder);
        }

    }

    /**
     * 为queryBuilder对象设置Like参数
     *
     * @param parameters
     * @param queryBuilder
     * @param args
     * @param indexs
     * @param end
     */
    private void setLike(Parameter[] parameters, QueryBuilder queryBuilder, Method method, Object[] args, List<Integer> indexs, int end) {
        List<String> likelist = new ArrayList<>();
        String[] array;
        for (int i = 0; i < end; i++) {
            if (!indexs.contains(i)) {
                if (List.class.isAssignableFrom(parameters[i].getType())) {
                    likelist.addAll((List<String>) args[i]);
                } else if (String.class.isAssignableFrom(parameters[i].getType())) {
                    likelist.add((String) args[i]);
                } else {
                    throw new RuntimeException("@Query模糊查询模式中意外的标注类型：" + parameters[i].getType().getName() + "!@Like注解只能标注String和List<String>类型的参数.错误位置：" + method.getName());
                }
            }
            array = new String[likelist.size()];
            likelist.toArray(array);
            queryBuilder.addLike((String[]) array);
        }
    }

    /**
     * 为queryBuilder对象设置Sort参数
     *
     * @param query
     * @param queryBuilder
     */
    private void setSort(Query query, QueryBuilder queryBuilder) {
        for (String sort : query.sort()) {
            String[] fs = sort.replaceAll(" ", "").split(":");
            int parseInt = Integer.parseInt(fs[1]);
            if (parseInt == 1)
                queryBuilder.addSort(fs[0], Sort.ASC);
            if (parseInt == -1)
                queryBuilder.addSort(fs[0], Sort.DESC);
        }
    }

    /**
     * 为queryBuilder对象设置Results参数
     *
     * @param query
     * @param queryBuilder
     */
    private void setResults(Method method, Query query, QueryBuilder queryBuilder) {
        if (query.hResults().length != 0 && query.sResults().length != 0)
            throw new RuntimeException("@Query注解的\"hResults\"属性和\"sResults\"属性不可以同时使用！错误位置：" + method.getName());
        if (query.sResults().length != 0)
            queryBuilder.addResult(query.sResults());
        if (query.hResults().length != 0)
            queryBuilder.hiddenResult(query.hResults());
    }


    private void pageParam(Method method, Object[] args) {
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].isAnnotationPresent(Page.class)) {
                if (i == parameters.length - 1) {
                    args[i] = ((int) args[i] - 1) * (int) args[i - 1];
                    break;
                } else {
                    args[i] = ((int) args[i] - 1) * (int) args[i + 1];
                    break;
                }
            }
        }
    }


}

class SqlAndArray {

    private String sql;

    private Object[] array;

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public Object[] getArray() {
        return array;
    }

    public void setArray(Object[] array) {
        this.array = array;
    }

    @Override
    public String toString() {
        return "SqlAndArray [sql=" + sql + ", array=" + Arrays.toString(array) + "]";
    }

}
