package dev.linhvu.example.voicechat_stt;

import org.jspecify.annotations.Nullable;

import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

@Service
public class Transcriber {

	private final TranscriptionModel transcriptionModel;

	public Transcriber(TranscriptionModel transcriptionModel) {
		this.transcriptionModel = transcriptionModel;
	}

	public String transcribe(byte[] audio) {
		ByteArrayResource audioResource = new ByteArrayResource(audio) {
			@Override
			public @Nullable String getFilename() {
				return "audio.wav";
			}
		};

		return this.transcriptionModel.transcribe(audioResource);
	}
}
