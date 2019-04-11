# Personal_Assistant WIP

A personal assistant similar to Amazon Alexa, but I am slowly adding features that I wish were in other assistants.

In order to use the news feature you must create the file Credentials.java with the following code:
```public class Credentials {
	public static final String GOOGLE_EMAIL = "INSERT_EMAIL_HERE";
	public static final String GOOGLE_PASSWORD = "INSERT_PASSWORD_HERE";
}
```

You will also need to download the chrome driver from http://chromedriver.chromium.org/ and edit the path in the settings class (WILL CHANGE THIS IN THE FUTURE)

To start you should edit the settings class to personalize it for yourself.

## Current State:

Responds to hello with the users name and a greeting appropriate to the time of day. If its the first start up of the day it gives the weather. Lets you navigate through personalized google news.

### Current commands:
- "take a log": starts a log entry for the current day and saves to a text file when you say "that's it".
- "play today's log": play whatever has been recorded today for the log.
- "goodbye": stops listening.
- "i'm going to bed": sets an alarm if you want and stops listening. (Does not actually set an alarm yet)
- "news": Fetches a list of articles from https://news.google.com/foryou.
- "next": Goes to next article in news list if a list has been fetched.
- "select": plays article contents if it can fetch it.

## Goals:

- Daily log that can later be analyzed to find cause effect relationships for habits and moods
- Calendar (Upcoming Events, Take into account events in logs)
