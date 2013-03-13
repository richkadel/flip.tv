
#include <iostream>
#include <string>
#include <sstream>
#include "stdafx.h"
#include "SampleGrabber.h"
#include <IL21Dec.h>

struct _capstuff
{
	int deviceNumber;
    CComPtr<ICaptureGraphBuilder2> pBuilder;
    IGraphBuilder* pFg;
    IMoniker* pmVideo;
    IBaseFilter* pVCap;
    IMoniker* pmVBI;
    IBaseFilter* pVBI;
	IAMTVTuner* pTuner;
	IMediaControl* pControl;
	IAMLine21Decoder* pLine21;
};

using std::string;
using std::cerr;

extern string KNOWBOUT_VIDEO_OpenDevice(int deviceNumber, void** devInfo);
extern string KNOWBOUT_VIDEO_CloseDevice(void* devInfo);
extern string KNOWBOUT_VIDEO_SetChannel(void* devInfo, int channel);
extern int KNOWBOUT_VIDEO_GetChannel(void* devInfo);
extern wchar_t* KNOWBOUT_VIDEO_GetDeviceName(void* devInfo);

static void ErrMsg(LPTSTR szFormat,...);

void AddDevicesToMenu();

string InitCapFilters(_capstuff* gcap);
string BuildPreviewGraph(_capstuff* gcap);
void printGraph(_capstuff* gcap);

string FindMultiInstanceVBI(_capstuff* gcap) {
	// Create the System Device Enumerator.
	HRESULT hr;
	ICreateDevEnum *pSysDevEnum = NULL;
	hr = CoCreateInstance(CLSID_SystemDeviceEnum, NULL, CLSCTX_INPROC_SERVER,
		IID_ICreateDevEnum, (void **)&pSysDevEnum);

	// Obtain a class enumerator for the multiple instance VBI CODEC category.
	IEnumMoniker *pEnumCat = NULL;
	hr = pSysDevEnum->CreateClassEnumerator(AM_KSCATEGORY_VBICODEC_MI, &pEnumCat, 0);

	if (hr == S_OK) 
	{
		// Enumerate the monikers.
		ULONG cFetched;
		hr = pEnumCat->Next(1, &gcap->pmVBI, &cFetched);
		pEnumCat->Release();
		if (hr == S_OK) {
			return "";
		}
	}
	return "Couldn't get Multi-instance VBI filter";
}

void IMonRelease(IMoniker *&pm)
{
    if(pm)
    {
        pm->Release();
        pm = 0;
    }
}

//void setCCService(AM_LINE21_CCSERVICE service) { // an enum
//	/*
//    AM_L21_CCSERVICE_None,
//    AM_L21_CCSERVICE_Caption1,
//    AM_L21_CCSERVICE_Caption2,
//    AM_L21_CCSERVICE_Text1,
//    AM_L21_CCSERVICE_Text2,
//    AM_L21_CCSERVICE_XDS,
//
//	*/
//	gcap->pLine21->SetCurrentService(service);
//}

int main()
{
	void* devInfo;
	KNOWBOUT_VIDEO_OpenDevice(0, &devInfo);
}

extern wchar_t* KNOWBOUT_VIDEO_GetDeviceName(void* devInfo) {
	if (devInfo == NULL) {
		return L"No Device Info!";
	}
	_capstuff* gcap = (_capstuff*)devInfo;
	
    IPropertyBag *pBag=0;
    HRESULT hr = gcap->pmVideo->BindToStorage(0, 0, IID_IPropertyBag, (void **)&pBag);
    VARIANT var;
    var.vt = VT_BSTR;
    hr = pBag->Read(L"FriendlyName", &var, NULL);
    return var.bstrVal;
}

extern string KNOWBOUT_VIDEO_SetChannel(void* devInfo, int channel) {
	if (devInfo == NULL) {
		return "No Device Info!";
	}
	_capstuff* gcap = (_capstuff*)devInfo;
	HRESULT hr = gcap->pTuner->put_Channel(channel, AMTUNER_SUBCHAN_DEFAULT, AMTUNER_SUBCHAN_DEFAULT);
	if (hr != S_OK) {
		std::ostringstream ostr;
		ostr << "Could not tune to channel " << channel;
		return ostr.str();
	}
	return "";
}

extern int KNOWBOUT_VIDEO_GetChannel(void* devInfo) {
	
	_capstuff* gcap = (_capstuff*)devInfo;
	
	long lChannel;
	long lVideoSubChannel;
	long lAudioSubChannel;
	
	HRESULT hr = gcap->pTuner->get_Channel(&lChannel, &lVideoSubChannel, &lAudioSubChannel);
	if (hr != S_OK) {
		return -1;
	}
	
	/*
LONG lAnalogVideoStandard;
ErrMsg(L"get_TVFormat ");
hr = gcap->pTuner->get_TVFormat(&lAnalogVideoStandard);
if (hr != S_OK) {
	ErrMsg(L"Not OK");
} else {
	ErrMsg(lAnalogVideoStandard);
}
ErrMsg(L"\n");
	
LONG lNumInputConnections;
ErrMsg(L"get_NumInputConnections ");
hr = gcap->pTuner->get_NumInputConnections(&lNumInputConnections);
if (hr != S_OK) {
	ErrMsg(L"Not OK");
} else {
	ErrMsg(lNumInputConnections);
}
ErrMsg(L"\n");
	
for (int i = 0; i < lNumInputConnections; i++) {
TunerInputType inputType;
ErrMsg(L"get_InputType(%d)", i);
hr = gcap->pTuner->get_InputType(i, &inputType);
if (hr != S_OK) {
	ErrMsg(L"Not OK");
} else {
	if (inputType == TunerInputCable) {
		ErrMsg(L"Cable");
	} else if (inputType == TunerInputAntenna) {
		ErrMsg(L"Antenna");
	} else {
		ErrMsg(L"BAD ENUM!");
	}
}
ErrMsg(L"\n");
}
	
LONG lIndex;
ErrMsg(L"get_ConnectInput ");
hr = gcap->pTuner->get_ConnectInput(&lIndex);
if (hr != S_OK) {
	ErrMsg(L"Not OK");
} else {
	ErrMsg(lIndex);
}
ErrMsg(L"\n");
	
LONG lFreq;
ErrMsg(L"get_VideoFrequency ");
hr = gcap->pTuner->get_VideoFrequency(&lFreq);
if (hr != S_OK) {
	ErrMsg(L"Not OK");
} else {
	ErrMsg(lFreq);
}
ErrMsg(L"\n");
	
AMTunerModeType lMode;
ErrMsg(L"get_Mode ");
hr = gcap->pTuner->get_Mode(&lMode);
if (hr != S_OK) {
	ErrMsg(L"Not OK");
} else {
	ErrMsg(lMode);
}
ErrMsg(L"\n");
	
LONG lSignalPresent;
ErrMsg(L"SignalPresent ");
hr = gcap->pTuner->SignalPresent(&lSignalPresent);
if (hr != S_OK) {
	ErrMsg(L"Not OK");
} else {
	ErrMsg(lSignalPresent);
}
ErrMsg(L"\n");

printGraph(gcap);
*/

	return (int)lChannel;
}

extern string KNOWBOUT_VIDEO_CloseDevice(void* devInfo) {
	_capstuff* gcap = (_capstuff*)devInfo;
	
	HRESULT hr = gcap->pControl->Stop();
	
	//delete gcap;

  //  CoUninitialize();
  //TODO: Well... We might be better off tearing down the graph and deleting objects on the heap
  // because "close" would then allow other processes to use this device.  But I don't have time
  // to write it that way yet, so I'm just going to suspend it and allow it to be reused by this process
    
    return "";
}

extern string KNOWBOUT_VIDEO_OpenDevice(int deviceNumber, void** devInfo)
{
	if (*devInfo != NULL) {
		_capstuff* gcap = (_capstuff*)(*devInfo);
		HRESULT hr = gcap->pControl->Run();
		return "";
	}
	
	_capstuff* gcap = new _capstuff();
	*devInfo = gcap;
	
	gcap->deviceNumber = deviceNumber;
	
    CoInitializeEx( NULL, COINIT_APARTMENTTHREADED );

	IEnumMoniker *pEm=0;
    ICreateDevEnum *pCreateDevEnum=0;
    HRESULT hr = CoCreateInstance(CLSID_SystemDeviceEnum, NULL, CLSCTX_INPROC_SERVER,
                          IID_ICreateDevEnum, (void**)&pCreateDevEnum);
    hr = pCreateDevEnum->CreateClassEnumerator(CLSID_VideoInputDeviceCategory, &pEm, 0);
    if(hr != S_OK)
    {
		return "No video capture hardware!!\n";
    }

    pEm->Reset();
    ULONG cFetched;
    IMoniker *pM;
    
    gcap->pmVideo = NULL;
    
    int i = 0;
    while(hr = pEm->Next(1, &pM, &cFetched), hr==S_OK)
    {
    	if (i == deviceNumber) {
	        gcap->pmVideo = pM;
	        break;
    	}
        i++;
    }
    pEm->Release();
    
    if (gcap->pmVideo == NULL) {
		std::ostringstream ostr;
		ostr << "Device number " << deviceNumber << " does not exist!";
		return ostr.str();
    }
    
	string rtn = FindMultiInstanceVBI(gcap);
    if (rtn != "") {
    	return rtn;
    }
    
    rtn = InitCapFilters(gcap);
    if (rtn != "") {
    	return rtn;
    }

    rtn = BuildPreviewGraph(gcap);
    if (rtn != "") {
    	return rtn;
    }

	hr = gcap->pBuilder->FindInterface(&PIN_CATEGORY_CAPTURE,
                                 &MEDIATYPE_Video, gcap->pVCap, IID_IAMTVTuner, (void**)&(gcap->pTuner));
	//hr = gcap->pBuilder->FindInterface(&PIN_CATEGORY_CAPTURE,
 //                                &MEDIATYPE_Video, gcap->pVCap, IID_IAMLine21Decoder, (void**)&(gcap->pLine21));
	//the above didn't work (trying to get the line 21 decoder)

	//setCCService(AM_L21_CCSERVICE_XDS);

	CComQIPtr<IMediaControl> pControl(gcap->pFg);
	gcap->pControl = pControl;

	hr = gcap->pControl->Run();
	/*
	CComQIPtr<IMediaEvent> pEvent(gcap->pFg);
	long evCode = 0;
	hr = pEvent->WaitForCompletion(INFINITE, &evCode);
    */
    return "";
}

static string InitCapFilters(_capstuff* gcap)
{
    HRESULT hr=S_OK;

    hr = CoCreateInstance(CLSID_FilterGraph, NULL, CLSCTX_INPROC,
                                  IID_IGraphBuilder, (LPVOID *)&(gcap->pFg));
    if(hr != S_OK)
	{
        return "Cannot instantiate filtergraph";
    }

    hr = gcap->pBuilder.CoCreateInstance( CLSID_CaptureGraphBuilder2 ); 
    hr = gcap->pBuilder->SetFiltergraph(gcap->pFg);
    if(hr != S_OK)
    {
        return "Cannot give graph to builder";
    }

	/* add video capture filter */
    gcap->pVCap = NULL;

    if(gcap->pmVideo != 0)
    {
        hr = gcap->pmVideo->BindToObject(0, 0, IID_IBaseFilter, (void**)&(gcap->pVCap));
    }

    if(gcap->pVCap == NULL)
    {
        return "Cannot create video capture filter";
    }
	hr = gcap->pFg->AddFilter(gcap->pVCap, L"Video Capture Device");

    if(hr != S_OK)
    {
        return "Cannot add vidcap to filtergraph";
    }
    
	/* add multi-instance VBI */
    gcap->pVBI = NULL;

    if(gcap->pmVBI != 0)
    {
        hr = gcap->pmVBI->BindToObject(0, 0, IID_IBaseFilter, (void**)&gcap->pVBI);
    }

    if(gcap->pVBI == NULL)
    {
        ErrMsg(TEXT("Error %x: Cannot create multi-instance VBI"), hr);
		return FALSE;
    }
	hr = gcap->pFg->AddFilter(gcap->pVBI, L"Multi-instance VBI");

    if(hr != S_OK)
    {
        ErrMsg(TEXT("Error %x: Cannot add multi-instance VBI to filtergraph"), hr);
		return FALSE;
    }
    
    return "";
}

static HRESULT AddFilter(
    IGraphBuilder *pGraph,
    const GUID& clsid,
    LPCWSTR wszName,
    IBaseFilter **ppF)
{
    if (!pGraph || ! ppF) return E_POINTER;
    *ppF = 0;
    IBaseFilter *pF = 0;
    HRESULT hr = CoCreateInstance(clsid, 0, CLSCTX_INPROC_SERVER,
        IID_IBaseFilter, reinterpret_cast<void**>(&pF));
    if (SUCCEEDED(hr))
    {
        hr = pGraph->AddFilter(pF, wszName);
        if (SUCCEEDED(hr))
            *ppF = pF;
        else
            pF->Release();
    }
    return hr;
}

static HRESULT RenderCCPin(_capstuff* gcap)
{
	if (!gcap->pVCap)
	{
		return E_FAIL;
	}

	HRESULT hr;

	IBaseFilter* pF = gcap->pVBI;
	IPin* pP;
	hr = pF->FindPin(L"CC", &pP);

    CComPtr<IPin> pCCPin;

	pCCPin = pP;

    if (FAILED(hr))
    {

        return hr;
    }
	
    CCCap *pCCCapCB_Obj = new CCCap(gcap->deviceNumber);
    if (pCCCapCB_Obj == 0)
    {
		return E_OUTOFMEMORY;
    }
    
    CComQIPtr<ISampleGrabberCB> pCCCapCB(pCCCapCB_Obj);

    _ASSERTE(pCCCapCB);

	CComPtr<IBaseFilter> pSG_Filter;
	hr = AddFilter(gcap->pFg, CLSID_SampleGrabber, L"Sample Grabber", &pSG_Filter);
	if (FAILED(hr))
	{
		return hr;
	}
	CComQIPtr<ISampleGrabber> pSG(pSG_Filter);
	if (!pSG)
	{
		gcap->pFg->RemoveFilter(pSG_Filter);
		return E_NOINTERFACE;
	}
	AM_MEDIA_TYPE mt;
	ZeroMemory(&mt, sizeof(AM_MEDIA_TYPE));
	mt.majortype = MEDIATYPE_AUXLine21Data;
	mt.subtype = MEDIASUBTYPE_Line21_BytePair;
	pSG->SetBufferSamples(TRUE);
	pSG->SetCallback(pCCCapCB, 1);
	CComPtr<IBaseFilter> pNull;
	hr = AddFilter(gcap->pFg, CLSID_NullRenderer, L"Null Renderer", &pNull);
	if (FAILED(hr))
	{
		gcap->pFg->RemoveFilter(pSG_Filter);
		return hr;
	}

	pSG->SetMediaType(&mt);

	hr = gcap->pBuilder->RenderStream(NULL, NULL, pCCPin, pSG_Filter, pNull);
	if (FAILED(hr))
	{

		gcap->pFg->RemoveFilter(pSG_Filter);
		gcap->pFg->RemoveFilter(pNull);
        return hr;
	}

	return hr;
}

static void printFilter(IBaseFilter* pF) {
    FILTER_INFO filterinfo;
	HRESULT hr = pF->QueryFilterInfo(&filterinfo);
	OutputDebugString(filterinfo.achName);
	ErrMsg(L"\n");
}

static void printPin(IPin* pP) {
	IPin *pTo=0;
    PIN_INFO pininfo;
	AM_MEDIA_TYPE cmt;
	HRESULT hr = pP->ConnectionMediaType(&cmt);
    if(hr != S_OK)
    {
	}
    hr = pP->QueryPinInfo(&pininfo);
    if(hr == S_OK)
    {
		ErrMsg(L"    ");
		OutputDebugString(pininfo.achName);
		if (pininfo.dir == PINDIR_INPUT) {
			ErrMsg(L" for INPUT");
		} else {
			ErrMsg(L" for OUTPUT");
		}
        pP->ConnectedTo(&pTo);
        if(pTo)
        {
			ErrMsg(L" connected to ");
			hr = pTo->QueryPinInfo(&pininfo);
			if(hr == S_OK)
			{
				OutputDebugString(pininfo.achName);
				if (pininfo.dir == PINDIR_INPUT) {
					ErrMsg(L" for INPUT");
				} else {
					ErrMsg(L" for OUTPUT");
				}
				ErrMsg(L" of ");
				printFilter(pininfo.pFilter);
			}

		} else {
			ErrMsg(L" disconnected");
			ErrMsg(L"\n");
		}

	}
}

static void printPins(IBaseFilter* pF) {
	IPin *pP=0;
    IEnumPins *pins = NULL;
	ULONG u;

    HRESULT hr = pF->EnumPins(&pins);
    pins->Reset();

    while(hr == S_OK)
    {
        hr = pins->Next(1, &pP, &u);
		if(hr == S_OK && pP) {
			printPin(pP);
		}
	}
}

static void printFilters(_capstuff* gcap, BOOL showConnections) {
	IEnumFilters *filters = NULL;
	IBaseFilter* pF;
	ULONG u;

	HRESULT hr = gcap->pFg->EnumFilters(&filters);
    filters->Reset();

    while(hr == S_OK)
    {
        hr = filters->Next(1, &pF, &u);
        if(hr == S_OK && pF)
        {
			printFilter(pF);
			if (showConnections) {
				printPins(pF);
			}
		}
	}
}

static void printGraph(_capstuff* gcap)
{
	ErrMsg(L"\n==========================================================================\n\n");
	printFilters(gcap, true);
	ErrMsg(L"\n==========================================================================\n\n");
}

static string BuildPreviewGraph(_capstuff* gcap)
{

    if(gcap->pVCap == NULL)
        return FALSE;

	HRESULT hr = gcap->pBuilder->RenderStream(&PIN_CATEGORY_VBI, NULL,
                                         gcap->pVCap, gcap->pVBI, NULL);

    if(hr != S_OK)
    {
        //ErrMsg(TEXT("Cannot render closed captioning"));
		//May still be OK even if this failed, AND I have to call it
    }
	hr = RenderCCPin(gcap);
	if (hr != S_OK)
	{
		return "Cannot render CC Pin";
	}
    IVideoWindow *pVW;
    hr = gcap->pFg->QueryInterface(IID_IVideoWindow, (void **)&pVW);
    if(hr == S_OK)
    {
        pVW->put_AutoShow(OAFALSE);
        pVW->put_Visible(OAFALSE);
    }

    return "";
}

static void ErrMsg(LPTSTR szFormat,...)
{
    static TCHAR szBuffer[2048]={0};
    const size_t NUMCHARS = sizeof(szBuffer) / sizeof(szBuffer[0]);
    const int LASTCHAR = NUMCHARS - 1;
    va_list pArgs;
    va_start(pArgs, szFormat);

    HRESULT hr = StringCchVPrintf(szBuffer, NUMCHARS - 1, szFormat, pArgs);
    va_end(pArgs);
    szBuffer[LASTCHAR] = TEXT('\0');
    
    OutputDebugString(szBuffer);
}