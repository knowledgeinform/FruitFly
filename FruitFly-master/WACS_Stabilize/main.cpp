
#include <fstream>
#include <iostream>
#include <sstream>
#include <time.h>
#include <sys/types.h>
#include <conio.h>
#include <winsock2.h>
#include "ImageAlignment.h"
#include "IRPlumetracker.h"

using namespace std;
 

clock_t bef, aft;


void gotoxy(int xpos, int ypos)
{
  COORD scrn;    

  HANDLE hOuput = GetStdHandle(STD_OUTPUT_HANDLE);

  scrn.X = xpos; scrn.Y = ypos;

  SetConsoleCursorPosition(hOuput,scrn);
}



int main(int argc, char *argv[])
{

		IRPlumeTracker ir;

	    //string path = "C://WACS/WACS_Stabilize/Flight6/";
		//string base = "1242702056_93_";
		//int framestart = 20700;
		//int framestart = 18500;
		string path = "C://WACSTest/channel1/";
		string base = "1242702290_31_";
		//
		//int framestart = 1300;
		int framestart = 2700;
		//int framestart = 7950;
		//int framestart = 10750;
		//int framestart = 38200;
		//int framestart = 16800;
		int framestep = 20;
		int frame = framestart; 
		stringstream fullpath; 
		fullpath << path << base << framestart << ".bmp";

		IplImage* imgin = cvLoadImage(fullpath.str().c_str());

	    ir.setParameter("numFrames", 5);
		ir.setParameter("showTrackerImages", 1.0);
		ir.setParameter("showDetections", 1.0);
		ir.setParameter("detectonThreshold", 0.12);
		ir.setParameter("errorThreshold", 0.60);
	    ir.m_IAlign.setParameter("showAlignment",1.0);
	    ir.m_IAlign.setParameter("showImages",0.0);

		ir.addFrame(imgin);

		

	

		while(1)
		{
			bef = clock();
			frame = frame + framestep;
			fullpath.str("");
			fullpath << path << base << frame << ".bmp";
			imgin = cvLoadImage(fullpath.str().c_str());
			ir.addFrame(imgin);
			aft = clock();
			printf("Cycle took %f seconds\n", double(aft-bef)/CLOCKS_PER_SEC);

			//cvWaitKey(0);
		}

}



