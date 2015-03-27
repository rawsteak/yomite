package com.hatarakuma.yomite;

import android.app.Application;

public class IndexVar extends Application {

	private int indexVariable;
	
	public int getIndex() {
		return indexVariable;
	}
	
	public void setIndex(int newVar) {
		this.indexVariable = newVar;
	}
}
