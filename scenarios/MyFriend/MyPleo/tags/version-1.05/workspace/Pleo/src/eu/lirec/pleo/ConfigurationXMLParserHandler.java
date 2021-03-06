/** 
 * ConfigurationXMLParserHandler.java - Parser of the configuration xml that stores the read data:
 * the mac address of phypleo's bluetooth dongle, the duration the companion should remain in ViPleo
 * before migrating to PhyPleo, and if the automatic migration from ViPleo to PhyPleo should be
 * active. 
 *  
 * Copyright (C) 2011 GAIPS/INESC-ID 
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 * Company: GAIPS/INESC-ID
 * Project: Pleo Scenario
 * @author: Paulo F. Gomes
 * Email to: pgomes@gaips.inesc-id.pt
 */

package eu.lirec.pleo;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class ConfigurationXMLParserHandler extends DefaultHandler {
	private static final String LOG_TAG = "ConfigurationXMLParserHandler";

	private static final String XML_PLEO_BLUETOOTH_TAG = "pleoBluetooth";
	private static final String XML_PLEO_BLUETOOTH_ADDRESS_TAG = "address";
	private static final String XML_MYPLEO_TIMER_TAG = "myPleoTimer";
	private static final String XML_MYPLEO_TIMER_DURATION_TAG = "duration";
	private static final String XML_MYPLEO_TIMER_ACTIVE_TAG = "active";
	private static final String XML_MYPLEO_TIMER_ACTIVE_TRUE_TAG = "true";
	private static final String XML_MYPLEO_TIMER_ACTIVE_FALSE_TAG = "false";

	private String _pleoBluetoothDongleMACAddress;
	private int _myPleoTimerDuration;
	private boolean _myPleoTimerActive;

	public String getPleoBluetoothDongleMACAddress() {
		return _pleoBluetoothDongleMACAddress;
	}

	public int getMyPleoTimerDuration() {
		return _myPleoTimerDuration;
	}

	public boolean getMyPleoTimerActive() {
		return _myPleoTimerActive;
	}

	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {

		if (qName.equalsIgnoreCase(XML_PLEO_BLUETOOTH_TAG)) {
			_pleoBluetoothDongleMACAddress = attributes
					.getValue(XML_PLEO_BLUETOOTH_ADDRESS_TAG);
			Log.d(LOG_TAG, "address: " + _pleoBluetoothDongleMACAddress);
		} else if (qName.equalsIgnoreCase(XML_MYPLEO_TIMER_TAG)) {
			String myPleoTimerDurationString = attributes
					.getValue(XML_MYPLEO_TIMER_DURATION_TAG);
			try {
				_myPleoTimerDuration = Integer
						.parseInt(myPleoTimerDurationString);
				Log.d(LOG_TAG, "duration: " + _myPleoTimerDuration);
			} catch (NumberFormatException exception) {
				Log.e(LOG_TAG, XML_MYPLEO_TIMER_DURATION_TAG
						+ " NumberFormatException(" + myPleoTimerDurationString
						+ "): " + exception.getMessage());
			}

			String myPleoTimerActiveString = attributes
					.getValue(XML_MYPLEO_TIMER_ACTIVE_TAG);
			if (myPleoTimerActiveString
					.compareTo(XML_MYPLEO_TIMER_ACTIVE_TRUE_TAG) == 0) {
				_myPleoTimerActive = true;
			} else if (myPleoTimerActiveString
					.compareTo(XML_MYPLEO_TIMER_ACTIVE_FALSE_TAG) == 0) {
				_myPleoTimerActive = false;
			} else {
				_myPleoTimerActive = false;
				Log.e(LOG_TAG, myPleoTimerActiveString + " not ("
						+ XML_MYPLEO_TIMER_ACTIVE_TRUE_TAG + "/"
						+ XML_MYPLEO_TIMER_ACTIVE_FALSE_TAG + ")");
			}
			Log.d(LOG_TAG, "active: " + _myPleoTimerActive);
		}
	}

}
