//------------------------------------------------------------------------------------------------
// File Name:	NRC.cpp
// Author:		See copyright at the bottom of the file.
// Purpose:		Contains a variety of numerical routines from the Numerical Recipes text.
//
// Routines:	ran1
//				gasdev
//				fourn
//
//				(C) Copr. 1986-92 Numerical Recipes Software 21"t9,12. 
//------------------------------------------------------------------------------------------------

#include <math.h>
#include "NRC.h"
#include <stdio.h>
#include <stddef.h>
#include <stdlib.h>
#include "nrutil.h"

#define IA 16807
#define IM 2147483647
#define AM (1.0/IM)
#define IQ 127773
#define IR 2836
#define NTAB 32
#define NDIV (1+(IM-1)/NTAB)
#define EPS 1.2e-7
#define RNMX float(1.0-EPS)

//------------------------------------------------------------------------------------------------
// Generate a uniform pseudo-random variable.
//------------------------------------------------------------------------------------------------
float ran1(long *idum)
{
	int j;
	long k;
	static long iy=0;
	static long iv[NTAB];
	float temp;

	if (*idum <= 0 || !iy) {
		if (-(*idum) < 1) *idum=1;
		else *idum = -(*idum);
		for (j=NTAB+7;j>=0;j--) {
			k=(*idum)/IQ;
			*idum=IA*(*idum-k*IQ)-IR*k;
			if (*idum < 0) *idum += IM;
			if (j < NTAB) iv[j] = *idum;
		}
		iy=iv[0];
	}
	k=(*idum)/IQ;
	*idum=IA*(*idum-k*IQ)-IR*k;
	if (*idum < 0) *idum += IM;
	j=iy/NDIV;
	iy=iv[j];
	iv[j] = *idum;
	if ((temp=float(AM*iy)) > RNMX) return RNMX;
	else return temp;
}

#undef IA
#undef IM
#undef AM
#undef IQ
#undef IR
#undef NTAB
#undef NDIV
#undef EPS
#undef RNMX

//------------------------------------------------------------------------------------------------
// Generate a normal pseudo-random variable.
//------------------------------------------------------------------------------------------------
float gasdev(long *idum)
{
	float ran1(long *idum);
	static int iset=0;
	static float gset;
	float fac,rsq,v1,v2;

	if  (iset == 0) {
		do {
			v1=float(2.0*ran1(idum)-1.0);
			v2=float(2.0*ran1(idum)-1.0);
			rsq=v1*v1+v2*v2;
		} while (rsq >= 1.0 || rsq == 0.0);
		fac=float(sqrt(-2.0*log(rsq)/rsq));
		gset=v1*fac;
		iset=1;
		return v2*fac;
	} else {
		iset=0;
		return gset;
	}
}


//------------------------------------------------------------------------------------------------
// Compute the Fourier transform of an N-D signal.
//------------------------------------------------------------------------------------------------
#define SWAP(a,b) tempr=(a);(a)=(b);(b)=tempr
void fourn(double data[], unsigned long nn[], int ndim, int isign)
{
	int idim;
	unsigned long i1,i2,i3,i2rev,i3rev,ip1,ip2,ip3,ifp1,ifp2;
	unsigned long ibit,k1,k2,n,nprev,nrem,ntot;
	double tempi,tempr;
	double theta,wi,wpi,wpr,wr,wtemp;

	for (ntot=1,idim=1;idim<=ndim;idim++)
		ntot *= nn[idim];
	nprev=1;
	for (idim=ndim;idim>=1;idim--) {
		n=nn[idim];
		nrem=ntot/(n*nprev);
		ip1=nprev << 1;
		ip2=ip1*n;
		ip3=ip2*nrem;
		i2rev=1;
		for (i2=1;i2<=ip2;i2+=ip1) {
			if (i2 < i2rev) {
				for (i1=i2;i1<=i2+ip1-2;i1+=2) {
					for (i3=i1;i3<=ip3;i3+=ip2) {
						i3rev=i2rev+i3-i2;
						SWAP(data[i3],data[i3rev]);
						SWAP(data[i3+1],data[i3rev+1]);
					}
				}
			}
			ibit=ip2 >> 1;
			while (ibit >= ip1 && i2rev > ibit) {
				i2rev -= ibit;
				ibit >>= 1;
			}
			i2rev += ibit;
		}
		ifp1=ip1;
		while (ifp1 < ip2) {
			ifp2=ifp1 << 1;
			theta=isign*6.28318530717959/(ifp2/ip1);
			wtemp=sin(0.5*theta);
			wpr = -2.0*wtemp*wtemp;
			wpi=sin(theta);
			wr=1.0;
			wi=0.0;
			for (i3=1;i3<=ifp1;i3+=ip1) {
				for (i1=i3;i1<=i3+ip1-2;i1+=2) {
					for (i2=i1;i2<=ip3;i2+=ifp2) {
						k1=i2;
						k2=k1+ifp1;
						tempr=(double)wr*data[k2]-(double)wi*data[k2+1];
						tempi=(double)wr*data[k2+1]+(double)wi*data[k2];
						data[k2]=data[k1]-tempr;
						data[k2+1]=data[k1+1]-tempi;
						data[k1] += tempr;
						data[k1+1] += tempi;
					}
				}
				wr=(wtemp=wr)*wpr-wi*wpi+wr;
				wi=wi*wpr+wtemp*wpi+wi;
			}
			ifp1=ifp2;
		}
		nprev *= n;
	}
}
#undef SWAP


#define NR_END 1
#define FREE_ARG char*

unsigned long *lvector(long nl, long nh)
/* allocate an unsigned long vector with subscript range v[nl..nh] */
{
unsigned long *v;
v=(unsigned long *)malloc((size_t) ((nh-nl+1+NR_END)*sizeof(long)));
if (!v)
{
	//nrerror("allocation failure in lvector()");
}
return v-nl+NR_END;
}

void free_lvector(unsigned long *v, long nl, long nh)
/* free an unsigned long vector allocated with lvector() */
{
free((FREE_ARG) (v+nl-NR_END));
}


// Cholesky Decomposition of a symmetric positive definite matrix
// Overwrites lower triangle of matrix
// Numerical Recipes, but has a bit of the fancy C++ template thing happening

bool n_choldc(float **A, float *diag, int N)
{
	int i,j,k;
	float sum;

	for (i=0; i < N; i++) 
	{
		for (j=i; j < N; j++) 
		{
			sum = A[i][j];
			for (k=i-1; k >= 1; k--)
			{
				sum -= A[i][k] * A[j][k];
			}
			if (i == j) 
			{
				if (sum < 0.0)
					return false;
				diag[i] = sqrt(sum);
			} 
			else 
			{
				A[j][i] = sum / diag[i];
			}
		}
	}

	return true;
}


// Solve Ax=B after choldc
void n_cholsl(float **A,
			  float *diag,
			  float *B,
			  float *x,
			  int N)
{
	for (int i=0; i < N; i++) {
		double sum = B[i];
		for (int k=i-1; k >= 0; k--)
			sum -= A[i][k] * x[k];
		x[i] = (float)sum / diag[i];
	}
	for (int i=N-1; i >= 0; i--) {
		double sum = x[i];
		for (int k=i+1; k < N; k++)
			sum -= A[k][i] * x[k];
		x[i] = (float)sum / diag[i];
	}
}





// Cholesky Decomposition of a symmetric positive definite matrix
// Overwrites lower triangle of matrix
// Numerical Recipes, but has a bit of the fancy C++ template thing happening

bool choldc(double A[6][6], double diag[6])
{
	int i,j,k;
	float sum;

	for (i=0; i < 6; i++) 
	{
		for (j=i; j < 6; j++) 
		{
			sum = A[i][j];
			for (k=i-1; k >= 1; k--)
			{
				sum -= A[i][k] * A[j][k];
			}
			if (i == j) 
			{
				if (sum < 0.0)
					return false;
				diag[i] = sqrt(sum);
			} 
			else 
			{
				A[j][i] = sum /diag[i];
			}
		}
	}

	return true;
}

// Solve Ax=B after choldc
void cholsl(double A[6][6],
			  double diag[6],
			  double B[6],
			  double x[6])
{
	for (int i=0; i < 6; i++) {
		double sum = B[i];
		for (int k=i-1; k >= 0; k--)
			sum -= A[i][k] * x[k];
		x[i] = sum / diag[i];
	}
	for (int i=6-1; i >= 0; i--) {
		double sum = x[i];
		for (int k=i+1; k < 6; k++)
			sum -= A[k][i] * x[k];
		x[i] = sum / diag[i];
	}
}



#define SWAP(a,b) temp=(a);(a)=(b);(b)=temp;
#define M 7
#define NSTACK 50
void sort2(unsigned long n, float arr[], int brr[])
//Sorts an array arr[1..n] into ascending order using Quicksort, while making the corresponding
//rearrangement of the array brr[1..n].
{
	unsigned long i,ir=n,j,k,l=1,*istack;
	int jstack=0;
	float a,b,temp;

	istack=lvector(1,NSTACK);
	for (;;) 
	{										//Insertion sort when subarray small enough.
		if (ir-l < M) 
		{
			for (j=l+1;j<=ir;j++) 
			{
				a=arr[j];
				b=(float)brr[j];
				for (i=j-1;i>=l;i--) 
				{
					if (arr[i] <= a) break;
					arr[i+1]=arr[i];
					brr[i+1]=brr[i];
				}
				arr[i+1]=a;
				brr[i+1]=(int)b;
			}
			if (!jstack) 
			{
				free_lvector(istack,1,NSTACK);
				return;
			}
			ir=istack[jstack];								//Pop stack and begin a new round of parti-
													//tioning. 
			l=istack[jstack-1];
			jstack -= 2;
		} 
		else 
		{
			k=(l+ir) >> 1;									//Choose median of left, center and right el-
													//ements as partitioning element a. Also
													//rearrange so that a[l]
													//.
													//a[l+1]
													//.
													//a[ir].
			SWAP(arr[k],arr[l+1])
			SWAP(brr[k],brr[l+1])
			if (arr[l] > arr[ir]) 
			{
				SWAP(arr[l],arr[ir])
				SWAP(brr[l],brr[ir])
			}
			if (arr[l+1] > arr[ir]) 
			{
				SWAP(arr[l+1],arr[ir])
				SWAP(brr[l+1],brr[ir])
			}
			if (arr[l] > arr[l+1]) 
			{
				SWAP(arr[l],arr[l+1])
				SWAP(brr[l],brr[l+1])
			}
			i=l+1;											//Initialize pointers for partitioning.
			j=ir;
			a=arr[l+1];										//Partitioning element.
			b=brr[l+1];
			for (;;) 
			{										//Beginning of innermost loop.
				do i++; while (arr[i] < a);						//Scan up to nd element > a.
				do j--; while (arr[j] > a);						//Scan down to nd element < a.
				if (j < i) break;								//Pointers crossed. Partitioning complete.
				SWAP(arr[i],arr[j])								//Exchange elements of both arrays.
				SWAP(brr[i],brr[j])
			}												//End of innermost loop.
			arr[l+1]=arr[j];								//Insert partitioning element in both arrays.
			arr[j]=a;
			brr[l+1]=brr[j];
			brr[j]=b;
			jstack += 2;
			//Push pointers to larger subarray on stack, process smaller subarray immediately.
			if (jstack > NSTACK) 
			{
				//nrerror("NSTACK too small in sort2.");
			}
			if (ir-i+1 >= j-l) 
			{
				istack[jstack]=ir;
				istack[jstack-1]=i;
				ir=j-1;
			} 
			else 
			{
				istack[jstack]=j-1;
				istack[jstack-1]=l;
				l=i;
			}
		}
	}
}
#undef SWAP


/*
#define SWAP(a,b) tempr=(a);(a)=(b);(b)=tempr

void four1(float data[], unsigned long nn, int isign)
{
	unsigned long n,mmax,m,j,istep,i;
	double wtemp,wr,wpr,wpi,wi,theta;
	float tempr,tempi;

	n=nn << 1;
	j=1;
	for (i=1;i<n;i+=2) {
		if (j > i) {
			SWAP(data[j],data[i]);
			SWAP(data[j+1],data[i+1]);
		}
		m=n >> 1;
		while (m >= 2 && j > m) {
			j -= m;
			m >>= 1;
		}
		j += m;
	}
	mmax=2;
	while (n > mmax) {
		istep=mmax << 1;
		theta=isign*(6.28318530717959/mmax);
		wtemp=sin(0.5*theta);
		wpr = -2.0*wtemp*wtemp;
		wpi=sin(theta);
		wr=1.0;
		wi=0.0;
		for (m=1;m<mmax;m+=2) {
			for (i=m;i<=n;i+=istep) {
				j=i+mmax;
				tempr=wr*data[j]-wi*data[j+1];
				tempi=wr*data[j+1]+wi*data[j];
				data[j]=data[i]-tempr;
				data[j+1]=data[i+1]-tempi;
				data[i] += tempr;
				data[i+1] += tempi;
			}
			wr=(wtemp=wr)*wpr-wi*wpi+wr;
			wi=wi*wpr+wtemp*wpi+wi;
		}
		mmax=istep;
	}
}
#undef SWAP


void realft(float data[], unsigned long n, int isign)
{
	void four1(float data[], unsigned long nn, int isign);
	unsigned long i,i1,i2,i3,i4,np3;
	float c1=0.5,c2,h1r,h1i,h2r,h2i;
	double wr,wi,wpr,wpi,wtemp,theta;

	theta=3.141592653589793/(double) (n>>1);
	if (isign == 1) {
		c2 = -0.5;
		four1(data,n>>1,1);
	} else {
		c2=0.5;
		theta = -theta;
	}
	wtemp=sin(0.5*theta);
	wpr = -2.0*wtemp*wtemp;
	wpi=sin(theta);
	wr=1.0+wpr;
	wi=wpi;
	np3=n+3;
	for (i=2;i<=(n>>2);i++) {
		i4=1+(i3=np3-(i2=1+(i1=i+i-1)));
		h1r=c1*(data[i1]+data[i3]);
		h1i=c1*(data[i2]-data[i4]);
		h2r = -c2*(data[i2]+data[i4]);
		h2i=c2*(data[i1]-data[i3]);
		data[i1]=h1r+wr*h2r-wi*h2i;
		data[i2]=h1i+wr*h2i+wi*h2r;
		data[i3]=h1r-wr*h2r+wi*h2i;
		data[i4] = -h1i+wr*h2i+wi*h2r;
		wr=(wtemp=wr)*wpr-wi*wpi+wr;
		wi=wi*wpr+wtemp*wpi+wi;
	}
	if (isign == 1) {
		data[1] = (h1r=data[1])+data[2];
		data[2] = h1r-data[2];
	} else {
		data[1]=c1*((h1r=data[1])+data[2]);
		data[2]=c1*(h1r-data[2]);
		four1(data,n>>1,-1);
	}
}

void twofft(float data1[], float data2[], float fft1[], float fft2[], unsigned long n)
{
	void four1(float data[], unsigned long nn, int isign);
	unsigned long nn3,nn2,jj,j;
	float rep,rem,aip,aim;

	nn3=1+(nn2=2+n+n);
	for (j=1,jj=2;j<=n;j++,jj+=2) {
		fft1[jj-1]=data1[j];
		fft1[jj]=data2[j];
	}
	four1(fft1,n,1);
	fft2[1]=fft1[2];
	fft1[2]=fft2[2]=0.0;
	for (j=3;j<=n+1;j+=2) {
		rep=0.5*(fft1[j]+fft1[nn2-j]);
		rem=0.5*(fft1[j]-fft1[nn2-j]);
		aip=0.5*(fft1[j+1]+fft1[nn3-j]);
		aim=0.5*(fft1[j+1]-fft1[nn3-j]);
		fft1[j]=rep;
		fft1[j+1]=aim;
		fft1[nn2-j]=rep;
		fft1[nn3-j] = -aim;
		fft2[j]=aip;
		fft2[j+1] = -rem;
		fft2[nn2-j]=aip;
		fft2[nn3-j]=rem;
	}
}


#define NRANSI


void convlv(float data[], unsigned long n, float respns[], unsigned long m,int isign, float ans[])
{
	void realft(float data[], unsigned long n, int isign);
	void twofft(float data1[], float data2[], float fft1[], float fft2[],unsigned long n);
	unsigned long i,no2;
	float dum,mag2,*fft;

	fft=vector(1,n<<1);
	for (i=1;i<=(m-1)/2;i++)
		respns[n+1-i]=respns[m+1-i];
	for (i=(m+3)/2;i<=n-(m-1)/2;i++)
		respns[i]=0.0;
	twofft(data,respns,fft,ans,n);
	no2=n>>1;
	for (i=2;i<=n+2;i+=2) {
		if (isign == 1) {
			ans[i-1]=(fft[i-1]*(dum=ans[i-1])-fft[i]*ans[i])/no2;
			ans[i]=(fft[i]*dum+fft[i-1]*ans[i])/no2;
		} else if (isign == -1) {
			if ((mag2=SQR(ans[i-1])+SQR(ans[i])) == 0.0)
				nrerror("Deconvolving at response zero in convlv");
			ans[i-1]=(fft[i-1]*(dum=ans[i-1])+fft[i]*ans[i])/mag2/no2;
			ans[i]=(fft[i]*dum-fft[i-1]*ans[i])/mag2/no2;
		} else nrerror("No meaning for isign in convlv");
	}
	ans[2]=ans[n+1];
	realft(ans,n,-1);
	free_vector(fft,1,n<<1);
}
#undef NRANSI

#define SWAP(a,b) tempr=(a);(a)=(b);(b)=tempr

void fourn(float data[], unsigned long nn[], int ndim, int isign)
{
	int idim;
	unsigned long i1,i2,i3,i2rev,i3rev,ip1,ip2,ip3,ifp1,ifp2;
	unsigned long ibit,k1,k2,n,nprev,nrem,ntot;
	float tempi,tempr;
	double theta,wi,wpi,wpr,wr,wtemp;

	for (ntot=1,idim=1;idim<=ndim;idim++)
		ntot *= nn[idim];
	nprev=1;
	for (idim=ndim;idim>=1;idim--) {
		n=nn[idim];
		nrem=ntot/(n*nprev);
		ip1=nprev << 1;
		ip2=ip1*n;
		ip3=ip2*nrem;
		i2rev=1;
		for (i2=1;i2<=ip2;i2+=ip1) {
			if (i2 < i2rev) {
				for (i1=i2;i1<=i2+ip1-2;i1+=2) {
					for (i3=i1;i3<=ip3;i3+=ip2) {
						i3rev=i2rev+i3-i2;
						SWAP(data[i3],data[i3rev]);
						SWAP(data[i3+1],data[i3rev+1]);
					}
				}
			}
			ibit=ip2 >> 1;
			while (ibit >= ip1 && i2rev > ibit) {
				i2rev -= ibit;
				ibit >>= 1;
			}
			i2rev += ibit;
		}
		ifp1=ip1;
		while (ifp1 < ip2) {
			ifp2=ifp1 << 1;
			theta=isign*6.28318530717959/(ifp2/ip1);
			wtemp=sin(0.5*theta);
			wpr = -2.0*wtemp*wtemp;
			wpi=sin(theta);
			wr=1.0;
			wi=0.0;
			for (i3=1;i3<=ifp1;i3+=ip1) {
				for (i1=i3;i1<=i3+ip1-2;i1+=2) {
					for (i2=i1;i2<=ip3;i2+=ifp2) {
						k1=i2;
						k2=k1+ifp1;
						tempr=(float)wr*data[k2]-(float)wi*data[k2+1];
						tempi=(float)wr*data[k2+1]+(float)wi*data[k2];
						data[k2]=data[k1]-tempr;
						data[k2+1]=data[k1+1]-tempi;
						data[k1] += tempr;
						data[k1+1] += tempi;
					}
				}
				wr=(wtemp=wr)*wpr-wi*wpi+wr;
				wi=wi*wpr+wtemp*wpi+wi;
			}
			ifp1=ifp2;
		}
		nprev *= n;
	}
}
#undef SWAP
// (C) Copr. 1986-92 Numerical Recipes Software p,{2. 


void rlft3(float ***data, float **speq, unsigned long nn1, unsigned long nn2,
	unsigned long nn3, int isign)
{
	void fourn(float data[], unsigned long nn[], int ndim, int isign);
	void nrerror(char error_text[]);
	unsigned long i1,i2,i3,j1,j2,j3,nn[4],ii3;
	double theta,wi,wpi,wpr,wr,wtemp;
	float c1,c2,h1r,h1i,h2r,h2i;

	if (1+&data[nn1][nn2][nn3]-&data[1][1][1] != nn1*nn2*nn3)
		nrerror("rlft3: problem with dimensions or contiguity of data array\n");
	c1=0.5;
	c2 = -0.5*isign;
	theta=isign*(6.28318530717959/nn3);
	wtemp=sin(0.5*theta);
	wpr = -2.0*wtemp*wtemp;
	wpi=sin(theta);
	nn[1]=nn1;
	nn[2]=nn2;
	nn[3]=nn3 >> 1;
	if (isign == 1) {
		fourn(&data[1][1][1]-1,nn,3,isign);
		for (i1=1;i1<=nn1;i1++)
			for (i2=1,j2=0;i2<=nn2;i2++) {
				speq[i1][++j2]=data[i1][i2][1];
				speq[i1][++j2]=data[i1][i2][2];
			}
	}
	for (i1=1;i1<=nn1;i1++) {
		j1=(i1 != 1 ? nn1-i1+2 : 1);
		wr=1.0;
		wi=0.0;
		for (ii3=1,i3=1;i3<=(nn3>>2)+1;i3++,ii3+=2) {
			for (i2=1;i2<=nn2;i2++) {
				if (i3 == 1) {
					j2=(i2 != 1 ? ((nn2-i2)<<1)+3 : 1);
					h1r=c1*(data[i1][i2][1]+speq[j1][j2]);
					h1i=c1*(data[i1][i2][2]-speq[j1][j2+1]);
					h2i=c2*(data[i1][i2][1]-speq[j1][j2]);
					h2r= -c2*(data[i1][i2][2]+speq[j1][j2+1]);
					data[i1][i2][1]=h1r+h2r;
					data[i1][i2][2]=h1i+h2i;
					speq[j1][j2]=h1r-h2r;
					speq[j1][j2+1]=h2i-h1i;
				} else {
					j2=(i2 != 1 ? nn2-i2+2 : 1);
					j3=nn3+3-(i3<<1);
					h1r=c1*(data[i1][i2][ii3]+data[j1][j2][j3]);
					h1i=c1*(data[i1][i2][ii3+1]-data[j1][j2][j3+1]);
					h2i=c2*(data[i1][i2][ii3]-data[j1][j2][j3]);
					h2r= -c2*(data[i1][i2][ii3+1]+data[j1][j2][j3+1]);
					data[i1][i2][ii3]=h1r+wr*h2r-wi*h2i;
					data[i1][i2][ii3+1]=h1i+wr*h2i+wi*h2r;
					data[j1][j2][j3]=h1r-wr*h2r+wi*h2i;
					data[j1][j2][j3+1]= -h1i+wr*h2i+wi*h2r;
				}
			}
			wr=(wtemp=wr)*wpr-wi*wpi+wr;
			wi=wi*wpr+wtemp*wpi+wi;
		}
	}
	if (isign == -1)
		fourn(&data[1][1][1]-1,nn,3,isign);
}
*/


