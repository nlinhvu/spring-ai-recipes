package dev.linhvu.example;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.ai.audio.tts.TextToSpeechPrompt;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.content.Media;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.openai.OpenAiAudioSpeechOptions;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.util.MimeTypeUtils;

/**
 * One chat call with audio in both directions: a spoken question goes in as
 * {@code input_audio}, and the model answers with text and speech ({@code modalities:
 * ["text", "audio"]}). The question is synthesized first (so the repo holds no audio file),
 * and both clips are written to {@code target/} to compare with what the inspector plays.
 */
@SpringBootApplication
public class Application {

	private static final Logger log = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public ApplicationRunner runner(TextToSpeechModel speech, ChatClient.Builder chatClientBuilder) {
		return args -> {

			Path target = Files.createDirectories(Path.of("target"));

			// The spoken question, made by a speech model: audio the chat model has to listen to.
			byte[] question = speech.call(new TextToSpeechPrompt("What is the capital of Belgium, and what is it famous for?",
					OpenAiAudioSpeechOptions.builder().voice("nova").responseFormat("wav").build())).getResult().getOutput();
			Files.write(target.resolve("question.wav"), question);

			// Audio in, text and audio out, in one Chat Completions call.
			ChatResponse response = chatClientBuilder.build()
					.prompt()
					.user(u -> u.text("Answer the spoken question in two sentences.")
							.media(MimeTypeUtils.parseMimeType("audio/wav"), new ByteArrayResource(question)))
					.options(OpenAiChatOptions.builder() // the spec takes the options builder
							.model("gpt-audio-1.5")
							.outputModalities(List.of("text", "audio"))
							.outputAudio(new OpenAiChatOptions.AudioParameters(OpenAiChatOptions.AudioParameters.Voice.ALLOY, OpenAiChatOptions.AudioParameters.AudioResponseFormat.WAV)))
					.call()
					.chatResponse();

			AssistantMessage answer = response.getResult().getOutput();
			System.out.println("\ntext: " + answer.getText());
			for (Media media : answer.getMedia()) {
				byte[] audio = bytes(media);
				Path file = target.resolve("answer.wav");
				Files.write(file, audio);
				System.out.println("audio: " + media.getMimeType() + " · " + audio.length + " bytes written to " + file.toAbsolutePath());
			}
		};
	}

	private static byte[] bytes(Media media) throws Exception {
		Object data = media.getData();
		if (data instanceof byte[] b) {
			return b;
		}
		if (data instanceof Resource r) {
			return r.getContentAsByteArray();
		}
		throw new IllegalStateException("unexpected media data: " + (data == null ? null : data.getClass()));
	}
}
