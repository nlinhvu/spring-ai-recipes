# Spring AI Recipes

Small, focused Spring AI examples for experimenting with chat clients, tools, memory, retrieval, audio, guardrails, and agent-oriented workflows.

Each recipe is a standalone Gradle project. The examples are intentionally independent so you can open one directory, run it, and inspect the smallest useful implementation.

## Requirements

- Java 25 (the projects use the Gradle Java toolchain)
- An API key for the model provider used by the recipe
- Docker, for the Redis and Qdrant recipes
- A local Ollama installation, for recipes that use Ollama

The examples currently use Spring Boot 4.1.1 and Spring AI 2.0.1. Dependencies are downloaded from Maven Central by each recipe's Gradle wrapper.

## Quick start

Choose a recipe, enter its directory, and run it with the checked-in wrapper:

```bash
cd simple-memory
export GEMINI_API_KEY=your-api-key
./gradlew bootRun
```

Run a recipe's tests with:

```bash
./gradlew test
```

On Windows, use `gradlew.bat` instead of `./gradlew`.

## Recipes

|  # | Recipe                                                         | Focus | Provider or service |
|---:|----------------------------------------------------------------| --- | --- |
|  1 | [`logging-openai`](logging-openai)                             | Log chat traffic through the OpenAI client | Google Gemini via OpenAI-compatible API |
|  2 | [`logging-googleai`](logging-googleai)                         | Log Google GenAI chat traffic | Google Gemini |
|  3 | [`tool-use`](tool-use)                                         | Basic tool calling | Google Gemini |
|  4 | [`simple-memory`](simple-memory)                               | Conversation memory | Google Gemini |
|  5 | [`ask-user-question`](ask-user-question)                       | Ask the user for missing information | Google Gemini via OpenAI-compatible API |
|  6 | [`todo-write-tool`](todo-write-tool)                           | Todo-writing tool workflow | Google Gemini |
|  7 | [`skill`](skill)                                               | Agent utility skills | Google Gemini via OpenAI-compatible API |
|  8 | [`skillsjars`](skillsjars)                                     | Skills packaged as JAR dependencies | Google Gemini via OpenAI-compatible API |
|  9 | [`auto-memory-tool`](auto-memory-tool)                         | Automatic memory tool workflow | Google Gemini via OpenAI-compatible API |
| 10 | [`tool-call-advisor`](tool-call-advisor)                       | Tool-call advisor pattern | Google Gemini via OpenAI-compatible API |
| 11 | [`augmented-tool-callback`](augmented-tool-callback)           | Augmented tool callbacks | Google Gemini via OpenAI-compatible API |
| 12 | [`redis-semantic-cache`](redis-semantic-cache)                 | Semantic response caching | Gemini chat, Google embeddings, Redis |
| 13 | [`qdrant-semantic-cache`](qdrant-semantic-cache)               | Semantic caching with Qdrant | Gemini chat, Google embeddings, Qdrant |
| 14 | [`rag`](rag)                                                   | Retrieval-augmented generation | Gemini chat, Google embeddings, Qdrant |
| 15 | [`rag-tool`](rag-tool)                                         | RAG exposed through a tool | Gemini chat, Google embeddings, Qdrant |
| 16 | [`voicechat-stt`](voicechat-stt)                               | Speech-to-text voice chat | OpenAI |
| 17 | [`voicechat-tts`](voicechat-tts)                               | Text-to-speech voice chat | OpenAI |
| 18 | [`voicechat-tts-elevenlabs`](voicechat-tts-elevenlabs)         | Text-to-speech with ElevenLabs | OpenAI, ElevenLabs |
| 19 | [`structured-output-validation`](structured-output-validation) | Structured output validation | Ollama |
| 20 | [`safeguard-input`](safeguard-input)                           | Input safety checks | Google Gemini |
| 21 | [`safeguard-output`](safeguard-output)                         | Output safety checks | Google Gemini |
| 22 | [`safeguard-semantic`](safeguard-semantic)                     | Semantic safety checks | Gemini via OpenAI-compatible API, Ollama |
| 23 | [`simple-memory-jdbc`](simple-memory-jdbc)                     | Conversation memory | Google Gemini |
| 24 | [`subagent-task-tool`](subagent-task-tool)                     | Delegate tasks to subagents through a tool | Google Gemini |
| 25 | [`rag-conversation-aware`](rag-conversation-aware)             | Conversation-aware retrieval-augmented generation | Gemini chat, Google embeddings, Qdrant |

## Configuration

API keys are read from environment variables and are intentionally not stored in the repository:

| Variable | Used by |
| --- | --- |
| `OPENAI_API_KEY` | `voicechat-stt`, `voicechat-tts`, and the OpenAI portion of `voicechat-tts-elevenlabs` |
| `GEMINI_API_KEY` | Gemini-based examples, including recipes using the OpenAI-compatible Gemini endpoint |
| `ELEVENLABS_API_KEY` | `voicechat-tts-elevenlabs` |

The exact model and endpoint settings are in each recipe's `src/main/resources/application.yaml`.

For local services, start the service from the recipe directory before launching the application:

```bash
cd redis-semantic-cache
docker compose up -d
./gradlew bootRun
```

The Qdrant recipes include their own `compose.yaml`; `rag`, `rag-tool`, `redis-semantic-cache`, and `qdrant-semantic-cache` are the recipes that need an additional service.

## Project layout

Every recipe follows the same basic structure:

```text
<recipe>/
├── build.gradle
├── settings.gradle
├── gradlew
└── src/
    ├── main/java/
    ├── main/resources/application.yaml
    └── test/java/
```

The `rag` and `rag-tool` examples include `src/main/resources/Sagrada.pdf`, an original synthetic knowledge-base PDF created for demonstrating document ingestion and retrieval. Despite the retained filename for compatibility with the examples, it contains no Sagrada game content.

## Contributing

Improvements and new recipes are welcome. Keep examples focused, avoid committing credentials or generated build output, and include or update a recipe-level test when practical.

## License

This project is licensed under the [MIT License](LICENSE).

## Inspiration

This repository is inspired by [habuma/spring-ai-recipes](https://github.com/habuma/spring-ai-recipes), a collection of Spring AI recipe examples.
