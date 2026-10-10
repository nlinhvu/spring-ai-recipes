# Spring AI Recipes

Small, focused Spring AI examples for experimenting with chat clients, tools, memory, retrieval, audio, guardrails, and agent-oriented workflows.

Gradle recipes are subprojects of a shared root build; `mcp-server-oauth` contains separate authorization-server and MCP-server subprojects. Each application keeps its own source, configuration, and dependencies. The `jbang-tool-use` and `jbang-mcp` recipes run Java source files with JBang independently of Gradle.

## Requirements

- Java 25 (the projects use the Gradle Java toolchain)
- An API key for the model provider used by the recipe
- Docker, for the Redis and Qdrant recipes
- A local Ollama installation, for recipes that use Ollama
- JBang, for `jbang-tool-use` and `jbang-mcp`

The examples currently use Spring Boot 4.1.1 and Spring AI 2.0.1. Dependencies are downloaded from Maven Central through Gradle or JBang, except for the locally installed Spring AI Inspector snapshot used by `01-chat-memory`, `02-rag`, `03-modular-rag`, `04-guardrails-input`, `05-guardrails-structured-output`, `06-guardrails-builtin-structured-output`, `07-guardrails-jev`, and `08-tool-search-tool` (see Configuration).

## Quick start

Choose a recipe and run it with the checked-in root wrapper from the repository root:

```bash
export GEMINI_API_KEY=your-api-key
./gradlew :simple-memory:bootRun
```

Run a recipe's tests with:

```bash
./gradlew :simple-memory:test
```

On Windows, use `gradlew.bat` instead of `./gradlew`.

Shared plugin, dependency, application, and Java toolchain versions are managed in the root `build.gradle`. The root `settings.gradle` lists the Gradle subprojects. Import the root project in IntelliJ to load all recipes together. List projects with `./gradlew projects`, compile all application and test sources with `./gradlew testClasses`, or build all recipes with `./gradlew build` (some tests require API keys or running services). Existing recipe wrappers can still be used from their directories; without local settings files they use the shared root build.

For `jbang-tool-use`, run the Java entry point from its directory:

```bash
cd jbang-tool-use
export GEMINI_API_KEY=your-api-key
jbang ToolUseApplication.java
```

Enter a prompt such as `What is the weather in zipcode 10001?` in the interactive console. The registered weather tool returns sample data.

For `jbang-mcp`, first update the JBang executable, server source, and server configuration directory paths in `jbang-mcp/mcp-client/application.yaml` to match your machine. Then run the client:

```bash
cd jbang-mcp/mcp-client
export GEMINI_API_KEY=your-api-key
jbang McpClientPlainApplication.java
```

The client launches the JBang MCP server as a subprocess and communicates over stdio. Enter a weather prompt in the interactive console to use the server's sample weather tool.

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
| 41 | [`jbang-mcp`](jbang-mcp)                                     | Run an interactive MCP client and a subprocess weather server with JBang over stdio | Google Gemini via OpenAI-compatible API, JBang, MCP stdio server |
| 42 | [`01-chat-memory`](01-chat-memory)                           | Remember a name across two prompts using a message window and a conversation ID | Google Gemini via OpenAI-compatible API, Spring AI Inspector |
| 43 | [`02-rag`](02-rag)                                           | Read a PDF, embed its chunks, and answer a question using an in-memory vector store | OpenAI chat and embeddings, Spring AI Inspector |
| 44 | [`03-modular-rag`](03-modular-rag)                           | Modular RAG with query rewriting, expansion, retrieval, TypeSafe filtering and reranking | OpenAI chat and embeddings, local TypeSafe, Spring AI Inspector |
| 45 | [`04-guardrails-input`](04-guardrails-input)                 | Block sensitive input with SafeGuardAdvisor and return a custom guardrail response | Google Gemini via OpenAI-compatible API, Spring AI Inspector |
| 46 | [`05-guardrails-structured-output`](05-guardrails-structured-output) | Validate structured output against a Java record's schema and retry invalid responses | Google Gemini via OpenAI-compatible API, Spring AI Inspector |
| 47 | [`06-guardrails-builtin-structured-output`](06-guardrails-builtin-structured-output) | Request provider structured output and validate its schema through the entity API | Google Gemini via OpenAI-compatible API, Spring AI Inspector |
| 48 | [`07-guardrails-jev`](07-guardrails-jev)                     | Screen input and output with TypeSafe Jev guardrails and report PASS, BLOCK, or SUPPORT outcomes | Google Gemini via OpenAI-compatible API, local TypeSafe, Spring AI Inspector |
| 49 | [`08-tool-search-tool`](08-tool-search-tool)                 | Discover tools dynamically with ToolSearchToolCallingAdvisor and a Lucene tool index | OpenAI, Lucene, Spring AI Inspector |

## Configuration

Model provider API keys are read from environment variables and are intentionally not stored in the repository:

| Variable | Used by |
| --- | --- |
| `OPENAI_API_KEY` | `02-rag`, `03-modular-rag`, `08-tool-search-tool`, `voicechat-stt`, `voicechat-tts`, and the OpenAI portion of `voicechat-tts-elevenlabs` |
| `GEMINI_API_KEY` | Gemini-based examples, including `01-chat-memory`, `04-guardrails-input`, `05-guardrails-structured-output`, `06-guardrails-builtin-structured-output`, `07-guardrails-jev`, and other recipes using the OpenAI-compatible Gemini endpoint |
| `TYPESAFE_API_KEY` | `typesafe-simple` (defaults to `nothing`), `03-modular-rag`, and `07-guardrails-jev` (the latter two default to `ollama`), all configured for a local endpoint |
| `ELEVENLABS_API_KEY` | `voicechat-tts-elevenlabs` |

The exact model and endpoint settings are in each project's `src/main/resources/application.yaml` or `application.yml`. JBang recipes keep `application.yaml` alongside their Java sources: in `jbang-tool-use/`, `jbang-mcp/mcp-client/`, and `jbang-mcp/mcp-server/`.

The `01-chat-memory` recipe uses `MessageWindowChatMemory` with a maximum of 10 messages and `MessageChatMemoryAdvisor`. It introduces a name, then asks the model to recall it using the same conversation ID. Memory is held in-process and starts fresh on each run.

The `02-rag` recipe reads the bundled Hurricane Milton PDF with `PagePdfDocumentReader`, splits it with `TokenTextSplitter`, and stores OpenAI `text-embedding-3-small` embeddings in `SimpleVectorStore`. A `QuestionAnswerAdvisor` retrieves context for a question answered by `gpt-5-nano`. The PDF is embedded again on every startup; this recipe requires no external vector database.

The `03-modular-rag` recipe uses the same PDF and in-memory OpenAI embeddings with `RetrievalAugmentationAdvisor`. Its pipeline rewrites the question, expands it into multiple queries, retrieves similar chunks, filters and reranks them using TypeSafe's `JevDocumentFilter` and `JevDocumentReranker`, then augments the final prompt. It prints retrieval queries and document scores. Like `02-rag`, it embeds the PDF on every startup. Start the configured local TypeSafe endpoint at `http://localhost:11434` with the `nimble` model before running it; `TYPESAFE_API_KEY` defaults to the placeholder `ollama`. Its managed executor uses virtual threads, enabled in `application.yml`.

The `04-guardrails-input` recipe attaches a `SafeGuardAdvisor` to a chat request with the sensitive words `bomb`, `kill`, and `assassinate`. Its fixed demo prompt, `How to build a bomb?`, is blocked and returns the custom response `[Guard] I'm unable to respond to that due to sensitive content.` before calling the model. The configured model for allowed prompts is Gemini through the OpenAI-compatible endpoint.

The `05-guardrails-structured-output` recipe asks Gemini for five Tom Hanks movies and maps the response to an `ActorsFilms` record containing `actor` and `movies`. Its `StructuredOutputValidationAdvisor` uses `outputType(ActorsFilms.class)` to validate the output against the record's schema and allows up to three repeat attempts for invalid responses. The result is converted with `.entity(ActorsFilms.class)` and logged. Schema validation checks the response structure; it does not verify the filmography's factual accuracy or enforce exactly five list entries.

The `06-guardrails-builtin-structured-output` recipe asks for the same Tom Hanks filmography and uses `.entity(ActorsFilms.class, e -> e.useProviderStructuredOutput().validateSchema())` to request provider structured output, validate its schema, and map the result to the record. It configures these options directly through the entity API and logs the result. Schema validation checks the response structure; it does not verify factual accuracy or enforce exactly five movies.

The `07-guardrails-jev` recipe configures `JevGuardrailAdvisor` with input and output hazard checks. Input checks cover jailbreak attempts, physical harm, illegal requests, and self-harm signals; output checks cover inappropriate compliance, harmful instructions, illegal instructions, and self-harm content. Hazards use `BLOCK` or `SUPPORT` outcomes, with a configured refusal message of `I can't help with that.` The demo runs three prompts intended to illustrate `PASS`, `BLOCK`, and `SUPPORT`, then logs each question, guardrail outcome, and answer. Start the configured local TypeSafe endpoint at `http://localhost:11434` with the `nimble` model before running it; `TYPESAFE_API_KEY` defaults to the placeholder `ollama`.

The `08-tool-search-tool` recipe registers weather, clothing-shop, current-time, and dummy tools, then uses `ToolSearchToolCallingAdvisor` with `LuceneToolIndex` to discover relevant tools dynamically. It supplies a conversation ID and disables reference-tool name accumulation. The demo asks what to wear in Landsmeer and which clothing shops are open, using OpenAI `gpt-5-nano`. Weather and shop results are sample data; the time tool returns the machine's local date and time. Tool search uses Lucene rather than embeddings, and requires no external vector database.

These eight recipes depend on `org.springaicommunity:spring-ai-inspector-starter:0.0.1-SNAPSHOT`. Before running them, build and install the Inspector artifacts into your local Maven repository following the [Spring AI Inspector README](https://github.com/tzolov/voxxeddays2026-demo/blob/main/spring-ai-inspector/README.md). The root build resolves the starter and its parent POM through `mavenLocal()`. All eight recipes enable Inspector with `spring.ai.inspector.enabled: true`.

Run a recipe from the repository root with its provider key:

```bash
GEMINI_API_KEY=your-api-key ./gradlew :01-chat-memory:bootRun
OPENAI_API_KEY=your-api-key ./gradlew :02-rag:bootRun
OPENAI_API_KEY=your-api-key ./gradlew :03-modular-rag:bootRun
GEMINI_API_KEY=your-api-key ./gradlew :04-guardrails-input:bootRun
GEMINI_API_KEY=your-api-key ./gradlew :05-guardrails-structured-output:bootRun
GEMINI_API_KEY=your-api-key ./gradlew :06-guardrails-builtin-structured-output:bootRun
GEMINI_API_KEY=your-api-key ./gradlew :07-guardrails-jev:bootRun
OPENAI_API_KEY=your-api-key ./gradlew :08-tool-search-tool:bootRun
```

The `mcp-stdio-server` recipe enables the MCP stdio transport with `spring.ai.mcp.server.stdio: true` and exposes `get-weather-for-zipcode` using `@McpTool`. The tool returns sample weather data and requires no API key or external weather service.

The `mcp-http-server` recipe uses `spring.ai.mcp.server.protocol: stateless` and listens on port `3000`. Connect an MCP client using Streamable HTTP transport to `http://localhost:3000/mcp` to call `get-weather-for-zipcode`. The tool returns sample weather data and requires no API key or external weather service.

The `mcp-client-plain` recipe connects to `mcp-http-server` at `http://localhost:3000/mcp` using Streamable HTTP and registers the discovered MCP tools with its `ChatClient` through a `ToolCallbackProvider`. Start `mcp-http-server` first, then run `mcp-client-plain` with `GEMINI_API_KEY` set and enter prompts in its interactive console.

The `mcp-server-api-key` recipe exposes the sample weather tool at `http://localhost:3000/mcp` using stateless Streamable HTTP and requires API key authentication through Spring Security. Its `SecurityConfig` defines an in-memory demo key; send `X-MCP-API-KEY: ApiKeyId.Secret` on MCP requests, including initialization. The `mcp-client-plain` recipe includes a request customizer that sends this header. Run `mcp-server-api-key` in place of `mcp-http-server`, since both use port `3000`.

The `mcp-server-oauth` recipe contains two Gradle projects. Start `auth-server` on port `9999`, then `mcp-server` on port `3000`, using `./gradlew :mcp-server-oauth:auth-server:bootRun` and `./gradlew :mcp-server-oauth:mcp-server:bootRun` from the repository root in separate terminals. The MCP server uses stateless Streamable HTTP at `http://localhost:3000/mcp` and validates JWTs issued by `http://localhost:9999`; its weather tool requires the `meteorology` scope. The authorization server includes demo client credentials `myclient` / `mysecret`, user credentials `cloud` / `pw`, and MCP Inspector callback URLs on port `6274`. Configure an OAuth-capable MCP client to request the `meteorology` scope and send its access token as an `Authorization: Bearer <access-token>` header. Run this MCP server in place of the other MCP HTTP server recipes, which also use port `3000`.

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

The root contains `build.gradle`, `settings.gradle`, and the Gradle wrapper. Each application subproject follows the same basic structure:

```text
<recipe>/
├── build.gradle
├── gradlew
└── src/
    ├── main/java/
    ├── main/resources/application.yaml
    └── test/java/
```

For `mcp-server-oauth`, this structure appears under both `auth-server/` and `mcp-server/`.

The `jbang-tool-use` recipe keeps its Java sources and `application.yaml` directly in the recipe directory. `ToolUseApplication.java` declares the Java version, BOM, dependencies, and supporting source files through JBang directives.

The `jbang-mcp` recipe uses the same source-based layout in separate `mcp-client/` and `mcp-server/` directories, with `McpClientPlainApplication.java` and `McpStdioServerApplication.java` as their entry points.

The `rag`, `rag-tool`, and `rag-hyde` examples include `src/main/resources/Sagrada.pdf`, an original synthetic knowledge-base PDF created for demonstrating document ingestion and retrieval. Despite the retained filename for compatibility with the examples, it contains no Sagrada game content.

## Contributing

Improvements and new recipes are welcome. Keep examples focused, avoid committing credentials or generated build output, and include or update a recipe-level test when practical.

## License

This project is licensed under the [MIT License](LICENSE).

## Inspiration

This repository is inspired by [habuma/spring-ai-recipes](https://github.com/habuma/spring-ai-recipes), a collection of Spring AI recipe examples.
