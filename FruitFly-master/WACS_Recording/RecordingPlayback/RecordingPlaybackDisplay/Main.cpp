#include "Main.h"
#include <stdlib.h>
#include <exception>

using namespace std;

ImageFrame* App::m_pImageFrame;
static const int IMAGE_WIDTH = 320;
static const int IMAGE_HEIGHT = 240;
static const int DISPLAY_THREAD_SLEEP_TIME = 10;


ImageFrame::ImageFrame()
	: wxFrame(NULL, wxID_ANY, _T("WACS Playback Display"),
			  wxDefaultPosition, wxDefaultSize,
			  0) // Set no window border
{
	SetClientSize(IMAGE_WIDTH, IMAGE_HEIGHT);
}

void ImageFrame::SetImage(const unsigned char* const pPixelData)
{
	unsigned char* pPixel = m_oImage.GetData();
	if (!pPixel)
	{
		throw exception("Failed to gain raw access to image data");
	}

	//
	// Copy pixel data into image memory
	//
	for ( int y = 0; y < IMAGE_HEIGHT; ++y )
	{
		for ( int x = 0; x < IMAGE_WIDTH; ++x )
		{
			char grayscaleValue = pPixelData[y * IMAGE_HEIGHT + x];

			// Set R,G,B bytes all as the grayscale value
			memset(pPixel, grayscaleValue, 3);
			pPixel += 3;
		}
	}

	m_oBitmap = wxBitmap(m_oImage);

	Refresh();
}

void ImageFrame::OnPaint(wxPaintEvent& WXUNUSED(event))
{
	if(m_oBitmap.Ok())
	{
		wxPaintDC dc(this);
		dc.DrawBitmap(m_oBitmap, 0, 0, true);
	}
}

void ImageFrame::OnCloseWindow(wxCloseEvent& event)
{
	exit(0);
}

DisplayThread::DisplayThread(App &app)
:m_oApp(app)
{
}

void DisplayThread::Run()
{
	const unsigned char *pFrame;

	while (true)
	{
		pFrame = (unsigned char*)m_oApp.m_pRecordingPlayback->GetLatestFrame();

		if (pFrame)
		{
			m_oApp.m_pImageFrame->SetImage(pFrame);
		}

		::Sleep(DISPLAY_THREAD_SLEEP_TIME);
	}
}

App::App()
:m_oDisplayThread(*this)
{
}

bool App::OnInit()
{
	m_pRecordingPlayback = new RecordingPlayback("c:\\recording\\channel1\\");
	m_pRecordingPlayback->Play();
	m_oDisplayThread.Start();

	return true;
}

IMPLEMENT_APP(App)