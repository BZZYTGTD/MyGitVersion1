package com.tju.bluetoothlegatt52;

public class Filter { 
	
     private final int FILTER_LENGTH = 101;

	 public final static double[] filterh = 
	    	{0.0005  , -0.0012  ,  0.0009   ,-0.0000,   -0.0012, -0.0002 ,  -0.0025   , 0.0011 ,   0.0014   ,-0.0039,
	    	-0.0023 ,  -0.0047,    0.0007  ,  0.0046  , -0.0087,   -0.0079 ,  -0.0070 ,  -0.0008  ,  0.0093 ,  -0.0143,
	    	-0.0186  , -0.0083  , -0.0031   , 0.0135 ,  -0.0182,   -0.0342  , -0.0084  , -0.0045   , 0.0146,   -0.0180,
	    	-0.0525  , -0.0082 ,  -0.0024  ,  0.0099,   -0.0124,   -0.0700 ,  -0.0101  ,  0.0060 ,  -0.0026  , -0.0011,
	        -0.0834 ,  -0.0177 ,   0.0258 ,  -0.0287,   0.0202,   -0.0911  , -0.0460 ,   0.0963 ,  -0.1458 ,   0.1824,
	        0.7059  ,  0.1824   ,-0.1458  ,  0.0963  , -0.0460,   -0.0911  ,  0.0202 ,  -0.0287  ,  0.0258  , -0.0177,
	        -0.0834  , -0.0011 ,  -0.0026 ,   0.0060  , -0.0101,   -0.0700  , -0.0124 ,   0.0099 ,  -0.0024,   -0.0082,
	        -0.0525  , -0.0180  ,  0.0146 ,  -0.0045  , -0.0084,   -0.0342 ,  -0.0182,    0.0135,   -0.0031,   -0.0083,
	        -0.0186   ,-0.0143  ,  0.0093 ,  -0.0008 ,  -0.0070,   -0.0079 ,  -0.0087 ,   0.0046 ,   0.0007 ,  -0.0047,
	        -0.0023  , -0.0039  ,  0.0014 ,   0.0011  , -0.0025,   -0.0002  , -0.0012,   -0.0000 ,   0.0009,   -0.0012,
	        0.0005
	};
	    public final static double[] filterh2 = {
	    		 0.0008 ,   0.0005  ,  0.0001 ,  -0.0003,   -0.0008	,-0.0012 ,  -0.0014 ,  -0.0014 ,  -0.0010   ,-0.0003,
	    		 0.0007  ,  0.0017 ,   0.0023  ,  0.0020,    0.0004,-0.0030  , -0.0081   ,-0.0149 , -0.0228,   -0.0308,
	    		 -0.0378,   -0.0425 ,  -0.0436,   -0.0401 ,  -0.0317,-0.0183 ,  -0.0009,    0.0191  ,  0.0397,    0.0589,
	    		 0.0744  ,  0.0845,    0.0880 ,   0.0845 ,   0.0744, 0.0589  ,  0.0397 ,   0.0191  , -0.0009 ,  -0.0183,
	    		 -0.0317 ,  -0.0401  , -0.0436  , -0.0425 ,  -0.0378,-0.0308 ,  -0.0228 ,  -0.0149,   -0.0081,   -0.0030,
	    		 0.0004  ,  0.0020 ,   0.0023 ,   0.0017  ,  0.0007,-0.0003 ,  -0.0010  , -0.0014  , -0.0014 ,  -0.0012,
	    		 -0.0008  , -0.0003 ,   0.0001,    0.0005 ,   0.0008
	    };
	    
	    private final int AVG_N = 100;
		float tempx[] = {0,0,0,0,0};
		private double tempy[] = {0,0,0,0,0,0,0};
		float tempz[] = new float[AVG_N];
		int sumz = 0;
		int countz = 0;
		float FilterLowPass(double val)
		{
			int i;
			for(i = 0;i < 6;i++)
				tempy[i] = tempy[i + 1];
			tempy[6] = val;

			return (float) (0.239*tempy[6] + 1.4342*tempy[5] + 3.5855*tempy[4] + 4.7807*tempy[3]
			         + 3.5855*tempy[2] + 1.4342*tempy[1] + 0.2390*tempy[0]);
		}
		double FilterBandStop(float val)
		{
			int i;
			for(i = 0;i < 4;i++)
				tempx[i] = tempx[i + 1];
			tempx[4] = val;
		    //fs = 360
		    
		 //fs=250
			return (0.8541 * tempx[4] + (-1.0899) * tempx[3] + 2.0559 * tempx[2]
			         + (-1.0899) * tempx[1] + 0.8541 * tempx[0]);

		}
		float FilterDC(float val)
		{
			float average = 0;
//			sumz = (int) (sumz - tempz[countz] + val);
			sumz = 0;
			for(float f : tempz)
				sumz += f;
			tempz[countz] = val;
			countz ++;
			if(countz == AVG_N)
				countz = 0;

			average = sumz / AVG_N;

			val = val - average;
			return val;
		}
		float filterFIR(float val)
		{
			double band = 0;
			float lowPass = 0;
			band = FilterBandStop(val);
			lowPass =  FilterLowPass(band);
			lowPass = FilterDC(lowPass);
//			lowPass +=20;
			return lowPass;
		}
		
		float filterAF(double[] input){
			float fy = 0.0f;
			for (int n = 0;n<FILTER_LENGTH;n++)
				fy += filterh[FILTER_LENGTH-1-n]*input[n];
			return fy;
		}
}
