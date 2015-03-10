#include <jni.h>
#include <math.h>
#include "filter.h"

#define PI 3.1415926
#define FILTER_LENGTH 101
#define FS 250
#define LENGTH 4096
#define RLENGTH (LENGTH*3/250)

double h[FILTER_LENGTH];
double ha[101];

//int RR_LOCATION_FILTED[RLENGTH];
//int RRCOUNT = 0;
//int Tstart[RLENGTH], Tend[RLENGTH], Qstart[RLENGTH], Send[RLENGTH], Pstart[RLENGTH], Pend[RLENGTH];
//�˲������ݺ���

void h_Create(double fl, double fh) {
	//double fl=40;
	//double fh=250;
	int m = FILTER_LENGTH;
	int alpha = (m - 1) / 2;
	double hd[FILTER_LENGTH], win[FILTER_LENGTH], h1[FILTER_LENGTH],
			h2[FILTER_LENGTH];
	double wc1, wc2;
	wc1 = 2.0 * fl / FS * PI;
	wc2 = 2.0 * fh / FS * PI;

	for (int i = 0; i < FILTER_LENGTH; i++)
		win[i] = 0.54 - 0.46 * cos(2 * PI * i / (m - 1));

	for (int n = 0; n < (m - 1) / 2; n++)
	//for(int n=0;n<32;n++)
			{
		hd[n] = sin(wc1 * (n - alpha)) / ((n - alpha) * PI);
		h1[n] = hd[n] * win[n];
	}
	hd[alpha] = wc1 / PI;
	h1[alpha] = hd[alpha] * win[alpha];

	for (int n = alpha + 1; n < FILTER_LENGTH; n++) {
		hd[n] = sin(wc1 * (n - alpha)) / ((n - alpha) * PI);
		h1[n] = hd[n] * win[n];
	}
	for (int n = 0; n < (m - 1) / 2; n++)
	//for(int n=0;n<32;n++)
			{
		hd[n] = sin(wc2 * (n - alpha)) / ((n - alpha) * PI);
		h2[n] = hd[n] * win[n];
	}
	hd[alpha] = wc2 / PI;
	h2[alpha] = hd[alpha] * win[alpha];
	for (int n = alpha + 1; n < FILTER_LENGTH; n++) {
		hd[n] = sin(wc2 * (n - alpha)) / ((n - alpha) * PI);
		h2[n] = hd[n] * win[n];
	}
	for (int n = 0; n < FILTER_LENGTH; n++)
		h[n] = h2[n] - h1[n];
}

void filter(double fl, double fh, int* Input, double* Output) {
	h_Create(fl, fh);

	for (int n = 0; n < FILTER_LENGTH; n++) {
		Output[n] = 0.0;
	}

	for (int n = FILTER_LENGTH; n < LENGTH; n++) {
		Output[n] = 0.0;
		for (int i = n - FILTER_LENGTH - 1; i < n; i++)
			Output[n] += h[n - i] * Input[i];
	}

}

void filter2(double* Input, double output) {
	for (int i = 0; i < FILTER_LENGTH; i++)
		output += h[FILTER_LENGTH - 1] * Input[i];
}

void filterHCreat(double* filterh) {
	h_Create(5, 100);
	for (int i = 0; i < 101; i++)
		filterh[i] = h[i];
}

void conv(double* window, int N, double* win) {
	for (int i = 0; i < N; i++) {
		for (int j = 0; j <= i; j++) {
			win[i] += window[j];
		}
	}
	for (int i = N; i < 2 * N - 1; i++) {
		win[i] = win[2 * N - 2 - i];
	}

}

double* h_ApFftCreate(double* data) {
	//����ȫ��λ�˲���
	double fl = 5, fh = 100;
	int N_filter_length = 101;
	int/* fL = 25, fH = 250, */fs = 250;
	double w1, w2;
	w1 = 2 * PI * fl / fs;
	w2 = 2 * PI * fh / fs;
	int N_filter = (N_filter_length + 1) / 2; //
	int k1 = ceil((double) N_filter * fl / fs);
	int k2 = ceil((double) N_filter * fh / fs);
	//double *H = new double[N_filter];
	//double *h = new double[N_filter];
	//double *I = new double[N_filter];
	double H[51] = { 0 };
	double h[51] = { 0 };
	double I[51] = { 0 };
	//memset(H, 0, N_filter);
	//memset(I, 0, N_filter);
	for (int i = k1; i < k2; i++)
		H[i] = 1.0;

	for (int i = N_filter - k2 + 1; i < N_filter - k1 + 1; i++)
		H[i] = 1.0;

	for (int i = 0; i < N_filter; i++) {
		for (int j = 0; j < N_filter; j++) {
			h[i] += (H[j] * cos(2 * PI * i * j / N_filter) / N_filter);
		}
	}

	//�ӵ���win
	//-------------------------------------
	double hamming[51];
	double squre[51];

	for (int i = -25; i <= 25; i++) {
		hamming[i + 25] = 0.54 + 0.46 * cos(2 * PI * i / 51);
	}
	for (int i = 0; i < 51; i++) {
		squre[i] = 1.0;
	}
	double win[101] = { 0 }; //hamming��squre�ľ��
	conv(hamming, N_filter, win);
	//��һ��
	double max_win = win[N_filter - 1];
	for (int i = 0; i < 2 * N_filter - 1; i++) {
		win[i] /= max_win;
	}
	//------------------------------------------------------
	//��ͨ�ɹ�
	//ha[265] = {0};
	for (int i = 0; i < N_filter - 1; i++) {
		ha[i] = h[i + 1] * win[i];
	}
	for (int i = 0; i < N_filter; i++) {
		ha[N_filter - 1 + i] = h[i] * win[N_filter - 1 + i];
	}
	//-------------------------------------------------------------------------
	//50hz�ݲ�
	int fs_m = floor(2 * PI * 50 / fs / (2 * PI / N_filter));
	double fs_lam = 2 * PI * 50 / fs / (2 * PI / N_filter) - fs_m;
	double g[101] = { 0 };
	for (int i = -N_filter + 1; i <= N_filter - 1; i++) {
		g[i + N_filter - 1] = win[i + N_filter - 1]
				* cos((fs_m + fs_lam) * i * 2 * PI / N_filter) / N_filter * 2;
	}

	for (int i = 0; i < 2 * N_filter - 1; i++) {
		ha[i] -= g[i];
	}

//	ofstream ofs1("my_file.dat",ios::binary | ios::out);
//	for (int i = 0; i < 565; i++)
//	{
//		ofs1.write((char*)&ha[i], sizeof(double));
//	}
//	ofs1.close();
	for (int i = 0; i < N_filter_length; i++)
		data[i] = ha[i];
	return data;
}

void filter_apfft(double fl, double fh, int* Input, double* Output) {
//	h_ApFftCreate(fl, fh);

	for (int n = 0; n < FILTER_LENGTH + 400; n++) {
		Output[n] = 0.0;
	}
	for (int n = FILTER_LENGTH + 400; n < LENGTH; n++) {
		Output[n] = 0.0;
		for (int i = n - FILTER_LENGTH - 400; i < n; i++)
			Output[n] += ha[n - i - 1] * Input[i];
	}
}

//void QRS_det(double OutputRaw[], int rdIdx, double Output2[], int Output3[],
void QRS_det(double OutputRaw[], int rdIdx,double Output2[], int Output3[],
		int Output4[], int Output5[], int Output6[], int Output7[],
		int Output8[], int Output9[], int Output10[]) {

	double Output[LENGTH];//���鳤��4096
	for(int i = 0; i < LENGTH; i++){
		if((i + rdIdx) >= LENGTH*3){
			Output[i] = OutputRaw[i + rdIdx - LENGTH*3];
		}else {
			Output[i] = OutputRaw[i + rdIdx];
		}
	}
	int RR_LOCATION_FILTED[RLENGTH];
	int RRCOUNT = 0;
	int Tstart[RLENGTH], Tend[RLENGTH], Qstart[RLENGTH], Send[RLENGTH],
			Pstart[RLENGTH], Pend[RLENGTH];
	double qrsbuf[8], noisebuf[8], rrbuf[8];
	double nmean, qmean, rrmean, threshold;
	int IND[20000];
	double TH = 0.575;
	int index = max_index(Output); //��ʼ����һ��R��λ��
	for (int k = 0; k < RLENGTH; k++) {
		RR_LOCATION_FILTED[k] = 0;
	}
	// �����һ��R���25-35���ݵľ�ֵ��Ȼ�����R��������ֵ�Ĳ�����ֵ��Ϊ����һ����ֵ�������м��
	int RR_height;
	int RR_all10 = 0;
	for (int i = (index + 25); i < (index + 35); i++) {
		RR_all10 += Output[i];
	}
	RR_height = Output[index] - RR_all10 / 10;

	//
	int k = 0;
	for (int i = 0; i < LENGTH; i++) //////index ��Ϊ0
			{
		if ((Output[i] > Output[i - 1]) && (Output[i] > Output[i + 1])) {
			IND[k] = i;//IND[k]�����漫��ֵ��λ��
			k += 1;
		}
	}

	//��ʼ�����Ĵ���
	for (int i = 0; i < 8; i++) {
		qrsbuf[i] = Output[IND[0]];//���ٳ���Ϊ8�ļ�¼���8��QRS��ֵλ�õĻ��������õ�һ��R��λ�ó�ʼ��
		noisebuf[i] = Output[IND[1]];//���ٳ���Ϊ8�ļ�¼���8������λ�õĻ��������õ�һ��T��λ�ó�ʼ��
		rrbuf[i] = 200;//���ٳ���Ϊ8�ļ�¼���8��RR����λ�õĻ��������������˵�200ֵ���г�ʼ��,�˴�����600���г�ʼ��,�˴������޹�
	}

	RR_LOCATION_FILTED[0] = index;//index��һ��R��λ�ø������飬���������R����ֵλ��

	for (int i = 1; i < k; i++) {
		int m = RR_LOCATION_FILTED[RRCOUNT]; //mΪ�����R����ֵλ�á�
		//180����Ҫ~~~�����MATLAB���
		if ((IND[i] < m +  180) && (IND[i] > m - 180) && (IND[i] != m))
			//ȥ��R�������ļ���ֵ��
			IND[i] = 0;

		//ÿ����һ�μĴ����͸���һ�ξ�ֵ
		qmean = mean(qrsbuf);
		nmean = mean(noisebuf);
		rrmean = mean(rrbuf);

		//�ж���ֵ
		threshold = nmean + TH * (qmean - nmean);

		// ��IND(i)>0,˵����ǰ��ֵû���ڵ�ǰR����ǰ��200ms��Χ��
		if (IND[i] > 0) {
			if ((Output[IND[i]] > threshold)

//				&& ((Output[IND[i]] - Output[IND[i] + 30]) > 0.5 * RR_height)
					) {
				RRCOUNT += 1;
				RR_LOCATION_FILTED[RRCOUNT] = IND[i];

				circshift(qrsbuf, Output[IND[i]]);//�� Output[IND[i]]�Ž�qrsbuf[7]��λ��
			}

			else
				circshift(noisebuf, Output[IND[i]]);

			int temp1 = RR_LOCATION_FILTED[RRCOUNT - 1] + 130;//��Ӱ������
			int temp2 = RR_LOCATION_FILTED[RRCOUNT] - 90;

			if ((RRCOUNT >= 2)
					&& (RR_LOCATION_FILTED[RRCOUNT]
							- RR_LOCATION_FILTED[RRCOUNT - 1] > 1.5 * rrmean)) {
				for (int j = temp1; j < temp2; j++) {
					if ((Output[j] > 0.75 * threshold)
							&& (Output[j] > Output[j - 1])
							&& (Output[j] > Output[j + 1])
							//RRheight��һ����ֵ
		//					&& ((Output[IND[i]] - Output[IND[i] + 30])> 0.5 * RR_height)
							) {
					RR_LOCATION_FILTED[RRCOUNT + 1] =RR_LOCATION_FILTED[RRCOUNT];
						RR_LOCATION_FILTED[RRCOUNT] = j;
						RRCOUNT += 1;
					}
				}
			}

			//RR_LOCATION_FILTED=swap(RR_LOCATION_FILTED)
			//RR=circshift(RR)
			//circshift(rrbuf,IND[i]);

		}
	}

	//qrs
	////Ѱ��Q��������S���յ�


	for (int i = 0; i < RRCOUNT; i++) {

		int min = Output[RR_LOCATION_FILTED[i]];
		int min_number = RR_LOCATION_FILTED[i];
		for (int j = RR_LOCATION_FILTED[i]; j > RR_LOCATION_FILTED[i] - 100;
				j--) {
			if (Output[j] < min) {
				min = Output[j];
				min_number = j;
			}
		}

		Qstart[i] = min_number - 5;
		int min_right = Output[RR_LOCATION_FILTED[i]];
		int min_right_number = RR_LOCATION_FILTED[i];
		for (int j = RR_LOCATION_FILTED[i]; j < RR_LOCATION_FILTED[i] + 100;
				j++) {
			if (Output[j] < min_right) {
				min_right = Output[j];
				min_right_number = j;
			}
		}
		Send[i] = min_right_number + 12;

	}

	//Ѱ��T���ķ�ֵ�����ֹ��
	//Ӧ��output2����3-20�˲��������

	int alpha = 0; // (101 - FILTER_LENGTH) / 2;//565��Ϊ��101ym
	for (int i = 0; i < RRCOUNT; i++) {
		int max = 0; //Output[Send[i] + 30 + alpha];
		int max_number = Send[i] + 30 + alpha;
		for (int j = Send[i] + 20 + alpha; j < Send[i] + 80 + alpha; j++) {
			if (Output[j] > max) {
				max = Output[j];
				max_number = j;
			}
		}
		int min = Output[max_number];
		int min_number = max_number;
		for (int j = max_number; j < max_number + 200; j++) {
			if (Output[j] < min) {
				min = Output[j];
				min_number = j;
			}
		}
		Tstart[i] = max_number; //max_number - 60 - alpha;
		Tend[i] = max_number; //min_number - alpha;

	}

	//Ѱ��P���ķ�ֵ�����ֹ��

	for (int i = 0; i < RRCOUNT; i++) {
		int max = Output[Qstart[i] + alpha];
		int max_number = Qstart[i] + alpha;
		for (int j = Qstart[i] + alpha; j > Qstart[i] + alpha - 300; j--) {
			if (Output[j] > max) {
				max = Output[j];
				max_number = j;
			}
		}
		int min = Output[max_number];
		int min_number = max_number;
		for (int j = max_number; j > max_number - 150; j--) {
			if (Output[j] < min) {
				min = Output[j];
				min_number = j;
			}
		}
		Pstart[i] = min_number - alpha;
		Pend[i] = max_number + 40 - alpha;
	}
	for (int i = 0; i < RRCOUNT; i++) {
		Output3[i] = RR_LOCATION_FILTED[i];
		Output4[i] = Tstart[i];
		Output5[i] = Tend[i];
		Output6[i] = Qstart[i];
		Output7[i] = Send[i];
		Output8[i] = Pstart[i];
		Output9[i] = Pend[i];
	}
	Output10[0] = RRCOUNT - 1; //�ҵ��ĵ�һ����ֵ���ظ���¼��RR_LOCATION_FILTED��
	int min = RR_LOCATION_FILTED[0];
	int max = RR_LOCATION_FILTED[0];
	for (int i = 0; i < RRCOUNT; i++) {
		if (min > RR_LOCATION_FILTED[i])
			min = RR_LOCATION_FILTED[i];
		if (max < RR_LOCATION_FILTED[i])
			max = RR_LOCATION_FILTED[i];
	}
	Output10[1] = min;
	Output10[2] = max;


}


int max_index(double* A) {
	float max = A[0];
	int j = 0;
	for (int i = 150; i < 1000; i++) {
		if (A[i] > max) {
			max = A[i];
			j = i;
		}
	}
	return j;
}

//	��������CHRVDoc::mean(float buf[])
//	���ڣ�2012��10��9��
//	���ߣ����
//	ע�⣺�Ĵ�����ֵ�Ӻ���
double mean(double buf[]) {
	double sum = 0;
	for (int j = 0; j < 8; j++) {
		sum += buf[j];
	}
	double a = sum / 8;
	return a;
}

double circshift(double buf[], double b) {
	for (int i = 0; i < 7; i++)
		buf[i] = buf[i + 1];
	buf[7] = b;
	return *buf;
}
