/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.application.texttospeech.texttospeech;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.texttospeech.v1beta1.AudioConfig;
import com.google.cloud.texttospeech.v1beta1.AudioEncoding;
import com.google.cloud.texttospeech.v1beta1.ListVoicesRequest;
import com.google.cloud.texttospeech.v1beta1.ListVoicesResponse;
import com.google.cloud.texttospeech.v1beta1.SsmlVoiceGender;
import com.google.cloud.texttospeech.v1beta1.SynthesisInput;
import com.google.cloud.texttospeech.v1beta1.SynthesizeSpeechResponse;
import com.google.cloud.texttospeech.v1beta1.TextToSpeechClient;
import com.google.cloud.texttospeech.v1beta1.TextToSpeechSettings;
import com.google.cloud.texttospeech.v1beta1.Voice;
import com.google.cloud.texttospeech.v1beta1.VoiceSelectionParams;
import com.google.protobuf.ByteString;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Kiezie
 */
public class Main {
    
    private static TextToSpeechSettings getSetting() throws FileNotFoundException, IOException {
        
        String jsonPath = "D:/Java/02-Java-Text-To-Speech/kiezie-projects.json";
        
        CredentialsProvider credentialsProvider = FixedCredentialsProvider.create(ServiceAccountCredentials.fromStream(new FileInputStream(jsonPath)));
        TextToSpeechSettings settings = TextToSpeechSettings.newBuilder().setCredentialsProvider(credentialsProvider).build();
         
        return settings;
        
    }
    
    
    /**
    * Demonstrates using the Text to Speech client to list the client's supported voices.
    *
    * @throws Exception on TextToSpeechClient Errors.
    */
   public static List<Voice> listAllSupportedVoices() throws Exception {
       
       TextToSpeechSettings settings = getSetting();
       
     // Instantiates a client
     try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create(settings)) {
       // Builds the text to speech list voices request
       ListVoicesRequest request = ListVoicesRequest.getDefaultInstance();

       // Performs the list voices request
       ListVoicesResponse response = textToSpeechClient.listVoices(request);
       List<Voice> voices = response.getVoicesList();

       for (Voice voice : voices) {
         // Display the voice's name. Example: tpc-vocoded
         System.out.format("Name: %s\n", voice.getName());

         // Display the supported language codes for this voice. Example: "en-us"
         List<ByteString> languageCodes = voice.getLanguageCodesList().asByteStringList();
         for (ByteString languageCode : languageCodes) {
           System.out.format("Supported Language: %s\n", languageCode.toStringUtf8());
         }

         // Display the SSML Voice Gender
         System.out.format("SSML Voice Gender: %s\n", voice.getSsmlGender());

         // Display the natural sample rate hertz for this voice. Example: 24000
         System.out.format("Natural Sample Rate Hertz: %s\n\n", voice.getNaturalSampleRateHertz());
       }
       return voices;
     }
   }
   
   /**
    * Demonstrates using the Text to Speech client to synthesize text or ssml.
    *
    * @param text the raw text to be synthesized. (e.g., "Hello there!")
    * @throws Exception on TextToSpeechClient Errors.
    */
   public static void synthesizeText(String text) throws Exception {
       
       TextToSpeechSettings setting = getSetting();
       
     // Instantiates a client
     try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create(setting)) {
       // Set the text input to be synthesized
       SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();

       // Build the voice request
       VoiceSelectionParams voice =
           VoiceSelectionParams.newBuilder()
               .setLanguageCode("id-ID") // languageCode = "en_us"
               .setSsmlGender(SsmlVoiceGender.MALE) // ssmlVoiceGender = SsmlVoiceGender.FEMALE
               .build();

       // Select the type of audio file you want returned
       AudioConfig audioConfig =
           AudioConfig.newBuilder()
               .setAudioEncoding(AudioEncoding.MP3) // MP3 audio.
               .build();

       // Perform the text-to-speech request
       SynthesizeSpeechResponse response =
           textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);

       // Get the audio contents from the response
       ByteString audioContents = response.getAudioContent();

       // Write the response to the output file.
       try (OutputStream out = new FileOutputStream("output_cerita_123.mp3")) {
         out.write(audioContents.toByteArray());
         System.out.println("Audio content written to file \"output.mp3\"");
       }
     }
   }
   
   /**
    * Demonstrates using the Text to Speech client to synthesize a text file or ssml file.
    *
    * @param ssmlFile the ssml document to be synthesized. (e.g., hello.ssml)
    * @throws Exception on TextToSpeechClient Errors.
    */
   public static ByteString synthesizeSsmlFile(String ssmlFile, String outputFileName) throws Exception {
       
       TextToSpeechSettings setting = getSetting();
     // Instantiates a client
     try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create(setting)) {
       // Read the file's contents
       String contents = new String(Files.readAllBytes(Paths.get(ssmlFile)));
       // Set the ssml input to be synthesized
       SynthesisInput input = SynthesisInput.newBuilder().setSsml(contents).build();

       // Build the voice request
       VoiceSelectionParams voice =
           VoiceSelectionParams.newBuilder()
               .setLanguageCode("id-ID") // languageCode = "en_us"
               .setName("id-ID-Wavenet-A")
               .setSsmlGender(SsmlVoiceGender.FEMALE) // ssmlVoiceGender = SsmlVoiceGender.FEMALE
               .build();

       // Select the type of audio file you want returned
       AudioConfig audioConfig =
           AudioConfig.newBuilder()
                   //.setPitch(5)
                   //.setSpeakingRate(0.75)
               .setAudioEncoding(AudioEncoding.MP3) // MP3 audio.
               .build();

       // Perform the text-to-speech request
       SynthesizeSpeechResponse response =
           textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);

       // Get the audio contents from the response
       ByteString audioContents = response.getAudioContent();

       // Write the response to the output file.
       try (OutputStream out = new FileOutputStream(outputFileName)) {
         out.write(audioContents.toByteArray());
         System.out.println("Audio content written to file \""+outputFileName+"\"");
         return audioContents;
        }
        }
    }
   
   public static void main(String[] args) throws Exception {
       //listAllSupportedVoices();
       //synthesizeText("Halo Semuanya, Apa kabar? Semoga Sehat selalu. Amin.");
       String SourceFileName = "ssmltext.txt"; 
       
       SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss"); 
       String timestamp = dateFormat.format(new Date()); 
  
       // Create the file name with the timestamp 
       String fileName = SourceFileName.replaceFirst("[.][^.]+$", "") + "_" + timestamp + ".mp3"; 
  
       synthesizeSsmlFile("D:/Java/02-Java-Text-To-Speech/sources/"+SourceFileName, "outputs/" + fileName);
   }
}
