//#include "stdafx.h"
#include "comm.h"

#ifdef _DEBUG
#define new DEBUG_NEW
#undef THIS_FILE
static char THIS_FILE[]=__FILE__;
#endif
IMPLEMENT_DYNCREATE(CComm, CObject)

CComm::CComm()
{
	hComm=NULL;  //comport �ڵ� �ʱ�ȭ
	bFlowCtrl=FC_XONXOFF;  //flow_control ����
	fConnected=FALSE;  //thread stop
//	m_pToServoMsg = _T(""); //CString �ʱ�ȭ
//	Rev_Sel=0;
//	m_pServoEnableflag = FALSE;
}

CComm::~CComm()
{
	DestroyComm();
}

//����� �ϴ� ���ν���, �����ϴ� ��ƾ, �� ���ô� OpenComPort�Լ� ����� ���ν����� �����
DWORD CommWatchProc(LPVOID lpData)
{
	DWORD dwEvtMask;
	OVERLAPPED os;   //overlap ����ü ����
	CComm *npComm = (CComm *)lpData;  //CComm Ŭ���� ������ ����
	char InData[MAXBLOCK+1];  //receive ������ �����ϴ� �迭
	int nLength;  //receive ������ ���̸� �����ϴ� ����
	if(npComm==NULL) return -1; //npComm ��� �ڵ鿡 �ƹ��� ����Ʈ�� �� �پ� ������ ��������

	memset(&os, 0, sizeof(OVERLAPPED));  //overlap ����ü os �� �ʱ�ȭ
    os.hEvent=CreateEvent(NULL, //no security
		TRUE,  //explicit reset req
		FALSE, //initial event reset
		NULL); //no name

	//event ���� ����
	if(os.hEvent==NULL) {
		AfxMessageBox("Fail to Create Event!", MB_OK, 0);

        return FALSE;
	}

	//EV_RXCHAR �� �̺�Ʈ�� ����, �ٸ� �̺�Ʈ�� ������
	if(!SetCommMask(npComm->hComm, EV_RXCHAR)) return FALSE;

	//fConnected�� TRUE �϶��� EVENT �� ��ٸ�
	while(npComm->fConnected) {
		dwEvtMask=0; //������ EVENT�� �����ϴ� ���� 
		WaitCommEvent(npComm->hComm, &dwEvtMask, NULL);	//EVENT�� �߻��ϱ⸦ ��ٸ�
		if((dwEvtMask & EV_RXCHAR) == EV_RXCHAR) {	//EV_RXCHAR �̺�Ʈ�� �߻��ϸ�
			do {
				memset(InData, 0, 1024); //InData �迭 0���� �ʱ�ȭ
				//ReadCommBlock�Լ����� ���ۿ� �����Ͱ� �ִ����� Ȯ���Ѵ�.
				if(nLength = npComm->ReadCommBlock((LPSTR)InData, MAXBLOCK)) {
					npComm->SetReadData(InData);
					//�̰����� �����͸� �޴´�.
				}
			}
			while(nLength>0); //�����͸� ������ ���ۿ��� ���� �����ʹ� ������Ƿ�
			                  //���ۿ� �ִ� �����͸� �� ����.
		}
	}

	CloseHandle(os.hEvent);

	return TRUE;
}

//receive ����Ÿ�� data �� �����Ѵ�.
void CComm::SetReadData(LPSTR data)
{
	if(m_bStartFlag == TRUE)
	{
		m_pInBuffer += (LPSTR)data;

		// ������ ������ WM_RECEIVEDATA �޽����� �����־� ���� �����Ͱ� ���Դٴ� ���� �˷��ش�.
		SendMessage(m_hWnd, WM_RECEIVEDATA, (WPARAM)hComm, 0); 
	}
}

//�޽����� ������ hWnd ����
void CComm::SetHwnd(HWND hwnd)
{
	m_hWnd=hwnd;
}

//����Ʈ�� �����Ѵ�.
void CComm::SetComport(int port, DWORD rate, BYTE byteSize, BYTE stop, BYTE parity)
{
	bPort=port;
	dwBaudRate=rate;
	bByteSize=byteSize;
	bStopBits=stop;
	bParity=parity;
}

//XonOff, ���ϰ� ���� ����
void CComm::SetXonOff(BOOL chk)
{
	fXonXoff=chk;
}

void CComm::SetDtrRts(BYTE chk)
{
	bFlowCtrl=chk;
}

//����Ʈ ���� ����
//SetComport()->SetXonOff()->SetDtrRts()�Ѵ��� �����Ѵ�.
BOOL CComm::CreateCommInfo()
{
	osWrite.Offset=0;
	osWrite.OffsetHigh=0;
	osRead.Offset=0;
	osRead.OffsetHigh=0;

	//�̺�Ʈ�� �����Ѵ�. ���������̺�Ʈ, �ʱ���ȣ����
	osRead.hEvent=CreateEvent(NULL, TRUE, FALSE, NULL);
	if(osRead.hEvent=NULL) {

		return FALSE;
	}
	osWrite.hEvent=CreateEvent(NULL, TRUE, FALSE, NULL);
	if(osWrite.hEvent=NULL) {
		CloseHandle(osRead.hEvent);

		return FALSE;
	}

	return TRUE;
}

//����Ʈ�� ���� ������ �õ��Ѵ�.
BOOL CComm::OpenComport()
{
	char szPort[15];
	BOOL fRetVal;
	COMMTIMEOUTS CommTimeOuts;
	if(bPort>10)
		lstrcpy(szPort, "error");
	else 
		wsprintf(szPort, "COM%d", bPort);

	if((hComm=CreateFile(szPort, GENERIC_READ | GENERIC_WRITE,
		0,
		NULL,
		OPEN_EXISTING,
		FILE_ATTRIBUTE_NORMAL | FILE_FLAG_OVERLAPPED,
		NULL)) == (HANDLE)-1)
		return FALSE;
	else {
		//����Ʈ���� �����͸� ��ȯ�ϴ� ����� char������ �⺻���� ����
		SetCommMask(hComm, EV_RXCHAR);
		SetupComm(hComm, 4096, 4096);
		//����̽��� �����Ⱑ ������ �𸣴ϱ� ������ û�Ҹ� �Ѵ�.
		PurgeComm(hComm, PURGE_TXABORT | PURGE_RXABORT | PURGE_TXCLEAR | PURGE_RXCLEAR);
		CommTimeOuts.ReadIntervalTimeout=0xFFFFFFFF;
		CommTimeOuts.ReadTotalTimeoutMultiplier=0;
		CommTimeOuts.ReadTotalTimeoutConstant=1000;
		CommTimeOuts.WriteTotalTimeoutMultiplier=0;
		CommTimeOuts.WriteTotalTimeoutConstant=1000;
		SetCommTimeouts(hComm, &CommTimeOuts);
	}

	fRetVal = SetupConnection();
	if(fRetVal) {	//������ �Ǿ��ٸ� fRetVal �� TRUE �̹Ƿ�
		fConnected=TRUE;  //����Ǿ��ٰ� ������
		AfxBeginThread((AFX_THREADPROC)CommWatchProc,(LPVOID)this, THREAD_PRIORITY_NORMAL, 0, 0, NULL);
	}
	else {
		fConnected=FALSE;
		AfxMessageBox("��� ��Ʈ�� ������ �߻��߽��ϴ�.", MB_OK);
		CloseHandle(hComm);
	}

	return fRetVal;
}

//���Ϸ� ������ ����Ʈ�� ���� ��Ʈ�� �����Ų��.
//SetupConnection ������ CreateComport�� ���־�� �Ѵ�.
BOOL CComm::SetupConnection()
{
	BOOL fRetVal;
	//BYTE bSet;
	DCB dcb;
	dcb.DCBlength=sizeof(DCB);
	GetCommState(hComm, &dcb);  //dcb�� �⺻���� �޴´�.

	dcb.BaudRate=dwBaudRate;
	dcb.ByteSize=bByteSize;
	dcb.Parity=bParity;
	dcb.StopBits=bStopBits;
	
	fRetVal=SetCommState(hComm, &dcb);

	return fRetVal;
}

//����Ʈ�κ��� �����͸� �д´�.
int CComm::ReadCommBlock(LPSTR lpszBlock, int nMaxLength)
{
	BOOL fReadStat;
	COMSTAT ComStat;
	DWORD dwErrorFlags;
	DWORD dwLength;

	//only try to read number of bytes in queue
	ClearCommError(hComm, &dwErrorFlags, &ComStat);
	dwLength=min( (DWORD)nMaxLength, ComStat.cbInQue);
	if (dwLength>0) {
		fReadStat=ReadFile(hComm, lpszBlock, dwLength, &dwLength, &osRead);
		if (!fReadStat) {
			//Error Message
		}
	}
	return dwLength;
}

//����Ʈ�� ������ �����Ѵ�.
BOOL CComm::DestroyComm()
{
	if(fConnected) CloseConnection();
	CloseHandle(osRead.hEvent);
	CloseHandle(osWrite.hEvent);

	return TRUE;
}

//������ �ݴ´�.
BOOL CComm::CloseConnection()
{
	//set connected flag to FALSE;
	fConnected=FALSE;
	//disable event notification and wait for thread to halt
	SetCommMask(hComm, 0);
	EscapeCommFunction(hComm, CLRDTR);
	PurgeComm(hComm, PURGE_TXABORT | PURGE_RXABORT | PURGE_TXCLEAR | PURGE_RXCLEAR);
	CloseHandle(hComm);

	return TRUE;
}

BOOL CComm::WriteCommBlock(LPSTR lpByte, DWORD dwBytesToWrite)
{
	BOOL fWriteStat;
	DWORD dwBytesWritten;
	fWriteStat=WriteFile(hComm, lpByte, dwBytesToWrite, &dwBytesWritten, &osWrite);
	if(!fWriteStat) {
		//Error Message
	}

	return TRUE;
}

