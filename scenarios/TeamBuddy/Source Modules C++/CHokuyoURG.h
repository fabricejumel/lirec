/* +---------------------------------------------------------------------------+
   |          The Mobile Robot Programming Toolkit (MRPT) C++ library          |
   |                                                                           |
   |                   http://mrpt.sourceforge.net/                            |
   |                                                                           |
   |   Copyright (C) 2005-2010  University of Malaga                           |
   |                                                                           |
   |    This software was written by the Machine Perception and Intelligent    |
   |      Robotics Lab, University of Malaga (Spain).                          |
   |    Contact: Jose-Luis Blanco  <jlblanco@ctima.uma.es>                     |
   |                                                                           |
   |  This file is part of the MRPT project.                                   |
   |                                                                           |
   |     MRPT is free software: you can redistribute it and/or modify          |
   |     it under the terms of the GNU General Public License as published by  |
   |     the Free Software Foundation, either version 3 of the License, or     |
   |     (at your option) any later version.                                   |
   |                                                                           |
   |   MRPT is distributed in the hope that it will be useful,                 |
   |     but WITHOUT ANY WARRANTY; without even the implied warranty of        |
   |     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         |
   |     GNU General Public License for more details.                          |
   |                                                                           |
   |     You should have received a copy of the GNU General Public License     |
   |     along with MRPT.  If not, see <http://www.gnu.org/licenses/>.         |
   |                                                                           |
   +---------------------------------------------------------------------------+ */
#ifndef CHokuyoURG_H
#define CHokuyoURG_H

#include <mrpt/poses/CPose3D.h>
#include <mrpt/hwdrivers/C2DRangeFinderAbstract.h>
#include <mrpt/utils/stl_extensions.h>

namespace mrpt
{
	namespace hwdrivers
	{
		/** This software driver implements the protocol SCIP-2.0 for interfacing HOKUYO URG and UTM laser scanners.
		  *  Refer to the wiki page for more details:
		  *    http://www.mrpt.org/Example:HOKUYO_URG/UTM_Laser_Scanner
		  *
		  *  See also the application "rawlog-grabber" for a ready-to-use application to gather data from the scanner.
		  *
		  *  \code
		  *  PARAMETERS IN THE ".INI"-LIKE CONFIGURATION STRINGS:
		  * -------------------------------------------------------
		  *   [supplied_section_name]
		  *    HOKUYO_motorSpeed_rpm=600
		  *    COM_port_WIN = COM3
		  *    COM_port_LIN = ttyS0
		  *    pose_x=0.21	; Laser range scaner 3D position in the robot (meters)
		  *    pose_y=0
		  *    pose_z=0.34
		  *    pose_yaw=0	; Angles in degrees
		  *    pose_pitch=0
		  *    pose_roll=0
		  *
		  *    // Optional: reduced FOV:
		  *    // reduced_fov  = 25 // Deg
		  *
		  *    // Optional: Exclusion zones to avoid the robot seeing itself:
		  *    //exclusionZone1_x = 0.20 0.30 0.30 0.20
		  *    //exclusionZone1_y = 0.20 0.30 0.30 0.20
		  *
		  *    // Optional: Exclusion zones to avoid the robot seeing itself:
		  *    //exclusionAngles1_ini = 20  // Deg
		  *    //exclusionAngles1_end = 25  // Deg
		  *
		  *  \endcode
		  */
		class HWDRIVERS_IMPEXP CHokuyoURG : public C2DRangeFinderAbstract
		{
			DEFINE_GENERIC_SENSOR(CHokuyoURG)
		public:

			/** Used in CHokuyoURG::displayVersionInfo */
			struct TSensorInfo
			{
				std::string		model;			//!< The sensor model
				double			d_min,d_max;	//!< Min/Max ranges, in meters.
				int				scans_per_360deg;	//!< Number of measuremens per 360 degrees.
				int				scan_first,scan_last, scan_front;	//!< First, last, and front step of the scanner angular span.
				int				motor_speed_rpm;		//!< Standard motor speed, rpm.
			};

		private:
			/** The first and last ranges to consider from the scan.
			  */
			int		m_firstRange,m_lastRange;

			/** The motor speed (default=600rpm)
			  */
			int		m_motorSpeed_rpm;

			/** The sensor 6D pose:
			  */
			poses::CPose3D	m_sensorPose;

			mrpt::utils::circular_buffer<uint8_t> m_rx_buffer; //!< Auxiliary buffer for readings

			std::string     m_lastSentMeasCmd; //!< The last sent measurement command (MDXXX), including the last 0x0A.

			bool 			m_verbose;

			/** Enables the SCIP2.0 protocol (this must be called at the very begining!).
			  * \return false on any error
			  */
			bool  enableSCIP20();

			/** Passes to 115200bps bitrate.
			  * \return false on any error
			  */
			bool  setHighBaudrate();

			/** Switchs the laser on.
			  * \return false on any error
			  */
			bool  switchLaserOn();

			/** Switchs the laser off
			  * \return false on any error
			  */
			bool  switchLaserOff();

			/** Changes the motor speed in rpm's (default 600rpm)
			  * \return false on any error
			  */
			bool  setMotorSpeed(int motoSpeed_rpm);

			/** Ask to the device, and print to the debug stream, details about the firmware version,serial number,...
			  * \return false on any error
			  */
			bool  displayVersionInfo(  );

			/** Ask to the device, and print to the debug stream, details about the sensor model.
			  *  It also optionally saves all the information in an user supplied data structure "out_data".
			  * \return false on any error
			  */
			bool  displaySensorInfo( CHokuyoURG::TSensorInfo * out_data = NULL );

			/** Start the scanning mode, using parameters stored in the object (loaded from the .ini file)
			  * After this command the device will start to send scans until "switchLaserOff" is called.
			  * \return false on any error
			  */
			bool  startScanningMode();

			/** Turns the laser on */
			void initialize();

			/** Waits for a response from the device.
			  * \return false on any error
			  */
			bool  receiveResponse(
					const char	*sentCmd_forEchoVerification,
					char	&rcv_status0,
					char	&rcv_status1,
					char	*rcv_data,
					int		&rcv_dataLength);


			/** Assures a minimum number of bytes in the input buffer, reading from the serial port only if required.
			  * \return false if the number of bytes are not available, even after trying to fetch more data from the serial port.
			  */
			bool assureBufferHasBytes(const size_t nDesiredBytes);

		public:
			/** Constructor
			  */
			CHokuyoURG();

			/** Destructor: turns the laser off */
			virtual ~CHokuyoURG();

			/** Specific laser scanner "software drivers" must process here new data from the I/O stream, and, if a whole scan has arrived, return it.
			  *  This method will be typically called in a different thread than other methods, and will be called in a timely fashion.
			  */
			void  doProcessSimple(
				bool							&outThereIsObservation,
				mrpt::slam::CObservation2DRangeScan	&outObservation,
				bool							&hardwareError );

			/** Enables the scanning mode (which may depend on the specific laser device); this must be called before asking for observations to assure that the protocol has been initializated.
			  * \return If everything works "true", or "false" if there is any error.
			  */
			bool  turnOn();

			/** Disables the scanning mode (this can be used to turn the device in low energy mode, if available)
			  * \return If everything works "true", or "false" if there is any error.
			  */
			bool  turnOff();

			/** Empties the RX buffers of the serial port */
			void purgeBuffers();

			/** If set to non-empty, the serial port will be attempted to be opened automatically when this class is first used to request data from the laser.  */
			void setSerialPort(const std::string &port_name) { m_com_port = port_name; }

			/** Returns the currently set serial port \sa setSerialPort */
			const std::string getSerialPort() { return m_com_port; }

			/** If called (before calling "turnOn"), the field of view of the laser is reduced to the given range (in radians), discarding the rest of measures.
			  *  Call with "0" to disable this reduction again (the default).
			  */
			void setReducedFOV(const double fov) { m_reduced_fov = fov; }

			void setVerbose(bool enable = true) { m_verbose = enable; }


		protected:
			/** Returns true if there is a valid stream bound to the laser scanner, otherwise it first try to open the serial port "m_com_port"
			  */
			bool  checkCOMisOpen();

			double  		m_reduced_fov; //!< Used to reduce artificially the interval of scan ranges.

			std::string		m_com_port;		//!< If set to non-empty, the serial port will be attempted to be opened automatically when this class is first used to request data from the laser.

			/** The information gathered when the laser is first open */
			TSensorInfo		m_sensor_info;

			bool			m_I_am_owner_serial_port;

			uint32_t		m_timeStartUI;	//!< Time of the first data packet, for synchronization purposes.
			mrpt::system::TTimeStamp	m_timeStartTT;

			/** Loads specific configuration for the device from a given source of configuration parameters, for example, an ".ini" file, loading from the section "[iniSection]" (see utils::CConfigFileBase and derived classes)
			  *  See hwdrivers::CHokuyoURG for the possible parameters
			  */
			void  loadConfig_sensorSpecific(
				const mrpt::utils::CConfigFileBase &configSource,
				const std::string	  &iniSection );

		};	// End of class

	} // End of namespace

} // End of namespace

#endif
