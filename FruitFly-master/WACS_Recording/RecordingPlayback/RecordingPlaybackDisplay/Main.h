#ifndef MainH
#define MainH

#include "wx/wxprec.h"
#include "PThreadC.h"
#include "RecordingPlayback.h"

class ImageFrame : public wxFrame
{
	public:
		ImageFrame();

		void SetImage(const unsigned char* const pPixelData);

		void OnPaint(wxPaintEvent& WXUNUSED(event));

		void OnCloseWindow(wxCloseEvent& event);

	private:
		wxBitmap m_oBitmap;
		wxImage m_oImage;
		unsigned int m_nScreenHeight, m_nScreenWidth;
		char *m_pImageBuffer;

		DECLARE_EVENT_TABLE()
};

class App;

class DisplayThread : public PThreadC
{
	public:
		DisplayThread(App &app);
	protected:
		virtual void Run();
	private:
		App &m_oApp;
};

class App : public wxApp
{
	public:
		App();
		virtual bool OnInit();

		friend class DisplayThread;

	private:
		static ImageFrame *m_pImageFrame;
		DisplayThread m_oDisplayThread;
		RecordingPlayback *m_pRecordingPlayback;
};

BEGIN_EVENT_TABLE(ImageFrame, wxFrame)
    EVT_PAINT(ImageFrame::OnPaint)
	EVT_CLOSE(ImageFrame::OnCloseWindow)
END_EVENT_TABLE()

#endif