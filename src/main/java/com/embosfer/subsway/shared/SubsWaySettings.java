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
 * Keeps track of both default and user's settings/preferences
 * @author embosfer
 *
 */
public class SubsWaySettings {
	
	private static final Logger LOG = LoggerFactory.getLogger(SubsWaySettings.class);
	
	private static final String DEF_CONFIG_PROPS_FILENAME = "default_settings";
	private static final String USR_CONFIG_PROPS_FILENAME = "user_settings";
	// props
	private static final String PROP_SHOW_SETTINGS_WIN = "show.settings.window";
	private static final String PROP_SUBS_PREF_LANG = "subtitles.pref.lang";
	private static final String PROP_SMART_TVSHOW_SEARCH = "smart.tvshow.search";
	private static final String PROP_PREF_DWLOAD_FOLDER = "pref.download.folder";
	private static final String PROP_CLEAR_RES_ON_NEW_SEARCH = "clear.res.on.new.search";
	
	private static SubsWaySettings settings = new SubsWaySettings();
	
	private Properties defaultProps, userProps;
	
	private SubsWaySettings() {
		LOG.info("Loading default settings...");
		defaultProps = new Properties();
		boolean loaded = SubsWayUtils.loadPropertiesFile(DEF_CONFIG_PROPS_FILENAME, defaultProps, true);
		if (!loaded) {
			LOG.error("Settings file ({}) not found! Exiting application...", DEF_CONFIG_PROPS_FILENAME);
			System.exit(1);
		}
		
		LOG.info("Loading user settings...");
		userProps = new Properties();
		loaded = SubsWayUtils.loadPropertiesFile(USR_CONFIG_PROPS_FILENAME, userProps, false);
		if (!loaded) {
			LOG.info("No " + USR_CONFIG_PROPS_FILENAME + " found. Defaults will be used");
			userProps = null; // no user settings to be checked for now
		}
	}
	
	private Properties getDefaultProps() {
		return defaultProps;
	}
	
	private Properties getUserProps() {
		return userProps;
	}
	
	// helper
	private static String resolveUserSetting(String key) {
		Properties userProps = settings.getUserProps();
		if (userProps == null) return null;
		return userProps.getProperty(key);
	}
	
	// helper
	private static String resolveDefaultSetting(String key) {
		String res = settings.getDefaultProps().getProperty(key);
		if (res == null) throw new RuntimeException(key + " default setting not found!");
		return res;
	}
	
	private static String resolveStringSetting(String key) {
		String res = resolveUserSetting(key);
		if (res == null) return resolveDefaultSetting(key);
		return res;
	}

	private static boolean resolveBooleanSetting(String key) {
		String res = resolveUserSetting(key);
		if (res == null) {
			res = resolveDefaultSetting(key);
			return (res == null ? null : Boolean.parseBoolean(res));
		}
		return Boolean.parseBoolean(res);
	}
	
	public static String getPreferredDwnldFolder() {
		String res = resolveUserSetting(PROP_PREF_DWLOAD_FOLDER);
		if (res == null) return System.getProperty("user.home");
		return res;
	}
	
	public static String getSubsPreferredLang() {
		return resolveStringSetting(PROP_SUBS_PREF_LANG);
	}
	
	public static boolean isSmartTvShowSearch() {
		return resolveBooleanSetting(PROP_SMART_TVSHOW_SEARCH);
	}
	
	public static boolean isShowSettingsWindow() {
		return resolveBooleanSetting(PROP_SHOW_SETTINGS_WIN);
	}
	
	public static boolean isClearResultsOnNewSearch() {
		return resolveBooleanSetting(PROP_CLEAR_RES_ON_NEW_SEARCH);
	}
	
//	public static boolean saveUserSettings() {
//		
//	}
	
	public static void main(String[] args) {
		System.out.println(SubsWaySettings.getPreferredDwnldFolder());
		System.out.println(SubsWaySettings.isSmartTvShowSearch());
	}

}
