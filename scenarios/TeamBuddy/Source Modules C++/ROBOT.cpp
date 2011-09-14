#define DEBUGsystem 1

//#include "Vlaser.h"
#include "MapUpdater.h"
#include "PlaySounds.h"
#include "VirtualRobot.h"
#include <conio.h>				// VERY NAUGHTY HERE!!!!! NOT A CROSS PLATFORM HEADER


using namespace mrpt;
using namespace mrpt::system;
using namespace mrpt::math;
using namespace mrpt::utils;
using namespace mrpt::synch;
using namespace mrpt::random;


#if DEBUGsystem == 1

   // usefull little port that just writes whatever it recives to the screen


BufferedPort<Bottle> TargetForMap;
BufferedPort<Bottle> PlayMusic;
BufferedPort<Bottle> Robot;


#endif

CThreadSafeVariable<bool>   WeAreRunning; // a global flag for all threads, if this dies they all die
										  // we care of course about multiple threads acessing (and waiting) for the varible 
										  // but it makes it alot easer for people to add a new thread
										  // ***UPDATE, NOT USEING BUT LEAVING INCASE SOMEONE WANTS TO USE IT**
//void ThreadForLaser(void);
void ThreadForMap(void);
void ThreadForSound(void);
void ThreadForRobot(void);


//Vlaser *MyLaser;
MapUpdater *MyMapBuild;
Vsounds *MySound;
VRobot *MyRobot;
CPose2D RechargePose(0,0,0);
CPose2D HomePose(-0.75,3.0,4.5);//0
CPose2D DoorPose(-2.0,1.8,3.1);//6
CPose2D Table1Pose(1.0,2.3,0.1);//1
CPose2D Table2Pose(0,0,0);//2
CPose2D Table3Pose(0,0,0);//3
CPose2D Table4Pose(1.3,-1.3,0.1);//4
CPose2D Table5Pose(0,0,0);//5


int main(void) 
{

	//yarp::os::Network::setVerbosity(-1);

	WeAreRunning.set(true);


	//MyLaser = new Vlaser;
	MyMapBuild = new MapUpdater;
	MySound = new Vsounds;
	MyRobot = new VRobot;

	MyRobot->SamInit();
	//MyLaser->SamInit();
	MyMapBuild->SamInit();
	MySound->SamInit();

	//TThreadHandle LaserThread = createThread(ThreadForLaser);
	TThreadHandle MapThread = createThread(ThreadForMap);
	TThreadHandle MusicThread = createThread(ThreadForSound);
	TThreadHandle RobotThread = createThread(ThreadForRobot);


	yarp::os::Time::delay(3);
if (DEBUGsystem == 1)
	{



	PlayMusic.open("/MUSIC");
	Robot.open("/ROBOT");
	TargetForMap.open("/MAPTARGET");
	

	yarp::os::Network::connect("/MUSIC","/Sound_In");
	yarp::os::Network::connect("/ROBOT","/Robot_CMDin");
	yarp::os::Network::connect("/MAPTARGET","/MAP_Ion",false);

//	if(yarp::os::Network::isConnected("/MAPTARGET","/MAP_Ion"))	{puts("connected");}
//	else														{puts("not connected");}
	//yarp::os::Network::connect("/RD","/MAP_CMion");

	puts("READY FOR YOUR COMMAND ");

	}



while(WeAreRunning.get())
	{

	
	if (kbhit())
		{
			

			char mychar = getch();
			if(mychar=='q')
			{
				if(DEBUGsystem==1)
				{
					PlayMusic.close();
					Robot.close();
					TargetForMap.close();
				}
				MyRobot->Disconnect();
				//MyLaser->Disconnect();
				WeAreRunning.set(false);
			}
			else if(mychar=='o'&& DEBUGsystem ==1)
			{
			puts("push m to play some music");
			puts("push r to control robot movement");
			puts("push s to make the robot go in a square");
			puts("push g to choose somewhere for the robot to go");

			}
			else if(mychar=='g'&& DEBUGsystem ==1)
			{
				
				int userPose=0;
				CPose2D inputPose = HomePose;
				/*
				CPose2D HomePose(-0.75,3.0,4.5);//0
				CPose2D DoorPose(-2.0,1.8,3.1);//6
				CPose2D Table1Pose(1.3,2.8,0.1);//1
				CPose2D Table2Pose(0,0,0);//2
				CPose2D Table3Pose(0,0,0);//3
				CPose2D Table4Pose(1.4,-1.3,0.1);//4
				CPose2D Table5Pose(0,0,0);//5
				*/

				puts("Press 0 to go to HomePose(-0.75,3.0,1.5)");
				puts("Press 1 to go to Table1Pose(1.3,2.8,0.1)");
				puts("Press 4 to go to Table4Pose(1.4,-1.3,0.1)");
				puts("Press 6 to go to DoorPose(-2.0,1.8,3.1)");
						

				cin >> userPose;

				switch ( userPose ) 
				{

					case 0 : 	inputPose = HomePose;
					break;

					case 1 : 	inputPose = Table1Pose;
					break;

					case 4 : 	inputPose = Table4Pose;
					break;

					case 6 : 	inputPose = DoorPose;
					break;
					
					default : 
					inputPose = HomePose;
					break;
				}

				

			

			//double xx,yy;
			//puts("X loc");
			//cin >> xx;
			//puts("Y loc");
			//cin >> yy;

			Bottle &BTarget = TargetForMap.prepare();
			BTarget.clear();
			BTarget.addDouble(inputPose.x());
			BTarget.addDouble(inputPose.y());
			BTarget.addDouble(inputPose.phi());
			TargetForMap.writeStrict();

		//	puts("PS It has been checked the mapbuilder sends a reply when done");
			}
			else if(mychar=='m'&& DEBUGsystem ==1)
			{
				puts("playing music");
				Bottle &Music = PlayMusic.prepare();
				Music.clear();
				Music.addDouble(A);Music.addDouble(QUARTER);
				Music.addDouble(D);Music.addDouble(EIGHTH);
				Music.addDouble(F);Music.addDouble(EIGHTH);
				Music.addDouble(C);Music.addDouble(QUARTER);
				Music.addDouble(E);Music.addDouble(EIGHTH);
				Music.addDouble(G);Music.addDouble(EIGHTH);
				Music.addDouble(E);Music.addDouble(HALF);
				Music.addDouble(D);Music.addDouble(HALF);
				PlayMusic.writeStrict();
			}
			else if(mychar=='r'&& DEBUGsystem ==1)
			{
				double temp;
				Bottle &Robo = Robot.prepare();
				Robo.clear();
				puts("please put priority");
				cin >> temp;
				Robo.addInt(temp);
				puts("please put time for movement(s)");
				cin >> temp;
				Robo.addDouble(temp);
				puts("please liner velocity (mm)");
				cin >> temp;
				Robo.addDouble(temp);
				puts("please rot velocity (deg)");
				cin >> temp;
				Robo.addDouble(temp);
				puts("sending to robot");
				Robot.writeStrict();
			}
			else if(mychar=='s'&& DEBUGsystem ==1)
			{
	//			double temp;
				Bottle &Robo = Robot.prepare();
				Robo.clear();
				Robo.addInt(1);
				Robo.addDouble(5);Robo.addDouble(50);Robo.addDouble(0);// 250 mm for 2 secs
				Robo.addDouble(3);Robo.addDouble(0);Robo.addDouble(90); // 45degs per sec for 2 secs
				Robo.addDouble(5);Robo.addDouble(50);Robo.addDouble(0);
				Robo.addDouble(3);Robo.addDouble(0);Robo.addDouble(90);
				Robo.addDouble(5);Robo.addDouble(50);Robo.addDouble(0);
				Robo.addDouble(3);Robo.addDouble(0);Robo.addDouble(90);
				Robo.addDouble(5);Robo.addDouble(50);Robo.addDouble(0);
				Robo.addDouble(3);Robo.addDouble(0);Robo.addDouble(90);
				Robot.writeStrict();
			}	
		}
	}

//LaserThread.clear();
//yarp::os::Time::delay(0.2); // nice to let everything clear up properly
MapThread.clear();
yarp::os::Time::delay(0.2);
MusicThread.clear();
yarp::os::Time::delay(0.2);


  return 0;
}
/*
void ThreadForLaser(void)
{
	while(1){MyLaser->SamIter();}
}
*/
void ThreadForMap(void)
{
	while(1){MyMapBuild->SamIter();}
}
void ThreadForSound(void)
{
	while(1){MySound->SamIter();}
}
void ThreadForRobot(void)
{
	while(1){MyRobot->SamIter();}

}