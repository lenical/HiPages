package com.hipages.controllers;

import java.util.Map;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.RateLimitStatus;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.Query.ResultType;
import twitter4j.auth.OAuth2Token;
import twitter4j.conf.ConfigurationBuilder;

public class ds {

	private static final String CONSUMER_KEY = "FQrqPSAMu33PJSqEWKkRldwb0";
	private static final String CONSUMER_SECRET = "KcSWQV8qdKhRgNPwjiGQMHlJva8eR5Zehs1OKFnfgJLC0sxduK";

	private static final int TWEETS_PER_QUERY = 100;
	private static final int MAX_QUERIES = 5;
	
	//private static final String SEARCH_TERM = "hipages";
	private static final String SEARCH_TERM = "google";

	public static String cleanText(String text) {
		text = text.replace("\n", "\\n");
		text = text.replace("\t", "\\t");
		return text;
	}

	public static OAuth2Token getOAuth2Token() {
		OAuth2Token token = null;
		ConfigurationBuilder cb;
		cb = new ConfigurationBuilder();
		cb.setApplicationOnlyAuthEnabled(true);
		cb.setOAuthConsumerKey(CONSUMER_KEY).setOAuthConsumerSecret(
				CONSUMER_SECRET);
		try {
			token = new TwitterFactory(cb.build()).getInstance()
					.getOAuth2Token();
		} catch (Exception e) {
			System.out.println("Could not get OAuth2 token");
			e.printStackTrace();
			System.exit(0);
		}
		return token;
	}

	public static Twitter getTwitter() {
		OAuth2Token token;
		token = getOAuth2Token();
		// Now, configure our new Twitter object to use application
		// authentication and provide it with // our CONSUMER key and secret and
		// the bearer token we got back from Twitter
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setApplicationOnlyAuthEnabled(true);
		cb.setOAuthConsumerKey(CONSUMER_KEY);
		cb.setOAuthConsumerSecret(CONSUMER_SECRET);
		cb.setOAuth2TokenType(token.getTokenType());
		cb.setOAuth2AccessToken(token.getAccessToken()); // And create the
															// Twitter object!
		return new TwitterFactory(cb.build()).getInstance();
	}

	public static void main(String[] args) {

		int totalTweets = 0;

		long maxID = -1;
		Twitter twitter = getTwitter();
		try {
			Map<String, RateLimitStatus> rateLimitStatus = twitter.getRateLimitStatus("search");
			RateLimitStatus searchTweetsRateLimit = rateLimitStatus.get("/search/tweets");
			System.out.printf("You have %d calls remaining out of %d, Limit resets in %d seconds\n",searchTweetsRateLimit.getRemaining(),searchTweetsRateLimit.getLimit(),searchTweetsRateLimit.getSecondsUntilReset());
			for (int queryNumber = 0; queryNumber < MAX_QUERIES; queryNumber++) {
				System.out.printf("\n\n!!! Starting loop %d\n\n", queryNumber);
				if (searchTweetsRateLimit.getRemaining() == 0) {
					System.out.printf("!!! Sleeping for %d seconds due to rate limits\n",searchTweetsRateLimit.getSecondsUntilReset());Thread.sleep((searchTweetsRateLimit.getSecondsUntilReset() + 2) * 1000l);
				}
				
				Query q = new Query(SEARCH_TERM);
				q.setCount(TWEETS_PER_QUERY); 
				q.resultType(ResultType.mixed); // Get all tweets
				//q.setLang("en");
				if (maxID != -1) {
					q.setMaxId(maxID - 1);
				}
				QueryResult r = twitter.search(q);
				if (r.getTweets().size() == 0) {
					break;
				}
				for (Status s : r.getTweets()) {
					totalTweets++;
					if (maxID == -1 || s.getId() < maxID) {
						maxID = s.getId();
					}
					System.out.printf("At %s, @%-20s said: %s\n", s.getCreatedAt().toString(), s.getUser().getScreenName(), cleanText(s.getText()));
					searchTweetsRateLimit = r.getRateLimitStatus();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.printf("\n\nA total of %d tweets retrieved\n", totalTweets);
	}
}
