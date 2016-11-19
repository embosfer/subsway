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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.xmlrpc.XmlRpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.embosfer.subsway.front.SubsWayProgressBar;

/**
 * @author embosfer
 *
 */
public class OpenSubtitlesLoginHandler {
	
	private static final Logger LOG = LoggerFactory.getLogger(SubsWayProgressBar.class);
	
	// login
	private static final String USER_NAME = "";
	private static final String PWD = "";
	private static final String LANGUAGE = "";
	private static final String USER_AGENT = "SubsWay";
	private static final int RETRY_LOGIN_IN_MS = 2000; // retry every 2 secs by default
	private static final int MAX_RETRIES = 5;
	
	// keep alive session
	private static final long DELAY = 15 * 60; // every 15 minutes
	
	private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(); // TODO cannot specify the thread factory
	
	private static OpenSubtitlesLoginHandler instance = new OpenSubtitlesLoginHandler(); // eager initialisation
	
	private OpenSubtitlesLoginHandler() {
		// do nothing
	}
	
	public static OpenSubtitlesLoginHandler getInstance() {
		return instance;
	}
	
	public void login() {
		doLogin();
		triggerCheckAlive();
	}
	
	private void triggerCheckAlive() {
		final OpenSubtitlesManager osm = OpenSubtitlesManager.getInstance();
		// start daemon session checker
		scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
			
			@Override
			public void run() {
				try {
					boolean alive = osm.noOperation();
					if (!alive) {
						doLogin();
					}
				} catch (XmlRpcException e) {
					LOG.error("Error while checking aliveness with OS server", e);
				}
				
			}
		}, DELAY, DELAY, TimeUnit.SECONDS);
		
	}
	
	private void doLogin() {
		// login
		boolean loginOK = false;
		int nbLoginAttempts = 0;
		String userAgent = USER_AGENT;
		final OpenSubtitlesManager osm = OpenSubtitlesManager.getInstance();
		do {
			if (nbLoginAttempts == MAX_RETRIES) {
				userAgent = "";
				LOG.info("Try to login to OS server with userAgent '" + userAgent + "'");
			}
			try {
				loginOK = osm.login(USER_NAME, PWD, LANGUAGE, userAgent);
				if (!loginOK) {
					LOG.info("Couldn't login to OS server with userAgent '" + userAgent + "'. A retry will be done in "
							+ RETRY_LOGIN_IN_MS + " millis...");
					try {
						Thread.sleep(RETRY_LOGIN_IN_MS);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			} catch (XmlRpcException e1) {
				LOG.error("Error while logging to OS server", e1.getCause());
			}
			nbLoginAttempts++;
		} while (!loginOK);
	}

}
