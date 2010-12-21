package com.ihub.android.app;

import greendroid.app.GDApplication;

public class IhubApplication extends GDApplication {

	@Override
	public Class<?> getHomeActivityClass() {
		return Home.class;
	}
}
