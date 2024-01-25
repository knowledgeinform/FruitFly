//---------------------------------------------------------------------------------------
/*! \class File Name: DataFile.cpp
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
//---------------------------------------------------------------------------------------

#include "DataFile.h"
#include <stdlib.h>
#include <math.h>
#include "float.h"

/*! \fn DataFile::DataFile(void)
 *  \brief Default Constructor
 *  \param 
 *  \exception 
 *  \return 
 */
DataFile::DataFile(void)
{
	allocHeader = false;
	columnData = NULL;
	headerData = NULL;
	maxVals = NULL;
	minVals = NULL;
	offsets = NULL;
	labels = NULL;
}

/*! \fn DataFile::DataFile(const string &path, int npoints, int nfields)
 *  \brief Basic Constructor - to use when adding data
 *  \param  path - the file path
 *  \param  npoints - the number of points for which to allocate memory
 *  \param  nfields - the number of fields to allocate
 *  \exception 
 *  \return 
 */
DataFile::DataFile(const string &path, int npoints, int nfields)
{
	columnData = NULL;
	headerData = NULL;
	maxVals = NULL;
	minVals = NULL;
	offsets = NULL;
	labels = NULL;
	allocHeader = false;
	this->path = path.c_str();
	numColumns = 0;
	numRows = 0;
	populated = false;
	this->Allocate(npoints,nfields);
}

/*! \fn DataFile::DataFile(const string &path)
 *  \brief Basic Constructor - to use when adding data
 *  \param  path - the file path
 *  \exception 
 *  \return 
 */
DataFile::DataFile(const string &path)
{
	columnData = NULL;
	headerData = NULL;
	maxVals = NULL;
	minVals = NULL;
	offsets = NULL;
	labels = NULL;
	allocHeader = false;
	this->path = path.c_str();
	numColumns = 0;
	numRows = 0;
	populated = false;
}

/*! \fn DataFile::DataFile(const string &path, int headersize, int numcolumns)
 *  \brief Constructor for fixed width binary
 *  \param  path - the file path
 *  \param  headersize - the number of bytes in the header
 *  \param  numcolumns - the number of data fields
 *  \exception 
 *  \return 
 */
DataFile::DataFile(const string &path, int headersize, char*headerData, int colstoread)
{
		columnData = NULL;
		maxVals = NULL;
		minVals = NULL;
		offsets = NULL;
		labels = NULL;
		allocHeader = false;
	    this->path = path.c_str();
		numRows = 0;
		if(colstoread > 0)
			numColumns = colstoread;
		else
		numColumns = 0;
		this->setHeaderData(headerData,headersize);
		readBinaryFile(true);

		this->offsets = offsets;
}

/*! \fn DataFile::DataFile(const string &path, int numheaderrows, char delim)
 *  \brief Constructor for delimited ASCII
 *  \param  path - the file path
 *  \param  numheaderrows - the number of ascii lines of text in the header
 *  \param  delim - ascii file delimiter
 *  \exception 
 *  \return 
 */
DataFile::DataFile(const string &path, int numheaderrows, char delim, int colstoread, double* offsets)
{
	columnData = NULL;
	headerData = NULL;
	maxVals = NULL;
	minVals = NULL;
	offsets = NULL;
	labels = NULL;
	allocHeader = false;
	this->path = path.c_str();
	numRows = 0;
	this->offsets = offsets;
	if(colstoread > 0)
		numColumns = colstoread;
	else
		numColumns = 0;
	headerRows = numheaderrows;
	delimiter = delim;
	readAsciiFile(delim);
	populated = true;

	
}

/*! \fn DataFile::~DataFile(void)
 *  \brief Destructor
 *  \param 
 *  \exception 
 *  \return 
 */
DataFile::~DataFile(void)
{
	if(columnData != NULL)
	{
		for(int i=0; i<numColumns; i++)
			free (columnData[i]);
		free(columnData);
	}
	if(offsets != NULL)
		free(offsets);
	if(maxVals != NULL)
		free(maxVals);
	if(minVals != NULL)
		free(minVals);
	if(allocHeader)
		free(headerData);

	if(labels != NULL)
	{
		for(int i=0; i<numColumns; i++)
			delete[] labels[i];
		delete[] labels;
	}
}

/*! \fn void DataFile::setColumnData(float** data, int numrows, int numcols)
 *  \brief Sets column data
 *  \param data - the 2D float data
 *  \param numrows - the number of data points in each field
 *  \param numcols - the number of data fields
 *  \exception 
 *  \return 
 */
void DataFile::setColumnData(float** data, int numrows, int numcols)
{
	if(data != NULL)
	{
		numRows = numrows;
		numColumns = numcols;
		columnData = data;
		populated = true;
	}

}

/*! \fn void DataFile::setHeaderData(char* data, int headersize)
 *  \brief Sets the header data in one block - intended use to directly
 *  read into and out of a header data structure.
 *  \param data - the header data
 *  \param headersize - the number of bytes in the header data 
 *  \exception 
 *  \return 
 */
void DataFile::setHeaderData(char* data, int headersize)
{
	if(allocHeader)
		free(headerData);
	allocHeader = false;
	if(data != NULL)
	{
		headerSize = headersize;
		headerData = data;
	}
}

/*! \fn bool DataFile::isPopulated()
 *  \brief Check if the column data pointer is valid
 *  \exception 
 *  \return boolean value true if valid
 */
bool DataFile::isPopulated()
{
	return populated;
}

/*! \fn inline void DataFile::readAsciiFile(char delim)
 *  \brief Reads data in from a delimited ASCII file
 *  and allocates memory for said data.
 *  \param delim - the delimiting character
 *  \exception 
 *  \return 
 */
inline void DataFile::readAsciiFile(char delim, bool createXYoffset, bool createExtraOffsets, bool createZoffset)
{
	int i;
	char str[maxLineLength];
	double vals[maxColumns];
	bool firsttime = true;
	int rowix=0;

	fileIn = fopen(path.c_str(), "r");

	readAsciiHeader();

	int numVars;

	do
	{
		if (fgets(str, maxLineLength, fileIn) == NULL) break;
		numVars = parseString(str, numColumns, vals);
		if(firsttime)
		{
			if(numColumns<=0)
				numColumns = numVars;
			columnData = (float**)malloc(numColumns * sizeof(float*));
			minVals = (double*)malloc(numColumns*sizeof(double));
			maxVals = (double*)malloc(numColumns*sizeof(double));
			offsets = (double*)malloc(numColumns*sizeof(double));

			for(i=0; i<numColumns; i++)
			{
				minVals[i] = FLT_MAX;
				maxVals[i] = -FLT_MAX;
				columnData[i] = (float*)malloc(numRows * sizeof(float));
				offsets[i] = 0.0;
			}

			if(createXYoffset)
			{
				offsets[0] = (double)((long)vals[0]);
				offsets[1] = (double)((long)vals[1]);
			}

			if(createZoffset)
				offsets[2] = (double)((long)vals[2]);

			if(createExtraOffsets)
				for(i=3; i<numVars; i++)
					offsets[i] = (double)((long)vals[i]);

		   labels  = new char*[numColumns];
		   for(i=0; i<numColumns; i++)
		   {
				labels[i] = new char[LABEL_LENGTH];
				sprintf(labels[i], "Unknown\0");
		   }

			firsttime=false;
		}

		for(i=0; i<numVars; i++)
		{
			columnData[i][rowix] = (float)(vals[i] - offsets[i]);
			if (vals[i] < minVals[i]) minVals[i] = vals[i];
			if (vals[i] > maxVals[i]) maxVals[i] = vals[i];
		}

		rowix++;
	}
	while (numVars == numColumns);

	fclose(fileIn);

}

/*! \fn long DataFile::parseString(char *str, long numVars, double *vars)
 *  \brief Fast parse a string for double precision floating point values.
 *  \param str - character array to be parsed
 *  \param numVars - number of Variables to parse
 *  \param vars - the parsed double values
 *  \exception 
 *  \return the number parsed
 */
long DataFile::parseString(char *str, long numVars, double *vars)
{
	long num = 0;
	char *pos = str;
	long len = (long)strlen(str);
	char *endpos = &str[len-1] + 1;

	while (1)
	{
		// If separator, then skip.
		while (((*pos < 48) || (*pos > 57)) && (*pos != '-') && (*pos != '+') 
			&& (*pos != '.') && (pos < endpos)) pos++;
		
		// Else read the next variable.
		long place = 1, sgn = 1;
		long exponent = 0, sgnExp = 1;
		long preDecimal = 1;
		double pre = 0.0, post = 0.0, power = 0;
		while (((*pos >= 48) && (*pos <= 57)) || (*pos == '-') || (*pos == '.') || (*pos == '+')
			|| (*pos == 'e') || (*pos == 'E') && (pos < endpos))
		{
			switch (*pos)
			{
				case '-': { if (preDecimal) sgn = -1; else sgnExp = -1; break; }
				case '+': { break; }
				case '.': { preDecimal = 0; place = 1; break; }
				case 'e':
				case 'E': { exponent = 1; preDecimal = 0; break; }
				
				default:
				{
					if (exponent)
					{
						power = power * 10.0 + double(*pos - 48);
					}
					else if (preDecimal)
					{
						pre = pre * 10.0 + double(*pos - 48);
						place++;
					}
					else
					{
						post = post * 10.0 + double(*pos - 48);
						place++;
					}
				}
			}

			// Update counters.
			pos++;
		}
		place--;
		vars[num] = double(sgn * (pre + post / pow(10.0, place)));
		if (exponent) vars[num] *= (double)pow(10.0, power * sgnExp);
		num++;

		// Quit at end of line or all variables read.
		if ((pos >= endpos) || (num == numVars)) break;
	}

	// Return the number of variables read.
	return num;
}


/*! \fn long DataFile::processHeaderFile()
 *  \brief Default for no header - override to process header
 */
void DataFile::processHeaderFile()
{
}


/*! \fn void DataFile::readBinaryFile()
 *  \brief read in a Binary Data File  File Format:
 *  int - header size
 *  int - num rows
 *  int - num columns
 *  header - block of user defined header bytes
 *  col 1 data
 *  ...
 *  col n data
 *  \exception 
 *  \return
 */
void DataFile::readBinaryFile(bool readData)
{
	if(columnData != NULL)
	{
		for(int i=0; i<numColumns; i++)
			free (columnData[i]);
		free(columnData);
	}
	if(offsets != NULL)
		free(offsets);
	if(maxVals != NULL)
		free(maxVals);
	if(minVals != NULL)
		free(minVals);
	if(labels != NULL)
	{
		for(int i=0; i<numColumns; i++)
			delete []labels[i];
		delete []labels;
	}

	int i;
	int cols;

	fileIn = fopen(path.c_str(), "rb");

	//read number of header bytes
	fread(&headerSize,sizeof(int),1,fileIn);
	fread(&numRows,sizeof(int),1,fileIn);
	fread(&cols,sizeof(int),1,fileIn);
	if(numColumns <= 0 || numColumns > cols)
		numColumns = cols;

	columnData = (float**)malloc(numColumns * sizeof(float*));
		for(i=0; i<numColumns; i++)
			columnData[i] = (float*)malloc(numRows * sizeof(float));

	offsets = (double*)malloc(numColumns*sizeof(double));
	maxVals = (double*)malloc(numColumns*sizeof(double));
	minVals = (double*)malloc(numColumns*sizeof(double));
    labels  = new char*[numColumns];
	for(i=0; i<numColumns; i++)
		labels[i] = new char[LABEL_LENGTH];

	for(i=0; i<numColumns; i++)
		fread(&offsets[i],sizeof(double),1,fileIn);
	for(i=0; i<numColumns; i++)
		fread(&maxVals[i],sizeof(double),1,fileIn);
	for(i=0; i<numColumns; i++)
		fread(&minVals[i],sizeof(double),1,fileIn);
	for(i=0; i<numColumns; i++)
		fread(labels[i], sizeof(char), LABEL_LENGTH, fileIn);


	//read header
	if(headerSize > 0 && headerSize < 100000) //Sanity check
	if(headerData == NULL)
	{
		allocHeader = true;
		headerData = (char*)malloc(headerSize*sizeof(char));
	}
	fread(headerData,sizeof(char),headerSize,fileIn);

	processHeaderFile();

	populated=false;
	if(readData)
	{
		populated=true;
		//read data
		for(i=0; i<numColumns; i++)
		{
			fread(&columnData[i][0],sizeof(float),numRows,fileIn);
		}
	}

	fclose(fileIn);

}

/*! \fn void DataFile::writeBinaryFile()
 *  \brief write in a Binary Data File  File Format:
 *  int - header size
 *  int - num rows
 *  int - num columns
 *  header - block of user defined header bytes
 *  col 1 data
 *  ...
 *  col n data
 *  \exception 
 *  \return
 */
void DataFile::writeBinaryFile(string path)
{
	int i;

	FILE* fileOut = fopen(path.c_str(), "wb");

	//write number of header bytes
	if(headerData == NULL)
		headerSize = 0;
	fwrite(&headerSize,sizeof(int),1,fileOut);
	fwrite(&numRows,sizeof(int),1,fileOut);
	fwrite(&numColumns,sizeof(int),1,fileOut);

	//write basic header data (offsets, maxes, mins and labels)
	for(i=0; i<numColumns; i++)
		fwrite(&offsets[i],sizeof(double),1,fileOut);
	for(i=0; i<numColumns; i++)
		fwrite(&maxVals[i],sizeof(double),1,fileOut);
	for(i=0; i<numColumns; i++)
		fwrite(&minVals[i],sizeof(double),1,fileOut);
	for (i=0;i<numColumns;i++) 
		fwrite(labels[i], sizeof(char), LABEL_LENGTH, fileOut);
	
	//write header
	if(headerSize > 0 && headerSize < 100000) //Sanity check
		fwrite(headerData,sizeof(char),headerSize,fileOut);

	//write data
	for(i=0; i<numColumns; i++)
	{
		if(columnData[i] != NULL)
			fwrite(columnData[i],sizeof(float),numRows,fileOut);
	}

	fclose(fileOut);

}

/*! \fn void DataFile::readAsciiHeader()
 *  \brief read in a ASCII header and determine number of rows of data 
 *  \exception 
 *  \return
 */
void DataFile::readAsciiHeader()
{
	char linebuffer[maxLineLength];
	int numrows = 0;
	int headerlength = 0;
	while (1)
	{
		if (fgets(linebuffer, 1024, fileIn) == NULL) break;
		numrows++;
		if(numrows <= headerRows)
			headerlength += (int)strlen(linebuffer);
	}
	numRows =numrows;
	fseek(fileIn, 0, SEEK_SET);
	
	headerSize = headerlength;
	numRows = numrows-headerRows;
	
	if(headerSize >0)
	{
		allocHeader = true;
		headerData = (char*)malloc((headerSize+1)*sizeof(char));

		fseek(fileIn, 0, SEEK_SET);
		
		int headerdatapos = 0;

		for(int i=0; i<headerRows; i++)
		{
			fgets(&headerData[headerdatapos], maxLineLength, fileIn);
			headerdatapos = (int)strlen(headerData) + 1;
		}	
	}
	
}

/*! \fn int DataFile::getNumColumns()
 *  \brief return number of Columns
 *  \exception 
 *  \return number of Columns
 */
int DataFile::getNumColumns()
{
	return numColumns;
}

/*! \fn float* DataFile::getColumn(int column)
 *  \brief return Column data specified
 *  \param column - The column to return
 *  \exception 
 *  \return the Column of data (floats)
 */
float* DataFile::getColumn(int column)
{
	if(column < numColumns)
		return columnData[column];
	else
		return NULL;
}

/*! \fn int DataFile::getNumRows()
 *  \brief return number of Rows
 *  \exception 
 *  \return number of Rows
 */
int DataFile::getNumRows()
{
	return numRows;
}


/*! \fn float** DataFile::getColumnData()
 *  \brief return all Column data (2D floats)
 *  \exception 
 *  \return All the Column of data (float**)
 */
float** DataFile::getColumnData()
{
	return columnData;
}


double DataFile::getMaxVal(int column)
{
	if(maxVals!=NULL)
		return maxVals[column];
	else
		return NULL;

}

double DataFile::getMinVal(int column)
{
	if(minVals!=NULL)
		return minVals[column];
	else
		return NULL;

}





// Write a point set to an ASCII file.
bool DataFile::writeAsciiFile(char* fileName, bool applyOffsets)
{
	int len = (int)strlen(fileName);
	char fname[1024];
	if (strcmp(&fileName[len-4],".xyz") != 0)
		sprintf(fname, "%s.xyz", fileName);
	else
		sprintf(fname, "%s", fileName);
	FILE *fptr = fopen(fname, "w");
	if (!fptr) return false;
	for (int m=0;m<numRows;m++)
	{
		for(int n=0; n<numColumns; n++)
		{
			if(applyOffsets)
				fprintf(fptr, "%f ", this->columnData[n][m] + offsets[n]);
			else
				fprintf(fptr, "%f ", (double)this->columnData[n][m]);
		}
		fprintf(fptr, "\n");
	}
	fclose(fptr);
	return true;

}

// Allocate memory for Data File.
bool DataFile::Allocate(int npoints, int nfields)
{
	numRows = npoints;
	numColumns = nfields;
	if (numRows <= 0) return false;

	try
	{
		columnData = (float**)malloc(numColumns * sizeof(float*));
		minVals = (double*)malloc(numColumns*sizeof(double));
		maxVals = (double*)malloc(numColumns*sizeof(double));
		offsets = (double*)malloc(numColumns*sizeof(double));
		labels = new char*[numColumns];

		for(int i=0; i<numColumns; i++)
		{
			minVals[i] = FLT_MAX;
			maxVals[i] = -FLT_MAX;
			columnData[i] = (float*)malloc(numRows * sizeof(float));
			offsets[i] = 0.0;
			labels[i] = new char[LABEL_LENGTH];
			sprintf(labels[i], "Unknown\0");
		}

	}
	catch (bad_alloc&)
	{
		printf("Insufficient memory to allocate point set.\n");
		return false;
	}
	return true;
}


