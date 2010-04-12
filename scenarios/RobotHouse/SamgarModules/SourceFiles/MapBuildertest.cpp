
#include <mrpt/core.h>

#include <iostream>

using namespace mrpt;
using namespace mrpt::utils;
using namespace mrpt::slam;
using namespace std;


//#include <mrpt/examples_config.h>

string   myGridMap("robothouse" );


// ------------------------------------------------------
//				TestPathPlanning
// ------------------------------------------------------
void TestPathPlanning()
{
	// Load the gridmap:
	COccupancyGridMap2D		gridmap;

	if (!mrpt::system::fileExists(myGridMap))
		THROW_EXCEPTION_CUSTOM_MSG1("Map file '%s' not found",myGridMap.c_str());

	printf("Loading gridmap...");
	CFileGZInputStream(myGridMap) >> gridmap;
	printf("Done! %f x %f m\n", gridmap.getXMax()-gridmap.getXMin(), gridmap.getYMax()-gridmap.getYMin());


	// Find path:
	CPathPlanningCircularRobot	pathPlanning;
	pathPlanning.robotRadius = 0.30f;

	std::deque<poses::TPoint2D>		thePath;
	bool	notFound;
	CTicTac	tictac;

//	CPoint2D  origin( 20, 1 );
	CPoint2D  origin( 20, -110 );
	CPoint2D  target( 90, 40 );

	cout << "Origin: " << origin << endl;
	cout << "Target: " << target << endl;

	cout << "Searching path..."; cout.flush();
	tictac.Tic();

	pathPlanning.computePath( gridmap, origin, target, thePath, notFound, 100.0f /* Max. distance */ );

	double t = tictac.Tac();
	cout << "Done in " << t*1000 << " ms" << endl;

	printf("Path found: %s\n", notFound ? "NO":"YES");
	printf("Path has %u steps\n", (unsigned)thePath.size());

	// Save result:
	CImage		img;
	gridmap.getAsImage(img,false, true);  // Force a RGB image

	// Draw the path:
	// ---------------------
	int R = round(pathPlanning.robotRadius / gridmap.getResolution() );

	for (std::deque<poses::TPoint2D>::const_iterator it=thePath.begin();it!=thePath.end();++it)
		img.drawCircle( gridmap.x2idx(it->x),gridmap.getSizeY()-1-gridmap.y2idx(it->y),R, TColor(0,0,255) );

	img.cross(gridmap.x2idx(origin.x()),gridmap.getSizeY()-1-gridmap.y2idx(origin.y()),0x202020,'+',10);
	img.cross(gridmap.x2idx(target.x()),gridmap.getSizeY()-1-gridmap.y2idx(target.y()),0x505050,'x',10);

	const std::string dest = "path_planning.png";
	cout << "Saving output to: " << dest << endl;
	img.saveToFile(dest);
	printf("Done\n");

#if MRPT_HAS_WXWIDGETS
	mrpt::gui::CDisplayWindow	win("Computed path");
	win.showImage(img.scaleHalf().scaleHalf());

	win.waitForKey();
#endif

}

int main(int argc, char **argv)
{
	try
	{
		TestPathPlanning();
		return 0;
	} catch (exception &e)
	{
		cout << "MRPT exception caught: " << e.what() << endl;
		return -1;
	}
	catch (...)
	{
		printf("Another exception!!");
		return -1;
	}
}

