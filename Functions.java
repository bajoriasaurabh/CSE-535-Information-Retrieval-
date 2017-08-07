
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import twitter4j.*;
import twitter4j.conf.*;

class Functions {
	private static final String[] EMOTICON_UNICODE_LIST = getUnicodeList();

	void configure(ConfigurationBuilder cb) {
		cb.setJSONStoreEnabled(true);
		cb.setDebugEnabled(true);
		cb.setOAuthConsumerKey("");
		cb.setOAuthConsumerSecret("");
		cb.setOAuthAccessToken("");
		cb.setOAuthAccessTokenSecret("");
	}

	public static void main(String[] args) throws IOException, TwitterException {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		Scanner scan = new Scanner(System.in);
		Functions F = new Functions();
		F.configure(cb);
		Twitter twitter = new TwitterFactory(cb.build()).getInstance();
		Query query = null;
		int totalNumberOfTweets = 0;
		FileWriter writer = new FileWriter("A:\\Semester 1\\IRProject4\\Statistics_Final.csv", true);
		writer.write("Topic,Language,Date,Count");
		// PrintStream out = new PrintStream(new
		// FileOutputStream("RequirementCount.csv"));
		String lang = "en";
		String DateSince = "2016-11-20";
		String DateTill = "2016-12-02";
		int count = 5000;

		/*
		 * System.out.println("Enter the Search language:"); String lang =
		 * scan.next(); System.out.println("Enter the Date Since: "); String
		 * DateSince = scan.next(); System.out.println("Enter the Date till: ");
		 * String DateTill = scan.next();
		 * System.out.println("Enter the max number of tweets req.: ");
		 * 
		 * int count = scan.nextInt();
		 */
		totalNumberOfTweets = totalNumberOfTweets + F.calculation(query, lang, DateSince, DateTill, count, twitter);
		// System.out.println(totalNumberOfTweets);
		writer.write("Trump" + "," + lang + "," + DateSince + "-" + DateTill + "," + totalNumberOfTweets);
		writer.write("\n");
		writer.close();

	}

	int calculation(Query q, String lang, String DateSince, String Datetill, int count, Twitter twitter)
			throws IOException, TwitterException {
		String Topic = "Trump";
		q = new Query("(#Trump) +exclude:retweets");
		// q = new Query("(US Open) OR (Serena Williams) OR (Kerber) OR
		// (Wawrinka) OR (Djokovic) OR (Monfils) OR (Nadal) +exclude:retweets");
		// q = new Query("(Game of Thrones) OR (Jon Snow) OR (George Martin) OR
		// (Tyrion) OR (ì™•ì¢Œì�˜ ê²Œìž„) +exclude:retweets");
		q.lang(lang);
		q.setSince(DateSince);
		q.setUntil(Datetill);
		int size = 0;
		long lastID = Long.MAX_VALUE;
		QueryResult result = null;
		FileWriter jsonFileName = new FileWriter("A:\\Semester 1\\IRProject4\\Trump_en", true);
		FileWriter jsonFileNameRaw = new FileWriter("A:\\Semester 1\\IRProject4\\Trump_raw_en", true);
		FileWriter jsonAppendAll = new FileWriter("A:\\Semester 1\\IRProject4\\Trump", true);
		ArrayList<Status> tweets = new ArrayList<Status>();
		while (tweets.size() < count) {
			if (count - tweets.size() > 100)
				q.setCount(100);
			else
				q.setCount(count - tweets.size());
			size = tweets.size();
			result = twitter.search(q);
			String json = "";
			String hashtag = "", mentions = "", URL = "", emoticons = "", temp = "";
			for (Status s : result.getTweets()) {
				String rawJson = TwitterObjectFactory.getRawJSON(s);
				hashtag = "";
				mentions = "";
				URL = "";
				emoticons = "";
				temp = "";

				String tweetText = s.getText().replaceAll("\n", "").replaceAll("\"", "").replaceAll("\'", "")
						.replaceAll("â€œ", "").replaceAll("â€�", "").replaceAll("}", "").replaceAll("\\)", "")
						.replaceAll("\\(", "").replaceAll("\\[", "").replaceAll("\\]", "")
						.replaceAll("[!\\\"$%&'()*+,;<=>?[\\\\]^`{|}~\\u201C\\u201D\\r\\n]", "") + "        ";
				System.out.println("#### " + s.getText());
				for (String emoticon : EMOTICON_UNICODE_LIST) {
					if (tweetText.contains(emoticon) && !emoticon.equals("")) {
						while (tweetText.contains(emoticons)) {
							tweetText = tweetText.replaceFirst(emoticon, "");
							emoticons += "\"" + emoticon + "\",";
							System.out.println("EMOTICON --->" + emoticon);
						}
					}
				}
				if (emoticons.length() > 0) {
					emoticons = emoticons.substring(0, emoticons.length() - 1);
				}
				if (tweetText.contains("#")) {
					while (tweetText.contains("#")) {
						temp = tweetText.substring(tweetText.indexOf("#"),
								tweetText.indexOf(" ", tweetText.indexOf("#")));
						for (String temp1 : temp.split("#")) {
							if (!temp1.equals(""))
								hashtag += "\"" + temp1 + "\",";
						}
						tweetText = tweetText.replaceAll(temp + " ", "");
					}
					if (hashtag.length() > 0) {
						hashtag = hashtag.substring(0, hashtag.length() - 1);
					}
				}
				if (tweetText.contains("@")) {
					while (tweetText.contains("@")) {
						temp = tweetText.substring(tweetText.indexOf("@"),
								tweetText.indexOf(" ", tweetText.indexOf("@")));
						for (String temp1 : temp.split("@")) {
							if (!temp1.equals(""))
								mentions += "\"" + temp1 + "\",";
						}
						tweetText = tweetText.replaceAll(temp + " ", "");
						System.out.println(mentions);
					}
					if (mentions.length() > 1) {
						mentions = mentions.substring(0, mentions.length() - 1);
					}
				}
				if (tweetText.contains("http")) {
					while (tweetText.contains("http")) {
						temp = tweetText.substring(tweetText.indexOf("http"),
								tweetText.indexOf(" ", tweetText.indexOf("http")));
						URL += "\"" + temp + "\",";
						tweetText = tweetText.replaceAll(temp + " ", "");

					}
					if (URL.length() > 1) {
						URL = URL.substring(0, URL.length() - 1);
					}
				}

				tweetText = tweetText.replaceAll("[!\\\"#$%&'()*+,-./:;<=>?@[\\\\]^_`{|}~\\u201C\\u201D\\r\\n]", "")
						+ "           ";
				String tweetText_raw = s.getText().replaceAll("\n", " ").replaceAll("\"", "").replaceAll("\'", "")
						.replaceAll("â€œ", "").replaceAll("â€�", "");
				if (s.getLang().compareTo("en") == 0) {
					json = "{\"topic\":\"" + Topic + "\",\"Id\":\"" + s.getId() + "\",\"tweet_text\":\""
							+ tweetText_raw.trim() + "\",\"tweet_lang\":\"" + s.getLang() + "\",\"text_en\":\""
							+ tweetText.trim() + "\"," + "\"text_es\":\"\"," + "\"text_ko\":\"\","
							+ "\"text_tr\":\"\",";
				} else if (s.getLang().compareTo("es") == 0) {
					json = "{\"topic\":\"" + Topic + "\",\"Id\":\"" + s.getId() + "\",\"tweet_text\":\""
							+ tweetText_raw.trim() + "\",\"tweet_lang\":\"" + s.getLang() + "\",\"text_en\":\"\","
							+ "\"text_es\":\"" + tweetText.trim() + "\",\"text_ko\":\"\"," + "\"text_tr\":\"\",";
				} else if (s.getLang().compareTo("ko") == 0) {
					json = "{\"topic\":\"" + Topic + "\",\"Id\":\"" + s.getId() + "\",\"tweet_text\":\""
							+ tweetText_raw.trim() + "\",\"tweet_lang\":\"" + s.getLang() + "\",\"text_en\":\"\","
							+ "\"text_es\":\"\"," + "\"text_ko\":\"" + tweetText.trim() + "\",\"text_tr\":\"\",";
				} else if (s.getLang().compareTo("tr") == 0) {
					json = "{\"topic\":\"" + Topic + "\",\"Id\":\"" + s.getId() + "\",\"tweet_text\":\""
							+ tweetText_raw.trim() + "\",\"tweet_lang\":\"" + s.getLang() + "\",\"text_en\":\"\","
							+ "\"text_es\":\"\"," + "\"text_ko\":\"\"," + "\"text_tr\":\"" + tweetText.trim() + "\",";
				} else {
					json = "{\"topic\":\"" + Topic + "\",\"Id\":\"" + s.getId() + "\",\"tweet_text\":\""
							+ tweetText_raw.trim() + "\",\"tweet_lang\":\"" + s.getLang() + "\",\"text_en\":\"\","
							+ "\"text_es\":\"\"," + "\"text_ko\":\"\"," + "\"text_tr\":\"\",";
				}
				json += "\"hashtags\":[" + hashtag.trim() + "],\"mentions\":[" + mentions.trim() + "],\"tweet_urls\":["
						+ URL.trim() + "],\"tweet_emoticons\":[" + emoticons.trim() + "],\"tweet_date\":\""
						+ dateConvertor(LocalDateTime.ofInstant(s.getCreatedAt().toInstant(), ZoneId.systemDefault()))
						+ "\",\"tweet_loc\":\"";
				if (s.getGeoLocation() == null) {
					json += "\"}";
				} else {
					json += s.getGeoLocation().getLatitude() + ", " + s.getGeoLocation().getLongitude() + "\"}";
				}

				jsonFileName.write(json.toString() + "\n");
				jsonFileNameRaw.write(rawJson + "\n");
				jsonAppendAll.write(json.toString() + "\n");
				// System.out.println(json);
			}
			tweets.addAll(result.getTweets());
			if (tweets.size() == size) {
				break;
			}
			for (Status t : tweets)
				if (t.getId() < lastID)
					lastID = t.getId();
			q.setMaxId(lastID - 1);
		}
		System.out.println("tweets.size() ---->  " + tweets.size());
		jsonFileName.close();
		jsonFileNameRaw.close();
		jsonAppendAll.close();

		return tweets.size();
	}

	void Statistics(String topic, String lang, String DateSince, String DateTill, int totalNumberOfTweets)
			throws IOException {
	}

	private static String dateConvertor(LocalDateTime date) {
		if (date.getMinute() > 30)
			date = date.plusHours(1);
		date = date.withMinute(0);
		date = date.withSecond(0);
		DateTimeFormatter ldf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
		return String.valueOf(date.format(ldf));
	}

	private static String[] getUnicodeList() {
		String unicodeString = "\ud83c\uddec\ud83c\udde7#\ud83c\uddfa\ud83c\uddf8#\ud83c\udde9\ud83c\uddea#\ud83c\uddea\ud83c\uddf8#"
				+ "\ud83c\uddeb\ud83c\uddf7#\ud83c\udde8\ud83c\uddf3#\ud83c\uddee\ud83c\uddf9#\ud83c\uddef\ud83c\uddf5#"
				+ "\ud83c\uddf0\ud83c\uddf7#\ud83c\uddf7\ud83c\uddfa#\ud83d\udc7a#\ud83c\udd94#\ud83c\udd95#\ud83c\udd96#"
				+ "\ud83c\udd97#\ud83c\udd98#\ud83c\udd99#\ud83c\udd9a#\ud83c\udde6#\ud83c\udde7#\ud83d\udc83#\ud83c\udde8#"
				+ "\ud83c\udccf#\ud83c\udde9#\ud83c\udd70#\ud83c\uddea#\ud83c\udd71#\ud83c\uddeb#\ud83c\udd7e#\ud83c\uddec#"
				+ "\ud83c\udded#\ud83c\uddee#\ud83c\udd8e#\ud83c\uddef#\ud83c\udd91#\ud83c\uddf0#\ud83c\uddf1#\ud83c\uddf2#"
				+ "\ud83c\uddf3#\ud83c\uddf4#\ud83c\uddf5#\ud83c\uddf6#\ud83c\udd92#\ud83c\uddf7#\ud83c\uddf8#\ud83c\uddf9#"
				+ "\ud83c\udd93#\ud83c\uddfa#\ud83c\uddfb#\ud83c\uddfc#\ud83c\uddfd#\ud83c\uddfe#\ud83c\uddff#\ud83c\ude01#"
				+ "\ud83c\ude02#\ud83c\ude32#\ud83c\ude33#\ud83c\ude34#\ud83c\ude35#\ud83c\ude36#\ud83c\ude37#\ud83c\ude38#"
				+ "\ud83c\ude39#\ud83c\ude3a#\ud83c\ude50#\ud83c\ude51#\ud83c\udf00#\ud83c\udf01#\ud83c\udf02#\ud83c\udf03#"
				+ "\ud83c\udf04#\ud83c\udf05#\ud83c\udf06#\ud83c\udf07#\ud83c\udf08#\ud83c\udf09#\ud83c\udf0a#\ud83c\udf0b#"
				+ "\ud83c\udf0c#\ud83c\udf0d#\ud83c\udf0e#\ud83c\udf0f#\ud83c\udf10#\ud83c\udf11#\ud83c\udf12#\ud83c\udf13#"
				+ "\ud83c\udf14#\ud83c\udf15#\ud83c\udf16#\ud83c\udf17#\ud83c\udf18#\ud83c\udf19#\ud83c\udf1a#\ud83c\udf1b#"
				+ "\ud83c\udf1c#\ud83c\udf1d#\ud83c\udf1e#\ud83c\udf1f#\ud83c\udf20#\ud83c\udf30#\ud83c\udf31#\ud83c\udf32#"
				+ "\ud83c\udf33#\ud83c\udf34#\ud83c\udf35#\ud83c\udf37#\ud83c\udf38#\ud83c\udf39#\ud83c\udf3a#\ud83c\udf3b#"
				+ "\ud83c\udf3c#\ud83c\udf3d#\ud83c\udf3e#\ud83c\udf3f#\ud83c\udf40#\ud83c\udf41#\ud83c\udf42#\ud83c\udf43#"
				+ "\ud83c\udf44#\ud83c\udf45#\ud83c\udf46#\ud83c\udf47#\ud83c\udf48#\ud83c\udf49#\ud83c\udf4a#\ud83c\udf4b#"
				+ "\ud83c\udf4c#\ud83c\udf4d#\ud83c\udf4e#\ud83c\udf4f#\ud83c\udf50#\ud83c\udf51#\ud83c\udf52#\ud83c\udf53#"
				+ "\ud83c\udf54#\ud83c\udf55#\ud83c\udf56#\ud83c\udf57#\ud83c\udf58#\ud83c\udf59#\ud83c\udf5a#\ud83c\udf5b#"
				+ "\ud83c\udf5c#\ud83c\udf5d#\ud83c\udf5e#\ud83c\udf5f#\ud83c\udf60#\ud83c\udf61#\ud83c\udf62#\ud83c\udf63#"
				+ "\ud83c\udf64#\ud83c\udf65#\ud83c\udf66#\ud83c\udf67#\ud83c\udf68#\ud83c\udf69#\ud83c\udf6a#\ud83c\udf6b#"
				+ "\ud83c\udf6c#\ud83c\udf6d#\ud83c\udf6e#\ud83c\udf6f#\ud83c\udf70#\ud83c\udf71#\ud83c\udf72#\ud83c\udf73#"
				+ "\ud83c\udf74#\ud83c\udf75#\ud83c\udf76#\ud83c\udf77#\ud83c\udf78#\ud83c\udf79#\ud83c\udf7a#\ud83c\udf7b#"
				+ "\ud83c\udf7c#\ud83c\udf80#\ud83c\udf81#\ud83c\udf82#\ud83c\udf83#\ud83c\udf84#\ud83c\udf85#\ud83c\udf86#"
				+ "\ud83c\udf87#\ud83c\udf88#\ud83c\udf89#\ud83c\udf8a#\ud83c\udf8b#\ud83c\udf8c#\ud83c\udf8d#\ud83c\udf8e#"
				+ "\ud83c\udf8f#\ud83c\udf90#\ud83c\udf91#\ud83c\udf92#\ud83c\udf93#\ud83c\udfa0#\ud83c\udfa1#\ud83c\udfa2#"
				+ "\ud83c\udfa3#\ud83c\udfa4#\ud83c\udfa5#\ud83c\udfa6#\ud83c\udfa7#\ud83c\udfa8#\ud83c\udfa9#\ud83c\udfaa#"
				+ "\ud83c\udfab#\ud83c\udfac#\ud83c\udfad#\ud83c\udfae#\ud83c\udfaf#\ud83c\udfb0#\ud83c\udfb1#\ud83c\udfb2#"
				+ "\ud83c\udfb3#\ud83c\udfb4#\ud83c\udfb5#\ud83c\udfb6#\ud83c\udfb7#\ud83c\udfb8#\ud83c\udfb9#\ud83c\udfba#"
				+ "\ud83c\udfbb#\ud83c\udfbc#\ud83c\udfbd#\ud83c\udfbe#\ud83c\udfbf#\ud83c\udfc0#\ud83c\udfc1#\ud83c\udfc2#"
				+ "\ud83c\udfc3#\ud83c\udfc4#\ud83c\udfc6#\ud83c\udfc7#\ud83c\udfc8#\ud83c\udfc9#\ud83c\udfca#\ud83c\udfe0#"
				+ "\ud83c\udfe1#\ud83c\udfe2#\ud83c\udfe3#\ud83c\udfe4#\ud83c\udfe5#\ud83c\udfe6#\ud83c\udfe7#\ud83c\udfe8#"
				+ "\ud83c\udfe9#\ud83c\udfea#\ud83c\udfeb#\ud83c\udfec#\ud83c\udfed#\ud83c\udfee#\ud83c\udfef#\ud83c\udff0#"
				+ "\ud83d\udc00#\ud83d\udc01#\ud83d\udc02#\ud83d\udc03#\ud83d\udc04#\ud83d\udc05#\ud83d\udc06#\ud83d\udc07#"
				+ "\ud83d\udc08#\ud83d\udc09#\ud83d\udc0a#\ud83d\udc0b#\ud83d\udc0c#\ud83d\udc0d#\ud83d\udc0e#\ud83d\udc0f#"
				+ "\ud83d\udc10#\ud83d\udc11#\ud83d\udc12#\ud83d\udc13#\ud83d\udc14#\ud83d\udc15#\ud83d\udc16#\ud83d\udc17#"
				+ "\ud83d\udc18#\ud83d\udc19#\ud83d\udc1a#\ud83d\udc1b#\ud83d\udc1c#\ud83d\udc1d#\ud83d\udc1e#\ud83d\udc1f#"
				+ "\ud83d\udc20#\ud83d\udc21#\ud83d\udc22#\ud83d\udc23#\ud83d\udc24#\ud83d\udc25#\ud83d\udc26#\ud83d\udc27#"
				+ "\ud83d\udc28#\ud83d\udc29#\ud83d\udc2a#\ud83d\udc2b#\ud83d\udc2c#\ud83d\udc2d#\ud83d\udc2e#\ud83d\udc2f#"
				+ "\ud83d\udc30#\ud83d\udc31#\ud83d\udc32#\ud83d\udc33#\ud83d\udc34#\ud83d\udc35#\ud83d\udc36#\ud83d\udc37#"
				+ "\ud83d\udc38#\ud83d\udc39#\ud83d\udc3a#\ud83d\udc3b#\ud83d\udc3c#\ud83d\udc3d#\ud83d\udc3e#\ud83d\udc40#"
				+ "\ud83d\udc42#\ud83d\udc43#\ud83d\udc44#\ud83d\udc45#\ud83d\udc46#\ud83d\udc47#\ud83d\udc48#\ud83d\udc49#"
				+ "\ud83d\udc4a#\ud83d\udc4b#\ud83d\udc4c#\ud83d\udc4d#\ud83d\udc4e#\ud83d\udc4f#\ud83d\udc50#\ud83d\udc51#"
				+ "\ud83d\udc52#\ud83d\udc53#\ud83d\udc54#\ud83d\udc55#\ud83d\udc56#\ud83d\udc57#\ud83d\udc58#\ud83d\udc59#"
				+ "\ud83d\udc5a#\ud83d\udc5b#\ud83d\udc5c#\ud83d\udc5d#\ud83d\udc5e#\ud83d\udc5f#\ud83d\udc60#\ud83d\udc61#"
				+ "\ud83d\udc62#\ud83d\udc63#\ud83d\udc64#\ud83d\udc65#\ud83d\udc66#\ud83d\udc67#\ud83d\udc68#\ud83d\udc69#"
				+ "\ud83d\udc6a#\ud83d\udc6b#\ud83d\udc6c#\ud83d\udc6d#\ud83d\udc6e#\ud83d\udc6f#\ud83d\udc70#\ud83d\udc71#"
				+ "\ud83d\udc72#\ud83d\udc73#\ud83d\udc74#\ud83d\udc75#\ud83d\udc76#\ud83d\udc77#\ud83d\udc78#\ud83d\udc79#"
				+ "\ud83d\udc7b#\ud83d\udc7c#\ud83d\udc7d#\ud83d\udc7e#\ud83d\udc7f#\ud83d\udc80#\ud83d\udc81#\ud83d\udc82#"
				+ "\ud83d\udc84#\ud83d\udc85#\ud83d\udc86#\ud83d\udc87#\ud83d\udc88#\ud83d\udc89#\ud83d\udc8a#\ud83d\udc8b#"
				+ "\ud83d\udc8c#\ud83d\udc8d#\ud83d\udc8e#\ud83d\udc8f#\ud83d\udc90#\ud83d\udc91#\ud83d\udc92#\ud83d\udc93#"
				+ "\ud83d\udc94#\ud83d\udc95#\ud83d\udc96#\ud83d\udc97#\ud83d\udc98#\ud83d\udc99#\ud83d\udc9a#\ud83d\udc9b#"
				+ "\ud83d\udc9c#\ud83d\udc9d#\ud83d\udc9e#\ud83d\udc9f#\ud83d\udca0#\ud83d\udca1#\ud83d\udca2#\ud83d\udca3#"
				+ "\ud83d\udca4#\ud83d\udca5#\ud83d\udca6#\ud83d\udca7#\ud83d\udca8#\ud83d\udca9#\ud83d\udcaa#\ud83d\udcab#"
				+ "\ud83d\udcac#\ud83d\udcad#\ud83d\udcae#\ud83d\udcaf#\ud83d\udcb0#\ud83d\udcb1#\ud83d\udcb2#\ud83d\udcb3#"
				+ "\ud83d\udcb4#\ud83d\udcb5#\ud83d\udcb6#\ud83d\udcb7#\ud83d\udcb8#\ud83d\udcb9#\ud83d\udcba#\ud83d\udcbb#"
				+ "\ud83d\udcbc#\ud83d\udcbd#\ud83d\udcbe#\ud83d\udcbf#\ud83d\udcc0#\ud83d\udcc1#\ud83d\udcc2#\ud83d\udcc3#"
				+ "\ud83d\udcc4#\ud83d\udcc5#\ud83d\udcc6#\ud83d\udcc7#\ud83d\udcc8#\ud83d\udcc9#\ud83d\udcca#\ud83d\udccb#"
				+ "\ud83d\udccc#\ud83d\udccd#\ud83d\udcce#\ud83d\udccf#\ud83d\udcd0#\ud83d\udcd1#\ud83d\udcd2#\ud83d\udcd3#"
				+ "\ud83d\udcd4#\ud83d\udcd5#\ud83d\udcd6#\ud83d\udcd7#\ud83d\udcd8#\ud83d\udcd9#\ud83d\udcda#\ud83d\udcdb#"
				+ "\ud83d\udcdc#\ud83d\udcdd#\ud83d\udcde#\ud83d\udcdf#\ud83d\udce0#\ud83d\udce1#\ud83d\udce2#\ud83d\udce3#"
				+ "\ud83d\udce4#\ud83d\udce5#\ud83d\udce6#\ud83d\udce7#\ud83d\udce8#\ud83d\udce9#\ud83d\udcea#\ud83d\udceb#"
				+ "\ud83d\udcec#\ud83d\udced#\ud83d\udcee#\ud83d\udcef#\ud83d\udcf0#\ud83d\udcf1#\ud83d\udcf2#\ud83d\udcf3#"
				+ "\ud83d\udcf4#\ud83d\udcf5#\ud83d\udcf6#\ud83d\udcf7#\ud83d\udcf9#\ud83d\udcfa#\ud83d\udcfb#\ud83d\udcfc#"
				+ "\ud83d\udd00#\ud83d\udd01#\ud83d\udd02#\ud83d\udd03#\ud83d\udd04#\ud83d\udd05#\ud83d\udd06#\ud83d\udd07#"
				+ "\ud83d\udd08#\ud83d\udd09#\ud83d\udd0a#\ud83d\udd0b#\ud83d\udd0c#\ud83d\udd0d#\ud83d\udd0e#\ud83d\udd0f#"
				+ "\ud83d\udd10#\ud83d\udd11#\ud83d\udd12#\ud83d\udd13#\ud83d\udd14#\ud83d\udd15#\ud83d\udd16#\ud83d\udd17#"
				+ "\ud83d\udd18#\ud83d\udd19#\ud83d\udd1a#\ud83d\udd1b#\ud83d\udd1c#\ud83d\udd1d#\ud83d\udd1e#\ud83d\udd1f#"
				+ "\ud83d\udd20#\ud83d\udd21#\ud83d\udd22#\ud83d\udd23#\ud83d\udd24#\ud83d\udd25#\ud83d\udd26#\ud83d\udd27#"
				+ "\ud83d\udd28#\ud83d\udd29#\ud83d\udd2a#\ud83d\udd2b#\ud83d\udd2c#\ud83d\udd2d#\ud83d\udd2e#\ud83d\udd2f#"
				+ "\ud83d\udd30#\ud83d\udd31#\ud83d\udd32#\ud83d\udd33#\ud83d\udd34#\ud83d\udd35#\ud83d\udd36#\ud83d\udd37#"
				+ "\ud83d\udd38#\ud83d\udd39#\ud83d\udd3a#\ud83d\udd3b#\ud83d\udd3c#\ud83d\udd3d#\ud83d\udd50#\ud83d\udd51#"
				+ "\ud83d\udd52#\ud83d\udd53#\ud83d\udd54#\ud83d\udd55#\ud83d\udd56#\ud83d\udd57#\ud83d\udd58#\ud83d\udd59#"
				+ "\ud83d\udd5a#\ud83d\udd5b#\ud83d\udd5c#\ud83d\udd5d#\ud83d\udd5e#\ud83d\udd5f#\ud83d\udd60#\ud83d\udd61#"
				+ "\ud83d\udd62#\ud83d\udd63#\ud83d\udd64#\ud83d\udd65#\ud83d\udd66#\ud83d\udd67#\ud83d\uddfb#\ud83d\uddfc#"
				+ "\ud83d\uddfd#\ud83d\uddfe#\ud83d\uddff#\ud83d\ude00#\ud83d\ude01#\ud83d\ude02#\ud83d\ude03#\ud83d\ude04#"
				+ "\ud83d\ude05#\ud83d\ude06#\ud83d\ude07#\ud83d\ude08#\ud83d\ude09#\ud83d\ude0a#\ud83d\ude0b#\ud83d\ude0c#"
				+ "\ud83d\ude0d#\ud83d\ude0e#\ud83d\ude0f#\ud83d\ude10#\ud83d\ude11#\ud83d\ude12#\ud83d\ude13#\ud83d\ude14#"
				+ "\ud83d\ude15#\ud83d\ude16#\ud83d\ude17#\ud83d\ude18#\ud83d\ude19#\ud83d\ude1a#\ud83d\ude1b#\ud83d\ude1c#"
				+ "\ud83d\ude1d#\ud83d\ude1e#\ud83d\ude1f#\ud83d\ude20#\ud83d\ude21#\ud83d\ude22#\ud83d\ude23#\ud83d\ude24#"
				+ "\ud83d\ude25#\ud83d\ude26#\ud83d\ude27#\ud83d\ude28#\ud83d\ude29#\ud83d\ude2a#\ud83d\ude2b#\ud83d\ude2c#"
				+ "\ud83d\ude2d#\ud83d\ude2e#\ud83d\ude2f#\ud83d\ude30#\ud83d\ude31#\ud83d\ude32#\ud83d\ude33#\ud83d\ude34#"
				+ "\ud83d\ude35#\ud83d\ude36#\ud83d\ude37#\ud83d\ude38#\ud83d\ude39#\ud83d\ude3a#\ud83d\ude3b#\ud83d\ude3c#"
				+ "\ud83d\ude3d#\ud83d\ude3e#\ud83d\ude3f#\ud83d\ude40#\ud83d\ude45#\ud83d\ude46#\ud83d\ude47#\ud83d\ude48#"
				+ "\ud83d\ude49#\ud83d\ude4a#\ud83d\ude4b#\ud83d\ude4c#\ud83d\ude4d#\ud83d\ude4e#\ud83d\ude4f#\ud83d\ude80#"
				+ "\ud83d\ude81#\ud83d\ude82#\ud83d\ude83#\ud83d\ude84#\ud83d\ude85#\ud83d\ude86#\ud83d\ude87#\ud83d\ude88#"
				+ "\ud83d\ude89#\ud83d\ude8a#\ud83d\ude8b#\ud83d\ude8c#\ud83d\ude8d#\ud83d\ude8e#\ud83d\ude8f#\ud83d\ude90#"
				+ "\ud83d\ude91#\ud83d\ude92#\ud83d\ude93#\ud83d\ude94#\ud83d\ude95#\ud83d\ude96#\ud83d\ude97#\ud83d\ude98#"
				+ "\ud83d\ude99#\ud83d\ude9a#\ud83d\ude9b#\ud83d\ude9c#\ud83d\ude9d#\ud83d\ude9e#\ud83d\ude9f#\ud83d\udea0#"
				+ "\ud83d\udea1#\ud83d\udea2#\ud83d\udea3#\ud83d\udea4#\ud83d\udea5#\ud83d\udea6#\ud83d\udea7#\ud83d\udea8#"
				+ "\ud83d\udea9#\ud83d\udeaa#\ud83d\udeab#\ud83d\udeac#\ud83d\udead#\ud83d\udeae#\ud83d\udeaf#\ud83d\udeb0#"
				+ "\ud83d\udeb1#\ud83d\udeb2#\ud83d\udeb3#\ud83d\udeb4#\ud83d\udeb5#\ud83d\udeb6#\ud83d\udeb7#\ud83d\udeb8#"
				+ "\ud83d\udeb9#\ud83d\udeba#\ud83d\udebb#\ud83d\udebc#\ud83d\udebd#\ud83d\udebe#\ud83d\udebf#\ud83d\udec0#"
				+ "\ud83d\udec1#\ud83d\udec2#\ud83d\udec3#\ud83d\udec4#\ud83d\udec5#\u0023\u20e3#\u0030\u20e3#\u0031\u20e3#"
				+ "\u0032\u20e3#\u0033\u20e3#\u0034\u20e3#\u0035\u20e3#\u0036\u20e3#\u0037\u20e3#\u0038\u20e3#\u0039\u20e3#"
				+ "\u3030#\u2705#\u2728#\u2122#\u23e9#\u23ea#\u23eb#\u23ec#\u23f0#\u23f3#\u26ce#\u270a#\u270b#\u274c#\u274e#"
				+ "\u27b0#\u27bf#\u2753#\u2754#\u2755#\u2795#\u2796#\u2797#\u00a9#\u00ae#\ue50a#"
				+ "\ud83c\udd7f#\ud83c\ude1a#\ud83c\ude2f#\ud83c\udc04#\u2935#\u3297#\u3299#\u2049#\u2139#\u2194#\u2195#"
				+ "\u2196#\u2197#\u2198#\u2199#\u2600#\u2601#\u2611#\u2614#\u2615#\u2648#\u2649#\u2650#\u2651#\u2652#\u2653#"
				+ "\u2660#\u2663#\u2665#\u2666#\u2668#\u2693#\u2702#\u2708#\u2709#\u2712#\u2714#\u2716#\u2733#\u2734#\u203c#"
				+ "\u21a9#\u21aa#\u2744#\u231a#\u231b#\u24c2#\u25aa#\u25ab#\u25b6#\u25c0#\u25fb#\u25fc#\u25fd#\u25fe#\u260e#"
				+ "\u261d#\u263a#\u264a#\u264b#\u264c#\u264d#\u264e#\u264f#\u267b#\u267f#\u26a0#\u26a1#\u26aa#\u26ab#\u26bd#"
				+ "\u26be#\u26c4#\u26c5#\u26d4#\u26ea#\u26f2#\u26f3#\u26f5#\u26fa#\u26fd#\u270c#\u270f#\u27a1#\u2b05#\u2b06#"
				+ "\u2b07#\u2b1b#\u2b1c#\u2b50#\u2b55#\u2747#\u303d#\u2757#\u2764#\u2934#([\ufe0e\ufe0f]?)";
		return unicodeString.split("#");
	}
}