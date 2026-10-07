# Spring AI Recipes

Small, focused Spring AI examples for experimenting with chat clients, tools, memory, retrieval, audio, guardrails, and agent-oriented workflows.

Most recipes are standalone Gradle projects; `mcp-server-oauth` contains separate authorization-server and MCP-server projects, and `jbang-tool-use` runs Java source files with JBang. The examples are intentionally independent so you can open one directory, run it, and inspect the smallest useful implementation.

## Requirements

- Java 25 (the projects use the Gradle Java toolchain)
- An API key for the model provider used by the recipe
- Docker, for the Redis and Qdrant recipes
- A local Ollama installation, for recipes that use Ollama
- JBang, for `jbang-tool-use`

The examples currently use Spring Boot 4.1.1 and Spring AI 2.0.1. Dependencies are downloaded from Maven Central through Gradle or JBang.

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

For the JBang recipe, run the Java entry point from its directory:

```bash
cd jbang-tool-use
export GEMINI_API_KEY=your-api-key
jbang ToolUseApplication.java
```

Enter a prompt such as `What is the weather in zipcode 10001?` in the interactive console. The registered weather tool returns sample data.

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
| 26 | [`rag-metadata-filter`](rag-metadata-filter)                   | Metadata-filtered retrieval-augmented generation | Gemini chat, Google embeddings, Qdrant |
| 27 | [`rag-hyde`](rag-hyde)                                         | Hypothetical document embeddings for retrieval | Gemini chat, Google embeddings, Qdrant |
| 28 | [`rag-hybrid`](rag-hybrid)                                     | Hybrid vector and BM25 retrieval | Gemini chat, Google embeddings, Qdrant, Lucene |
| 29 | [`rag-reranking`](rag-reranking)                               | LLM-based reranking after retrieval | Gemini chat, Google embeddings, Qdrant, Lucene |
| 30 | [`graph-workflow-langgraph4j`](graph-workflow-langgraph4j)     | Conditional support-routing graph workflow | Google Gemini via OpenAI-compatible API, LangGraph4j |
| 31 | [`tool-search-tool`](tool-search-tool)                         | Search and select tools dynamically | Google Gemini via OpenAI-compatible API, Lucene |
| 32 | [`local-modeljars`](local-modeljars)                           | Run a local model packaged as a Model JAR | Local Qwen3 model, Model JARs |
| 33 | [`memory-session-summarization`](memory-session-summarization) | Session memory with recursive summarization | Google Gemini via OpenAI-compatible API, Spring AI sessions |
| 34 | [`typesafe-simple`](typesafe-simple)                         | Typed classification and scoring with confidence-based support routing | TypeSafe (local Nimble model), Google Gemini via OpenAI-compatible API |
| 35 | [`mcp-stdio-server`](mcp-stdio-server)                       | Expose an annotated weather tool through an MCP server over standard input/output | MCP client, no model provider required |
| 36 | [`mcp-http-server`](mcp-http-server)                         | Expose an annotated weather tool through a stateless MCP server over HTTP | MCP client, no model provider required |
| 37 | [`mcp-client-plain`](mcp-client-plain)                       | Interactive chat with MCP tools registered through a ToolCallbackProvider | Google Gemini via OpenAI-compatible API, MCP HTTP server |
| 38 | [`mcp-server-api-key`](mcp-server-api-key)                   | Protect a stateless MCP HTTP server with API key authentication | MCP client, Spring Security, no model provider required |
| 39 | [`mcp-server-oauth`](mcp-server-oauth)                       | OAuth2 authorization and scope-based access to MCP tools | MCP client, Spring Authorization Server, Spring Security |
| 40 | [`jbang-tool-use`](jbang-tool-use)                           | Run interactive chat and weather tool calling directly from Java source with JBang | Google Gemini via OpenAI-compatible API, JBang |

## Configuration

Model provider API keys are read from environment variables and are intentionally not stored in the repository:

| Variable | Used by |
| --- | --- |
| `OPENAI_API_KEY` | `voicechat-stt`, `voicechat-tts`, and the OpenAI portion of `voicechat-tts-elevenlabs` |
| `GEMINI_API_KEY` | Gemini-based examples, including recipes using the OpenAI-compatible Gemini endpoint |
| `TYPESAFE_API_KEY` | `typesafe-simple` (defaults to `nothing` for the local endpoint) |
| `ELEVENLABS_API_KEY` | `voicechat-tts-elevenlabs` |

The exact model and endpoint settings are in each project's `src/main/resources/application.yaml`, or `jbang-tool-use/application.yaml` for the JBang recipe.

The `mcp-stdio-server` recipe enables the MCP stdio transport with `spring.ai.mcp.server.stdio: true` and exposes `get-weather-for-zipcode` using `@McpTool`. The tool returns sample weather data and requires no API key or external weather service.

The `mcp-http-server` recipe uses `spring.ai.mcp.server.protocol: stateless` and listens on port `3000`. Connect an MCP client using Streamable HTTP transport to `http://localhost:3000/mcp` to call `get-weather-for-zipcode`. The tool returns sample weather data and requires no API key or external weather service.

The `mcp-client-plain` recipe connects to `mcp-http-server` at `http://localhost:3000/mcp` using Streamable HTTP and registers the discovered MCP tools with its `ChatClient` through a `ToolCallbackProvider`. Start `mcp-http-server` first, then run `mcp-client-plain` with `GEMINI_API_KEY` set and enter prompts in its interactive console.

The `mcp-server-api-key` recipe exposes the sample weather tool at `http://localhost:3000/mcp` using stateless Streamable HTTP and requires API key authentication through Spring Security. Its `SecurityConfig` defines an in-memory demo key; send `X-MCP-API-KEY: ApiKeyId.Secret` on MCP requests, including initialization. The `mcp-client-plain` recipe includes a request customizer that sends this header. Run `mcp-server-api-key` in place of `mcp-http-server`, since both use port `3000`.

The `mcp-server-oauth` recipe contains two Gradle projects. Start `auth-server` on port `9999`, then `mcp-server` on port `3000`, using each project's `./gradlew bootRun` in separate terminals. The MCP server uses stateless Streamable HTTP at `http://localhost:3000/mcp` and validates JWTs issued by `http://localhost:9999`; its weather tool requires the `meteorology` scope. The authorization server includes demo client credentials `myclient` / `mysecret`, user credentials `cloud` / `pw`, and MCP Inspector callback URLs on port `6274`. Configure an OAuth-capable MCP client to request the `meteorology` scope and send its access token as an `Authorization: Bearer <access-token>` header. Run this MCP server in place of the other MCP HTTP server recipes, which also use port `3000`.

The `typesafe-simple` recipe uses a local TypeSafe endpoint at `http://localhost:11434` with the `nimble` model. It classifies a support message by urgency and department, scores customer frustration, and uses department confidence to decide whether to route automatically or send the message to a human. Its configured Gemini chat client also reads `GEMINI_API_KEY`.

For local services, start the service from the recipe directory before launching the application:

```bash
cd redis-semantic-cache
docker compose up -d
./gradlew bootRun
```

The Qdrant recipes include their own `compose.yaml`; `rag`, `rag-tool`, `rag-conversation-aware`, `rag-metadata-filter`, `rag-hyde`, `rag-hybrid`, `rag-reranking`, `redis-semantic-cache`, and `qdrant-semantic-cache` are the recipes that need an additional service.

The `rag-hybrid` and `rag-reranking` recipes require `RagIngestionConfig` to run before their `LuceneSearch` bean is created, so the Lucene index exists before search initialization.

## Project layout

Individual Gradle projects follow the same basic structure:

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

For `mcp-server-oauth`, this structure appears under both `auth-server/` and `mcp-server/`.

The `jbang-tool-use` recipe keeps its Java sources and `application.yaml` directly in the recipe directory. `ToolUseApplication.java` declares the Java version, BOM, dependencies, and supporting source files through JBang directives.

The `rag`, `rag-tool`, and `rag-hyde` examples include `src/main/resources/Sagrada.pdf`, an original synthetic knowledge-base PDF created for demonstrating document ingestion and retrieval. Despite the retained filename for compatibility with the examples, it contains no Sagrada game content.

## Contributing

Improvements and new recipes are welcome. Keep examples focused, avoid committing credentials or generated build output, and include or update a recipe-level test when practical.

## License

This project is licensed under the [MIT License](LICENSE).

## Inspiration

This repository is inspired by [habuma/spring-ai-recipes](https://github.com/habuma/spring-ai-recipes), a collection of Spring AI recipe examples.
