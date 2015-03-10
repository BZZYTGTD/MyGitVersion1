#include <jni.h>
#include "com_tju_bluetoothlegatt52_JNI_filter.h"
#define BUFLENGTH	25

JNIEXPORT jint JNICALL Java_com_tju_bluetoothlegatt52_JNI_1filter_DC_1filter
  (JNIEnv * env, jobject obj, jint raw){
	static int datBuf[BUFLENGTH];
	static int cnt = 0;
	int ave = 0;
	for(int i = 0; i < BUFLENGTH-1; i++){
		datBuf[i] = datBuf[i+1];
	}
	datBuf[BUFLENGTH-1] = raw;
	cnt++;
	if(cnt >= BUFLENGTH){
		cnt = BUFLENGTH;
		for(int i = 0; i < BUFLENGTH; i++){
			ave +=datBuf[i];
		}
		ave = (ave+BUFLENGTH/2)/BUFLENGTH;
		return raw-ave;
	}
	else{
		for(int i = BUFLENGTH-cnt; i < BUFLENGTH; i++){
					ave +=datBuf[i];
				}
		ave = (ave+cnt/2)/cnt;
		return raw-ave;
	}
}
