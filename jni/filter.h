#ifndef FILTER_H
#define FILTER_H
void h_Create(double fl, double fh);
void filter(double fl,double fh, int* Input, double* Output);
void filterHCreat(double* filterh);
double* h_ApFftCreate(double* data);
void QRS_det(double Output[],int rdIdx,double Output2[],int Output3[] ,int Output4[],int Output5[],int Output6[],
		int Output7[],int Output8[],int Output9[],int output10[]);
int max_index(double* A);
double mean(double buf[]);
double circshift(double buf[], double b);
void smooth(double inputRaw[], double OutputRaw[]);

#endif
