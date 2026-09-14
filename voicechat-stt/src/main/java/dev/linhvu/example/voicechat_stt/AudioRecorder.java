package dev.linhvu.example.voicechat_stt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import org.springframework.stereotype.Component;

@Component
public class AudioRecorder {

	private final AudioFormat format = new AudioFormat(16000.0f, 16, 1, true, false);

	private TargetDataLine microphone;
	private ByteArrayOutputStream pcmOutput;
	private Thread recordingThread;

	public void start() throws LineUnavailableException {
		this.microphone = AudioSystem.getTargetDataLine(format);
		this.microphone.open(format);
		this.microphone.start();

		this.pcmOutput = new ByteArrayOutputStream();

		this.recordingThread = Thread.startVirtualThread(() -> {
			byte[] buffer = new byte[4096];

			while (this.microphone.isOpen()) {
				int bytesRead = this.microphone.read(buffer, 0, buffer.length);
				if (bytesRead > 0) {
					pcmOutput.write(buffer, 0, bytesRead);
				}
			}
		});
	}

	public byte[] stop() throws InterruptedException, IOException {
		this.microphone.stop();
		this.microphone.close();
		recordingThread.join();

		byte[] pcmBytes = this.pcmOutput.toByteArray();
		int frameLength = pcmBytes.length / format.getFrameSize();

		try (
			ByteArrayInputStream pcmInput = new ByteArrayInputStream(pcmBytes);
			AudioInputStream audioStream = new AudioInputStream(pcmInput, format, frameLength);
			ByteArrayOutputStream wavOutput = new ByteArrayOutputStream();
		) {
			AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, wavOutput);

			return wavOutput.toByteArray();
		}
	}
}
