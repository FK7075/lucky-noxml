package com.lucky.jacklamb.rest;

import java.lang.reflect.Field;
import java.util.Stack;

public class AttrUtil {
	
	
	private static Stack<String> stack;
	
	public static String getField(Field field) {
		if(!field.isAnnotationPresent(Attr.class))
			return field.getName();
		return field.getAnnotation(Attr.class).value();
	}
	
	public static boolean check(String jsonStr) {
		stack=new Stack<String>();
		char[] jsonChars = jsonStr.toCharArray();
		
		
		//"{} []"校验
		for(char ch:jsonChars) {
			if(ch=='{'||ch=='[') {
				stack.push(ch+"");
			}else if(ch=='}'||ch==']') {
				if(stack.isEmpty())
					return false;
				if((ch=='}'&&!"{".equals(stack.peek()))||ch==']'&&!"[".equals(stack.peek())){
					return false;
				}else {
					stack.pop();
				}
			}
		}
		return stack.isEmpty();
	}
	
	public static void main(String[] args) {
		String str="{\"TT-STR\":\"String属性\",\"TT-DOUBLE-LIST\":[12.5,55.7,99.99],\"TT-STRING-BB-MAP\":{\"map3\":{\"BB-BNAME\":\"MAPBB\"},\"map2\":{\"BB-BNAME\":\"BNAME\",\"BB-ARRAY\":[\"OK\",\"YES\",\"HELLO\"]},\"map1\":{\"BB-BNAME\":\"BNAME\",\"BB-ARRAY\":[\"OK\",\"YES\",\"HELLO\"]}},\"TT-BB-LIST\":[{\"BB-BNAME\":\"BNAME\",\"BB-ARRAY\":[\"OK\",\"YES\",\"HELLO\"]},{\"BB-BNAME\":\"BB2\"}],\"TT-STRING-INTEGER-MAP\":{\"key1\":111,\"key2\":222},\"TT-BB\":{\"BB-BNAME\":\"BNAME\",\"BB-ARRAY\":[\"OK\",\"YES\",\"HELLO\"]}}";
		boolean check = AttrUtil.check(str);
		System.out.println(check);
	}

}
