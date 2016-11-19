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

package com.embosfer.subsway.core.opensub;

public class OpenSubtitlesLanguage {
	
	private static final String SEP = ", ";
	
	private String subLanguageID;
	private String languageName;
	private String iso639;
	
	public String getSubLanguageID() {
		return subLanguageID;
	}
	
	public void setSubLanguageID(String subLanguageID) {
		this.subLanguageID = subLanguageID;
	}
	
	public String getLanguageName() {
		return languageName;
	}
	
	public void setLanguageName(String languageName) {
		this.languageName = languageName;
	}
	
	public String getIso639() {
		return iso639;
	}
	
	public void setIso639(String iso639) {
		this.iso639 = iso639;
	}
	
	@Override
	public String toString() {
		return "subLanguageID: " + subLanguageID + SEP + "languageName: "
				+ languageName + SEP + "iso639: " + iso639;
	}

}
