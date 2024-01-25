//!---------------------------------------------------------------------------------------
/*! \class File Name: DataFile.h
 *
 *  \brief Purpose:	Generic class for processing data file consisting of numerous fields of
 *				data stored in columns.  Methods for reading/writing ASCII files in this
 *				format with configurable delimiter and for converting, reading,writing 
 *				the data into binary formats.
 *
 *  \author	Author: Jason Stipes
 *
 *  \note Copyright (c) 2008 Johns Hopkins University 
 *  \note Applied Physics Laboratory.  All Rights Reserved.
 *
 *  \date    2008
 */
//!---------------------------------------------------------------------------------------

#pragma once
#include <string>

using namespace std;

#define LABEL_LENGTH	32

class DataFile
{
	public:

		//! The maximum number of column data - for code simplicity
		static const int maxColumns = 16;

		//! The maximum size of an ASCII line of data - for code simplicity
		static const int maxLineLength = 1024;

		//! The input file
		FILE*		fileIn;

		//! The number of columns of data
		int			numColumns;

		//! The number of rows of data
		int			numRows;

		//! The number of ASCII header rows
		int			headerRows;

		//! The size of the binary header in bytes
		int			headerSize;

		//! byte pointer to the block of header data
		char*		headerData;

		//! ASCII delimiting character for reading in ASCII files
		char		delimiter;

		//! The 2D array of data
		float**     columnData;

		//! String representatio of the file path
		string		path;

		//! Populated indicates if the columnData structure has bee filled with data
		bool		populated;

		//! An array of maximum values in each column
		double*		maxVals;

		//! An array of maximum values in each column
		double*		minVals;

		//! An array of offsets to apply to each column
		double*		offsets;

		//! An array of descriptors to apply to each column
		char**		labels;

	public:

		//!Default Constructor
		DataFile(void);

		//!Empty Data File constructor
		DataFile(const string &path, int npoints, int nfields);

		//!Basic Constructor
		DataFile(const string &path);

		//!Constructor for fixed width binary
		DataFile(const string &path, int headersize, char* headerData, int colstoread = -1);

		//!Constructor for delimited ASCII
		DataFile(const string &path, int numheaderrows, char delim, int colstoread = -1, double* offsets = NULL);

		//!Destructor
		virtual ~DataFile(void);

		//!Override this to put in code on how to handle your specific header
		virtual void processHeaderFile();

		//!Allocates memory for data to be filled in
		bool Allocate(int npoints, int nfields);

		//!Sets the column data
		void setColumnData(float** data, int numrows, int numcols);

		//!Gets the column data
		float** getColumnData();

		//!Gets the maximum value in a column
		double getMaxVal(int column);

		//!Gets the minimum value in a column
		double getMinVal(int column);

		//!Sets the header data block
		void setHeaderData(char* data, int headersize);

		//!Gets the number of columns
		int getNumColumns();

		//!Indicates whether the data array is populated
		bool isPopulated();

		//!Writes out a file in binary format
		void writeBinaryFile(string path);

		//!Gets a specified column of data as a float array
		float* getColumn(int column);

		//!Gets the number of rows
		int getNumRows();

		//!Reads in a binary file and populates the object with the data
		void readBinaryFile(bool readData);

		//!Utility function for parsing ASCII file lines
		long parseString(char *str, long numVars, double *vars);

		//!Reads in an ASCII file and populates the object with the data
		void readAsciiFile(char delim, bool createXYoffset=true, bool createExtraOffsets=true, bool createZoffset=false); //!EMW

		//!Writes an ASCII file with the object data
		bool writeAsciiFile(char* filename, bool applyOffsets=true);

		//!Skips over header lines in an ASCII file
		void readAsciiHeader();

private:

		//!Keeps track if this object allocated the header data for cleanup
		bool allocHeader;

};
