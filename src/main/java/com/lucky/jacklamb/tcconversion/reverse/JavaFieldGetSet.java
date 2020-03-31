package com.lucky.jacklamb.tcconversion.reverse;

import java.util.ArrayList;
import java.util.List;

import com.lucky.jacklamb.sqlcore.c3p0.DataSource;
import com.lucky.jacklamb.sqlcore.c3p0.ReadIni;
import com.lucky.jacklamb.utils.LuckyUtils;

/**
 * 得到关于某个属性的源代码（属性声明，get/set方法的源码）
 * @author fk-7075
 *
 */
public class JavaFieldGetSet {
	private String field;
	private String getField;
	private String setField;
	private static String dbname;
	private static DataSource data;
	public String getField() {
		return field;
	}
	public void setField(String field) {
		this.field = field;
	}
	public String getGetField() {
		return getField;
	}
	public void setGetField(String getField) {
		this.getField = getField;
	}
	public String getSetField() {
		return setField;
	}
	public void setSetField(String setField) {
		this.setField = setField;
	}
	public void to_String() {
		System.out.println(field);
		System.out.println(getField);
		System.out.println(setField);
	}
	
	public JavaFieldGetSet(String field,String type,String id,List<String> keys,String dbname) {
		JavaFieldGetSet.dbname=dbname;
		String Id="";
		String key="";
		data=ReadIni.getDataSource(dbname);
		if(id.equals(field)) {
			Id="\t@Id\n";
		}
		if(keys.contains(field)) {
			key="\t@Key\n";
		}
		this.field=Id+key+"\tprivate "+type+" "+field+";\n\n";
		this.getField="\tpublic "+type+" get"+LuckyUtils.TableToClass(field)+"(){\n\t\treturn this."+field+";\n\t}";
		this.setField="\tpublic void set"+LuckyUtils.TableToClass(field)+"("+type+" "+field+"){\n\t\tthis."+field+"="+field+";\n\t}";
	}
	
	/**
	 * 给出TableStructure对应表的类的源代码
	 * @param ts TableStructure对象
	 * @return 对应表的java源代码
	 */
	public static GetJavaSrc getOneJavaSrc(TableStructure ts,String name){
		GetJavaSrc javasrc=new GetJavaSrc();
		List<JavaFieldGetSet> list=new ArrayList<JavaFieldGetSet>();
		javasrc.setClassName(ts.getTableName());
		javasrc.setPack("package "+data.getCaeateTable()+";");
		javasrc.setImpor("import java.util.Date;\nimport java.sql.*;\nimport java.util.*;\nimport com.lucky.annotation.Id;\nimport com.lucky.annotation.Key;");
		javasrc.setToString(ts.getToString());
		javasrc.setConstructor(ts.getConstructor());
		javasrc.setParameterConstructor(ts.getParameterConstructor());
		String src="@SuppressWarnings(\"all\")\npublic class "+ts.getTableName()+"{\n";
		for(int i=0;i<ts.getFields().size();i++) {
			JavaFieldGetSet jf=new JavaFieldGetSet(ts.getFields().get(i), ts.getTypes().get(i),ts.getPri(),ts.getMuls(),name);
			list.add(jf);
		}
		for (JavaFieldGetSet jf : list) {
			src+=jf.getField()+"";
		}
		src+="\n"+ts.getConstructor()+"\n\n"+ts.getParameterConstructor()+"\n";
		for (JavaFieldGetSet jf : list) {
			src+="\n"+jf.getGetField()+"\n";
			src+=jf.getSetField()+"\n";
		}
		javasrc.setJavaSrc(src);
		return javasrc;
	}
	
	
	/**
	 * 得到指定表的java代码
	 * @param tables 表名
	 * @return
	 */
	public static List<GetJavaSrc> getAssignJavaSrc(String name,String...tables){
		List<GetJavaSrc> javasrclist=new ArrayList<GetJavaSrc>();
		List<TableStructure> list=TableStructure.getAssignTableStructure(tables);
		for (TableStructure tableStructure : list) {
			GetJavaSrc java=JavaFieldGetSet.getOneJavaSrc(tableStructure,name);
			javasrclist.add(java);
		}
		return javasrclist;
	}
	
	/**
	 * 得到数据库中所有表对应的java源码对象GetJavaSrc的集合
	 * @return
	 */
	public static List<GetJavaSrc> getMoreJavaSrc(String name){
		List<GetJavaSrc> javasrclist=new ArrayList<GetJavaSrc>();
		List<TableStructure> list=TableStructure.getMoreTableStructure(new Tables(dbname));
		for (TableStructure tableStructure : list) {
			GetJavaSrc java=JavaFieldGetSet.getOneJavaSrc(tableStructure,name);
			javasrclist.add(java);
		}
		return javasrclist;
	}

}
