//------------------------------------------------------------------------------------------------
// File Name:	NRC.h
// Author:		See copyright at the bottom of the file.
// Purpose:		Numerical Recipes structures, macros and prototypes.
//------------------------------------------------------------------------------------------------

float ran1(long *idum);
float gasdev(long *idum);
void fourn(double data[], unsigned long nn[], int ndim, int isign);
void sort2(unsigned long n, float arr[], int brr[]);
bool choldc(double A[6][6], double diag[6]);
void cholsl(double A[6][6],
			  double diag[6],
			  double B[6],
			  double x[6]);

bool n_choldc(float **A, float *diag, int N);
void n_cholsl(float **A,
			  float *diag,
			  float *B,
			  float *x,
			  int N);

/*
void four1(float data[], unsigned long nn, int isign);
void realft(float data[], unsigned long n, int isign);
void twofft(float data1[], float data2[], float fft1[], float fft2[],unsigned long n);
void convlv(float data[], unsigned long n, float respns[], unsigned long m,int isign, float ans[]);
*/
/* (C) Copr. 1986-92 Numerical Recipes Software 21"t9,12. */
