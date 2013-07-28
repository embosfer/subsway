/***********************************************************************************************************************
 *
 * SubsWay - an open source subtitles downloading tool
 * ===================================================
 *
 * Copyright (C) 2013 by Emilio Bosch Ferrando
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.ws.commons.util.Base64;
import org.apache.ws.commons.util.Base64.DecodingException;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.embosfer.subsway.front.SubsWayUI;

public class OpenSubtitlesManager {

	private static final Logger LOG = LoggerFactory
			.getLogger(OpenSubtitlesManager.class);

	private static final String SERVER_URL_XML_RPC_OPENSUBTITLES = "http://api.opensubtitles.org/xml-rpc";
	private static final String USER_AGENT_OS_TEST = "OS Test User Agent";
//	private static final String USER_AGENT_OS = "SubsWay";

	private static final String METHOD_LOG_IN = "LogIn";
	private static final String METHOD_GET_SUB_LANGUAGES = "GetSubLanguages";
	private static final String METHOD_SEARCH_SUBTITLES = "SearchSubtitles";
	private static final String METHOD_DOWNLOAD_SUB = "DownloadSubtitles";

	private static final String PARAM_TOKEN = "token";
	private static final String PARAM_QUERY = "query";
	private static final String PARAM_SEASON = "season";
	private static final String PARAM_EPISODE = "episode";
	private static final String PARAM_SUBLANGUAGEID = "sublanguageid";

	// main data res object
	private static final String RES_DATA = "data";
	private static final String RES_STATUS = "status";

	// subtitle object
	private static final String RES_ZIPDOWLOAD_LINK = "ZipDownloadLink";
	private static final String RES_SUBFILE_NAME = "SubFileName";
	private static final String RES_ID_SUBTITLE_FILE = "IDSubtitleFile";

	// language object
	private static final String RES_LANG_SUB_LANGUAGE_ID = "SubLanguageID";
	private static final String RES_LANG_LANGUAGE_NAME = "LanguageName";
	private static final String RES_LANG_ISO639 = "ISO639";

	private XmlRpcClient client;
	private String idSession;

	// singleton: eager initialisation
	private static final OpenSubtitlesManager instance = new OpenSubtitlesManager();

	public static OpenSubtitlesManager getInstance() {
		return instance;
	}

	private OpenSubtitlesManager() {
		// config
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		try {
			config.setServerURL(new URL(SERVER_URL_XML_RPC_OPENSUBTITLES));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		client = new XmlRpcClient();
		client.setConfig(config);
	}

	@SuppressWarnings("unchecked")
	public boolean login(String userName, String pwd, String language,
			String userAgent) throws XmlRpcException {
		if (userAgent == null || (userAgent != null && userAgent.equals(""))) {
			userAgent = USER_AGENT_OS_TEST;
		}
		Object[] p = { userName, pwd, language, userAgent };
		if (LOG.isDebugEnabled())
			LOG.debug("Trying to connect with userAgent " + userAgent);
		Map<String, String> resLogin = (Map<String, String>) client.execute(
				METHOD_LOG_IN, p);
		if (isOkStatus(resLogin.get(RES_STATUS))) {
			idSession = resLogin.get(PARAM_TOKEN);
			if (LOG.isDebugEnabled())
				LOG.debug("Connected! Got token (id) " + idSession
						+ " from OS server");
			return true;
		}
		LOG.warn("Failed connecting via userAgent " + userAgent
				+ ". Will try to connect with test userAgent "
				+ USER_AGENT_OS_TEST + " in 2 seconds...");
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return login(userName, pwd, language, "");
	}

	@SuppressWarnings("unchecked")
	public List<OpenSubtitlesLanguage> getSubLanguages() throws XmlRpcException {
		List<OpenSubtitlesLanguage> resLanguages = null;
		Map<String, Object> temp = (Map<String, Object>) client.execute(
				METHOD_GET_SUB_LANGUAGES, Arrays.asList(""));
		try {
			Object[] data = (Object[]) temp.get(RES_DATA);
			if (data != null) {
				resLanguages = new ArrayList<OpenSubtitlesLanguage>();
				for (Object object : data) {
					resLanguages.add(convertToOpenSubtitleLanguage(object));
				}
			}
		} catch (ClassCastException ex) {
			// if data=false, catch Exception and display: no results
			return Collections.EMPTY_LIST;
		}
		return resLanguages;
	}

	// TODO search subs by providing a folder containing video files

	// TODO search subs by providing a query by hand

	public List<OpenSubtitlesSubtitle> searchSubtitlesByQuery(String query,
			Integer season, Integer episode, String subLanguageId) {
		List<Object> params = new ArrayList<Object>();
		List<Object> searches = new ArrayList<Object>();
		Map<String, Object> search1 = new HashMap<String, Object>();
		search1.put(PARAM_QUERY, query);
		if (season != null)
			search1.put(PARAM_SEASON, season);
		if (episode != null)
			search1.put(PARAM_EPISODE, episode);
		if (subLanguageId != null)
			search1.put(PARAM_SUBLANGUAGEID, subLanguageId);
		searches.add(search1);

		params.add(idSession);
		params.add(searches);
		return searchSubtitles(params);
	}

	/**
	 * search subtitles array SearchSubtitles( $token, array(
	 * array('sublanguageid' => $sublanguageid, 'moviehash' => $moviehash,
	 * 'moviebytesize' => $moviesize, imdbid => $imdbid, query => 'movie name',
	 * "season" => 'season number', "episode" => 'episode number', 'tag' => tag
	 * ), array(...) ) )
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<OpenSubtitlesSubtitle> searchSubtitles(List<Object> params) {
		List<OpenSubtitlesSubtitle> resSubs = null;
		// Build request
		try {
			Map<String, Object> temp = (Map<String, Object>) client.execute(
					METHOD_SEARCH_SUBTITLES, params);
			String status = (String) temp.get(RES_STATUS);
			if (isOkStatus(status)) {
				try {
					Object[] data = (Object[]) temp.get(RES_DATA);
					if (data != null) {
						resSubs = new ArrayList<OpenSubtitlesSubtitle>();
						for (Object object : data) {
							resSubs.add(convertToOpenSubtitleSub(object));
						}
					}
				} catch (ClassCastException ex) {
					// if data=false, catch Exception and display: no results
					resSubs = Collections.EMPTY_LIST;
				}
			}
		} catch (XmlRpcException ex) {
			resSubs = Collections.EMPTY_LIST;
		}
		return resSubs;
	}

	@SuppressWarnings("unchecked")
	private OpenSubtitlesLanguage convertToOpenSubtitleLanguage(Object object) {
		Map<String, Object> res = ((Map<String, Object>) object);
		OpenSubtitlesLanguage lang = new OpenSubtitlesLanguage();
		lang.setSubLanguageID((String) res.get(RES_LANG_SUB_LANGUAGE_ID));
		lang.setLanguageName((String) res.get(RES_LANG_LANGUAGE_NAME));
		lang.setIso639((String) res.get(RES_LANG_ISO639));
		return lang;
	}

	@SuppressWarnings("unchecked")
	private OpenSubtitlesSubtitle convertToOpenSubtitleSub(Object object) {
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
		Map<String, Object> res = ((Map<String, Object>) object);
		OpenSubtitlesSubtitle sub = new OpenSubtitlesSubtitle();
		sub.setDownloadLink((String) res.get(RES_ZIPDOWLOAD_LINK));
		sub.setSubFileName((String) res.get(RES_SUBFILE_NAME));
		sub.setIdSubtitleFile((String) res.get(RES_ID_SUBTITLE_FILE));
		return sub;
	}

	private boolean isOkStatus(String status) {
		return status.equals("200 OK");
	}

	/**
	 * @param subsToDownload
	 * @return OK/NOK per subtitle
	 * @throws XmlRpcException
	 */
	@SuppressWarnings("unchecked")
	public Map<OpenSubtitlesSubtitle, Boolean> downloadSubtitles(
			List<OpenSubtitlesSubtitle> subsToDownload, String destFolder) {
		Map<OpenSubtitlesSubtitle, Boolean> res = new HashMap<OpenSubtitlesSubtitle, Boolean>();
		// download sub
		// DownloadSubtitles( $token, array($IDSubtitleFile,
		// $IDSubtitleFile,...) )
		List<Object> requestDownload = new ArrayList<Object>();
		requestDownload.add(idSession);
		String[] subtitleFileIDs = new String[subsToDownload.size()];
		int i = 0;
		for (OpenSubtitlesSubtitle sub : subsToDownload) {
			subtitleFileIDs[i] = sub.getIdSubtitleFile();
			i++;
		}
		requestDownload.add(subtitleFileIDs);

		Map<String, Object> temp = null;
		try {
			temp = (Map<String, Object>) client.execute(METHOD_DOWNLOAD_SUB,
					requestDownload);
		} catch (XmlRpcException e) {
			e.printStackTrace();
			fillFailureMap(res, subsToDownload);
			return res;
		}

		if (isOkStatus((String) temp.get(RES_STATUS))) {
			Object[] subs = (Object[]) temp.get(RES_DATA);
			i = 0;
			for (Object val : subs) {
				OpenSubtitlesSubtitle subToGunzip = subsToDownload.get(i);
				boolean resGunziping = decodeAndGunzipSubtitle(val,
						destFolder + File.separator + subToGunzip.getSubFileName());
				res.put(subToGunzip, resGunziping);
				i++;
			}
		} else {
			// all of them have failed
			fillFailureMap(res, subsToDownload);
		}
		return res;
	}
	
	private void fillFailureMap(Map<OpenSubtitlesSubtitle, Boolean> failureMap, List<OpenSubtitlesSubtitle> subsToDownload) {
		// all of them have failed
		for (OpenSubtitlesSubtitle sub : subsToDownload) {
			failureMap.put(sub, false);
		}
	}

	/**
	 * @param subtitleStruct
	 * @param subFileName
	 * @return true if decoding + gunziping has been successful, false otherwise
	 */
	@SuppressWarnings("unchecked")
	private boolean decodeAndGunzipSubtitle(Object subtitleStruct,
			String subFileName) {
		Map<String, Object> sub = (Map<String, Object>) subtitleStruct;
		try {
			byte[] decodedBytes = Base64.decode((String) sub.get(RES_DATA));
			FileOutputStream out = null;
			try {
				out = new FileOutputStream(subFileName);
			} catch (IOException e) {
				e.printStackTrace();
				LOG.error("Error while creating the output file");
				return false;
			}

			int len;
			byte[] buffer = new byte[1024];
			GZIPInputStream gzipInputStream = null;
			try {
				gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(
						decodedBytes));
			} catch (IOException ex) {
				ex.printStackTrace();
				System.err
						.println("Error while creating the GZIP input stream file");
				return false;
			}

			try {
				while ((len = gzipInputStream.read(buffer)) > 0) {
					out.write(buffer, 0, len);
				}
			} catch (IOException e) {
				e.printStackTrace();
				LOG.error("Error while gunziping the subs file");
				return false;
			} finally {
				try {
					gzipInputStream.close();
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
					System.err
							.println("Error while closing the resulting subs file");
					return false;
				}
			}

		} catch (DecodingException e) {
			e.printStackTrace();
			LOG.error("Error while decoding the downloaded subs file");
		}
		return true;
	}

}
