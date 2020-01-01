package com.eb02;

public class Docs {
	public String document;
	public Double score;
	
	public Docs(String document, Double score) {
		this.document = document;
		this.score = score;
	}
	
	public String getDocument () {
		return this.document;
	}
	
	public Double getScore () {
		return this.score;
	}
	
	public void setDocument (String document) {
		this.document = document;
	}
	
	public void setScore (Double score) {
		this.score = score;
	}
	
}