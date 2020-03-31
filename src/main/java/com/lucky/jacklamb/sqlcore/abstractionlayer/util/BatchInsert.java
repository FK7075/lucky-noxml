package com.lucky.jacklamb.sqlcore.abstractionlayer.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import com.lucky.jacklamb.annotation.orm.Id;
import com.lucky.jacklamb.enums.PrimaryType;

public class BatchInsert {
	
	private String insertSql;
	
	private Object[] insertObject;
	
	private int size;

	public String getInsertSql() {
		return insertSql;
	}

	public Object[] getInsertObject() {
		return insertObject;
	}
	
	public <T> BatchInsert(Collection<T> collection) {
		size=collection.size();
		if(!collection.isEmpty()) {
			Class<?> pojoClass = null;
			for(T t:collection) {
				pojoClass=t.getClass();
				break;
			}
			insertSql=createInsertSql(pojoClass,collection.size());
			insertObject=createInsertObject(collection);
		}
	}
	
	public static String createInsertSql(Class<?> clzz,int size) {
		Field[] fields=clzz.getDeclaredFields();
		StringBuffer prefix=new StringBuffer("INSERT INTO "+PojoManage.getTable(clzz));
		StringBuffer suffix=new StringBuffer(" VALUES ");
		if(PojoManage.getIdType(clzz)==PrimaryType.AUTO_INT) {
			List<Field> list=new ArrayList<>();
			String id=PojoManage.getIdString(clzz);
			Stream.of(fields).filter(field->!id.equals(PojoManage.getTableField(field))
					&&field.getType().getClassLoader()==null
					&&!(field.getType()).isAssignableFrom(Collection.class)).forEach(list::add);
			StringBuffer fk=new StringBuffer("");
			for(int i=0;i<list.size();i++) {
				if(i==0) {
					prefix.append("(").append(PojoManage.getTableField(list.get(i))).append(",");
					fk.append("(?,");
				}else if(i==list.size()-1) {
					prefix.append(PojoManage.getTableField(list.get(i))).append(")");
					fk.append("?)");
				}else {
					prefix.append(PojoManage.getTableField(list.get(i))).append(",");
					fk.append("?,");
				}
			}
			for(int j=0;j<size;j++) {
				if(j==size-1) {
					suffix.append(fk);
				}else {
					suffix.append(fk).append(",");
				}
			}
		}else {
			List<Field> list=new ArrayList<>();
			Stream.of(fields).filter(field->field.getType().getClassLoader()==null
					&&!(field.getType()).isAssignableFrom(Collection.class)).forEach(list::add);
			StringBuffer fk=new StringBuffer("");
			for(int i=0;i<list.size();i++) {
				if(i==0) {
					prefix.append("(").append(PojoManage.getTableField(list.get(i))).append(",");
					fk.append("(?,");
				}else if(i==list.size()-1) {
					prefix.append(PojoManage.getTableField(list.get(i))).append(")");
					fk.append("?)");
				}else {
					prefix.append(PojoManage.getTableField(list.get(i))).append(",");
					fk.append("?,");
				}
			}
			for(int j=0;j<size;j++) {
				if(j==size-1) {
					suffix.append(fk);
				}else {
					suffix.append(fk).append(",");
				}
			}
		}
		return prefix.append(suffix).toString();
	}
	
	private <T> Object[] createInsertObject(Collection<T> collection) {
		List<Object> po=new ArrayList<>();
		for(T t:collection) {
			Class<?> clzz=t.getClass();
			String id=PojoManage.getIdString(clzz);
			Field[] fields=clzz.getDeclaredFields();
			if(PojoManage.getIdType(clzz)==PrimaryType.AUTO_INT) {
				for(Field fie:fields) {
					if(fie.getType().getClassLoader()==null
							&&!(fie.getType()).isAssignableFrom(Collection.class)
							&&!id.equals(PojoManage.getTableField(fie))) {
						fie.setAccessible(true);
						try {
							Object object=fie.get(t);
							po.add(object);
						} catch (IllegalArgumentException | IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}else {
				for(Field fie:fields) {
					if(fie.getType().getClassLoader()==null&&!(fie.getType()).isAssignableFrom(Collection.class)) {
						fie.setAccessible(true);
						try {
							Object object=fie.get(t);
							po.add(object);
						} catch (IllegalArgumentException | IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
		return po.toArray();
	}
	
	public String singleInsertSql() {
		String insertSql = getInsertSql();
		int end=insertSql.indexOf("?)")+2;
		return insertSql.substring(6, end)+" ";
	}
	public String OrcaleInsetSql() {
		StringBuilder insert=new StringBuilder("INSERT ALL");
		String singleSql = singleInsertSql();
		for(int i=0;i<size;i++) {
			insert.append(singleSql);
		}
		insert.append("SELECT * FROM DUAL");
		return insert.toString();
	}
	public static void main(String[] args) {
		List<Book> list=new ArrayList<>();
		list.add(new Book(1,"b1",23.4));
		list.add(new Book(2,null,28.8));
		list.add(new Book(3,"b3",null));
		BatchInsert bi=new BatchInsert(list);
		String insertSql2 = bi.getInsertSql();
		System.out.println(insertSql2);
		System.out.println(Arrays.toString(bi.getInsertObject()));
		System.out.println(bi.OrcaleInsetSql());
	}
	
}

class Book{
	@Id
	private Integer bid;
	private String bname;
	private Double price;
	
	
	public Book(Integer bid, String bname, Double price) {
		this.bid = bid;
		this.bname = bname;
		this.price = price;
	}
	public Integer getBid() {
		return bid;
	}
	public void setBid(Integer bid) {
		this.bid = bid;
	}
	public String getBname() {
		return bname;
	}
	public void setBname(String bname) {
		this.bname = bname;
	}
	public Double getPrice() {
		return price;
	}
	public void setPrice(Double price) {
		this.price = price;
	}
	
	
}
