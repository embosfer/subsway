/***********************************************************************************************************************
 *
 * SubsWay - an open source subtitles downloading tool
 * ===================================================
 *
 * https://github.com/embosfer
 *
 ***********************************************************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 ***********************************************************************************************************************/

package com.embosfer.subsway.shared;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author embosfer
 *
 */
public class SubsWayData {
	
	private static final Logger LOG = LoggerFactory.getLogger(SubsWayData.class);
	
	private static final String DATA_FILENAME = "subsway_data";

	// props
	private static final String PROP_SUBSWAY_VERSION = "subsway.version";
	
	private static SubsWayData data = new SubsWayData();
	
	private Properties dataHolder;
	
	private SubsWayData() {
		LOG.info("Loading SubsWay data...");
		dataHolder = new Properties();
		boolean loaded = SubsWayUtils.loadPropertiesFile(DATA_FILENAME, dataHolder, true);
		if (!loaded) {
			LOG.error("Exiting application...");
			throw new RuntimeException();
		}
	}
	
	private Properties getData() {
		return dataHolder;
	}
	
	// helper
	private static String resolveSetting(String key) {
		String res = data.getData().getProperty(key);
		if (res == null) throw new RuntimeException(key + " property not found!");
		return res;
	}
	
	public static String getVersion() {
		String version = resolveSetting(PROP_SUBSWAY_VERSION);
		return "SubsWay Version: " + version; 
	}
	
	public static void main(String[] args) {
		SubsWayData.getVersion();
	}

}
