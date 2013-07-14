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
