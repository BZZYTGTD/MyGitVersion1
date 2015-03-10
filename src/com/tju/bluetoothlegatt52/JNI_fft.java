package com.tju.bluetoothlegatt52;

public class JNI_fft {
	static{
		System.loadLibrary("filter");
	}
	public native static int fft(int dat[],int length);
}
