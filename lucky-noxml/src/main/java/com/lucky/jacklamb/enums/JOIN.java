package com.lucky.jacklamb.enums;

public enum JOIN {
	
	
	INNER_JOIN("INNER JOIN"),
	LEFT_OUTER_JOIN("LEFT OUTER JOIN"),
	RIGHT_OUTER_JOIN("RIGHT OUTER JOIN");
	
	private String join;
	
	private JOIN(String join) {
		this.join=join;
	}

	public String getJoin() {
		return join;
	}

	public void setJoin(String join) {
		this.join = join;
	}
	
	

}
