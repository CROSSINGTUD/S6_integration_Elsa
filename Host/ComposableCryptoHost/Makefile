ComposableCryptoHost.exe: main.obj
	LINK main.obj "..\packages\Microsoft.Web.WebView2.1.0.864.35\build\native\x64\WebView2LoaderStatic.lib" version.lib Shell32.lib /subsystem:console /OUT:ComposableCryptoHost.exe

main.obj: main.cpp webview.h
	CL  /c /std:c++17 /EHsc /I "..\packages\Microsoft.Web.WebView2.1.0.864.35\build\native\include" main.cpp
