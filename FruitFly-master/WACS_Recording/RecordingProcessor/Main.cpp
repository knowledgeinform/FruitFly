#include <algorithm>
#include <stdlib.h>
#include <stdio.h>
#include <conio.h>
#include <string>
#include <iostream>
#include <sys/timeb.h>
#include <direct.h>
#include <sys/stat.h>
#include <sys/types.h>
#include "XvidEncoder.h"
#include "edu_jhuapl_nstd_tase_RecordingProcessor.h"

#define WIN32_LEAN_AND_MEAN
#include <windows.h>

void ParseRecordingFile(const char* const szInputFileName);
void UpdateTimeRemaining();
void PrintCurrFrameNum();
void gotoxy(const int column, const int line);

//Bitmap definitions
static const int HEADER_LENGTH = 54;
static const int LOOKUP_TABLE_LENGTH = 256 * 4;
static const int IMAGE_WIDTH = 320;
static const int IMAGE_HEIGHT = 240;
static const int PIXEL_DATA_SIZE = IMAGE_WIDTH * IMAGE_HEIGHT;
static const int BITMAP_FILE_SIZE = HEADER_LENGTH + LOOKUP_TABLE_LENGTH + PIXEL_DATA_SIZE;
static const int PIXEL_DATA_OFFSET = HEADER_LENGTH + LOOKUP_TABLE_LENGTH;
static char s_pBitmapBuffer[BITMAP_FILE_SIZE];
static char* const s_pBitmapPixelData = &s_pBitmapBuffer[PIXEL_DATA_OFFSET];

static char s_szOutputFormat[16] = "frame";
static char s_szInputDirectory[MAX_PATH];
static char s_szOutputDirectory[MAX_PATH];
static char s_szChannel1OutputDirectory[MAX_PATH];
static char s_szChannel2OutputDirectory[MAX_PATH];
static int s_nInputDirectoryLength;
static int s_nOutputDirectoryLength;
static int s_nOutputDirectoryChannelNumIndex;
static _timeb s_oStartTime;
static float s_fAverageFileTime = 0;
static int s_nNumFilesCompleted = 0;
static float s_fCurrFilePercentComplete = 0;
static char s_pImageBuffer[PIXEL_DATA_SIZE];
static char s_pWarpBuffer[72];
static char s_szFullInputFileName[MAX_PATH];
static char s_szStartingOutputFileName[MAX_PATH];
static char s_szFinalOutputFileName[MAX_PATH] = {'\0'};
static char s_szOutputFileEnding[16];
static int s_nOutputBaseNameLength;
static int s_nChannel1FrameCount = 0;
static int s_nChannel2FrameCount = 0;
static int s_nNumInputFiles;
static int s_nTargetFrameNum;

static int MOVING_AVERAGE_WIDTH = 10;
static JNIEnv* s_env = NULL;
static jobject s_obj = NULL;

enum OUTPUT_MODE {OUTPUT_FRAMES, OUTPUT_BITMAPS, OUTPUT_XVID, OUTPUT_TIMESTAMP};
static OUTPUT_MODE s_nOutputMode;


int compare_string(const void* str1, const void* str2)
{
	return strcmp(*(char**)str1, *(char**)str2);
}



int main(const int argc, const char* const argv[])
{
	bool bArgsValid = true;


    if (argc < 3 || argc > 4)
    {
        bArgsValid = false;
    }
    else
    {                
        if (*argv[1] == '-')
        {
            if (strcmp(argv[1], "-bmp") == 0)
            {
				strcpy(s_szOutputFormat, "bmp");
				s_nOutputMode = OUTPUT_BITMAPS;
            }
			else if (strcmp(argv[1], "-xvid") == 0)
			{
				strcpy(s_szOutputFormat, "xvid");
				s_nOutputMode = OUTPUT_XVID;
			}
			else if (strcmp(argv[1], "-time") == 0)
			{
				strcpy(s_szOutputFormat, "timestamp");
				s_nOutputMode = OUTPUT_TIMESTAMP;
			}
            else
            {
                bArgsValid = false;
            }
        }
		else
		{
			s_nOutputMode = OUTPUT_FRAMES;
		}

        if (bArgsValid)
        {
			if (s_nOutputMode == OUTPUT_BITMAPS)
			{
				//
				// Create bitmap header that is the same for all images
				//
				int nDummy;
				char *pBufferField = s_pBitmapBuffer;
				memcpy(pBufferField, "BM", 2);
				pBufferField += 2;
				memcpy(pBufferField, &BITMAP_FILE_SIZE, 4); //total length of bitmap
				pBufferField += 4;
				nDummy = 0;
				memcpy(pBufferField, &nDummy, 4); //reserved
				pBufferField += 4;
				memcpy(pBufferField, &PIXEL_DATA_OFFSET, 4); //pixel data offset
				pBufferField += 4;
				nDummy = 0x28;
				memcpy(pBufferField, &nDummy, 4); //header size
				pBufferField += 4;
				memcpy(pBufferField, &IMAGE_WIDTH, 4);
				pBufferField += 4;
				memcpy(pBufferField, &IMAGE_HEIGHT, 4);
				pBufferField += 4;
				nDummy = 1;
				memcpy(pBufferField, &nDummy, 2); //num color planes
				pBufferField += 2;
				nDummy = 8;
				memcpy(pBufferField, &nDummy, 2); //num bits per pixel
				pBufferField += 2;
				memset(pBufferField, 0, 24); //rest of fields are all zero
				pBufferField += 24;

				//
				// Create grayscale lookup table
				//
				for (int i = 0; i < (LOOKUP_TABLE_LENGTH / 4); ++i)
				{
					*pBufferField = i;
					++pBufferField;
					*pBufferField = i;
					++pBufferField;
					*pBufferField = i;
					++pBufferField;
					*pBufferField = 0;
					++pBufferField;
				}
			}

			if (s_nOutputMode == OUTPUT_FRAMES ||
				s_nOutputMode == OUTPUT_BITMAPS ||
				s_nOutputMode == OUTPUT_XVID)
			{
				if (argc == 3)
				{
					strcpy_s(s_szInputDirectory, sizeof(s_szInputDirectory), argv[1]);
					strcpy_s(s_szOutputDirectory, sizeof(s_szOutputDirectory), argv[2]);
				}
				else
				{
					strcpy_s(s_szInputDirectory, sizeof(s_szInputDirectory), argv[2]);
					strcpy_s(s_szOutputDirectory, sizeof(s_szOutputDirectory), argv[3]);
				}
			}
			else if (s_nOutputMode == OUTPUT_TIMESTAMP)
			{
				strcpy_s(s_szInputDirectory, sizeof(s_szInputDirectory), argv[2]);
				strcpy_s(s_szOutputDirectory, sizeof(s_szOutputDirectory), s_szInputDirectory);
				s_nTargetFrameNum = atoi(argv[3]);
			}

            if (s_szInputDirectory[strlen(s_szInputDirectory) - 1] != '\\')
            {
				strcat(s_szInputDirectory, "\\");
            }

            if (s_szOutputDirectory[strlen(s_szOutputDirectory) - 1] != '\\')
            {
				strcat(s_szOutputDirectory, "\\");
            }

			strcpy(s_szChannel1OutputDirectory, s_szOutputDirectory);
			strcat(s_szChannel1OutputDirectory, "channel1\\");

			strcpy(s_szChannel2OutputDirectory, s_szOutputDirectory);
			strcat(s_szChannel2OutputDirectory, "channel2\\");

			s_nInputDirectoryLength = strlen(s_szInputDirectory);
			s_nOutputDirectoryLength = strlen(s_szOutputDirectory);
			s_nOutputDirectoryChannelNumIndex = s_nOutputDirectoryLength + 7;
			strcpy(s_szFullInputFileName, s_szInputDirectory);

			HANDLE hFindFiles;
			WIN32_FIND_DATA oFindFileData;
			BOOL bMoreFiles;
			char szDirectorySearch[MAX_PATH];
			strcpy(szDirectorySearch, s_szInputDirectory);
			strcat(szDirectorySearch, "*.rec");
			hFindFiles = ::FindFirstFile(szDirectorySearch, &oFindFileData);

			if (hFindFiles == INVALID_HANDLE_VALUE)
			{
				printf("Invalid input directory specified\n");
				exit(1);
			}


			//
			// Create an array containing all the .rec file names
			//
			s_nNumInputFiles = 0;
			int nInputFileNamesArrayLength = 1024;
			char **pInputFileNames = (char**)malloc(nInputFileNamesArrayLength * sizeof(char*));
			do
			{
				//
				// Resize array if necessary
				//
				if (s_nNumInputFiles == nInputFileNamesArrayLength)
				{
					nInputFileNamesArrayLength *= 2;
					realloc(pInputFileNames, nInputFileNamesArrayLength * sizeof(char*));
				}

				//
				// Add current file name to the array
				//
				pInputFileNames[s_nNumInputFiles] = (char*)malloc(strlen(oFindFileData.cFileName) + 1);
				strcpy(pInputFileNames[s_nNumInputFiles], oFindFileData.cFileName);
				++s_nNumInputFiles;

				bMoreFiles = FindNextFile(hFindFiles, &oFindFileData);
			}
			while(bMoreFiles);


			qsort(pInputFileNames, s_nNumInputFiles, sizeof(char*), compare_string);
			

			// Clear the console screen
			system("cls");

			printf("Input Directory: %s\n", s_szInputDirectory);
			
			if (s_nOutputMode != OUTPUT_TIMESTAMP)
			{
				printf("Output Directory: %s\n", s_szOutputDirectory);			
			}

			printf("Output Format: %s\n", s_szOutputFormat);		


			const char** pInputFileName = (const char**)pInputFileNames;
			for (int i = 0; i < s_nNumInputFiles; ++i)
			{
				_ftime(&s_oStartTime);

				s_fCurrFilePercentComplete = 0;

				strcpy(&s_szFullInputFileName[s_nInputDirectoryLength], *pInputFileName);
				ParseRecordingFile(s_szFullInputFileName);

				_timeb oEndTime;
				_ftime(&oEndTime);

				++s_nNumFilesCompleted;
				++pInputFileName;


				float fNumSecondsThisFile = (oEndTime.time + ((float)oEndTime.millitm) / 1000) - (s_oStartTime.time + ((float)oEndTime.millitm) / 1000);
				int nNumFilesInAverage;
				if (s_nNumFilesCompleted >= MOVING_AVERAGE_WIDTH)
				{
					nNumFilesInAverage = MOVING_AVERAGE_WIDTH;
				}
				else
				{
					nNumFilesInAverage = 0;
				}

				s_fAverageFileTime = (s_fAverageFileTime * (s_nNumFilesCompleted - 1) + fNumSecondsThisFile) / (s_nNumFilesCompleted > 0 ? s_nNumFilesCompleted : 1);
			}

			if (s_nOutputMode == OUTPUT_XVID)
			{
				TermiateEncoding();
			}


			gotoxy(5, 5);
			printf("                                                "); //Clear line
			gotoxy(5, 6);
			printf("                                                "); //Clear line
			gotoxy(5, 5);
			printf("Processing Complete");
        }
    }

    if (!bArgsValid)
    {
        printf("Usage: RecordingProcessor.exe [-frame|-bmp|-xvid|-time] input_directory [output_directory] [frame_num]\n");
    }

	//_getch();
	return 0;
}

JNIEXPORT void JNICALL Java_edu_jhuapl_nstd_tase_RecordingProcessor_GenerateAvi(JNIEnv *env, jobject obj, jstring lFormat, jstring lInputDir, jstring lOutputDir, jint lFrame)
{
	s_env = env;
	jboolean b;
	std::string format(env->GetStringUTFChars(lFormat, &b));
	std::string inputDir(env->GetStringUTFChars(lInputDir, &b));
	std::string outputDir(env->GetStringUTFChars(lOutputDir, &b));
	long frame = lFrame; 		

	if (format != "")
	{
		if (format == "bmp")
		{
			strcpy(s_szOutputFormat, "bmp");
			s_nOutputMode = OUTPUT_BITMAPS;			
		}
		else if (format == "xvid")
		{
			strcpy(s_szOutputFormat, "xvid");
			s_nOutputMode = OUTPUT_XVID;
		}
		else if (format == "time")
		{
			strcpy(s_szOutputFormat, "timestamp");
			s_nOutputMode = OUTPUT_TIMESTAMP;
		}
	}
	else
	{
		s_nOutputMode = OUTPUT_FRAMES;
	}

	if (s_nOutputMode == OUTPUT_BITMAPS)
	{
		//
		// Create bitmap header that is the same for all images
		//
		int nDummy;
		char *pBufferField = s_pBitmapBuffer;
		memcpy(pBufferField, "BM", 2);
		pBufferField += 2;
		memcpy(pBufferField, &BITMAP_FILE_SIZE, 4); //total length of bitmap
		pBufferField += 4;
		nDummy = 0;
		memcpy(pBufferField, &nDummy, 4); //reserved
		pBufferField += 4;
		memcpy(pBufferField, &PIXEL_DATA_OFFSET, 4); //pixel data offset
		pBufferField += 4;
		nDummy = 0x28;
		memcpy(pBufferField, &nDummy, 4); //header size
		pBufferField += 4;
		memcpy(pBufferField, &IMAGE_WIDTH, 4);
		pBufferField += 4;
		memcpy(pBufferField, &IMAGE_HEIGHT, 4);
		pBufferField += 4;
		nDummy = 1;
		memcpy(pBufferField, &nDummy, 2); //num color planes
		pBufferField += 2;
		nDummy = 8;
		memcpy(pBufferField, &nDummy, 2); //num bits per pixel
		pBufferField += 2;
		memset(pBufferField, 0, 24); //rest of fields are all zero
		pBufferField += 24;

		//
		// Create grayscale lookup table
		//
		for (int i = 0; i < (LOOKUP_TABLE_LENGTH / 4); ++i)
		{
			*pBufferField = i;
			++pBufferField;
			*pBufferField = i;
			++pBufferField;
			*pBufferField = i;
			++pBufferField;
			*pBufferField = 0;
			++pBufferField;
		}
	}
	
	std::copy(inputDir.begin(), inputDir.end(), s_szInputDirectory);
	std::copy(outputDir.begin(), outputDir.end(), s_szOutputDirectory);

	if (s_nOutputMode == OUTPUT_TIMESTAMP)
	{	
		s_nTargetFrameNum = (int)frame;
	}

    if (s_szInputDirectory[strlen(s_szInputDirectory) - 1] != '\\')
    {
		strcat(s_szInputDirectory, "\\");
    }

    if (s_szOutputDirectory[strlen(s_szOutputDirectory) - 1] != '\\')
    {
		strcat(s_szOutputDirectory, "\\");
    }

	strcpy(s_szChannel1OutputDirectory, s_szOutputDirectory);
	strcat(s_szChannel1OutputDirectory, "channel1\\");

	strcpy(s_szChannel2OutputDirectory, s_szOutputDirectory);
	strcat(s_szChannel2OutputDirectory, "channel2\\");

	s_nInputDirectoryLength = strlen(s_szInputDirectory);
	s_nOutputDirectoryLength = strlen(s_szOutputDirectory);
	s_nOutputDirectoryChannelNumIndex = s_nOutputDirectoryLength + 7;
	strcpy(s_szFullInputFileName, s_szInputDirectory);

	HANDLE hFindFiles;
	WIN32_FIND_DATA oFindFileData;
	BOOL bMoreFiles;
	char szDirectorySearch[MAX_PATH];
	strcpy(szDirectorySearch, s_szInputDirectory);
	strcat(szDirectorySearch, "*.rec");
	hFindFiles = ::FindFirstFile(szDirectorySearch, &oFindFileData);

	if (hFindFiles == INVALID_HANDLE_VALUE)
	{
		printf("Invalid input directory specified\n");
		exit(1);
	}

	//
	// Create an array containing all the .rec file names
	//
	s_nNumInputFiles = 0;
	int nInputFileNamesArrayLength = 1024;
	char **pInputFileNames = (char**)malloc(nInputFileNamesArrayLength * sizeof(char*));
	do
	{
		//
		// Resize array if necessary
		//
		if (s_nNumInputFiles == nInputFileNamesArrayLength)
		{
			nInputFileNamesArrayLength *= 2;
			realloc(pInputFileNames, nInputFileNamesArrayLength * sizeof(char*));
		}

		//
		// Add current file name to the array
		//
		pInputFileNames[s_nNumInputFiles] = (char*)malloc(strlen(oFindFileData.cFileName) + 1);
		strcpy(pInputFileNames[s_nNumInputFiles], oFindFileData.cFileName);
		++s_nNumInputFiles;

		bMoreFiles = FindNextFile(hFindFiles, &oFindFileData);
	}
	while(bMoreFiles);


	qsort(pInputFileNames, s_nNumInputFiles, sizeof(char*), compare_string);
			
	const char** pInputFileName = (const char**)pInputFileNames;
	for (int i = 0; i < s_nNumInputFiles; ++i)
	{
		_ftime(&s_oStartTime);

		s_fCurrFilePercentComplete = 0;

		strcpy(&s_szFullInputFileName[s_nInputDirectoryLength], *pInputFileName);
		ParseRecordingFile(s_szFullInputFileName);

		_timeb oEndTime;
		_ftime(&oEndTime);

		++s_nNumFilesCompleted;
		++pInputFileName;


		float fNumSecondsThisFile = (oEndTime.time + ((float)oEndTime.millitm) / 1000) - (s_oStartTime.time + ((float)oEndTime.millitm) / 1000);
		int nNumFilesInAverage;
		if (s_nNumFilesCompleted >= MOVING_AVERAGE_WIDTH)
		{
			nNumFilesInAverage = MOVING_AVERAGE_WIDTH;
		}
		else
		{
			nNumFilesInAverage = 0;
		}

		s_fAverageFileTime = (s_fAverageFileTime * (s_nNumFilesCompleted - 1) + fNumSecondsThisFile) / (s_nNumFilesCompleted > 0 ? s_nNumFilesCompleted : 1);
	}

	if (s_nOutputMode == OUTPUT_XVID)
	{
		TermiateEncoding();
	}

	std::cout << "Recording Processing Complete" << std::endl;
	// reset variables to force reinitiating xvid encoding
	memset(s_szFinalOutputFileName, 0, sizeof(s_szFinalOutputFileName));
	s_nInputDirectoryLength = 0;
	s_nOutputDirectoryLength = 0;
	s_nOutputDirectoryChannelNumIndex = 0;	
	s_fAverageFileTime = 0;
	s_nNumFilesCompleted = 0;
	s_fCurrFilePercentComplete = 0;
	s_nOutputBaseNameLength = 0;
	s_nChannel1FrameCount = 0;
	s_nChannel2FrameCount = 0;
	s_nNumInputFiles = 0;
	s_nTargetFrameNum = 0;
}

void ParseRecordingFile(const char* const szInputFileName)
{
	struct _stat oFileStat;
	_stat(szInputFileName, &oFileStat);
	int nInputFileSize = oFileStat.st_size;	

	FILE *pInputFile = fopen(szInputFileName, "rb");

	if (pInputFile)
	{
		float fFormatVersion;
		int nImageBufferSize;
		int nWarpBufferSize;

		fread(&fFormatVersion, sizeof(fFormatVersion), 1, pInputFile);
		fread(&nImageBufferSize, sizeof(nImageBufferSize), 1, pInputFile);
		fread(&nWarpBufferSize, sizeof(nWarpBufferSize), 1, pInputFile);

		bool bEndOfFile = false;

		do
		{
			//
			//read frame name
			//
			int nInputChar;
			char *pNameChar = s_szStartingOutputFileName;
			do
			{
				nInputChar = fgetc(pInputFile);

				if (nInputChar != EOF)
				{
					*pNameChar = (char)nInputChar;
					++pNameChar;
				}
				else
				{
					bEndOfFile = true;
				}
			}
			while((nInputChar != '\0') && !bEndOfFile);

			*pNameChar = '\0';

			

				
			if (!bEndOfFile)
			{
				fread(s_pImageBuffer, 1, nImageBufferSize, pInputFile);
				fread(s_pWarpBuffer, 1, nWarpBufferSize, pInputFile);

				if (s_szFinalOutputFileName[0] == '\0')
				{
					strcpy(s_szFinalOutputFileName, s_szOutputDirectory);
					strcpy(&s_szFinalOutputFileName[s_nOutputDirectoryLength], s_szStartingOutputFileName);
					s_nOutputBaseNameLength = strlen(s_szFinalOutputFileName);
					
					if (s_nOutputMode == OUTPUT_XVID)
					{
						char szXvidFileName[MAX_PATH];
						strcpy(szXvidFileName, s_szFinalOutputFileName);
						strcat(szXvidFileName, ".avi");
						InitEncoding(szXvidFileName, IMAGE_WIDTH, IMAGE_HEIGHT);
					}
				}

				char cChannelChar = s_szStartingOutputFileName[7];
				if (cChannelChar == '1')
				{
					//
					// Create the channel1 output directory if this is the first image from that channel
					//
					if (s_nChannel1FrameCount == 0)
					{
						mkdir(s_szChannel1OutputDirectory);
					}

					sprintf(s_szOutputFileEnding, "_%i.%s", s_nChannel1FrameCount, s_szOutputFormat);

					if (s_nOutputMode == OUTPUT_TIMESTAMP && s_nChannel1FrameCount == s_nTargetFrameNum)
					{
						fclose(pInputFile);
						printf("\n");
						printf("Timestamp: %s\n", &s_szStartingOutputFileName[9]);
						printf("\nPress a key to quit\n");						
						getch();
						exit(0);
					}

					++s_nChannel1FrameCount;
				}
				else
				{
					//
					// Create the channel2 output directory if this is the first image from that channel
					//
					if (s_nChannel2FrameCount == 0)
					{
						mkdir(s_szChannel2OutputDirectory);
					}

					sprintf(s_szOutputFileEnding, "_%i.%s", s_nChannel2FrameCount, s_szOutputFormat);

					++s_nChannel2FrameCount;
				}

				//
				// Insert channel num into output filename
				//
				s_szFinalOutputFileName[s_nOutputDirectoryChannelNumIndex] = cChannelChar;

				

				//
				// Insert frame num and file extension into output filename
				//
				strcpy(&s_szFinalOutputFileName[s_nOutputBaseNameLength], s_szOutputFileEnding);

				if (s_nOutputMode == OUTPUT_XVID)
				{
					if (cChannelChar == '1')
					{
						EncodeFrame((unsigned char*)s_pImageBuffer);
					}
				}
				else if (s_nOutputMode == OUTPUT_BITMAPS || s_nOutputMode == OUTPUT_FRAMES)
				{
					FILE *pOutputFile = fopen(s_szFinalOutputFileName, "wb");
					
					if (s_nOutputMode == OUTPUT_BITMAPS)
					{
						char *pBitmapPixel = s_pBitmapPixelData;
						for (int i = IMAGE_HEIGHT - 1; i >= 0; --i)
						{
							for (int j = 0; j < IMAGE_WIDTH; ++j)
							{ 
								*pBitmapPixel = s_pImageBuffer[i * IMAGE_WIDTH + j];
								++pBitmapPixel;
							}
						}

						//memcpy(s_pBitmapPixelData, s_pImageBuffer, PIXEL_DATA_SIZE);
						fwrite(s_pBitmapBuffer, 1, BITMAP_FILE_SIZE, pOutputFile);

					}
					else if (s_nOutputMode == OUTPUT_FRAMES)
					{
						//
						// Output .frame file
						//
						fwrite(&fFormatVersion, sizeof(fFormatVersion), 1, pOutputFile);
						fwrite(&nImageBufferSize, sizeof(nImageBufferSize), 1, pOutputFile);
						fwrite(&nWarpBufferSize, sizeof(nWarpBufferSize), 1, pOutputFile);
						fwrite(s_pImageBuffer, 1, nImageBufferSize, pOutputFile);
						fwrite(s_pWarpBuffer, 1, nWarpBufferSize, pOutputFile);
					}

					fclose(pOutputFile);
				}
			}

			int nFilePosition = ftell(pInputFile);
			s_fCurrFilePercentComplete = ((float)ftell(pInputFile)) / nInputFileSize;
			
			if (s_nOutputMode == OUTPUT_TIMESTAMP)
			{
				PrintCurrFrameNum();
			}
			else
			{
				UpdateTimeRemaining();
			}
			


			::Sleep(1);
		}
		while(!bEndOfFile);

		fclose(pInputFile);

	}
}

void UpdateTimeRemaining()
{
	gotoxy(5, 5);
	printf("                                                "); //Clear line
	gotoxy(5, 5);
	printf("Processing file %i of %i - %i%s", s_nNumFilesCompleted + 1, s_nNumInputFiles, (int)(s_fCurrFilePercentComplete * 100), "%");
	printf("                                                "); //Clear line
	gotoxy(5, 6);
	if (s_nNumFilesCompleted + s_fCurrFilePercentComplete != 0)
	{
		_timeb oCurrTime;
		_ftime(&oCurrTime);
		__time64_t lNumSecondsRemaining = s_fAverageFileTime * (s_nNumInputFiles - (s_nNumFilesCompleted + s_fCurrFilePercentComplete));
		int nHours = lNumSecondsRemaining / 3600;
		int nMinutes = (lNumSecondsRemaining / 60) % 60;
		int nSeconds = (lNumSecondsRemaining % 60);
		printf("Total Time Remaining: %02i:%02i:%02i\n", nHours, nMinutes, nSeconds);
	}
}

void PrintCurrFrameNum()
{
	gotoxy(5, 5);
	printf("                                                "); //Clear line
	gotoxy(5, 5);
	printf("Frame Num: %i\n", s_nChannel1FrameCount);
}

void gotoxy(const int column, const int line)
{
	COORD coord;
	coord.X = column;
	coord.Y = line;
	SetConsoleCursorPosition(GetStdHandle(STD_OUTPUT_HANDLE), coord);
}