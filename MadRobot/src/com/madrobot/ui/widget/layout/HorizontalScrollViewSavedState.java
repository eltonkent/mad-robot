package com.madrobot.ui.widget.layout;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View.BaseSavedState;

class HorizontalScrollViewSavedState extends BaseSavedState {
	public static final Parcelable.Creator<HorizontalScrollViewSavedState> CREATOR = new Parcelable.Creator<HorizontalScrollViewSavedState>() {
		@Override
		public HorizontalScrollViewSavedState createFromParcel(Parcel in) {
			return new HorizontalScrollViewSavedState(in);
		}

		@Override
		public HorizontalScrollViewSavedState[] newArray(int size) {
			return new HorizontalScrollViewSavedState[size];
		}
	};

	int currentPage = -1;

	private HorizontalScrollViewSavedState(Parcel in) {
		super(in);
		currentPage = in.readInt();
	}

	HorizontalScrollViewSavedState(Parcelable superState) {
		super(superState);
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		super.writeToParcel(out, flags);
		out.writeInt(currentPage);
	}
}