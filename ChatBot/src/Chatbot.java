import com.google.api.gax.rpc.ClientStream;
import com.google.api.gax.rpc.ResponseObserver;
import com.google.api.gax.rpc.StreamController;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.StreamingRecognitionConfig;
import com.google.cloud.speech.v1.StreamingRecognitionResult;
import com.google.cloud.speech.v1.StreamingRecognizeRequest;
import com.google.cloud.speech.v1.StreamingRecognizeResponse;
import com.google.cloud.texttospeech.v1.AudioConfig;
import com.google.cloud.texttospeech.v1.AudioEncoding;
import com.google.cloud.texttospeech.v1.SsmlVoiceGender;
import com.google.cloud.texttospeech.v1.SynthesisInput;
import com.google.cloud.texttospeech.v1.SynthesizeSpeechResponse;
import com.google.cloud.texttospeech.v1.TextToSpeechClient;
import com.google.cloud.texttospeech.v1.VoiceSelectionParams;
import com.google.protobuf.ByteString;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.DataLine.Info;
import javax.sound.sampled.TargetDataLine;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.json.JSONArray;
import org.json.JSONObject;

import static javax.sound.sampled.AudioSystem.getAudioInputStream;
import static javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED;
/**
 * This app will eventually be a personal assistant.
 * @author Todd Sandberg
 * @version 12/27/2018
 */
public class Chatbot {
	//Customizable variables
	//Name the bot will recognize to activate
	static String bot_name = "Kara";
	//Name of the user
	static String user_name = "Todd";
	//Current city
	static String city = "leesburg";
	//Current country code
	static String country_code = "840";
	
	
	
	//Variables used by program
	static boolean started = false;
	static String log = "";
	static String date= "";
	static int day = -1;
	static boolean firstStartup = false;
	static Calendar cal = Calendar.getInstance();
	static boolean bedtime = false;
	static boolean logging = false;
	public static void main(String [] args) {
		try {
			//playString("Hello "+user_name+", how are you this morning!");
			streamingMicRecognize();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** Performs microphone streaming speech recognition with a duration of 1 minute. */
	public static void streamingMicRecognize() throws Exception {

	  ResponseObserver<StreamingRecognizeResponse> responseObserver = null;
	  try (SpeechClient client = SpeechClient.create()) {

	    responseObserver =
	        new ResponseObserver<StreamingRecognizeResponse>() {
	          ArrayList<StreamingRecognizeResponse> responses = new ArrayList<>();

	          public void onStart(StreamController controller) {
	        	  System.out.println("Started");
	          }

	          public void onResponse(StreamingRecognizeResponse response) {
	        	  String result = response.getResults(0).getAlternatives(0).getTranscript().trim();
	        	  System.out.println(result);
	        	  if(result.equals("hey "+bot_name) || result.equals("hello")) {
	        		  DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy.MM.dd");  
	        		  LocalDateTime now = LocalDateTime.now();
	        		  date = dtf.format(now);
	        		  if(day!=now.getDayOfYear()) {
	        			  firstStartup = true;
	        			  day = now.getDayOfYear();
	        		  }
	        		  String greeting;
	      	        	SimpleDateFormat sdf = new SimpleDateFormat("HH");
	      	        	String hourstring = sdf.format(cal.getTime());
	      	        	int hour = Integer.parseInt(hourstring);
	      	        	if(hour<11 && hour>2) {
	      	        		greeting = "morning";
	      	        	}
	      	        	else if(hour>=11 && hour<17) {
	      	        		greeting = "afternoon";
	      	        	}
	      	        	else {
	      	        		greeting = "evening";
	      	        	}
	        		  //playString("Hello "+user_name+", how are you this morning!");
	      	        	if (firstStartup) {
	      	        		playString("Good "+greeting+" "+user_name+"!");
	      	        		//TODO weather
	      	        		getWeather();
	      	        		//TODO news debrief?
	      	        		//TODO get calendar
	      	        		playString("Anything else I can do for you?");
	      	        	}
	      	        	else {
	      	        		playString("Good "+greeting+" "+user_name+"! What can I do for you?");
	      	        	}
	        		  started =true;
	        	  }
	        	  else if(bedtime && (result.equals("yes") || result.equals("yep") || result.equals("yup"))) {
	        		  //TODO set an alarm
	        		  playString("Great! Good night "+user_name+"!");
	        		  started=false;
	        		  bedtime=false;
	        	  }
	        	  else if(bedtime && (result.equals("no") || result.equals("nope"))) {
	        		  playString("Ah sleeping in I see. Well good night "+user_name+"!");
	        		  started=false;
	        		  bedtime=false;
	        	  }
	        	  else if(logging && (result.endsWith("thats it") || result.endsWith("that's it"))) {
	        		  playString("Thanks for telling me. Saving to log now.");
	        		  log += result;
	        		  try {
	        			  DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy.MM.dd");  
		        		  LocalDateTime now = LocalDateTime.now();
		        		  File f = new File(dtf.format(now)+".txt");
	        			  PrintWriter pw = new PrintWriter(new FileOutputStream(f,true));
	        			  pw.write(log+"\n");
	        			  log = "";
	        			  pw.close();
	        			  logging = false;
	        			  playString("All set.");
	        		  }catch(Exception e) {
	        			  e.printStackTrace();
	        			  logging = false;
	        			  playString("Something went wrong I'm sorry.");
	        		  }
	        	  }
	        	  else if(logging) {
	        		  log += result+"\n";
	        	  }
	        	  else if(started && result.equals("play today's log")) {
	        		  try {
		        		  File f = new File(date+".txt");
		        		  if(f.exists() && !f.isDirectory()) { 
		        			  String s = "Sure thing!";
			        		  Scanner scan = new Scanner(f);
			        		  while(scan.hasNextLine()) {
			        			  s+=". "+scan.nextLine();
			        		  }
			        		  scan.close();
			        		  playString(s);
		        		  }
	        		  }catch(Exception e) {
	        			  e.printStackTrace();
	        			  playString("Something went wrong I'm sorry.");
	        		  }
	        	  }
	        	  else if(started && (result.equals("take a log") || result.equals("start log"))) {
	        		  logging = true;
	        		  playString("Talk away! Say that's it when youre done.");
	        	  }
	        	  else if(started && result.equals("goodbye")) {
	        		  started=false;
	        		  playString("See you later "+user_name);
	        	  }
	        	  else if(started && (result.equals("I'm headed to bed") || result.equals("I'm going to bed"))) {
	        		  SimpleDateFormat sdf = new SimpleDateFormat("HH");
	      	        String hourstring = sdf.format(cal.getTime());
	      	        	int hour = Integer.parseInt(hourstring) + 8;
	      	        	String formattedtime;
	      	        	if(hour>12) {
	      	        		hour = hour-12;
	      	        		formattedtime = hour+":00 PM";
	      	        	}
	      	        	else {
	      	        		formattedtime = hour+":00 AM";
	      	        	}
	        		  playString("Alright! I'll set an alarm for " +formattedtime+". Sound good?");
	        		  bedtime = true;
	        	  }
	        	  responses.add(response);
	          }

	          public void onComplete() {
	            for (StreamingRecognizeResponse response : responses) {
	              StreamingRecognitionResult result = response.getResultsList().get(0);
	   
	              com.google.cloud.speech.v1.SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
	              System.out.printf("Transcript : %s\n", alternative.getTranscript());
	            }
	          }

	          public void onError(Throwable t) {
	            System.out.println(t);
	          }
	        };

	    ClientStream<StreamingRecognizeRequest> clientStream =
	        client.streamingRecognizeCallable().splitCall(responseObserver);

	    RecognitionConfig recognitionConfig =
	        RecognitionConfig.newBuilder()
	            .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
	            .setLanguageCode("en-US")
	            .setSampleRateHertz(16000)
	            .build();
	    StreamingRecognitionConfig streamingRecognitionConfig =
	        StreamingRecognitionConfig.newBuilder().setConfig(recognitionConfig).build();

	    StreamingRecognizeRequest request =
	        StreamingRecognizeRequest.newBuilder()
	            .setStreamingConfig(streamingRecognitionConfig)
	            .build(); // The first request in a streaming call has to be a config

	    clientStream.send(request);
	    // SampleRate:16000Hz, SampleSizeInBits: 16, Number of channels: 1, Signed: true,
	    // bigEndian: false
	    AudioFormat audioFormat = new AudioFormat(16000, 16, 1, true, false);
	    DataLine.Info targetInfo =
	        new Info(
	            TargetDataLine.class,
	            audioFormat); // Set the system information to read from the microphone audio stream

	    if (!AudioSystem.isLineSupported(targetInfo)) {
	      System.out.println("Microphone not supported");
	      System.exit(0);
	    }
	    // Target data line captures the audio stream the microphone produces.
	    TargetDataLine targetDataLine = (TargetDataLine) AudioSystem.getLine(targetInfo);
	    targetDataLine.open(audioFormat);
	    targetDataLine.start();
	    System.out.println("Start speaking");
	    long startTime = System.currentTimeMillis();
	    // Audio Input Stream
	    AudioInputStream audio = new AudioInputStream(targetDataLine);
	    while (true) {
	      long estimatedTime = System.currentTimeMillis() - startTime;
	      byte[] data = new byte[6400];
	      audio.read(data);
	      /*if (estimatedTime > 6000) { // 60 seconds
	        System.out.println("Stop speaking.");
	        targetDataLine.stop();
	        targetDataLine.close();
	        break;
	      }*/
	      request =
	          StreamingRecognizeRequest.newBuilder()
	              .setAudioContent(ByteString.copyFrom(data))
	              .build();
	      clientStream.send(request);
	    }
	  } catch (Exception e) {
	    System.out.println(e);
	  }
	  responseObserver.onComplete();
	}
	
	public static void getWeather() {
		try {
  			URL url = new URL("http://api.openweathermap.org/data/2.5/weather?q="+city+","+country_code+"&appid=f64957722661272de76a751839f0e552");
  			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
  			conn.setRequestMethod("GET");
  			conn.connect();
  			int responsecode = conn.getResponseCode();
  			if(responsecode != 200)
  				throw new RuntimeException("HttpResponseCode: " +responsecode);
  			else
  			{
  				 BufferedReader in = new BufferedReader(
  			             new InputStreamReader(conn.getInputStream()));
  			     String inputLine;
  			     StringBuffer weatherresponse = new StringBuffer();
  			     while ((inputLine = in.readLine()) != null) {
  			     	weatherresponse.append(inputLine);
  			     }
  			     in.close();
  			     JSONObject myResponse = new JSONObject(weatherresponse.toString());
  			     String weather="";
  			     JSONArray weatherarray = myResponse.getJSONArray("weather");
  			     for(int x=0;x<weatherarray.length();x++) {
  			    	 JSONObject thisweather = weatherarray.getJSONObject(x);
  			    	 String mainweather = thisweather.getString("main");
  			    	 if (weather.length()>0) {
  			    		 weather += " and "+mainweather;
  			    	 }
  			    	 else {
  			    		 weather+=mainweather;
  			    	 }
  			     }
  			     JSONObject main = myResponse.getJSONObject("main");
  			     String temp="";
  			     double faren = main.getDouble("temp");
  			     faren = ((9/5)*(faren - 273)) + 32;
  			     faren = Math.round(faren * 100.0) / 100.0;
  			     temp = ""+faren;
  			   playString("It looks like there's going to be "+weather+" today with a temperature of "+temp+" degrees.");
  			}
  		}catch(Exception e) {
  			e.printStackTrace();
  			playString("Could not get weather data.");
  		}
	}
	
	public static void playString(String s) {
		try {
			
		      // Set the text input to be synthesized;
			TextToSpeechClient textToSpeechClient = TextToSpeechClient.create();
		      SynthesisInput input = SynthesisInput.newBuilder()
		            .setText(s)
		            .build();

		      // Build the voice request, select the language code ("en-US") and the ssml voice gender
		      // ("neutral")
		      //japan voice
		      /*VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
		          .setLanguageCode("ja-JP")
		          .setSsmlGender(SsmlVoiceGender.FEMALE).setName("ja-JP-Wavenet-A")
		          .build();*/
		      //gb voice
		      VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
			          .setLanguageCode("en-GB")
			          .setSsmlGender(SsmlVoiceGender.FEMALE).setName("en-GB-Wavenet-A")
			          .build();
		      // Select the type of audio file you want returned
		      AudioConfig audioConfig = AudioConfig.newBuilder()
		          .setAudioEncoding(AudioEncoding.MP3)
		          .build();
		      // Perform the text-to-speech request on the text input with the selected voice parameters and
		      // audio file type
		      SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);
		      // Get the audio contents from the response
		      ByteString audioContents = response.getAudioContent();
		      // Write the response to the output file.
		      try (OutputStream out = new FileOutputStream("output.mp3")) {
		        out.write(audioContents.toByteArray());
		        System.out.println("Audio content written to file \"output.mp3\"");
		      }
		      play("output.mp3");
		    }
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	 public static void play(String filePath) {
	        final File file = new File(filePath);
	        try (final AudioInputStream in = getAudioInputStream(file)) {
	            final AudioFormat outFormat = getOutFormat(in.getFormat());
	            final Info info = new Info(SourceDataLine.class, outFormat);
	            try (final SourceDataLine line =
	                     (SourceDataLine) AudioSystem.getLine(info)) {
	                if (line != null) {
	                    line.open(outFormat);
	                    line.start();
	                    stream(getAudioInputStream(outFormat, in), line);
	                    line.drain();
	                    line.stop();
	                }
	            }
	        } catch (UnsupportedAudioFileException 
	               | LineUnavailableException 
	               | IOException e) {
	            throw new IllegalStateException(e);
	        }
	    }
	 private static AudioFormat getOutFormat(AudioFormat inFormat) {
	        final int ch = inFormat.getChannels();

	        final float rate = inFormat.getSampleRate();
	        return new AudioFormat(PCM_SIGNED, rate, 16, ch, ch * 2, rate, false);
	    }

	    private static void stream(AudioInputStream in, SourceDataLine line) 
	        throws IOException {
	        final byte[] buffer = new byte[4096];
	        for (int n = 0; n != -1; n = in.read(buffer, 0, buffer.length)) {
	            line.write(buffer, 0, n);
	        }
	    }
}

