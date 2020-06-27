package com.lucky.jacklamb.sqlcore.mapper.jpa;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lucky.jacklamb.sqlcore.abstractionlayer.util.PojoManage;
import com.lucky.jacklamb.utils.base.LuckyUtils;
import com.lucky.jacklamb.utils.reflect.ClassUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2020/6/26 2:48 下午
 */
public class JpaSample {


    private final String[] PREFIX={"findBy","readBy","getBy","find","read","get"};

    private final String REG ="\\@\\d\\d";

    /**
     * 运算符转换规则
     */
    private static Map<String, String> operationMap;

    /**
     * 所有运算符按照字符长度倒序排序后的集合
     */
    private static List<String> lengthSortKeys;

    /**
     * 查询语句的前缀
     */
    private StringBuilder selectSql;

    /**
     * 实体类属性名(首字母大写)与表字段所组成的Map
     */
    private Map<String, String> fieldColumnMap;

    static {
        try {
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, String>>() {
            }.getType();
            BufferedReader br = new BufferedReader(new InputStreamReader(JpaSample.class.getResourceAsStream("/config/jpa-find.json"),"UTF-8"));
            operationMap=gson.fromJson(br,type);
            lengthSortKeys=new ArrayList<>(operationMap.keySet());
            Collections.sort(lengthSortKeys,new SortByLengthComparator());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public JpaSample(){}

    public JpaSample(Class<?> pojoClass) {
        selectSql = new StringBuilder("SELECT * FROM ").append(PojoManage.getTable(pojoClass)).append(" WHERE ");
        fieldColumnMap = new HashMap<>();
        Field[] fields = ClassUtils.getAllFields(pojoClass);
        for (Field field : fields) {
            fieldColumnMap.put(LuckyUtils.TableToClass(field.getName()), PojoManage.getTableField(field));
        }
    }

    /*
        And	                findByLastnameAndFirstname	                    … where x.lastname = ?1 and x.firstname = ?2
        Or	                findByLastnameOrFirstname	                    … where x.lastname = ?1 or x.firstname = ?2
        Is,Equals	        findByFirstnameIs,findByFirstnameEquals	        … where x.firstname = ?1
        Between	            findByStartDateBetween	                        … where x.startDate between ?1 and ?2
        LessThan	        findByAgeLessThan	                            … where x.age < ?1
        LessThanEqual	    findByAgeLessThanEqual	                        … where x.age ⇐ ?1
        GreaterThan	        findByAgeGreaterThan	                        … where x.age > ?1
        GreaterThanEqual	findByAgeGreaterThanEqual	                    … where x.age >= ?1
        After	            findByStartDateAfter	                        … where x.startDate > ?1
        Before	            findByStartDateBefore	                        … where x.startDate < ?1
        IsNull	            findByAgeIsNull	                                … where x.age is null
        IsNotNull,NotNull	findByAge(Is)NotNull	                        … where x.age not null
        Like	            findByFirstnameLike	                            … where x.firstname like ?1
        NotLike	            findByFirstnameNotLike	                        … where x.firstname not like ?1
        StartingWith	    findByFirstnameStartingWith	                    … where x.firstname like ?1 (parameter bound with appended %)
        EndingWith	        findByFirstnameEndingWith	                    … where x.firstname like ?1 (parameter bound with prepended %)
        Containing	        findByFirstnameContaining	                    … where x.firstname like ?1 (parameter bound wrapped in %)
        OrderBy	            findByAgeOrderByLastnameDesc	                … where x.age = ?1 order by x.lastname desc
        Not	                findByLastnameNot	                            … where x.lastname <> ?1
        In	                findByAgeIn(Collection ages)	                … where x.age in ?1
        NotIn	            findByAgeNotIn(Collectionage)	                … where x.age not in ?1
        TRUE	            findByActiveTrue()	                            … where x.active = true
        FALSE	            findByActiveFalse()	                            … where x.active = false
        IgnoreCase	        findByFirstnameIgnoreCase	                    … where UPPER(x.firstame) = UPPER(?1)
     */

    /**
     * 将JPA表达式解析为SQL语句
     *
     * @param jpaSample JPA表达式[findByLastnameAndFirstname
     *                  readByLastnameAndFirstname
     *                  getByLastnameAndFirstname]
     * @return
     */
    public String sampleToSql(String jpaSample) {
        jpaSample=removePrefix(jpaSample);
        for (String s : lengthSortKeys) {
            jpaSample=jpaSample.replaceAll(s,operationMap.get(s));
        }
        List<String> varList=Arrays.asList(jpaSample.replaceAll(REG,"=").split("="))
                .stream().filter(a->a!=null&&!"".equals(a)).collect(Collectors.toList());
        List<String> copyVarList=new ArrayList<>(varList);
        Collections.sort(copyVarList,new SortByLengthComparator());
        String jpaSampleCopy=jpaSample;
        for (String var : copyVarList) {
            jpaSampleCopy=jpaSampleCopy.replaceAll(var,"=");
        }
        List<String> opeList=Arrays.asList(jpaSampleCopy.split("="))
                .stream().filter(a->a!=null&&!"".equals(a)).collect(Collectors.toList());
        List<String> varOpeSortList = getVarOpeSortList(varList, opeList, jpaSample);
        System.out.println(varList);
        System.out.println(opeList);
        System.out.println(varOpeSortList);
        joint(varOpeSortList);
        return selectSql.toString();
    }

    public List<String> getVarOpeSortList(List<String> varList,List<String> opeList,String jpaSample){
        List<String> varOpeSortList=new ArrayList<>();
        boolean varStatr=jpaSample.startsWith(varList.get(0));
        boolean varEnd=jpaSample.endsWith(varList.get(varList.size()-1));
        int varSize=varList.size();
        int opeSize=opeList.size();
        if(varStatr&&varEnd){//以终结符开头，以终结符结尾
            for(int i=0;i<opeSize;i++){
                varOpeSortList.add(varList.get(i));
                varOpeSortList.add(opeList.get(i));
            }
            varOpeSortList.add(varList.get(varSize-1));
        }else if(varStatr&&!varEnd){//以终结符开头，以运算符结尾
            for(int i=0;i<opeSize;i++){
                varOpeSortList.add(varList.get(i));
                varOpeSortList.add(opeList.get(i));
            }
        }else if(!varStatr&&varEnd){//以运算符开头，以终结符结尾
            for(int i=0;i<varSize;i++){
                varOpeSortList.add(opeList.get(i));
                varOpeSortList.add(varList.get(i));
            }
        }else{//以运算符开头，以运算符结尾
            for(int i=0;i<varSize;i++){
                varOpeSortList.add(opeList.get(i));
                varOpeSortList.add(varList.get(i));
            }
            varOpeSortList.add(opeList.get(opeSize-1));
        }
        return varOpeSortList;
    }

    public void joint(List<String> varOpeSortList){
        selectSql=new StringBuilder();
    }

    public String removePrefix(String jpaSample){
        for (String prefix : PREFIX) {
            if(jpaSample.startsWith(prefix)){
                return jpaSample.substring(prefix.length());
            }
        }

        throw new RuntimeException("不符合规范的JPA查询方法命名："+jpaSample);
    }

    public static void main(String[] args) {
        JpaSample jpaSample=new JpaSample();
        //...where name=?1 AND sex IS NOT NULL OR price=?2 ORDER BY age DESC
        jpaSample.sampleToSql("findOrderByNameDesc");

    }
}

class SortByLengthComparator implements Comparator<String> {

    @Override
    public int compare(String var1, String var2) {
        if (var1.length() > var2.length()) {
            return -1;
        } else if (var1.length() == var2.length()) {
            return 0;
        } else {
            return 1;
        }
    }

}

