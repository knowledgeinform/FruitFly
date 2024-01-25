//---------------------------------------------------------------------------------------
// File Name:	Matrix.h
// Author:		Myron Z. Brown
// Purpose:		Contains matrix and vector class templates and associated functions.  
//
// Notes:		There is no .cpp file, as all template functions must be in the same 
//				file as the declaration in order to be resolved by the compiler.
//
//				These classes are modified versions of Greg Hager's matrix and vector  
//				classes used in XVision.
//
// Classes:		Matrix
//				RowVector
//				ColVector
//
// Non-Member
// Routines:	mult		Matrix multiplication
//				crossp		Vector cross-product (only defined for 3-vectors)
//				dotp		Vector dot product
//				bilinear	Bilinear interpolation
//				bilinear2	Bilinear interpolation with zero padding at border
//				rotate		Matrix rotation
//				affine		Affine transformation of matrix (corner-based coordinates)
//				fliph		Horizontal flip
//				flipv		Vertical flip
//				total		Sum of matrix values
//				min			Minimum of matrix values
//				max			Maximum of matrix values
//				congrid		Resize matrix with bilinear interpolation
//				bytscl		Convert a matrix to bytes with linear scaling
//				_panic		Panic
//
// Copyright (c) 2005 Johns Hopkins University 
// Applied Physics Laboratory.  All Rights Reserved.
//
//---------------------------------------------------------------------------------------


#pragma once

//---------------------------------------------------------------------------------------
// Includes.
//---------------------------------------------------------------------------------------
#include <stdio.h>
#include <stdlib.h>
#include <iostream>
#include <string.h>
#include <math.h>

using namespace std;

//---------------------------------------------------------------------------------------
// Handle incompatibilities with MFC.
//---------------------------------------------------------------------------------------
#ifdef min
#undef min
#endif
#ifdef max
#undef max
#endif


typedef struct Point3D 
{
	float x;
	float y;
	float z;
} Point3D;

typedef struct Point2D 
{
	float x;
	float y;
} Point2D;


//---------------------------------------------------------------------------------------
// Forward class declarations.
//---------------------------------------------------------------------------------------
template <class TYPE> class RowVector;
template <class TYPE> class ColVector;
template <class TYPE> class Matrix;


//---------------------------------------------------------------------------------------
// Macros.
//---------------------------------------------------------------------------------------
#define CHECKSIZE(m,mess) {if ((m.rowNum != rowNum)||(m.colNum != colNum)) _panic(mess);}
#define DMatrix		Matrix<double>
#define FMatrix		Matrix<float>
#define BMatrix		Matrix<unsigned char>
#define USMatrix	Matrix<unsigned short>
#define SMatrix		Matrix<short>
#define LMatrix		Matrix<long>
#define DRowVector	RowVector<double>		
#define DColVector	ColVector<double>		
#define FColVector	ColVector<float>
#define LColVector	ColVector<long>
#define BColVector	ColVector<unsigned char>
#define	DVector		DColVector
#define FVector		FColVector
#define LVector		LColVector
#define BVector		BColVector

//---------------------------------------------------------------------------------------
// Forward function declarations.
//---------------------------------------------------------------------------------------
template <class TYPE> Matrix<TYPE>		mult(const Matrix<TYPE> &mat1, const Matrix<TYPE> &mat2);
template <class TYPE> ColVector<TYPE>	mult(const Matrix<TYPE> &mat1, const ColVector<TYPE> &mat);
template <class TYPE> RowVector<TYPE>	mult(const Matrix<TYPE> &mat1, const RowVector<TYPE> &mat);
template <class TYPE> ColVector<TYPE>	crossp(const ColVector<TYPE> &vec1, const ColVector<TYPE> &vec2);

template <class TYPE> TYPE				dotp(const ColVector<TYPE> &vec1, const ColVector<TYPE> &vec2);
template <class TYPE> TYPE				bilinear(Matrix<TYPE> &m, float x, float y);
template <class TYPE> TYPE				bilinear2(Matrix<TYPE> &m, float x, float y);
template <class TYPE> TYPE				bilinear3(Matrix<TYPE> &m, float x, float y);
template <class TYPE> Matrix<TYPE>		rotate(Matrix<TYPE> &m, double theta);
template <class TYPE> Matrix<TYPE>		affine(Matrix<TYPE> &m, DMatrix s);
template <class TYPE> Matrix<TYPE>		fliph(Matrix<TYPE> &m);
template <class TYPE> Matrix<TYPE>		flipv(Matrix<TYPE> &m);
template <class TYPE> void				congrid(Matrix<TYPE> &m, long cols, long rows);
template <class TYPE> BMatrix			bytscl(Matrix<TYPE> m);
template <class TYPE> Matrix<TYPE>		max(Matrix<TYPE> &m1, Matrix<TYPE> &m2);
template <class TYPE> Matrix<TYPE>		min(Matrix<TYPE> &m1, Matrix<TYPE> &m2);
template <class TYPE> TYPE				min(Matrix<TYPE> &m);
template <class TYPE> TYPE				max(Matrix<TYPE> &m);
template <class TYPE> TYPE				min(ColVector<TYPE> &m);
template <class TYPE> TYPE				max(ColVector<TYPE> &m);
template <class TYPE> TYPE				total(ColVector<TYPE> &v);
template <class TYPE> TYPE				total(Matrix<TYPE> &v);
template <class TYPE> TYPE				dist2(ColVector<TYPE> &v1, const TYPE x2,const TYPE y2,const TYPE z2);
template <class TYPE> TYPE				dist2(ColVector<TYPE> &v1, ColVector<TYPE> &v2);
template <class TYPE> TYPE				dist(ColVector<TYPE> &v1, const TYPE x2,const TYPE y2,const TYPE z2);
template <class TYPE> TYPE				dist(ColVector<TYPE> &v1, ColVector<TYPE> &v2);
template <class TYPE> void				normalize(TYPE *v0,TYPE *v1,TYPE *v2);
template <class TYPE> TYPE				length(TYPE *v0,TYPE *v1,TYPE *v2);


void static								_panic(char *mess);

//---------------------------------------------------------------------------------------
// Structures.
//---------------------------------------------------------------------------------------
typedef struct 
{              
	int refCount;
} 
RefCounter;


//---------------------------------------------------------------------------------------
// The Matrix class.
//---------------------------------------------------------------------------------------
template <class TYPE>
class Matrix
{
	friend class RowVector<TYPE>;
	friend class ColVector<TYPE>;

private:

	// Used for submatrix operations
	int init(Matrix<TYPE> &m,int startr, int startc, int nrows, int ncols);

protected:

	RefCounter *refPtr;			// Keeps track of references to data.

	// These are functions to reference and unreference shared data.
	RefCounter *ref();
	void unref();

public:

	int rowNum;					// Number of rows
	int colNum;					// Number of columns
	int dsize;					// Current matrix size (rowNum*colNum)
	int trsize;					// Total row size

	TYPE *data;					// Pointer to first data element
	TYPE **rowPtrs;				// Pointers to rows

	// Matrix constructors and destructors
	Matrix();
	Matrix(int rr,int cc);
	Matrix(const Matrix<TYPE>& m);
	Matrix(Matrix<TYPE> &m, int startr, int startc, int nrows, int ncols);
	~Matrix();

	// Accessor functions for the row and column number
	int n_of_rows() const;
	int n_of_cols() const;

	// Now resize () is used only in constructors
	int resize(int nrows, int ncols);

	// Overloaded operators
	Matrix<TYPE>& operator=(TYPE x);
	Matrix<TYPE>& operator=(const Matrix<TYPE> &m);
	Matrix<TYPE>& operator+=(TYPE x);
	Matrix<TYPE>& operator+=(const Matrix<TYPE> &mat);
	Matrix<TYPE>& operator-=(TYPE x);
	Matrix<TYPE>& operator-=(const Matrix<TYPE> &mat);
	Matrix<TYPE>& operator*=(TYPE x);
	Matrix<TYPE>& operator/=(TYPE x);
	Matrix<TYPE> operator*(TYPE x) const;
	Matrix<TYPE> operator*(const Matrix<TYPE> &mat) const;
	Matrix<TYPE> operator/(TYPE x) const;
	Matrix<TYPE> operator/(const Matrix<TYPE> &mat) const;
	Matrix<TYPE> operator+(const Matrix<TYPE> &mat) const;
	Matrix<TYPE> operator+(TYPE x) const;
	Matrix<TYPE> operator-(const Matrix<TYPE> &mat) const;
	Matrix<TYPE> operator-() const;
	Matrix<TYPE> operator-(TYPE x) const;

	TYPE SumSquare() const;	
	TYPE Sum() const;		

	// These functions return copies of specified rows & cols
	TYPE* operator[](int n);
	TYPE* operator[](int n) const;
	RowVector<TYPE> row(int i);
	ColVector<TYPE> col(int j);


	Matrix<TYPE> operator()(int sr, int lr, int sc, int lc);
	Matrix<TYPE> Rows(int first_row, int last_row);
	Matrix<TYPE> Columns(int first_col, int last_col);

	// Inverse, transpose, inner product and outer product
	Matrix<TYPE> i() const;
	Matrix<TYPE> t() const;
	Matrix<TYPE> ip() const;
	Matrix<TYPE> op() const;
	// Matrix Fast Inverse
	Matrix<TYPE> fastInv() const;
	bool isUnity() const;

	// Another inverse routine, but this one allows static parameters to be sent in.
	void inv(Matrix<TYPE> &B, Matrix<TYPE> &V, ColVector<TYPE> &W, 
		     Matrix<TYPE> &A, Matrix <TYPE> &X) const;

	// The following functions are both desctuctive and nondestructive
	// Destructive LU decomposition
	void LUDcmp(int* perm, int& d);
	
	// Destructive backsubstitution
	void LUBksb(int* perm, ColVector<TYPE>& b);
	
	// Nondestructive solving Ax = B
	void solveByLUD(const ColVector<TYPE>& b, ColVector<TYPE>& x);
	
	// Nondestructive x = A.LUDsolve(B), equivalent to solveByLUD
	ColVector<TYPE> LUDsolve(const ColVector<TYPE>& b);
	
	// Does L D Ltranspose decomposition
	void LDLtDcmp();
	
	// Matrix square root
	Matrix<TYPE> sqrt();
	Matrix<TYPE> abs();

	Matrix<TYPE> elem_sqrt();


	// Destructive singular value decomposition
	void SVDcmp(ColVector<TYPE>& w, Matrix<TYPE>& v);
	
	// Destructive singular value backsubstitution
	void SVBksb(const ColVector<TYPE>& w,const Matrix<TYPE>& v, const ColVector<TYPE>& b, 
				ColVector<TYPE>& x);

	// Nondestructive solving of Ax = B
	void solveBySVD(const ColVector<TYPE>& b, ColVector<TYPE>& x);
	
	// Nondestructive equivalent to above
	ColVector<TYPE> SVDsolve(const ColVector<TYPE>& b);
		
	// This takes the function "*fn" and performs it to each element
	// of the invoking matrix and puts the resulting elements into the
	// returned matrix
	Matrix<TYPE> Map(TYPE (*fn)(TYPE)) const;

};


//---------------------------------------------------------------------------------------
// Row vector class.
//---------------------------------------------------------------------------------------
template <class TYPE>
class RowVector : public Matrix<TYPE>
{
	friend class Matrix<TYPE>;

protected:

	RowVector(Matrix<TYPE> &m, int i);

public:

	RowVector();
	RowVector(int nn);
	RowVector(const RowVector<TYPE> &v);

	void resize(int i);
	TYPE &operator [](int n);
	const TYPE &operator [](int n) const;
	RowVector<TYPE> &operator=(const RowVector<TYPE> &v);
	RowVector<TYPE> &operator=(const Matrix<TYPE> &m);
	RowVector<TYPE> &operator=(TYPE x);
	ColVector<TYPE> t() const;
};


//---------------------------------------------------------------------------------------
// Column vector class.
//---------------------------------------------------------------------------------------
template <class TYPE>
class ColVector : public Matrix<TYPE>
{
	friend class Matrix<TYPE>;

protected:

	ColVector (Matrix<TYPE> &m, int j);
	ColVector (ColVector &m, int startr, int nrows);

public:

	ColVector();
	ColVector(int nn);
	ColVector (const ColVector<TYPE> &v);
	~ColVector();

	void resize(int i);
	TYPE &operator [](int n);
	const TYPE &operator [](int n) const;
	ColVector<TYPE> &operator=(const ColVector &v);
	ColVector<TYPE> &operator=(const Matrix<TYPE> &m);
	ColVector<TYPE> &operator=(TYPE x);

	ColVector<TYPE> operator+(const ColVector<TYPE> &mat) const;
	ColVector<TYPE> operator-(const ColVector<TYPE> &mat) const;
	ColVector<TYPE> operator*(TYPE x) const;
	ColVector<TYPE> operator*(const ColVector<TYPE> &mat) const;
	ColVector<TYPE> operator-();
	ColVector<TYPE> operator/(TYPE x) const;

	ColVector<TYPE> Rows(int first_row, int last_row);
	RowVector<TYPE> t() const;

	void normalize();
	TYPE length();
	TYPE lengthSquared();
	
	TYPE ip() const;
};


//---------------------------------------------------------------------------------------
// Inline member functions.
//---------------------------------------------------------------------------------------

// Reference Update
template <class TYPE> inline RefCounter*
Matrix<TYPE>::ref()
{
  if (refPtr == NULL) 
  {
    refPtr = new RefCounter;
    refPtr->refCount = 0;
  }
  refPtr->refCount++;
  return refPtr;
}

// Unreference
template <class TYPE> inline void
Matrix<TYPE>::unref() {refPtr->refCount--;}

// Matrix Constructor (Sub-matrix) 
template <class TYPE> inline
Matrix<TYPE>::Matrix(Matrix<TYPE> &m, int startr, int startc, int nrows, int ncols)
{
	if ( (startr<0) || (startc<0) )
		_panic("Illegal submatrix operation \n");
	if (((startr + nrows) > m.rowNum) || ((startc + ncols) > m.colNum))
		_panic ("Submatrix larger than matrix \n");
	memset((void *)this, 0, sizeof(Matrix<TYPE>));
	init(m,startr,startc,nrows,ncols);
}

// Matrix Constructor (No Parameters)
template <class TYPE> inline
Matrix<TYPE>::Matrix() {memset((void *)this, 0, sizeof(Matrix<TYPE>));}

// Matrix Constructor (Rows and Columns Defined)
template <class TYPE> inline
Matrix<TYPE>::Matrix(int rr,int cc)
{memset((void *)this, 0, sizeof(Matrix<TYPE>)); resize(rr, cc);}

// Number of Rows
template <class TYPE> inline int
Matrix<TYPE>::n_of_rows() const	{return rowNum;}

// Number of Columns
template <class TYPE> inline int
Matrix<TYPE>::n_of_cols() const	{return colNum;}

// Matrix Row Index Operator (Constant)
template <class TYPE> inline TYPE*
Matrix<TYPE>::operator[](int n) const {return rowPtrs[n];}

// Matrix Row Index Operator (Variable)
template <class TYPE> inline TYPE*
Matrix<TYPE>::operator[](int n) {return rowPtrs[n];}

// Return Sub-matrix (Columns and Rows Specified)
template <class TYPE> inline Matrix<TYPE>
Matrix<TYPE>::operator()(int sr, int lr, int sc, int lc)
{return Matrix<TYPE>(*this,sr,sc,lr-sr+1,lc-sc+1);}

// Return Sub-matrix (Rows Specified)
template <class TYPE> inline Matrix<TYPE>
Matrix<TYPE>::Rows(int first_row, int last_row)
{return Matrix<TYPE>(*this, first_row, 0, last_row-first_row+1, colNum);}

// Return Sub-matrix (Columns Specified)
template <class TYPE> inline Matrix<TYPE>
Matrix<TYPE>::Columns(int first_col, int last_col)
{return Matrix<TYPE>(*this, 0, first_col, rowNum, last_col-first_col+1);}
 
// RowVector Constructor (No Parameters)
template <class TYPE> inline
RowVector<TYPE>::RowVector() : Matrix<TYPE>() {;}

// RowVector Constructor (Size Defined)
template <class TYPE> inline
RowVector<TYPE>::RowVector(int nn) : Matrix<TYPE>(1,nn){;}

// Resize RowVector
template <class TYPE> inline void
RowVector<TYPE>::resize(int i) {Matrix<TYPE>::resize(1, i);}

// Resize ColVector
template <class TYPE> inline void
ColVector<TYPE>::resize(int j) {Matrix<TYPE>::resize(j, 1);}

// RowVector Element Index Operator (Constant)
template <class TYPE> inline const TYPE&
RowVector<TYPE>::operator [](int n) const {return rowPtrs[0][n];}

// RowVector Element Index Operator (Variable)
template <class TYPE> inline TYPE&
RowVector<TYPE>::operator [](int n) {return rowPtrs[0][n];}

// ColVector Constructor (Sub-column)
template <class TYPE> inline
ColVector<TYPE>::ColVector (ColVector<TYPE> &m, int startr, int nrows)
{
	if (startr<0)
		_panic("Illegal submatrix operation in Colvector\n");
	if ( (startr+nrows) > m.rowNum )
		_panic("Submatrix larger than matrix in Colvector\n");
	init(m, startr, 0, nrows, 1);
}

// ColVector Constructor (No Parameters)
template <class TYPE> inline
ColVector<TYPE>::ColVector() : Matrix<TYPE>() {;}

// ColVector Constructor (Size Defined)
template <class TYPE> inline
ColVector<TYPE>::ColVector(int nn) : Matrix<TYPE>(nn,1) {;}

// ColVector Destructor
template <class TYPE> inline
ColVector<TYPE>::~ColVector() {;}

// ColVector Element Index Operator (Constant)
template <class TYPE> inline const TYPE&
ColVector<TYPE>::operator [](int n) const 
{return rowPtrs[n][0];}

// ColVector Element Index Operator (Variable)
template <class TYPE> inline TYPE&
ColVector<TYPE>::operator [](int n) 
{return rowPtrs[n][0];}

// Return Sub-Column
template <class TYPE> inline ColVector<TYPE>
ColVector<TYPE>::Rows(int first_row, int last_row) 
{return ColVector(*this, first_row, last_row-first_row+1);}

// RowVector Constructor (Copy Constructor)
template <class TYPE> inline
RowVector<TYPE>::RowVector (const RowVector<TYPE> &v) : Matrix<TYPE>(v) {};

// ColVector Constructor (Copy Constructor)
template <class TYPE> inline
ColVector<TYPE>::ColVector (const ColVector<TYPE> &v) : Matrix<TYPE>(v) {};

// RowVector Constructor (Sub-row)
template <class TYPE> inline
RowVector<TYPE>::RowVector (Matrix<TYPE> &m, int i) : Matrix<TYPE>(m, i, 0, 1, m.colNum) {};

// ColVector Constructor (Sub-column)
template <class TYPE> inline
ColVector<TYPE>::ColVector(Matrix<TYPE>&m, int j) : Matrix<TYPE>(m,0,j,m.rowNum,1) {};

// Return Specified Matrix Row
template <class TYPE> inline RowVector<TYPE> 
Matrix<TYPE>::row(int i) { return RowVector<TYPE>(*this, i); }

// Return Specified Matrix Column
template <class TYPE> inline ColVector<TYPE> 
Matrix<TYPE>::col(int j) { return ColVector<TYPE>(*this, j); }


//---------------------------------------------------------------------------------------
// Member Functions.
//---------------------------------------------------------------------------------------

// Matrix Constructor (Copy Constructor)
template <class TYPE>
Matrix<TYPE>::Matrix(const Matrix<TYPE>& m) 
{
  memset((void *)this, 0, sizeof(Matrix<TYPE>));
  resize(m.rowNum,m.colNum);
  for (int i=0; i<rowNum; i++) {
    for (int j=0; j<colNum; j++) {
      rowPtrs[i][j] = m.rowPtrs[i][j];
    }
  }
}

// Resize Matrix
template <class TYPE> int
Matrix<TYPE>::resize(int nrows, int ncols) 
{
  // If things are the same, do nothing and return
  if ((nrows == rowNum) && (ncols == colNum)) {return 1;}
  rowNum = nrows; colNum = ncols;
    
  // Check data room
  if ( (nrows*ncols != dsize) || (dsize == 0) ) {
    dsize = nrows*ncols;
    if ( data ) delete []data;
    data = new TYPE[dsize];
  }
  // Check column room
  if ( (nrows != trsize) || (trsize == 0) ) {
    trsize = nrows;
    if ( rowPtrs ) delete []rowPtrs;
    rowPtrs = new TYPE*[trsize];
  }
  TYPE **t;
  t = rowPtrs;
  for (int i=0; i<dsize; i+=ncols)
    *t++ = data + i;

  // Set all values to zero.
  memset(data,0,rowNum*colNum*sizeof(TYPE));

  return 1;
}

// Matrix Initialization
template <class TYPE> int
Matrix<TYPE>::init(Matrix<TYPE> &m,int startr, int startc, int nrows, int ncols)
{  
  // Set up the Matrix parameters
  rowNum = nrows;
  colNum = ncols;
  rowPtrs = new TYPE*[nrows];
 
  // Set up the pointers
  int i;
  for (i=0;i<nrows;i++) {rowPtrs[i] = m.rowPtrs[i+startr]+startc;}
  
  // This is a shared structure, so reference it.
  refPtr = m.ref();
  
  data  = m.data;
  dsize = nrows*ncols;

  return 1;
}

// Matrix Destructor
template <class TYPE> 
Matrix<TYPE>::~Matrix()
{
	if (rowPtrs != NULL) delete []rowPtrs;
	if (refPtr == NULL) delete []data;
	else if (refPtr->refCount == 0) 
	{
		delete refPtr;
		delete []data;
	}
	else unref();
}

// Matrix Assignment (Matrix)
template <class TYPE> Matrix<TYPE>&
Matrix<TYPE>::operator=(const Matrix<TYPE> &m) 
{
  resize(m.rowNum, m.colNum);
  for (int i=0; i<rowNum; i++) {
    for (int j=0; j<colNum; j++) {
      rowPtrs[i][j] = m.rowPtrs[i][j];
    }
  }
  return *this;
}

// Matrix Assignment (Constant)
template <class TYPE> Matrix<TYPE>&
Matrix<TYPE>::operator=(TYPE x) 
{
  for (int i=0; i<rowNum; i++) {
    for (int j=0; j<colNum; j++) {
      rowPtrs[i][j] = x;
    }
  }
  return *this;
}

// Matrix Element-by-Element Product
template <class TYPE> Matrix<TYPE>
Matrix<TYPE>::operator*(const Matrix<TYPE> &mat) const
{
  Matrix<TYPE> p(rowNum,mat.colNum);
  
  for (int i=0;i<rowNum;i++)
    for (int j=0;j<mat.colNum;j++)
	  p[i][j] = rowPtrs[i][j] * mat.rowPtrs[i][j];
  return p;
}

// Matrix Divide (Constant)
template <class TYPE> Matrix<TYPE>
Matrix<TYPE>::operator/(TYPE x) const
{
  Matrix<TYPE> v(rowNum,colNum);
  int i;int j;
  for (i=0;i<rowNum;i++)
    for(j=0;j<colNum;j++)
      v.rowPtrs[i][j] = rowPtrs[i][j]/x;
  return v;
}

// Matrix Element-by-Element Divide
template <class TYPE> Matrix<TYPE>
Matrix<TYPE>::operator/(const Matrix<TYPE> &mat) const
{
  Matrix<TYPE> p(rowNum,mat.colNum);
  
  for (int i=0;i<rowNum;i++)
    for (int j=0;j<mat.colNum;j++)
	  p[i][j] = rowPtrs[i][j] / mat.rowPtrs[i][j];
  return p;
}

// Matrix Add (Matrix)
template <class TYPE> Matrix<TYPE>
Matrix<TYPE>::operator+(const Matrix<TYPE> &mat) const
{
  CHECKSIZE(mat,"Incompatible size in +");
  Matrix<TYPE> v(rowNum,colNum);
  int i;int j;
  for (i=0;i<rowNum;i++)
    for(j=0;j<colNum;j++)
      v.rowPtrs[i][j] = mat.rowPtrs[i][j]+rowPtrs[i][j];
  return v;
}

// ColVector Add (ColVector)
template <class TYPE> ColVector<TYPE>
ColVector<TYPE>::operator+(const ColVector<TYPE> &mat) const
{
  CHECKSIZE(mat,"Incompatible size in +");
  ColVector<TYPE> v(rowNum);
  for (int i=0;i<rowNum;i++)
      v[i] = (*this)[i] + mat[i];
  return v;
}

// Matrix Add (Constant)
template <class TYPE> Matrix<TYPE>
Matrix<TYPE>::operator+(TYPE x) const 
{
  Matrix<TYPE> A(rowNum,colNum);

  for (int i=0; i<rowNum; i++)
    for (int j=0; j<colNum; j++)
    	A[i][j] = rowPtrs[i][j] + x;
  return A;
}

// Matrix Subtract (Matrix)
template <class TYPE> Matrix<TYPE>
Matrix<TYPE>::operator-(const Matrix<TYPE> &mat) const 
{
  CHECKSIZE(mat,"Incompatible size in -");
  Matrix<TYPE> v(rowNum,colNum);
  int i;int j;
  for (i=0;i<rowNum;i++)
    for(j=0;j<colNum;j++)
      v.rowPtrs[i][j] = rowPtrs[i][j]-mat.rowPtrs[i][j];
  return v;
}

// ColVector Subtract (ColVector)
template <class TYPE> ColVector<TYPE>
ColVector<TYPE>::operator-(const ColVector<TYPE> &mat) const
{
  CHECKSIZE(mat,"Incompatible size in +");
  ColVector<TYPE> v(rowNum);
  for (int i=0;i<rowNum;i++)
      v[i] = (*this)[i] - mat[i];
  return v;
}

// ColVector Divide (Constant)
template <class TYPE> ColVector<TYPE>
ColVector<TYPE>::operator/(TYPE x) const
{
  ColVector<TYPE> v(rowNum);
  for (int i=0;i<rowNum;i++)
      v[i] = (*this)[i] / x;
  return v;
}

// Matrix Negate
template <class TYPE> Matrix<TYPE>
Matrix<TYPE>::operator-() const
{
  Matrix<TYPE> A=(*this);

  for (int i=0; i<rowNum; i++)
    for (int j=0; j<colNum; j++)
    	A[i][j] = - rowPtrs[i][j];
  return A;
}

// Matrix Subtract (Constant)
template <class TYPE> Matrix<TYPE>
Matrix<TYPE>::operator-(TYPE x) const
{
  Matrix<TYPE> A(rowNum,colNum);

  for (int i=0; i<rowNum; i++)
    for (int j=0; j<colNum; j++)
    	A[i][j] = rowPtrs[i][j] - x;
  return A;
}

// ColVector Negate
template <class TYPE> ColVector<TYPE>
ColVector<TYPE>::operator-() 
{
  ColVector<TYPE> A=(*this);

  for (int i=0; i<rowNum; i++)
    	A[i]= - (*this)[i];
  return A;
}

// Matrix Assignment Add (Constant)
template <class TYPE> Matrix<TYPE>&
Matrix<TYPE>::operator+=(TYPE x){
  int i;int j;
  for (i=0;i<rowNum;i++)
    for(j=0;j<colNum;j++)
      rowPtrs[i][j] += x;
  return *this;
}

// Matrix Assignment Add (Matrix)
template <class TYPE> Matrix<TYPE>&
Matrix<TYPE>::operator+=(const Matrix<TYPE> &mat)
{
  CHECKSIZE(mat,"Incompatible size in +=");
  int i;int j;
  for (i=0;i<rowNum;i++)
    for(j=0;j<colNum;j++)
      rowPtrs[i][j] += mat.rowPtrs[i][j];
  return *this;
}

// Matrix Assignment Subtract (Constant)
template <class TYPE> Matrix<TYPE>&
Matrix<TYPE>::operator-=(TYPE x){
  int i;int j;
  for (i=0;i<rowNum;i++)
    for(j=0;j<colNum;j++)
      rowPtrs[i][j] -= x;
  return *this;
}

// Matrix Assignment Subtract (Matrix)
template <class TYPE> Matrix<TYPE>&
Matrix<TYPE>::operator-=(const Matrix<TYPE> &mat) {
  CHECKSIZE(mat,"Incompatible size in -=");
  int i;int j;
  for (i=0;i<rowNum;i++)
    for(j=0;j<colNum;j++)
      rowPtrs[i][j] -= mat.rowPtrs[i][j];
  return *this;
}

// Matrix Assignment Element-by-Element Product (Constant)
template <class TYPE> Matrix<TYPE>&
Matrix<TYPE>::operator*=(TYPE x)
{
  int i;int j;
  for (i=0;i<rowNum;i++)
    for(j=0;j<colNum;j++)  rowPtrs[i][j] *= x;
  return *this;
}

// Matrix Assignment Divide (Constant)
template <class TYPE> Matrix<TYPE>&
Matrix<TYPE>::operator/=(TYPE x)
{
  int i;int j;
  for (i=0;i<rowNum;i++)
    for(j=0;j<colNum;j++)  rowPtrs[i][j] /= x;
  return *this;
}

// Matrix Element-by-Element Product (Constant)
template <class TYPE> Matrix<TYPE>
Matrix<TYPE>::operator*(TYPE x) const
{
  Matrix<TYPE> v(rowNum,colNum);
  int i;int j;
  for (i=0;i<rowNum;i++)
    for(j=0;j<colNum;j++)  v.rowPtrs[i][j] = rowPtrs[i][j]*x;
  return v;
}

// ColVector Element-by-Element Product (Constant)
template <class TYPE> ColVector<TYPE>
ColVector<TYPE>::operator*(TYPE x) const
{
  ColVector<TYPE> v(rowNum);
  for (int i=0;i<rowNum;i++)
    v[i] = (*this)[i] * x;
  return v;
}

// ColVector Element-by-Element Product (ColVector)
template <class TYPE> ColVector<TYPE>
ColVector<TYPE>::operator*(const ColVector<TYPE> &x) const
{
  ColVector<TYPE> v(rowNum);
  for (int i=0;i<rowNum;i++)
    v[i] = (*this)[i] * x[i];
  return v;
}

// ColVector Assignment (Matrix)
template <class TYPE> ColVector<TYPE>&
ColVector<TYPE>::operator=(const Matrix<TYPE> &m) 
{
  resize(m.rowNum);
  for (int i=0; i<rowNum; i++) {
    for (int j=0; j<colNum; j++) {
      rowPtrs[i][j] = m.rowPtrs[i][j];
    }
  }
  return *this;
}

// ColVector Assignment (ColVector)
template <class TYPE> ColVector<TYPE>&
ColVector<TYPE>::operator=(const ColVector<TYPE> &v) 
{
  resize(v.rowNum);
  for (int i=0; i<rowNum; i++) {
    for (int j=0; j<colNum; j++) {
      rowPtrs[i][j] = v.rowPtrs[i][j];
    }
  }
  return *this;
}

// ColVector Assignment (Constant)
template <class TYPE> ColVector<TYPE>&
ColVector<TYPE>::operator=(TYPE x) 
{
  for (int i=0; i<rowNum; i++) {
    for (int j=0; j<colNum; j++) {
      rowPtrs[i][j] = x;
    }
  }
  return *this;
}


// Normalize ColVector
template <class TYPE> inline 
void ColVector<TYPE>::normalize() 
{
	TYPE x = length();
	if (x == TYPE(0)) {
		rowPtrs[0][0] = rowPtrs[1][0] = TYPE(0);
		rowPtrs[2][0] = TYPE(1);
		return;
	}

	x = TYPE(1) / x;
	rowPtrs[0][0] *= x;
	rowPtrs[1][0] *= x;
	rowPtrs[2][0] *= x;
}


// Length of a 3-vector
template <class TYPE> inline 
TYPE ColVector<TYPE>::length()
{
	return ::sqrt((double)lengthSquared());
}

// Length Squared of a 3-vector
template <class TYPE> inline 
TYPE ColVector<TYPE>::lengthSquared()
{
	TYPE x2 = rowPtrs[0][0]*rowPtrs[0][0];
	TYPE y2 = rowPtrs[1][0]*rowPtrs[1][0];
	TYPE z2 = rowPtrs[2][0]*rowPtrs[2][0];
	return x2 + y2 + z2;
}


// RowVector Assignment (RowVector)
template <class TYPE> RowVector<TYPE>&
RowVector<TYPE>::operator=(const RowVector<TYPE> &v) 
{
  if (colNum==0) {resize(v.colNum);}
  for (int i=0; i<rowNum; i++) {
    for (int j=0; j<colNum; j++) {
      rowPtrs[i][j] = v.rowPtrs[i][j];
    }
  }
  return *this;
}

// RowVector Assignment (Matrix)
template <class TYPE> RowVector<TYPE>&
RowVector<TYPE>::operator=(const Matrix<TYPE> &m) 
{
  if (colNum==0) {resize(m.colNum);}
  for (int i=0; i<rowNum; i++) {
    for (int j=0; j<colNum; j++) {
      rowPtrs[i][j] = m.rowPtrs[i][j];
    }
  }
  return *this;
}

// RowVector Assignment (Constant)
template <class TYPE> RowVector<TYPE>&
RowVector<TYPE>::operator=(TYPE x) 
{
  for (int i=0; i<rowNum; i++) {
    for (int j=0; j<colNum; j++) {
      rowPtrs[i][j] = x;
    }
  }
  return *this;
}

// Matrix Sum of Squared Values
template <class TYPE> TYPE
Matrix<TYPE>::SumSquare() const
{
  TYPE sum=0.0;
  for (int i=0; i<rowNum; i++)
    for (int j=0; j<colNum; j++)
      sum +=rowPtrs[i][j]*rowPtrs[i][j];

  return sum;
}

// Matrix Sum
template <class TYPE> TYPE
Matrix<TYPE>::Sum() const
{
  TYPE sum=0.0;
  for (int i=0; i<rowNum; i++)
    for (int j=0; j<colNum; j++)
      sum +=rowPtrs[i][j];

  return sum;
}

// Matrix Transpose
template <class TYPE> Matrix<TYPE>
Matrix<TYPE>::t() const 
{
  Matrix<TYPE> tmp(colNum,rowNum);
  int i,j;
  for (i=0;i<rowNum;i++)
    for (j=0;j<colNum;j++)
      tmp[j][i] = (*this)[i][j];
  return tmp;
}

// RowVector Transpose
template <class TYPE> ColVector<TYPE>
RowVector<TYPE>::t() const 
{
  ColVector<TYPE> tmp(colNum);
  for (int i=0;i<colNum;i++)
      tmp[i] = (*this)[i];
  return tmp;
}

// ColVector Transpose
template <class TYPE> RowVector<TYPE>
ColVector<TYPE>::t() const 
{
  RowVector<TYPE> tmp(rowNum);
  for (int i=0;i<rowNum;i++)
      tmp[i] = (*this)[i];
  return tmp;
}

// Matrix Inner Product
template <class TYPE> Matrix<TYPE>
Matrix<TYPE>::ip() const
{
  Matrix<TYPE> tmp(colNum,colNum);
  TYPE sum = 0;

  int i,j,k;
  for (i=0;i<colNum;i++) {
    for (j=i;j<colNum;j++) {
      sum = 0;
      for (k=0;k<rowNum;k++)
	sum += (*this)[k][i]* (*this)[k][j];
      tmp[j][i] = tmp[i][j] = sum;
    }
  }
  return tmp;
}

// ColVector Inner Product
template <class TYPE> TYPE
ColVector<TYPE>::ip() const 
{
  TYPE sum = 0;
  for (int i=0;i<rowNum;i++)
//      sum += sqr((*this)[i]);
      sum += pow((*this)[i],2);
  return sum;
}

// Matrix Outer Product
template <class TYPE> Matrix<TYPE>
Matrix<TYPE>::op() const
{
  Matrix<TYPE> tmp(rowNum,rowNum);
  TYPE sum = 0;
  int i,j,k;
  for (i=0;i<rowNum;i++) {
    for (j=0;j<rowNum;j++) {
      sum = 0;
      for (k=0;k<colNum;k++) {
	sum += rowPtrs[i][k] * rowPtrs[j][k];
      }
      tmp[j][i] = tmp[i][j] = sum;
    }
  }
  return tmp;
}

#define TINY 1.0e-20;

// LU Decomposition
template <class TYPE> void
Matrix<TYPE>::LUDcmp(int *perm, int& d)
{
  int n = rowNum;

  int i,imax,j,k;
  TYPE big,dum,sum,temp;
  ColVector<TYPE> vv(n);
  
  d=1;
  for (i=0;i<n;i++) {
    big=0.0;
    for (j=0;j<n;j++)
      if ((temp=fabs(rowPtrs[i][j])) > big) big=temp;
    if (big == 0.0) _panic("Singular matrix in  LUDcmp");
    vv[i]=1.0/big;
  }
  for (j=0;j<n;j++) {
    for (i=0;i<j;i++) {
      sum=rowPtrs[i][j];
      for (k=0;k<i;k++) sum -= rowPtrs[i][k]*rowPtrs[k][j];
      rowPtrs[i][j]=sum;
    }
    big=0.0;
    for (i=j;i<n;i++) {
      sum=rowPtrs[i][j];
      for (k=0;k<j;k++)
	sum -= rowPtrs[i][k]*rowPtrs[k][j];
      rowPtrs[i][j]=sum;
      if ( (dum=vv[i]*fabs(sum)) >= big) {
	big=dum;
	imax=i;
      }
    }
    if (j != imax) {
      for (k=0;k<n;k++) {
	dum=rowPtrs[imax][k];
	rowPtrs[imax][k]=rowPtrs[j][k];
	rowPtrs[j][k]=dum;
      }
      d *= -1;
      vv[imax]=vv[j];
    }
    perm[j]=imax;
    if (rowPtrs[j][j] == 0.0) rowPtrs[j][j]=TINY;
    if (j != n) {
      dum=1.0/(rowPtrs[j][j]);
      for (i=j+1;i<n;i++) rowPtrs[i][j] *= dum;
    }
  }
}

#undef TINY


// LU Back-substitution
template <class TYPE> void
Matrix<TYPE>::LUBksb(int *perm, ColVector<TYPE>& b)
{
  int n = rowNum;

  int i,ii=-1,ip,j;
  TYPE sum;
  
  for (i=0;i<n;i++) {
    ip=perm[i];
    sum=b[ip];
    b[ip]=b[i];
    if (ii != -1)
      for (j=ii;j<=i-1;j++) sum -= rowPtrs[i][j]*b[j];
    else if (sum) ii=i;
    b[i]=sum;
  }
  for (i=n-1;i>=0;i--) {
    sum=b[i];
    for (j=i+1;j<n;j++) sum -= rowPtrs[i][j]*b[j];
    b[i]=sum/rowPtrs[i][i];
  }
}
		
// Solve Ax=B with LU Decomposition
template <class TYPE> void
Matrix<TYPE>::solveByLUD(const ColVector<TYPE> &B, ColVector<TYPE>& X)
{
  if (colNum != rowNum)
    _panic("Solution for nonsquare matrix");

  Matrix<TYPE> A(rowNum, rowNum);
  A = *this;

  X = B;

  int *perm = new int[rowNum];
  int p;

  A.LUDcmp(perm, p);

  A.LUBksb(perm, X);

  delete []perm;
}

// Solve Ax=B with LU Decomposition (Equivalent to solveByLUD)
template <class TYPE> ColVector<TYPE>
Matrix<TYPE>::LUDsolve(const ColVector<TYPE>& B) {
  ColVector<TYPE> X(rowNum);
  solveByLUD(B, X);
  return X;
}

// LDL Transpose Decomposition
template <class TYPE> void
Matrix<TYPE>::LDLtDcmp()
{
  Matrix<TYPE>* A = this;
  int n = rowNum;
  Matrix<TYPE> L(n, n);
  int i, j;

  ColVector<TYPE> v(A->rowNum);
  for (j=0; j<n; j++) {
    // Compute v
    for (i=0; i<=j-1; i++) {
      v[i] = (*A)[j][i]*(*A)[i][i];
    }

    if (j==0) {
      v[j] = (*A)[j][j];
      // Store D[j] only 
      (*A)[j][j] = v[j];
      (*A)(j+1,n-1,j,j) = (*A)(j+1,n-1,j,j)/v[j];
    } else {
      v[j] = (*A)[j][j]-((*A)(j,j,0,j-1)*v(0,j-1,0,0))[0][0];
      // Store D[j] and compute L(j+1:n,j)
      (*A)[j][j] = v[j];
      (*A)(j+1,n-1,j,j) = ((*A)(j+1,n-1,j,j)-
			   (*A)(j+1,n-1,0,j-1)*v(0,j-1,0,0))/
			   v[j];
    }
  }
}
  
// Matrix Square Root
template <class TYPE> Matrix<TYPE>
Matrix<TYPE>::sqrt()
{
  // The matrix has to be symmetric and positive definite
  Matrix<TYPE> A(rowNum, colNum);

  A = *this;
  A.LDLtDcmp();
  for (int j=0; j<colNum; j++) {
    A[j][j] = ::sqrt(A[j][j]);
    for (int i=j+1; i<rowNum; i++) {
      A[i][j] *= A[j][j];
      A[j][i] = 0.0;
    }
  }
  return A;
}

// Element-wise Matrix Square Root
template <class TYPE> Matrix<TYPE>
Matrix<TYPE>::elem_sqrt()
{
  // The matrix has to be symmetric and positive definite
  Matrix<TYPE> A(rowNum, colNum);

  A = *this;
  for (int j=0; j<colNum*rowNum; j++) 
  {
	  A.data[j] = ::sqrt(A.data[j]);
  }
  return A;
}

// Element-wise Absolute Value
template <class TYPE> Matrix<TYPE>
Matrix<TYPE>::abs()
{
  Matrix<TYPE> A(rowNum, colNum);

  A = *this;
  for (int j=0; j<colNum*rowNum; j++) 
  {
	  A.data[j] = (TYPE)fabs(A.data[j]);
  }
  return A;
}

// Assign Matrix to Functional Mapping of Itself
template <class TYPE> Matrix<TYPE>
Matrix<TYPE>::Map(TYPE (*fn)(TYPE)) const
{
  int i, j;
  Matrix<TYPE> temp(rowNum,colNum);
  
  for (i=0;i<rowNum;i++)
    for (j=0;j<colNum;j++)
      temp[i][j] = fn((*this)[i][j]);

  return temp;
}

static double at,bt,ct;
#define PYTHAG(a,b) ((at=fabs(a)) > (bt=fabs(b)) ? \
(ct=bt/at,at*::sqrt(1.0+ct*ct)) : (bt ? (ct=at/bt,bt*::sqrt(1.0+ct*ct)): 0.0))

static double maxarg1,maxarg2;

#ifdef MAX
#undef MAX
#endif
#ifdef MIN
#undef MIN
#endif

#define MAX(a,b) (a,b,a>b?a:b)
#define MIN(a,b) (a,b,a<b?a:b)

#ifdef SIGN
#undef SIGN
#endif

#define SIGN(a,b) ((b) >= 0.0 ? fabs(a) : -fabs(a))

// Element-wise max of two matrices.
template <class TYPE> 
Matrix<TYPE> max(Matrix<TYPE> &m1, Matrix<TYPE> &m2)
{
	long i;
	Matrix<TYPE> fm(m1.rowNum,m1.colNum);

	for (i=0;i<m1.rowNum*m1.colNum;i++)
	{
		fm.data[i] = MAX(m1.data[i], m2.data[i]);
	}
	return fm;
}

// Element-wise min of two matrices.
template <class TYPE> 
Matrix<TYPE> min(Matrix<TYPE> &m1, Matrix<TYPE> &m2)
{
	long i;
	Matrix<TYPE> fm(m1.rowNum,m1.colNum);

	for (i=0;i<m1.rowNum*m1.colNum;i++)
	{
		fm.data[i] = MIN(m1.data[i], m2.data[i]);
	}
	return fm;
}

// Squared distance between two points in 3-space
template <class TYPE> TYPE dist2(ColVector<TYPE> &v1, const TYPE x2,const TYPE y2,const TYPE z2)
{
	TYPE dx = x2 - v1[0];
	TYPE dy = y2 - v1[1];
	TYPE dz = z2 - v1[2];

	return dx*dx + dy*dy + dz*dz;
}

// Squared distance between two points in 3-space
template <class TYPE> TYPE dist2(ColVector<TYPE> &v1, ColVector<TYPE> &v2)
{
	TYPE dx = v2[0] - v1[0];
	TYPE dy = v2[1] - v1[1];
	TYPE dz = v2[2] - v1[2];

	return dx*dx + dy*dy + dz*dz;
}

// Squared distance between two points in 3-space
template <class TYPE> TYPE dist(ColVector<TYPE> &v1, const TYPE x2,const TYPE y2,const TYPE z2)
{
	return ::sqrt(Dist2(v1,x2,y2,z2));
}

// Squared distance between two points in 3-space
template <class TYPE> TYPE dist(ColVector<TYPE> &v1, ColVector<TYPE> &v2)
{
	return ::sqrt(Dist2(v1,v2));
}

// Normalize a 3-vector (make it unit length)
template <class TYPE> void normalize(TYPE *v0,TYPE *v1,TYPE *v2)
{
	TYPE x = length(*v0, *v1, *v2);
	if (x == TYPE(0)) {
		*v0 = *v1 = TYPE(0);
		*v2 = TYPE(1);
		return;
	}

	x = TYPE(1) / x;
	*v0 *= x;
	*v1 *= x;
	*v2 *= x;
}

template <class TYPE> TYPE length(const TYPE x,const TYPE y,const TYPE z)
{
	return ::sqrt(x*x + y*y + z*z);
}



// Singular Value Decomposition
template <class TYPE> void 
Matrix<TYPE>::SVDcmp(ColVector<TYPE>& W, Matrix<TYPE>& V)
{
	int m = this->rowNum;
	int n = this->colNum;

	int flag,i,its,j,jj,k,l,nm;
	TYPE c,f,h,s,x,y,z;
	TYPE anorm=0.0,g=0.0,scale=0.0;
	TYPE invscale;

	// So that the original NRC code (using 1..n indexing) can be used
	// This should be considered as a temporary fix.
	TYPE **a = new TYPE*[m+1];
	TYPE **v = new TYPE*[n+1];
	TYPE **w = W.rowPtrs;

	w--;
	for (i=1;i<=m;i++) 
	{
		a[i] = this->rowPtrs[i-1]-1; 
	}
	for (i=1;i<=n;i++) 
	{
		v[i] = V.rowPtrs[i-1]-1;
	}

	if (m < n) _panic("SVDcmp: You must augment A with extra zero rows");
	TYPE* rv1=new TYPE[n+1]; 

	for (i=1;i<=n;i++) 
	{
		l=i+1;
		rv1[i]=scale*g;
		g=s=scale=0.0;
		if (i <= m) 
		{
			for (k=i;k<=m;k++) scale += fabs(a[k][i]);
			if (scale) 
			{
				invscale = TYPE(1.0/scale);
				for (k=i;k<=m;k++) 
				{
					a[k][i] *= invscale;
					s += a[k][i]*a[k][i];
				}
				f=a[i][i];
				g = -SIGN(::sqrt(s),f);
				h=TYPE(1.0/(f*g-s));
				a[i][i]=f-g;
				if (i != n) 
				{
					for (j=l;j<=n;j++) 
					{
						for (s=0.0,k=i;k<=m;k++) s += a[k][i]*a[k][j];
						f=s*h;
						for (k=i;k<=m;k++) a[k][j] += f*a[k][i];
					}
				}
				for (k=i;k<=m;k++) a[k][i] *= scale;
			}
		}
		w[i][0]=scale*g;
		g=s=scale=0.0;
		if (i <= m && i != n) 
		{
			for (k=l;k<=n;k++) scale += fabs(a[i][k]);
			if (scale) 
			{
				invscale = TYPE(1.0/scale);
				for (k=l;k<=n;k++) 
				{
					a[i][k] *= invscale;
					s += a[i][k]*a[i][k];
				}
				f=a[i][l];
				g = -SIGN(::sqrt(s),f);
				h=TYPE(1.0/(f*g-s));
				a[i][l]=f-g;
				for (k=l;k<=n;k++) rv1[k]=a[i][k]*h;
				if (i != m) 
				{
					for (j=l;j<=m;j++) 
					{
						for (s=0.0,k=l;k<=n;k++) s += a[j][k]*a[i][k];
						for (k=l;k<=n;k++) a[j][k] += s*rv1[k];
					}
				}
				for (k=l;k<=n;k++) a[i][k] *= scale;
			}
		}
		anorm=MAX(anorm,(fabs(w[i][0])+fabs(rv1[i])));
	}
	for (i=n;i>=1;i--) 
	{
		if (i < n) 
		{
			if (g) 
			{
				g = TYPE(1.0/g);
				for (j=l;j<=n;j++) v[j][i]=(a[i][j]/a[i][l])*g;
				for (j=l;j<=n;j++) 
				{
					for (s=0.0,k=l;k<=n;k++) s += a[i][k]*v[k][j];
					for (k=l;k<=n;k++) v[k][j] += s*v[k][i];
				}
			}
			for (j=l;j<=n;j++) v[i][j]=v[j][i]=0.0;
		}
		v[i][i]=1.0;
		g=rv1[i];
		l=i;
	}
	for (i=n;i>=1;i--) 
	{
		l=i+1;
		g=w[i][0];
		if (i < n) 
		{
			for (j=l;j<=n;j++) a[i][j]=0.0;
		}
		if (g) 
		{
			g=TYPE(1.0/g);
			if (i != n) 
			{
				for (j=l;j<=n;j++) 
				{
					for (s=0.0,k=l;k<=m;k++) s += a[k][i]*a[k][j];
					f=(s/a[i][i])*g;
					for (k=i;k<=m;k++) a[k][j] += f*a[k][i];
				}
			}
			for (j=i;j<=m;j++) a[j][i] *= g;
		} 
		else 
		{
			for (j=i;j<=m;j++) a[j][i]=0.0;
		}
		++a[i][i];
	}
	for (k=n;k>=1;k--) 
	{
		for (its=1;its<=30;its++) 
		{
			flag=1;
			for (l=k;l>=1;l--) 
			{
				nm=l-1;
				if (fabs(rv1[l])+anorm == anorm)
				{
					flag=0;
					break;
				}
				if (fabs(w[nm][0])+anorm == anorm) break;
			}
			if (flag) 
			{
				c=0.0;
				s=1.0;
				for (i=l;i<=k;i++) 
				{
					f=s*rv1[i];
					if (fabs(f)+anorm != anorm) 
					{
						g=w[i][0];
						h=TYPE(PYTHAG(f,g));
						w[i][0]=h;
						h=TYPE(1.0/h);
						c=g*h;
						s=(-f*h);
						for (j=1;j<=m;j++) 
						{
							y=a[j][nm];
							z=a[j][i];
							a[j][nm]=y*c+z*s;
							a[j][i]=z*c-y*s;
						}
					}
				}
			}
			z=w[k][0];
			if (l == k) 
			{
				if (z < 0.0) 
				{
					w[k][0] = -z;
					for (j=1;j<=n;j++) v[j][k]=(-v[j][k]);
				}
				break;
			}
			if (its == 30) printf("SVDcmp:no convergence in 30 iterations");
			x=w[l][0];
			nm=k-1;
			y=w[nm][0];
			g=rv1[nm];
			h=rv1[k];
			f=TYPE(((y-z)*(y+z)+(g-h)*(g+h))/(2.0*h*y));
			g=TYPE(PYTHAG(f,1.0));
			f=((x-z)*(x+z)+h*((y/(f+SIGN(g,f)))-h))/x;
			c=s=1.0;
			for (j=l;j<=nm;j++) 
			{
				i=j+1;
				g=rv1[i];
				y=w[i][0];
				h=s*g;
				g=c*g;
				z=TYPE(PYTHAG(f,h));
				rv1[j]=z;
				c=f/z;
				s=h/z;
				f=x*c+g*s;
				g=g*c-x*s;
				h=y*s;
				y=y*c;
				for (jj=1;jj<=n;jj++) 
				{
					x=v[jj][j];
					z=v[jj][i];
					v[jj][j]=x*c+z*s;
					v[jj][i]=z*c-x*s;
				}
				z=TYPE(PYTHAG(f,h));
				w[j][0]=z;
				if (z) 
				{
					z=TYPE(1.0/z);
					c=f*z;
					s=h*z;
				}
				f=(c*g)+(s*y);
				x=(c*y)-(s*g);
				for (jj=1;jj<=m;jj++) 
				{
					y=a[jj][j];
					z=a[jj][i];
					a[jj][j]=y*c+z*s;
					a[jj][i]=z*c-y*s;
				}
			}
			rv1[l]=0.0;
			rv1[k]=f;
			w[k][0]=x;
		}
	}
	delete[] rv1;
	delete[] a;
	delete[] v;
}

#undef SIGN
#undef MAX
#undef PYTHAG

// SVD Back-substitution
template <class TYPE> void 
Matrix<TYPE>::SVBksb(const ColVector<TYPE>& w, const Matrix<TYPE>& v,  
		    const ColVector<TYPE>& b, ColVector<TYPE>& x)
{
  int m = this->rowNum;
  int n = this->colNum;
  TYPE** u = rowPtrs;

  int jj,j,i;
  TYPE s,*tmp=NULL;

  tmp=new TYPE[n];
  for (j=0;j<n;j++) {
    s=0.0;
    if (w[j]) {
      for (i=0;i<m;i++) s += u[i][j]*b[i];
      s /= w[j];
    }
    tmp[j]=s;
  }
  for (j=0;j<n;j++) {
    s=0.0;
    for (jj=0;jj<n;jj++) s += v[j][jj]*tmp[jj];
    x[j]=s;
  }
  delete []tmp;
}

#define TOL 1.0e-6

// Solve Ax=B by SVD
template <class TYPE> void 
Matrix<TYPE>::solveBySVD(const ColVector<TYPE>& b, ColVector<TYPE>& x)
{
  int j;
  TYPE wmax,thresh;

  int ma = this->colNum;

  ColVector<TYPE> w(ma);
  Matrix<TYPE> v(ma,ma);

  Matrix<TYPE> A(rowNum, colNum);
  A= *this;
  A.SVDcmp(w,v);

  wmax=0.0;
  for (j=0;j<ma;j++)
    if (w[j] > wmax) wmax=w[j];
  thresh=TOL*wmax;
  for (j=0;j<ma;j++)
    if (w[j] < thresh) w[j]=0.0;

  A.SVBksb(w,v,b,x);
}

// Solve Ax=B by SVD (Equivalent to solveBySVD)
template <class TYPE> ColVector<TYPE> 
Matrix<TYPE>::SVDsolve(const ColVector<TYPE>& B) {
  ColVector<TYPE> X(colNum);
  solveBySVD(B, X);
  return X;
}


// Matrix Fast Inverse
template <class TYPE> Matrix<TYPE>
Matrix<TYPE>::fastInv() const
{
	if ( rowNum != 4 || colNum != 4)
		_panic("Cannot Fast Invert Non 4x4 matrix");
	Matrix<TYPE>  matrix(rowNum, rowNum);
	matrix = *this;
	
	swap(matrix[1][0], matrix[0][1]);
	swap(matrix[2][0], matrix[0][2]);
	swap(matrix[2][1], matrix[1][2]);

	TYPE tx = -matrix[0][3];
	TYPE ty = -matrix[1][3];
	TYPE tz = -matrix[2][3];

	matrix[0][3] = tx*matrix[0][0] + ty*matrix[0][1] + tz*matrix[0][2];
	matrix[1][3] = tx*matrix[1][0] + ty*matrix[1][1] + tz*matrix[1][2];
	matrix[2][3] = tx*matrix[2][0] + ty*matrix[2][1] + tz*matrix[2][2];

	return matrix;

}

// Matrix Fast Inverse
template <class TYPE> bool
Matrix<TYPE>::isUnity() const
{
	bool retval = true;

	for(int i=0; i<rowNum; i++)
	{
		for(int j=0; j<colNum; j++)
		{
			if(i==j)
			{
				if(rowPtrs[i][j] != 1.0)
				{
					retval = false;
					break;
				}
			}	
			else
			{
				if(rowPtrs[i][j] != 0.0)
				{
					retval = false;
					break;
				}
			}
		}
	}
	return retval;

}




// Matrix Inverse
template <class TYPE> Matrix<TYPE>
Matrix<TYPE>::i() const
{
  int i,j;
  long ndx=0;

  if ( rowNum != colNum)
    _panic("Cannot invert a non-square matrix");

  Matrix<TYPE> B(rowNum, rowNum), X(rowNum, rowNum);
  Matrix<TYPE> V(rowNum, rowNum);
  ColVector<TYPE> W(rowNum);

  for (i=0; i<rowNum; i++) {
    for (j=0; j<rowNum; j++) {
      B.data[ndx++] = TYPE((i == j) ? 1 : 0);
    }
  }

  Matrix<TYPE> A(rowNum, rowNum);
  A = *this;
  A.SVDcmp(W, V);

  // Zero out small W's
  TYPE maxW=0.0;
  for (j=0;j<rowNum;j++)
    if (W.data[j] > maxW) maxW=W.data[j];
  TYPE thresh=TYPE(TOL*maxW);
  for (j=0;j<rowNum;j++)
    if (W.data[j] < thresh) W.data[j]=0.0;
  for (j=0; j<rowNum; j++) 
  { 
     A.SVBksb(W, V, B.col(j), X.col(j));
  }
  return X;
}

// Matrix Inverse (Same as the other routine, but static parameters can be passed in)
template <class TYPE> void
Matrix<TYPE>::inv(Matrix<TYPE> &B, Matrix<TYPE> &V, ColVector<TYPE> &W, 
				  Matrix<TYPE> &A, Matrix <TYPE> &X) const
{
  int i,j;
  long ndx=0;

  if ( rowNum != colNum)
    _panic("Cannot invert a non-square matrix");

  for (i=0; i<rowNum; i++) {
    for (j=0; j<rowNum; j++) {
      B.data[ndx++] = (i == j) ? 1 : 0;
    }
  }

  A = *this;
  A.SVDcmp(W, V);

  // Zero out small W's
  TYPE maxW=0.0;
  for (j=0;j<rowNum;j++)
    if (W.data[j] > maxW) maxW=W.data[j];
  TYPE thresh=TOL*maxW;
  for (j=0;j<rowNum;j++)
    if (W.data[j] < thresh) W.data[j]=0.0;
  for (j=0; j<rowNum; j++) 
  { 
     A.SVBksb(W, V, B.col(j), X.col(j));
  }
}

#undef TOL


//---------------------------------------------------------------------------------------
// Non-Member Functions.
//---------------------------------------------------------------------------------------

// Matrix Multiply (Matrix)
template <class TYPE> Matrix<TYPE>
mult(const Matrix<TYPE> &mat1, const Matrix<TYPE> &mat2)
{
	Matrix<TYPE> p(mat1.rowNum,mat2.colNum);
	long ndx=0;
	p = 0;

	if (mat1.colNum != mat2.rowNum)
		_panic("Matrix mismatch in matrix matrix multiply");

	for (int i=0;i<mat1.rowNum;i++)
		for (int j=0;j<mat2.colNum;j++)
		{
			for (int k=0;k<mat2.rowNum;k++)
				p.data[ndx]+=mat1.rowPtrs[i][k] * mat2.rowPtrs[k][j];
			ndx++;
		}

	return p;
}

// Matrix Multiply (ColVector)
template <class TYPE> ColVector<TYPE>
mult(const Matrix<TYPE> &mat, const ColVector<TYPE> &vec)
{
	ColVector<TYPE> p(mat.rowNum);

	if (mat.colNum != vec.rowNum)
		_panic("Matrix mismatch in matrix/vector multiply\n");

	p = 0;

	for (int i=0;i<mat.rowNum;i++)
		for (int j=0;j<mat.colNum;j++) 
			p.data[i]+=mat.rowPtrs[i][j] * vec[j];

	return p;
}

// Matrix Multiply (RowVector)
template <class TYPE> RowVector<TYPE>
mult(const Matrix<TYPE> &mat, const RowVector<TYPE> &vec)
{
	RowVector<TYPE> p(mat.colNum);
	TYPE *ptr = vec.rowPtrs[0],*ptr1;
	float pp;

	if (mat.rowNum != vec.colNum)
		_panic("Matrix mismatch in matrix/vector multiply\n");

	p = 0.0;
	for (int i=0;i<mat.rowNum;i++)
	{
		ptr1 = mat.rowPtrs[i];
		pp = (float)ptr[i];
		for (int j=0;j<mat.colNum;j++) 
		{
			p.data[j] += ptr1[j] * pp;
		}
	}

	return p;
}

// Vector Cross-Product (Only valid for 3-Vectors)
template <class TYPE> ColVector<TYPE>
crossp(const ColVector<TYPE> &vec1, const ColVector<TYPE> &vec2)
{
	ColVector<TYPE> p(vec1.rowNum);

	if ((vec1.rowNum != 3) && (vec2.rowNum != 3))
	{
		printf("Not 3-Vector.  Cross Product Failed.  Setting to Zero.\n");
		p=0;
	}
	else
	{
		p[0] = vec1[1]*vec2[2] - vec1[2]*vec2[1];
		p[1] = -(vec1[0]*vec2[2] - vec1[2]*vec2[0]);
		p[2] = vec1[0]*vec2[1] - vec1[1]*vec2[0];
	}
	return p;
}

// Vector Dot-Product
template <class TYPE> TYPE
dotp(const ColVector<TYPE> &vec1, const ColVector<TYPE> &vec2)
{
	TYPE sum = 0;
	return total(vec1*vec2);
}

// Total (ColVector)
template <class TYPE> 
TYPE total(ColVector<TYPE> &v)
{
	TYPE sum = 0;
	long i;

	for (i=0;i<v.dsize;i++)	sum += v.rowPtrs[i][0];
	return sum;
}

// Total (Matrix)
template <class TYPE> 
TYPE total(Matrix<TYPE> &m)
{
	TYPE sum = 0;
	long i;

	for (i=0;i<m.dsize;i++) sum += m.data[i];
	return sum;
}

// Min (Matrix)
template <class TYPE>
TYPE min(Matrix<TYPE> &m)
{
	TYPE minval=m.data[0];
	long i;

	for (i=0;i<m.dsize;i++)
	{
		if (m.data[i] < minval) minval = m.data[i];
	}
	return minval;
}

// Min (ColVector)
template <class TYPE>
TYPE min(ColVector<TYPE> &v)
{
	TYPE minval=v.data[0];
	long i;

	for (i=0;i<v.dsize;i++)
	{
		if (v.rowPtrs[i][0] < minval) minval = v.rowPtrs[i][0];
	}
	return minval;
}

// Max (Matrix)
template <class TYPE>
TYPE max(Matrix<TYPE> &m)
{
	TYPE maxval=m.data[0];
	long i;

	for (i=0;i<m.dsize;i++)
	{
		if (m.data[i] > maxval) maxval = m.data[i];
	}
	return maxval;
}

// Max (ColVector)
template <class TYPE>
TYPE max(ColVector<TYPE> &v)
{
	TYPE maxval=v.data[0];
	long i;

	for (i=0;i<v.dsize;i++)
	{
		if (v.rowPtrs[i][0] > maxval) maxval = v.rowPtrs[i][0];
	}
	return maxval;
}

// Bilinear
template <class TYPE> 
TYPE bilinear(Matrix<TYPE> &m, float x, float y)
{
	long i, j, ip, jp;
	double dx, dy, dx1, dy1;

	i  = long(x);
	j  = long(y);

	if (x >= m.colNum-1) 
		ip=i=m.colNum-1;
	else if (x < 0) 
		ip=i=0;
	else 
		ip = i+1;

	if (y >= m.rowNum-1) 
		jp=j=m.rowNum-1;
	else if (y < 0) 
		jp=j=0;
	else 
		jp = j+1;

	dx = x-i; dx1 = 1.0-dx;
	dy = y-j; dy1 = 1.0-dy;

	return (TYPE(m.data[j*m.colNum+i]*dx1*dy1 + m.data[jp*m.colNum+i]*dx1*dy + 
		          m.data[j*m.colNum+ip]*dx*dy1 + m.data[jp*m.colNum+ip]*dx*dy));
}


// Bilinear with zero padding
template <class TYPE> 
TYPE bilinear2(Matrix<TYPE> &m, float x, float y)
{
	long i, j, ip, jp;
	double dx, dy, dx1, dy1;

	i  = long(x);
	j  = long(y);

	if (x >= m.colNum-1) 
		return 0.0;
	else if (x < 0) 
		return 0.0; 
	else 
		ip = i+1;

	if (y >= m.rowNum-1) 
		return 0.0;
	else if (y < 0) 
		return 0.0;
	else 
		jp = j+1;
	
	dx = x-i; dx1 = 1.0-dx;
	dy = y-j; dy1 = 1.0-dy;

	return (TYPE(m.data[j*m.colNum+i]*dx1*dy1 + m.data[jp*m.colNum+i]*dx1*dy + 
		          m.data[j*m.colNum+ip]*dx*dy1 + m.data[jp*m.colNum+ip]*dx*dy));
}


// Bilinear with 255 padding
template <class TYPE> 
TYPE bilinear3(Matrix<TYPE> &m, float x, float y)
{
	long i, j, ip, jp;
	double dx, dy, dx1, dy1;

	i  = long(x);
	j  = long(y);

	if (x >= m.colNum-1) 
		return 255.0;
	else if (x < 0) 
		return 255.0; 
	else 
		ip = i+1;

	if (y >= m.rowNum-1) 
		return 255.0;
	else if (y < 0) 
		return 255.0;
	else 
		jp = j+1;
	
	dx = x-i; dx1 = 1.0-dx;
	dy = y-j; dy1 = 1.0-dy;

	return (TYPE(m.data[j*m.colNum+i]*dx1*dy1 + m.data[jp*m.colNum+i]*dx1*dy + 
		          m.data[j*m.colNum+ip]*dx*dy1 + m.data[jp*m.colNum+ip]*dx*dy));
}


// Matrix Rotate
template <class TYPE> 
Matrix<TYPE> rotate(Matrix<TYPE> &m, double theta)
{
	double	pi = 3.1415926535897931;
	double	angle = theta*pi/180.0;
	long	i,j,k;
	double	jcoord;
	float	cols2 = m.colNum * 0.5;
	float	rows2 = m.rowNum * 0.5;
	static	Matrix<double> r(2,2);
	Matrix<double> coords(2,m.rowNum*m.colNum);
	Matrix<double> ncoords(2,m.rowNum*m.colNum);
	Matrix<TYPE> rm(m.rowNum,m.colNum);

	// Define the rotation matrix.
	r.data[0] = cos(angle);
	r.data[1] = -sin(angle);
	r.data[2] = sin(angle);
	r.data[3] = cos(angle);

	// Define the image coordinate matrix.
	k = 0;
	for (j=0;j<m.rowNum;j++)
	{
		jcoord = j - rows2;
		for (i=0;i<m.colNum;i++)
		{
			// Coordinates should be centered on zero.
			coords.data[k] = i - cols2;
			coords.data[coords.colNum+k] = jcoord;
			k++;
		}
	}

	// Rotate the image coordinates.
	ncoords = mult(r, coords);

	// Copy the bilinearly interpolated values into a new matrix.
	for (i=0;i<m.rowNum*m.colNum;i++)
	{
		rm.data[i] = bilinear(m, ncoords.data[i] + 
			cols2, ncoords.data[ncoords.colNum+i] + rows2);
	}

	return rm;
}


// Matrix Affine Transformation (corner-based coordinates)
template <class TYPE> 
Matrix<TYPE> affine(Matrix<TYPE> &m, DMatrix s)
{
	long			i, j, k;
	float			xshift = s[0][2];
	float			yshift = s[1][2];
	short			width = m.colNum;
	short			height = m.rowNum;
	DMatrix			c(3,width*height);
	DMatrix			t(2,width*height);
	Matrix<TYPE>	m2(width,height);

	// Generate a matrix with image coordinates.
	k=0;
	for (j=0;j<height;j++)
	{
		for (i=0;i<width;i++)
		{
			c[0][k] = i;
			c[1][k] = j;
			c[2][k] = 1.0;
			k++;
		}
	}

	// Multiply the matrices to get the new coordinates.
	t = mult(s,c);

	// Copy the bilinearly interpolated values into a new matrix.
	for (i=0;i<width*height;i++)
	{
		m2.data[i] = bilinear(m, t[0][i], t[1][i]);
	}

	return m2;
}


// Matrix Horizontal Flip
template <class TYPE> 
Matrix<TYPE> fliph(Matrix<TYPE> &m)
{
	long i,j;
	Matrix<TYPE> fm(m.rowNum,m.colNum);
	long ndx=0, ndx2=0;

	for (j=0;j<m.rowNum;j++)
	{
		ndx2 = (m.rowNum-j)*m.colNum-1;
		for (i=0;i<m.colNum;i++)
		{
			fm.data[ndx++] = m.data[ndx2--];
		}
	}
	return fm;
}

// Matrix Vertical Flip
template <class TYPE> 
Matrix<TYPE> flipv(Matrix<TYPE> &m)
{
	long i,j,k=0;
	Matrix<TYPE> fm(m.rowNum,m.colNum);

	for (j=0;j<m.rowNum;j++)
	{
		for (i=0;i<m.colNum;i++)
		{
			fm.data[k++] = m.data[i+(m.rowNum-j-1)*m.colNum];
		}
	}
	return fm;
}


#if 0
// Resize a matrix with linear interpolation.
template <class TYPE>
void congrid(Matrix<TYPE> &m, long cols, long rows) 
{
	long i, j;
	double xf, yf;
	float jpos;
	Matrix<TYPE> n(rows,cols);
	long ndx=0;

	// Define the scale factors.
//	if (cols > m.colNum) 
//		xf = m.colNum/double(cols+1);
//	else
//		xf = double(m.colNum+1)/cols;
//	if (rows > m.rowNum) 
//		yf = m.rowNum/double(rows+1);
//	else
//		yf = double(m.rowNum+1)/rows;
	xf = m.colNum/double(cols);
	yf = m.rowNum/double(rows);

	// Interpolate into the grid.
	for (j=0;j<rows;j++)
	{
		jpos = (j+1)*yf-1;
		for (i=0;i<cols;i++)
		{
//			n.data[ndx++] = bilinear(m, i*xf, j*yf);
			n.data[ndx++] = (TYPE)bilinear(m, (i+1)*xf-1, jpos);
		}
	}
	m = n;
}
#else
// Resize a matrix with linear interpolation.
template <class TYPE>
void congrid(Matrix<TYPE> &m, long cols, long rows) 
{
	long i, j;
	double xf, yf;
	float jpos;
	Matrix<TYPE> n(rows,cols);
	long ndx=0;

	// Define the scale factors.
//	if (cols > m.colNum) 
//		xf = m.colNum/double(cols+1);
//	else
//		xf = double(m.colNum+1)/cols;
//	if (rows > m.rowNum) 
//		yf = m.rowNum/double(rows+1);
//	else
//		yf = double(m.rowNum+1)/rows;
	xf = m.colNum/double(cols);
	yf = m.rowNum/double(rows);

	// Interpolate into the grid.
	for (j=0;j<rows;j++)
	{
		jpos = float(j*yf);
//		jpos = (j+1)*yf-yf/2.0;
		for (i=0;i<cols;i++)
		{
			n.data[ndx++] = bilinear(m, float(i*xf), float(j*yf));
//			n.data[ndx++] = (TYPE)bilinear(m, (i+1)*xf-xf/2.0, jpos);
		}
	}
	m = n;
}
#endif

// Convert Matrix to Byte Matrix.
template <class TYPE>
BMatrix bytscl(Matrix<TYPE> m)
{
	long i;
	BMatrix bm(m.rowNum,m.colNum);

	// This could be more efficient if max and min were computed together.
	TYPE minval = min(m);
	TYPE maxval = max(m);
	float norm = float(255.0/(maxval-minval));

	for (i=0;i<m.colNum*m.rowNum;i++)
	{
		bm.data[i] = (unsigned char)(norm*(m.data[i] - minval));
	}
	return bm;		
}


// Panic
void static _panic(char *mess)
{
  cerr << mess << endl;
  exit(1);
}

// Singular Value Decomposition
template <class TYPE> 
void SVD(Matrix<TYPE>& M, Matrix<TYPE>& Md, Matrix<TYPE>& U, Matrix<TYPE>& V) {

  ColVector<TYPE> D(M.n_of_rows());
  U = M;
  U.SVDcmp(D, V); // see explanation on SVDcmp

  U = U.t();
  V = V.t();

  for (int i=0; i<M.n_of_rows(); i++)
    for (int j=0; j<M.n_of_cols(); j++)
        if (i==j)
          Md[i][i]=D[i];
        else //i!=j
          Md[i][j]=0;
}


