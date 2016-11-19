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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author embosfer
 *
 */
public class SubsWayUtils {
	
	private static final Logger LOG = LoggerFactory.getLogger(SubsWayUtils.class);

	private static final String OS_NAME;
	private static final Object MAC_OS_X = "mac os x";
	
	static {
		OS_NAME = System.getProperty("os.name").toLowerCase();
	}
	
	private SubsWayUtils() {}

	public static boolean loadPropertiesFile(String fileName, Properties props, boolean logError) {
		InputStream input = null;
		try {
			input = SubsWayUtils.class.getClassLoader().getResourceAsStream(fileName);
			if (input == null) throw new FileNotFoundException("File name => " + fileName);
			
			// load a properties file
			props.load(input);
			printSettings(fileName, props);
			return true;
		} catch (FileNotFoundException ex) {
			if (logError) LOG.error("File " + fileName + " not found!", ex);
			return false;
		} catch (IOException ex) {
			LOG.error("Error loading " + fileName, ex);
			return false;
		} catch (Exception ex) {
			LOG.error("Error loading " + fileName + ". Empty or corrupted file?", ex);
			return false;
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException ex) {
					LOG.warn("Error closing " + fileName, ex);
				}
			}
		}
	}
	
	private static void printSettings(String fileName, Properties props) {
		System.out.println("***** " + fileName + " *****");
		for (Map.Entry<Object, Object> entry : props.entrySet()) {
			System.out.println(entry.getKey() + "=" + entry.getValue());
		}
		System.out.println("******************************");
	}
	
	public static boolean isOSMac() {
		return OS_NAME.equals(MAC_OS_X);
	}

}
