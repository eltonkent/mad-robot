
package com.madrobot.util.pdf;

import java.util.ArrayList;

public abstract class List extends Base {

	protected ArrayList<String> mList;

	public List() {
		mList = new ArrayList<String>();
	}
	
	protected String renderList() {
		StringBuilder sb = new StringBuilder();
		int x = 0;
		while (x < mList.size()) {
			sb.append(mList.get(x).toString());
			x++;
		}
		return sb.toString();
	}
	
	@Override
	public void clear() {
		mList.clear();
	}
}
