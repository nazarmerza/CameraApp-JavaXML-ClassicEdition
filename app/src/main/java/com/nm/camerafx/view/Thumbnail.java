package com.nm.camerafx.view;

public class Thumbnail {
	
	private int resourceId;
	private String name;
	
	public Thumbnail(int resourceId, String name) {
		super();
		this.resourceId = resourceId;
		this.name = name.replace('_', ' ');
	}

	public int getResourceId() {
		return resourceId;
	}

	public void setResourceId(int resourceId) {
		this.resourceId = resourceId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	

}
