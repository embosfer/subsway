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
