#include <jni.h>
#include <math.h>
#include "com_tju_bluetoothlegatt52_JNI_fft.h"

class complex {
public:
	double re;
	double im;
	complex(double real, double imag) {
		re = real;
		im = imag;
	}
	complex(){}

	complex plus(complex b) {
//		complex a = this; // invoking object
		double real = re + b.re;
		double imag = im + b.im;
		complex sum = complex(real, imag);
		return sum;
	}

	complex times(double b) {
		return complex(re * b, im * b);
	}

	complex times(complex b) {
		double real = re * b.re - im * b.im;
		double imag = re * b.im + im * b.re;
		complex prod = complex(real, imag);
		return prod;
	}

	double abs() {
		return sqrt(re * re + im * im);
	}

	complex minus(complex b) {
    double real = re - b.re;
    double imag = im - b.im;
    complex diff = complex(real, imag);
    return diff;
  }
};

class MyFFT {
public:
	complex* fft(complex* x, int length) {
		int N = length;
		// base case
		if (N == 1){
			complex Res = x[0];
			return (complex*)&Res;
		}
		// radix 2 Cooley-Tukey FFT
		if (N % 2 != 0) {
			//throw new RuntimeException("N is not a power of 2");
		}

		// fft of even terms
		complex* even = new class complex[N/2];
		for (int k = 0; k < N/2; k++) {
			even[k] = x[2*k];
		}
		complex* q = fft(even,N/2);

		// fft of odd terms
		complex* odd = new class complex[N/2];
		for (int k = 0; k < N/2; k++) {
			odd[k] = x[2*k + 1];
		}
		complex* r = fft(odd,N/2);

		// combine
		complex* y = new class complex[N];
		for (int k = 0; k < N/2; k++) {
			double kth = -2 * k * M_PI / N;
			complex wk = complex(cos(kth), sin(kth));
			y[k] = q[k].plus(wk.times(r[k]));
			y[k + N/2] = q[k].minus(wk.times(r[k]));
		}
		return y;
	}
};

JNIEXPORT jint JNICALL Java_com_tju_bluetoothlegatt52_JNI_1fft_fft(JNIEnv *env,
		jobject obj, jintArray array, jint length) {
	class complex rawDat[length];
	class complex* pres;
	class MyFFT myfft;
	double max = 0.0;
	int index = 0;
	jboolean isCopy;
	jint* pArray = (jint*) env->GetPrimitiveArrayCritical(array, &isCopy);
	for (int i = 0; i < length; i++) {
		rawDat[i].re = pArray[i];
		rawDat[i].im = 0;
	}
	env->ReleasePrimitiveArrayCritical(array, pArray, JNI_COMMIT);

	pres = myfft.fft(rawDat,length);
	for(int i = 1; i < length; i++){
		if(pres[i].abs() > max){
			max =pres[i].abs();
			index = i;
		}
	}
	return index;
}
