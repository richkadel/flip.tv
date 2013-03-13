#include <iostream>
#include <jni.h>
#include "stdafx.h"
#include "SampleGrabber.h"

extern void KNOWBOUT_VIDEO_pushCCChars(int deviceNumber, BYTE c1, BYTE c2);

//-----------------------------------------------------------------------------
// Name: CCCap()
// Desc: Constructor
//-----------------------------------------------------------------------------

CCCap::CCCap(int deviceNumber)
: m_cRef(0), m_deviceNumber(deviceNumber)
{
}

//-----------------------------------------------------------------------------
// Name: ~CCCap()
// Desc: Destructor
//-----------------------------------------------------------------------------

CCCap::~CCCap(void)
{
	_ASSERTE(m_cRef == 0);
}

//-----------------------------------------------------------------------------
// Name: SetMediaType()
// Desc: Store the media type that the Sample Grabber connected with
//
// mt:   Reference to the media type
//-----------------------------------------------------------------------------

HRESULT CCCap::SetMediaType(AM_MEDIA_TYPE& mt)
{

	if (mt.majortype != MEDIATYPE_VBI) 
	{
		return VFW_E_INVALIDMEDIATYPE;
	}

	if (mt.formattype != FORMAT_None)
	{
		return VFW_E_INVALIDMEDIATYPE;
	}

	return S_OK;
}


//-----------------------------------------------------------------------------
// Name: QueryInterface()
// Desc: Our impementation of IUnknown::QueryInterface
//-----------------------------------------------------------------------------

STDMETHODIMP CCCap::QueryInterface(REFIID riid, void **ppvObject)
{
	if (NULL == ppvObject)
		return E_POINTER;
	if (riid == __uuidof(IUnknown))
		*ppvObject = static_cast<IUnknown*>(this);
	else if (riid == __uuidof(ISampleGrabberCB))
		*ppvObject = static_cast<ISampleGrabberCB*>(this);
	else 
		return E_NOTIMPL;
	AddRef();
	return S_OK;
}

// Note: ISampleGrabber supports two callback methods: One gets the IMediaSample
// and one just gets a pointer to the buffer.

//-----------------------------------------------------------------------------
// Name: SampleCB()
// Desc: Callback that gets the media sample. (NOTIMPL - We don't use this one.)
//-----------------------------------------------------------------------------

STDMETHODIMP CCCap::SampleCB(double SampleTime, IMediaSample *pSample)
{
	return E_NOTIMPL;
}

//-----------------------------------------------------------------------------
// Name: BufferCB()
// Desc: Callback that gets the buffer. 
//
// Note: In general it's a bad idea to do anything time-consuming inside the
// callback (like write a bmp file) because you can stall the graph. Also
// on Win9x you might be holding the Win16 Mutex which can cause deadlock. 
//
// Here, I know that (a) I'm not rendering the video, (b) I'm getting CC
// images one at a time, not a stream, (c) I'm running XP. However there's
// probably a better way to design this.
//-----------------------------------------------------------------------------

STDMETHODIMP CCCap::BufferCB(double SampleTime, BYTE *pBuffer, long BufferLen)
{
	if (BufferLen != 2) {
		OutputDebugString(TEXT("Unexpected buffer len!\n"));
	} else {
		KNOWBOUT_VIDEO_pushCCChars(m_deviceNumber, pBuffer[0], pBuffer[1]);
	}

	return S_OK;
}


