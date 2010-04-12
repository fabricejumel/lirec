#include <stdafx.h>
#define MAXBLOCK 1024
#define MAXPORT 4
//Flow Control flags
#define FC_DTRDSR 0x01
#define FC_RTSCTS 0x02
#define FC_XONXOFF 0x04

//Ascii definitions
#define ASCII_BEL 0x07
#define ASCII_BS 0x08
#define ASCII_LF 0x0A
#define ASCII_CR 0x0D
#define ASCII_XON 0x11
#define ASCII_XOFF 0x13
#define WM_RECEIVEDATA WM_USER+1
#define MAX_PROTOCOL_SIZE 22

#define	CR  13
#define LF  10
#define SPACE 32

class CComm:public CObject
{	
	DECLARE_DYNCREATE(CComm)
public:
	CComm();
	void SetXonOff(BOOL chk); //XonOff ����
	void SetComport(int port, DWORD rate, BYTE bytesize, BYTE stop, BYTE parity); //comport����
	void SetDtrRts(BYTE chk); //Dtr Rts����
	BOOL CreateCommInfo(); //comm ��Ʈ�� �����
	BOOL DestroyComm(); //comm ��Ʈ�� �����Ѵ�.
	int  ReadCommBlock(LPSTR, int); //comport���� �����͸� �޴´�.
	BOOL WriteCommBlock(LPSTR, DWORD); //comport�� �����͸� �ִ´�.
	BOOL OpenComport(); //����Ʈ�� ���� ������ �õ��Ѵ�.
	BOOL SetupConnection(); //��Ʈ�� �����Ѵ�.
	BOOL CloseConnection(); //������ �����Ѵ�.
	void SetReadData(LPSTR data); //���� �����͸� ���ۿ� �����Ѵ�.
	void SetHwnd(HWND hwnd);
	
public:
	BYTE bPort;
	BOOL fXonXoff;
	BYTE bByteSize, bFlowCtrl, bParity, bStopBits;
	DWORD dwBaudRate;
	HANDLE hWatchThread;
	HWND hTermWnd;
	DWORD dwThreadID;
	OVERLAPPED osWrite, osRead;
	unsigned char Rev_Sel;

public:
	HANDLE hComm;
	BOOL fConnected;
	BYTE abIn[MAXBLOCK+1];
	HWND m_hWnd;

public:
	CString m_pBuffer;
	CString m_pInBuffer;
	BOOL m_bStartFlag;
	virtual ~CComm();
};
DWORD CommWatchProc(LPVOID lpData);




