#include <jni.h>
#include <string.h>
#include "filter.h"

namespace example{

static jdoubleArray filter(JNIEnv *env, jobject obj,jdoubleArray output){
	double *filterh = env->GetDoubleArrayElements(output,NULL);
	filterHCreat(filterh);
	env->ReleaseDoubleArrayElements(output,filterh,0);
	return output;

}

static jdoubleArray h_ApFftCreatef(JNIEnv *env, jobject obj,jdoubleArray output){
	double *filterh = env->GetDoubleArrayElements(output,NULL);
	h_ApFftCreate(filterh);
	env->ReleaseDoubleArrayElements(output,filterh,0);
	return output;
}

static jint QRSDetection(JNIEnv *env,jobject obj,jdoubleArray data1,jint rd_idx,jintArray data2,
		jintArray data3,jintArray data4,jintArray data5,jintArray data6,jintArray data7,jintArray data8,jintArray data9){
	double* QRSData = env->GetDoubleArrayElements(data1,NULL);
	int* RLocation = env->GetIntArrayElements(data2,NULL);
	int* TStart = env->GetIntArrayElements(data3,NULL);
	int* TEnd = env->GetIntArrayElements(data4,NULL);
	int* QStart =env->GetIntArrayElements(data5,NULL);
	int* QEnd =env->GetIntArrayElements(data6,NULL);
	int* PStart =env->GetIntArrayElements(data7,NULL);
	int* PEnd =env->GetIntArrayElements(data8,NULL);
	int* RCount =env->GetIntArrayElements(data9,NULL);
//	int rdIdx = rd_idx;


	double output2[100] = {0};
	//QRS_det(QRSData,rd_idx,output2,RLocation,TStart,TEnd,QStart,QEnd,PStart,PEnd,RCount);
	QRS_det(QRSData,rd_idx,output2,RLocation,TStart,TEnd,QStart,QEnd,PStart,PEnd,RCount);
	env->ReleaseDoubleArrayElements(data1,QRSData,NULL);
	env->ReleaseIntArrayElements(data2,RLocation,NULL);
	env->ReleaseIntArrayElements(data3,TStart,NULL);
	env->ReleaseIntArrayElements(data4,TEnd,NULL);
	env->ReleaseIntArrayElements(data5,QStart,NULL);
	env->ReleaseIntArrayElements(data6,QEnd,NULL);
	env->ReleaseIntArrayElements(data7,PStart,NULL);
	env->ReleaseIntArrayElements(data8,PEnd,NULL);
	env->ReleaseIntArrayElements(data9,RCount,NULL);

	return 0;
}
static JNINativeMethod sMethods[] = {
		{"filterNative","([D)[D",(void*)filter},
		{"h_ApFftCreateNative","([D)[D",(void*)h_ApFftCreatef}
};
static JNINativeMethod sMethod[] = {
		{"QRSDetNative","([DI[I[I[I[I[I[I[I[I)I",(void*)QRSDetection}
};

static int jniRegisterNativeMethods(JNIEnv *env,const char *className,
		JNINativeMethod* Methods, int numMethods){
	jclass clazz = env->FindClass(className);
	    if (clazz == NULL) {
	        return JNI_FALSE;
	    }

	    if (env->RegisterNatives(clazz, Methods, numMethods) < 0) {
	        return JNI_FALSE;
	    }
	    return JNI_TRUE;
}

int register_Signal(JNIEnv *env) {
    return jniRegisterNativeMethods(env, "com/tju/bluetoothlegatt52/BluetoothLeService",
            sMethods, sizeof(sMethods) / sizeof(sMethods[0]));
}
int register_Signal2(JNIEnv *env) {
    return jniRegisterNativeMethods(env, "com/tju/bluetoothlegatt52/DeviceControlActivity",
            sMethod, sizeof(sMethod) / sizeof(sMethod[0]));
}

}
