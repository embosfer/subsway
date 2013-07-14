package com.embosfer.subsway.core.opensub;

public class OpenSubtitlesSubtitle {

	private static final String SEP = ", ";

	private String downloadLink;
	private String subFileName;
	private String idSubtitleFile;
	
	// Every object has the following keys:
	// UserNickName, SubFormat, IDSubtitle, IDMovie, SubBad,
	// UserID, ZipDownloadLink, SubSize, SubFileName,
	// SubDownloadLink, MovieKind,
	// UserRank, SubActualCD, MovieImdbRating, SubAuthorComment,
	// SubRating, QueryParameters, SeriesSeason, SubFeatured,
	// SubtitlesLink,
	// SubHearingImpaired, SubHash, IDSubMovieFile, ISO639,
	// SubDownloadsCnt, MovieHash, SubSumCD, SubComments,
	// QueryNumber, MovieByteSize,
	// LanguageName, MovieYear, SubLanguageID, MovieReleaseName,
	// SeriesEpisode, MovieTimeMS, MatchedBy, MovieName,
	// SubAddDate, IDMovieImdb, MovieNameEng, IDSubtitleFile

	public String getDownloadLink() {
		return downloadLink;
	}

	public void setDownloadLink(String downloadLink) {
		this.downloadLink = downloadLink;
	}

	public String getSubFileName() {
		return subFileName;
	}

	public void setSubFileName(String subFileName) {
		this.subFileName = subFileName;
	}

	public String getIdSubtitleFile() {
		return idSubtitleFile;
	}

	public void setIdSubtitleFile(String idSubtitleFile) {
		this.idSubtitleFile = idSubtitleFile;
	}
	
	//TODO hash
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof OpenSubtitlesSubtitle)) return false;
		OpenSubtitlesSubtitle sub = (OpenSubtitlesSubtitle) obj;
		if (this == sub) return false;
		if (!this.getIdSubtitleFile().equals(sub.getIdSubtitleFile())) return false;
		return true;
	}

	@Override
	public String toString() {
		return "idSubtitleFile: " + idSubtitleFile + SEP + "subFileName: "
				+ subFileName + SEP + "downloadLink: " + downloadLink;
	}

}
