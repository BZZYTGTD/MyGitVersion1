package com.tju.bluetoothlegatt52;

public class JNI_filter {
	static{
		System.loadLibrary("filter");
	}
	public native int DC_filter(int raw);

}
