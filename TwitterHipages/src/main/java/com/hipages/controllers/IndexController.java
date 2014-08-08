package com.hipages.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.RateLimitStatus;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.Query.ResultType;
import twitter4j.auth.OAuth2Token;
import twitter4j.conf.ConfigurationBuilder;

@Controller
public class IndexController {
	
	private static final String CONSUMER_KEY = "FQrqPSAMu33PJSqEWKkRldwb0";
	private static final String CONSUMER_SECRET = "KcSWQV8qdKhRgNPwjiGQMHlJva8eR5Zehs1OKFnfgJLC0sxduK";
	

	private static final int TWEETS_PER_QUERY = 100;
	private static final int MAX_QUERIES = 5;
	
	private static final String SEARCH_TERM = "hipages";
	//private static final String SEARCH_TERM = "google";
	
	
    @RequestMapping(value = "/")
    public ModelAndView index() {
        ModelAndView mav = new ModelAndView("index/index");

        StringBuilder msg = new StringBuilder();
        
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
					//msg.append(s.getCreatedAt().toString()).append(" - ").append(s.getUser().getScreenName()).append(" - ").append(cleanText(s.getText())).append("<br>");
					msg.append(" From :  ").append(s.getUser().getScreenName()).append("<br>Tweet : ").append(cleanText(s.getText())).append("<br><br>");
					searchTweetsRateLimit = r.getRateLimitStatus();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.printf("\n\nA total of %d tweets retrieved\n", totalTweets);
        
        /*
        try {
        	ConfigurationBuilder cb = new ConfigurationBuilder();
        	cb.setDebugEnabled(true)
        	  .setOAuthConsumerKey("FQrqPSAMu33PJSqEWKkRldwb0")
        	  .setOAuthConsumerSecret("KcSWQV8qdKhRgNPwjiGQMHlJva8eR5Zehs1OKFnfgJLC0sxduK")
        	  .setOAuthAccessToken("2708836267-eS12KpFSV4SLV99fZALweLiZsMpcy3VAxIpUIPe")
        	  .setOAuthAccessTokenSecret("5D3CZ2VwrtDxUsQc3pwnjHovZuHLbiIM9ni57kS1eKBMm");
        	
        	TwitterFactory tf = new TwitterFactory(cb.build());
        	Twitter twitter = tf.getInstance();

            Query query = new Query("hipages");
            query.setCount(1000);
            QueryResult result= twitter.search(query);
            do{
                      List<Status> tweets = result.getTweets();
                      for(Status tweet: tweets){
                    	  msg.append(tweet.getText()).append("<br>");
                      }
                      query=result.nextQuery();
                      if(query!=null) {
                           result=twitter.search(query);
                      }
            }while(query!=null);
            
            
        } catch (TwitterException te) {
            te.printStackTrace();
            System.out.println("Failed to get timeline: " + te.getMessage());
            //System.exit(-1);
        }
        */
        
        mav.addObject("msg", msg.toString());
        return mav;
    }
    
    private static Twitter getTwitter() {
		OAuth2Token token;
		token = getOAuth2Token();
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setApplicationOnlyAuthEnabled(true);
		cb.setOAuthConsumerKey(CONSUMER_KEY);
		cb.setOAuthConsumerSecret(CONSUMER_SECRET);
		cb.setOAuth2TokenType(token.getTokenType());
		cb.setOAuth2AccessToken(token.getAccessToken());
		return new TwitterFactory(cb.build()).getInstance();
	}
    
    private static OAuth2Token getOAuth2Token() {
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
    
    private static String cleanText(String text) {
		text = text.replace("\n", "\\n");
		text = text.replace("\t", "\\t");
		return text;
	}
    
}
