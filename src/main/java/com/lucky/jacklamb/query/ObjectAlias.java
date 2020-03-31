package com.lucky.jacklamb.query;

public class ObjectAlias {
	
	private Object entity;
	
	private String alias;
	

	public Object getEntity() {
		return entity;
	}

	public void setEntity(Object entity) {
		this.entity = entity;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ObjectAlias [entity=");
		builder.append(entity);
		builder.append(", alias=");
		builder.append(alias);
		builder.append("]");
		return builder.toString();
	}
	
	

}
