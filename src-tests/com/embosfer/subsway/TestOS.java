package com.embosfer.subsway;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.xmlrpc.XmlRpcException;

import com.embosfer.subsway.core.opensub.OpenSubtitlesManager;
import com.embosfer.subsway.core.opensub.OpenSubtitlesSubtitle;

public class TestOS {

	public static void main(String[] args) {
		OpenSubtitlesManager osm = OpenSubtitlesManager.getInstance();
		try {
			// login
			boolean loginOK = osm.login("", "", "", "");

			if (!loginOK) {
				System.out.println("Couldn't login to OS server");
				return;
			}

			// get params from user
			Scanner reader = new Scanner(System.in);
			System.out.println("Enter the name of the subtitle");
			String query = reader.nextLine();
			System.out.println("Enter the season");
			int season = reader.nextInt();
			System.out.println("Enter the episode");
			int episode = reader.nextInt();
			System.out.println("Enter the language (eng, esp, ...)");
			String languageId = reader.next();

			// search subs
			// List<OpenSubtitlesSubtitle> subs =
			// osm.searchSubtitlesByQuery("Dexter", 1, 1, "eng");
			List<OpenSubtitlesSubtitle> subs = osm.searchSubtitlesByQuery(
					query, season, episode, languageId);

			final List<OpenSubtitlesSubtitle> subsToDownload = new ArrayList<OpenSubtitlesSubtitle>();
			for (OpenSubtitlesSubtitle sub : subs) {
				System.out.println("Would you like to download " + sub.getSubFileName() + " ?");
				System.out.println("Type Y or N");
				String download = reader.next();
				if (download.equals("Y") || download.equals("y")) {
					subsToDownload.add(sub);
					break;
				}
			}

			if (subsToDownload.isEmpty()) {
				System.out.println("Nothing to download. Exiting...");
				System.exit(0);
			}
			// download subs
			Map<OpenSubtitlesSubtitle, Boolean> resDownloading = osm
					.downloadSubtitles(subsToDownload, "/Users/embosfer/Downloads");
			for (Map.Entry<OpenSubtitlesSubtitle, Boolean> entry : resDownloading
					.entrySet()) {
				System.out.println("Download "
						+ entry.getKey().getSubFileName() + " ==> "
						+ (entry.getValue() ? "OK" : "NOK"));
			}

		} catch (XmlRpcException e) {
			e.printStackTrace();
		}
	}
}
