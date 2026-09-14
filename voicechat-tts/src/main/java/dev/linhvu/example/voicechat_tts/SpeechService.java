package dev.linhvu.example.voicechat_tts;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.ai.audio.tts.TextToSpeechOptions;
import org.springframework.ai.audio.tts.TextToSpeechPrompt;
import org.springframework.ai.audio.tts.TextToSpeechResponse;
import org.springframework.stereotype.Component;

@Component
public class SpeechService {

	private static final Logger log = LoggerFactory.getLogger(SpeechService.class);

	private final TextToSpeechModel textToSpeechModel;

	public SpeechService(TextToSpeechModel textToSpeechModel) {
		this.textToSpeechModel = textToSpeechModel;
	}

	public void speak(String text) {
		TextToSpeechPrompt prompt = new TextToSpeechPrompt(
				text,
				TextToSpeechOptions.builder()
						.voice("nova")
						.build()
		);

		TextToSpeechResponse response = this.textToSpeechModel.call(prompt);
		byte[] audioBytes = response.getResult().getOutput();

		try (ByteArrayInputStream in = new ByteArrayInputStream(audioBytes)) {
			// Use this line if you'd rather write the response to an MP3 file:
			// Files.write(Path.of("/Users/habuma/audio-response.mp3"), audioBytes);
			new Player(in).play();
		}
		catch (IOException | JavaLayerException e) {
			log.error("Unable to speak: {}", e.getMessage());
		}
	}
}
