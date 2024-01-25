#include "stdafx.h"
#include <iostream>
#include "RawDataDialog.h"
#include "Resource.h"
#include "Config.h"
#include "Viewer3D.h"
#include "BeliefCollection.h"

char RawDataDialog::m_BeliefNames [m_BeliefNamesSize][256] = {
													("AgentPositionBelief\0"),
													("CircularOrbitBelief\0"),
													("RacetrackOrbitBelief\0"),
													("ParticleDetectionBelief\0"),
													("AnacondaDetectionBelief\0"),
													("GCSPositionBelief\0"),
													("PiccoloTelemetryBelief\0"),
													("AgentModeBelief\0"),
													("CloudDetectionBelief\0"),
													("CloudPredictionBelief\0"),
													("ExplosionBelief\0"),
													("IrExplosionAlgorithmEnabledBelief\0"),
													("MetBelief\0"),
													("LoiterApproachPathBelief\0")
													};

RawDataDialog::RawDataDialog(HINSTANCE newHinst, HWND newhParent, BeliefCollection* beliefCollection) : DialogBase (newHinst, newhParent)
{
	hWnd = NULL;
	m_BeliefCollection = beliefCollection;
	initDataRows();

	offsetXFromCenter = Config::getInstance()->getValueAsInt ("RawDataDialog.OffsetXFromCenter", 200);
}

RawDataDialog::~RawDataDialog()
{
	if (m_BeliefCollection != NULL)
		m_BeliefCollection->setDataRows (NULL);

	if (m_DataRows != NULL)
	{
		if (m_DataRows->m_DataRowLabels != NULL)
			delete [] m_DataRows->m_DataRowLabels;
		if (m_DataRows->m_DataRowValues != NULL)
			delete [] m_DataRows->m_DataRowValues;

		delete m_DataRows;
	}

}

void RawDataDialog::initDataRows()
{
	m_DataRows = new DataRows();
	m_DataRows->m_DataRowsCount = 22;
	m_DataRows->m_DataRowLabels = new int [m_DataRows->m_DataRowsCount];
	m_DataRows->m_DataRowValues = new int [m_DataRows->m_DataRowsCount];

	int index = 0;
	m_DataRows->m_DataRowLabels[index] = IDC_FIELDLABEL1;
	m_DataRows->m_DataRowValues[index++] = IDC_FIELDVALUE1;
	m_DataRows->m_DataRowLabels[index] = IDC_FIELDLABEL2;
	m_DataRows->m_DataRowValues[index++] = IDC_FIELDVALUE2;
	m_DataRows->m_DataRowLabels[index] = IDC_FIELDLABEL3;
	m_DataRows->m_DataRowValues[index++] = IDC_FIELDVALUE3;
	m_DataRows->m_DataRowLabels[index] = IDC_FIELDLABEL4;
	m_DataRows->m_DataRowValues[index++] = IDC_FIELDVALUE4;
	m_DataRows->m_DataRowLabels[index] = IDC_FIELDLABEL5;
	m_DataRows->m_DataRowValues[index++] = IDC_FIELDVALUE5;
	m_DataRows->m_DataRowLabels[index] = IDC_FIELDLABEL6;
	m_DataRows->m_DataRowValues[index++] = IDC_FIELDVALUE6;
	m_DataRows->m_DataRowLabels[index] = IDC_FIELDLABEL7;
	m_DataRows->m_DataRowValues[index++] = IDC_FIELDVALUE7;
	m_DataRows->m_DataRowLabels[index] = IDC_FIELDLABEL8;
	m_DataRows->m_DataRowValues[index++] = IDC_FIELDVALUE8;
	m_DataRows->m_DataRowLabels[index] = IDC_FIELDLABEL9;
	m_DataRows->m_DataRowValues[index++] = IDC_FIELDVALUE9;
	m_DataRows->m_DataRowLabels[index] = IDC_FIELDLABEL10;
	m_DataRows->m_DataRowValues[index++] = IDC_FIELDVALUE10;
	m_DataRows->m_DataRowLabels[index] = IDC_FIELDLABEL11;
	m_DataRows->m_DataRowValues[index++] = IDC_FIELDVALUE11;
	m_DataRows->m_DataRowLabels[index] = IDC_FIELDLABEL12;
	m_DataRows->m_DataRowValues[index++] = IDC_FIELDVALUE12;
	m_DataRows->m_DataRowLabels[index] = IDC_FIELDLABEL13;
	m_DataRows->m_DataRowValues[index++] = IDC_FIELDVALUE13;
	m_DataRows->m_DataRowLabels[index] = IDC_FIELDLABEL14;
	m_DataRows->m_DataRowValues[index++] = IDC_FIELDVALUE14;
	m_DataRows->m_DataRowLabels[index] = IDC_FIELDLABEL15;
	m_DataRows->m_DataRowValues[index++] = IDC_FIELDVALUE15;
	m_DataRows->m_DataRowLabels[index] = IDC_FIELDLABEL16;
	m_DataRows->m_DataRowValues[index++] = IDC_FIELDVALUE16;
	m_DataRows->m_DataRowLabels[index] = IDC_FIELDLABEL17;
	m_DataRows->m_DataRowValues[index++] = IDC_FIELDVALUE17;
	m_DataRows->m_DataRowLabels[index] = IDC_FIELDLABEL18;
	m_DataRows->m_DataRowValues[index++] = IDC_FIELDVALUE18;
	m_DataRows->m_DataRowLabels[index] = IDC_FIELDLABEL19;
	m_DataRows->m_DataRowValues[index++] = IDC_FIELDVALUE19;
	m_DataRows->m_DataRowLabels[index] = IDC_FIELDLABEL20;
	m_DataRows->m_DataRowValues[index++] = IDC_FIELDVALUE20;
	m_DataRows->m_DataRowLabels[index] = IDC_FIELDLABEL21;
	m_DataRows->m_DataRowValues[index++] = IDC_FIELDVALUE21;
	m_DataRows->m_DataRowLabels[index] = IDC_FIELDLABEL22;
	m_DataRows->m_DataRowValues[index++] = IDC_FIELDVALUE22;
}

bool RawDataDialog::show ()
{
	// If window is already visible, don't try again
	if (checkwindow(hWnd)) 
		return FALSE;

	RawDataDialog *thisPtr = this;
	hWnd = CreateDialogParam(hInst, MAKEINTRESOURCE(IDD_RAWTABLES), hParent, (DLGPROC) processMsg, (long)thisPtr); 

	RECT rect;
	GetWindowRect (hParent, &rect);
	
	positionWindow ((rect.right + rect.left)/2 - offsetXFromCenter, (rect.bottom + rect.top)/2);
	ShowWindow(hWnd, SW_SHOWNORMAL); 
	visible = true;
	Viewer3D::mainWinHasFocus = false;
	
	m_DataRows->m_hWnd = hWnd;
	if (m_BeliefCollection != NULL)
		m_BeliefCollection->setDataRows (m_DataRows);

	return 1;
}

bool RawDataDialog::containsWindow (HWND hwnd)
{
	return (hwnd == hWnd
		|| hwnd == GetDlgItem (hWnd, IDC_BELIEFCOMBO)
		);
}

LRESULT CALLBACK RawDataDialog::processMsg(HWND hDlg,UINT message,WPARAM wParam,LPARAM lParam) {
	
	// Get pointer to the object to send the message to
	LONG_PTR lptr = GetWindowLongPtr (hDlg, GWLP_USERDATA);
	RawDataDialog* dlg = NULL;
	dlg = reinterpret_cast<RawDataDialog*> ((LPARAM)lptr);

	// Until WM_INITDIALOG is first called, do default processing
	if (!dlg && message != WM_INITDIALOG)
		return DefWindowProc(hDlg, message, wParam, lParam);
	// First (and only) time WM_INITDIALOG is called, setup object for later access
	else if (message == WM_INITDIALOG)
	{
		try {
			dlg = reinterpret_cast<RawDataDialog*>(lParam);
			SetWindowLongPtr (hDlg, GWLP_USERDATA, (LONG)lParam);
			dlg->hWnd = hDlg;
		}
		catch (...)
		{
			MessageBox (NULL, "Error extracting RawDataDialog type", "Error", MB_OK|MB_ICONEXCLAMATION);
			dlg = NULL;
		}
		
	}
	
	if (dlg == NULL)
	{
		MessageBox (NULL, "Could not extract RawDataDialog object", "Error", MB_OK|MB_ICONEXCLAMATION);
		return false;
	}
	
	// Forward message for processing
	return dlg->processMessage(hDlg, message, wParam, lParam);
	
}

LRESULT RawDataDialog::processMessage(HWND hDlg,UINT message,WPARAM wParam,LPARAM lParam)
{
	
	switch (message) {
		int wmId, wmEvent;

		case WM_INITDIALOG:
		{

			HWND comboBox = GetDlgItem (hWnd, IDC_BELIEFCOMBO);
			for (int i = 0; i < m_BeliefNamesSize; i ++)
			{
				SendMessage(comboBox, CB_ADDSTRING, 0, reinterpret_cast<LPARAM>(&m_BeliefNames[i][0]));
			}
			SendMessage(comboBox, WM_SETTEXT, 0, (LPARAM)("AgentPositionBelief"));
			
			m_DataRows->m_hWnd = hDlg;
			if (m_BeliefCollection != NULL)
				m_BeliefCollection->setDataRows (m_DataRows);

			if (m_BeliefCollection != NULL)
				m_BeliefCollection->displayAgentPositionBeliefRawData();

			return TRUE;
		}
		case WM_PAINT: {
			PAINTSTRUCT ps;
			RECT updateRect;
			if (!GetUpdateRect(hDlg,&updateRect,false)) return 0;
			if (!BeginPaint(hDlg,&ps)) return 0;

			EndPaint(hDlg, &ps);
			break;

		}
		case WM_COMMAND: {
			wmId    = LOWORD(wParam); 
			wmEvent = HIWORD(wParam); 

			// Parse the menu selections:
			switch (wmId) {
				case IDOK:
				{
					if (m_BeliefCollection != NULL)
						m_BeliefCollection->setDataRows (m_DataRows);

					DestroyWindow (hDlg);
					Viewer3D::mainWinHasFocus = true;
					return TRUE;
				}
				case IDC_BELIEFCOMBO:
				{
					// If the combo box sent the message,
					switch(wmEvent) // Find out what message it was
					{
						case CBN_SELCHANGE:
						{
							int selIdx = SendMessage (GetDlgItem (hWnd, IDC_BELIEFCOMBO), CB_GETCURSEL, 0, 0);
							
							if (m_BeliefCollection != NULL)
							{
								if (strcmp (m_BeliefNames[selIdx], "AgentPositionBelief\0") == 0)
									m_BeliefCollection->displayAgentPositionBeliefRawData();
								if (strcmp (m_BeliefNames[selIdx], "CircularOrbitBelief\0") == 0)
									m_BeliefCollection->displayCircularOrbitBeliefRawData();
								if (strcmp (m_BeliefNames[selIdx], "RacetrackOrbitBelief\0") == 0)
									m_BeliefCollection->displayRacetrackOrbitBeliefRawData();
								if (strcmp (m_BeliefNames[selIdx], "ParticleDetectionBelief\0") == 0)
									m_BeliefCollection->displayParticleDetectionBeliefRawData();
								if (strcmp (m_BeliefNames[selIdx], "AnacondaDetectionBelief\0") == 0)
									m_BeliefCollection->displayAnacondaDetectionBeliefRawData();
								if (strcmp (m_BeliefNames[selIdx], "GCSPositionBelief\0") == 0)
									m_BeliefCollection->displayGCSPositionBeliefRawData();
								if (strcmp (m_BeliefNames[selIdx], "PiccoloTelemetryBelief\0") == 0)
									m_BeliefCollection->displayPiccoloTelemetryBeliefRawData();
								if (strcmp (m_BeliefNames[selIdx], "AgentModeBelief\0") == 0)
									m_BeliefCollection->displayAgentModeBeliefRawData();
								if (strcmp (m_BeliefNames[selIdx], "CloudDetectionBelief\0") == 0)
									m_BeliefCollection->displayCloudDetectionBeliefRawData();
								if (strcmp (m_BeliefNames[selIdx], "CloudPredictionBelief\0") == 0)
									m_BeliefCollection->displayCloudPredictionBeliefRawData();
								if (strcmp (m_BeliefNames[selIdx], "ExplosionBelief\0") == 0)
									m_BeliefCollection->displayExplosionBeliefRawData();
								if (strcmp (m_BeliefNames[selIdx], "IrExplosionAlgorithmEnabledBelief\0") == 0)
									m_BeliefCollection->displayIrExplosionAlgorithmEnabledBeliefRawData();
								if (strcmp (m_BeliefNames[selIdx], "MetBelief\0") == 0)
									m_BeliefCollection->displayMetBeliefRawData();
								if (strcmp (m_BeliefNames[selIdx], "LoiterApproachPathBelief\0") == 0)
									m_BeliefCollection->displayLoiterApproachPathBeliefRawData();
							}

							return TRUE;
							break;
						}
					
					}
					//return DefWindowProc (hDlg, message, wParam, lParam);
					return TRUE;
				}

			}
		}
	}
	return FALSE;

}