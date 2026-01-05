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
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 *
 * @author Kiezie
 */
public class Main {
    
    public static void playSound(ByteString audioBytes) {
        try {
            // 1. Convert ByteString to a byte array
            byte[] byteArray = audioBytes.toByteArray();
            
            // 2. Create an InputStream from the byte array
            InputStream inputStream = new ByteArrayInputStream(byteArray);
            
            // 3. Get an AudioInputStream
            // The AudioSystem.getAudioInputStream() method automatically detects the format 
            // if the byte array contains valid audio file headers (e.g., a WAV file format).
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(inputStream);
            
            // 4. Get a Clip resource
            Clip clip = AudioSystem.getClip();
            
            // 5. Open the audio input stream and start playback
            clip.open(audioInputStream);
            clip.start();
            
            // Optional: Add a listener to ensure the program doesn't exit before sound finishes
            clip.addLineListener(new LineListener() {
                public void update(LineEvent event) {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                }
            });

        } catch (UnsupportedAudioFileException | LineUnavailableException | java.io.IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error playing sound: " + e.getMessage(), "Sound Playback Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private static TextToSpeechSettings getSetting() throws FileNotFoundException, IOException {
        String jsonPath = "D:/Java/kiezie-projects.json";   
        CredentialsProvider credentialsProvider = FixedCredentialsProvider.create(ServiceAccountCredentials.fromStream(new FileInputStream(jsonPath)));
        TextToSpeechSettings settings = TextToSpeechSettings.newBuilder().setCredentialsProvider(credentialsProvider).build();
         
        return settings;
    }
    
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
    
    static ByteString synthesizeSsmlFromText(String text) throws Exception {
    
        TextToSpeechSettings setting = getSetting();
       
        // Instantiates a client
        try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create(setting)) {
            // Set the text input to be synthesized
            SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();

            // Build the voice request
            VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                .setLanguageCode("id-ID") // languageCode = "en_us"
                .setName("id-ID-Standard-B")
                .setSsmlGender(SsmlVoiceGender.MALE) // ssmlVoiceGender = SsmlVoiceGender.FEMALE
                .build();

            // Select the type of audio file you want returned
            AudioConfig audioConfig = AudioConfig.newBuilder()
               .setAudioEncoding(AudioEncoding.MP3) // MP3 audio.
               .build();

            // Perform the text-to-speech request
            SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);

            // Get the audio contents from the response
            ByteString audioContents = response.getAudioContent();

            //Write the response to the output file.
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss"); 
            String timestamp = dateFormat.format(new Date()); 
  
            // Create the file name with the timestamp 
            String fileName = "output_" + timestamp + ".mp3"; 
        
            try (OutputStream out = new FileOutputStream("outputs/" + fileName)) {
                out.write(audioContents.toByteArray());
                System.out.println("Audio content written to file \""+fileName+"\"");
                
                return audioContents;
            }
        }
    }
    
    static ByteString synthesizeSsmlFromFile(String ssmlFile) throws Exception {
    
        String pathSourceFile = "D:/Java/02-Java-Text-To-Speech/sources/";
        TextToSpeechSettings setting = getSetting();
        
        // Instantiates a client
        try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create(setting)) {
            // Read the file's contents
            String contents = new String(Files.readAllBytes(Paths.get(pathSourceFile + ssmlFile)));
            // Set the ssml input to be synthesized
            SynthesisInput input = SynthesisInput.newBuilder().setSsml(contents).build();

            // Build the voice request
            VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                .setLanguageCode("id-ID") // languageCode = "en_us"
                .setName("id-ID-Standard-B")
                .setSsmlGender(SsmlVoiceGender.MALE) // ssmlVoiceGender = SsmlVoiceGender.FEMALE
                .build();

            // Select the type of audio file you want returned
            AudioConfig audioConfig = AudioConfig.newBuilder()
                //.setPitch(0.5)
                //.setSpeakingRate(1)
                .setAudioEncoding(AudioEncoding.MP3) // MP3 audio.
                .build();

            // Perform the text-to-speech request
            SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);

            // Get the audio contents from the response
            ByteString audioContents = response.getAudioContent();

            // Write the response to the output file.
            // Create the file name with the timestamp 
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss"); 
            String timestamp = dateFormat.format(new Date()); 
            String fileName = "output_" + timestamp + ".mp3"; 
            try (OutputStream out = new FileOutputStream("outputs/" + fileName)) {
                out.write(audioContents.toByteArray());
                System.out.println("Audio content written to file \""+fileName+"\"");
            }
            
            return audioContents;
        }
    }
   
   public static void main(String[] args) throws Exception {
       //listAllSupportedVoices();
       
       
       ByteString mySoundData = synthesizeSsmlFromText("Halo Semuanya, Apa kabar? Semoga Sehat selalu. Amin.");
       
       //String SourceFileName = "testing.txt"; 
       //synthesizeSsmlFromFile(SourceFileName);
   
        SwingUtilities.invokeLater(() -> {
            playSound(mySoundData);
            // In a real Swing app, the main thread would keep running.
            // For a simple console app, you might need to wait for the clip to finish.
        });
   }
}
