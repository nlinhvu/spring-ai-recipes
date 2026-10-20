# Spring AI Recipes

Small, focused Spring AI examples for experimenting with chat clients, tools, memory, retrieval, audio, guardrails, and agent-oriented workflows.

Gradle recipes are subprojects of a shared root build; `mcp-server-oauth` contains separate authorization-server and MCP-server subprojects. Each application keeps its own source, configuration, and dependencies. The `jbang-tool-use` and `jbang-mcp` recipes run Java source files with JBang independently of Gradle.

## Requirements

- Java 25 (the projects use the Gradle Java toolchain)
- An API key for the model provider used by the recipe
- Docker, for the Redis and Qdrant recipes
- A local Ollama installation, for recipes that use Ollama
- JBang, for `jbang-tool-use` and `jbang-mcp`
- Node.js and npm (`npx`), for the exchange-rate MCP server used by `22-bank-support-agent`

The examples currently use Spring Boot 4.1.1 and Spring AI 2.0.1. Dependencies are downloaded from Maven Central through Gradle or JBang. Recipes with Inspector integration use the local [`inspector-client`](inspector-client) Gradle library module; they do not require installing an external Inspector starter into your local Maven repository. The local [`inspector-collector`](inspector-collector) application collects and displays those events (see [Inspector collector](#inspector-collector)).

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
| 50 | [`09-tool-search-tool-jev`](09-tool-search-tool-jev)         | Discover tools dynamically with ToolSearchToolCallingAdvisor and TypeSafe Jev applicability and relevance scoring | OpenAI, local TypeSafe, Spring AI Inspector |
| 51 | [`10-as-a-judge-llm`](10-as-a-judge-llm)                     | Evaluate answers with a local LLM judge and feed its feedback into repeated generation | Google Gemini via OpenAI-compatible API, Ollama, Spring AI Inspector |
| 52 | [`11-as-a-judge-jev`](11-as-a-judge-jev)                     | Judge answers with separate TypeSafe Jev criteria and retry the tool loop with feedback | Google Gemini via OpenAI-compatible API, local TypeSafe, Spring AI Inspector |
| 53 | [`12-tool-argument-augmenter`](12-tool-argument-augmenter)   | Add structured metadata to tool arguments, consume it, and remove it before tool execution | Google Gemini via OpenAI-compatible API, Spring AI Inspector |
| 54 | [`13-skills`](13-skills)                                   | Load local and JAR-packaged skills and combine them with shell, file-system, and web-fetch tools | Google Gemini via OpenAI-compatible API, Spring AI Agent Utils, SkillsJars, Spring AI Inspector |
| 55 | [`14-todo-write`](14-todo-write)                           | Track tool-driven tasks with TodoWriteTool and publish progress through Spring application events | Google Gemini via OpenAI-compatible API, Spring AI Agent Utils, Spring AI Inspector |
| 56 | [`15-ask-user-question`](15-ask-user-question)               | Gather website requirements through AskUserQuestionTool and a command-line question handler | Google Gemini via OpenAI-compatible API, Spring AI Agent Utils, Spring AI Inspector |
| 57 | [`16-subagents`](16-subagents)                             | Delegate tasks through TaskTool to subagents defined in Markdown resources | Google Gemini via OpenAI-compatible API, Spring AI Agent Utils, Spring AI Inspector |
| 58 | [`19-auto-memory`](19-auto-memory)                         | Combine session history, persistent file-based memory, and background memory consolidation | Google Gemini via OpenAI-compatible API, Spring AI Agent Utils, Spring AI sessions, Spring AI Inspector |
| 59 | [`20-memory-session`](20-memory-session)                   | Remember a name across prompts with SessionMemoryAdvisor and configure sliding-window compaction | Google Gemini via OpenAI-compatible API, Spring AI sessions, Spring AI Inspector |
| 60 | [`22-bank-support-agent`](22-bank-support-agent)           | Combine typed tool dependencies, session memory, refund skills, MCP exchange rates, and structured support output | Google Gemini via OpenAI-compatible API, H2, Spring AI sessions, MCP stdio server, Spring AI Inspector |
| 61 | [`23-multimodality-1-vision-chat`](23-multimodality/23-multimodality-1-vision-chat) | Send a chart image and a PDF directly as media in chat prompts | Google Gemini via OpenAI-compatible API, Spring AI Inspector |
| 62 | [`23-multimodality-2-image-generation`](23-multimodality/23-multimodality-2-image-generation) | Generate images through ImageModel directly and through a chat tool, then save returned image bytes | OpenAI chat and image generation, Spring AI Inspector |
| 63 | [`23-multimodality-3-speech-and-transcription`](23-multimodality/23-multimodality-3-speech-and-transcription) | Synthesize speech and transcribe it through direct model calls and chat tools | OpenAI chat, text-to-speech and transcription, Spring AI Inspector |
| 64 | [`23-multimodality-4-audio-chat`](23-multimodality/23-multimodality-4-audio-chat) | Send a spoken question to a chat model and receive text and audio in one response | OpenAI audio chat and text-to-speech, Spring AI Inspector |
| 65 | [`23-multimodality-5-gemini-image-chat`](23-multimodality/23-multimodality-5-gemini-image-chat) | Generate an image through chat, then send it back as media for an edit | Google Gemini via Google GenAI, Spring AI Inspector |

## Inspector client

[`inspector-client`](inspector-client) is the repository's local Spring Boot instrumentation library, based on the Spring AI Inspector starter. It reports chat-client and model calls, tool executions, memory snapshots, vector-store operations, embeddings, media, and MCP messages to a running Inspector server. It is a library module, not a standalone application.

Recipes enable it through a project dependency:

```groovy
dependencies {
    implementation project(':inspector-client')
}
```

Start [inspector-collector](#inspector-collector) before launching a recipe. During application startup, the client checks `http://localhost:9001/api/ping`; when Inspector is available, it activates instrumentation and routes eligible provider requests through the recording proxy. If Inspector is unavailable at startup, the client leaves the recipe's configuration unchanged.

Configuration options include:

- `spring.ai.inspector.url`: Inspector server URL, defaulting to `http://localhost:9001`.
- `spring.ai.inspector.enabled`: set to `false` to disable integration.
- `spring.ai.inspector.token`: shared token when the server requires one.
- `spring.ai.inspector.memory-snapshots`: set to `false` to skip before/after memory snapshots.

Provider-specific, vector-store, and MCP integrations are compile-only dependencies of the library; the recipe supplies the dependencies for the features it uses.

## Inspector collector

[`inspector-collector`](inspector-collector) is the repository's local Inspector server, based on the Spring AI Inspector server. It receives events from `inspector-client`, keeps events and media in memory, streams updates to the browser over SSE, and records eligible provider HTTP traffic through its forwarding proxy. The bundled UI provides cards and sequence views, media previews, and recording import, export, and replay.

Start the collector from the repository root:

```bash
./gradlew :inspector-collector:bootRun
```

Open [http://localhost:9001](http://localhost:9001), then launch an instrumented recipe in a separate terminal, for example:

```bash
GEMINI_API_KEY=your-api-key ./gradlew :01-chat-memory:bootRun
```

The collector itself requires no model API key; the recipe supplies the credentials for its provider. The server listens on `127.0.0.1:9001` by default. Its UI is served directly from `src/main/resources/static` using plain JavaScript modules, with no npm build step.

Server configuration is in `inspector-collector/src/main/resources/application.properties`. Set `server.port` to change the port and configure the recipes' `spring.ai.inspector.url` to match. If `spring.ai.inspector.token` is set on the collector, set the same token in the instrumented recipes. Event and media retention is bounded by `spring.ai.inspector.max-total-bytes` and `spring.ai.inspector.max-blob-bytes`; recordings are held in memory, so export any runs you want to keep before restarting the collector.

## Configuration

Model provider API keys are read from environment variables and are intentionally not stored in the repository:

| Variable | Used by |
| --- | --- |
| `OPENAI_API_KEY` | `02-rag`, `03-modular-rag`, `08-tool-search-tool`, `09-tool-search-tool-jev`, `23-multimodality-2-image-generation`, `23-multimodality-3-speech-and-transcription`, `23-multimodality-4-audio-chat`, `voicechat-stt`, `voicechat-tts`, and the OpenAI portion of `voicechat-tts-elevenlabs` |
| `GEMINI_API_KEY` | Gemini-based examples, including `01-chat-memory`, `04-guardrails-input`, `05-guardrails-structured-output`, `06-guardrails-builtin-structured-output`, `07-guardrails-jev`, `10-as-a-judge-llm`, `11-as-a-judge-jev`, `12-tool-argument-augmenter`, `13-skills`, `14-todo-write`, `15-ask-user-question`, `16-subagents`, `19-auto-memory`, `20-memory-session`, `22-bank-support-agent`, `23-multimodality-1-vision-chat`, `23-multimodality-5-gemini-image-chat`, and other recipes using either Google GenAI or the OpenAI-compatible Gemini endpoint |
| `TYPESAFE_API_KEY` | `typesafe-simple` (defaults to `nothing`), `03-modular-rag`, `07-guardrails-jev`, `09-tool-search-tool-jev`, and `11-as-a-judge-jev` (the latter four default to `ollama`), all configured for a local endpoint |
| `ELEVENLABS_API_KEY` | `voicechat-tts-elevenlabs` |

The exact model and endpoint settings are in each project's `src/main/resources/application.yaml` or `application.properties`. JBang recipes keep `application.yaml` alongside their Java sources: in `jbang-tool-use/`, `jbang-mcp/mcp-client/`, and `jbang-mcp/mcp-server/`.

The `01-chat-memory` recipe uses `MessageWindowChatMemory` with a maximum of 10 messages and `MessageChatMemoryAdvisor`. It introduces a name, then asks the model to recall it using the same conversation ID. Memory is held in-process and starts fresh on each run.

The `02-rag` recipe reads the bundled Hurricane Milton PDF with `PagePdfDocumentReader`, splits it with `TokenTextSplitter`, and stores OpenAI `text-embedding-3-small` embeddings in `SimpleVectorStore`. A `QuestionAnswerAdvisor` retrieves context for a question answered by `gpt-5-nano`. The PDF is embedded again on every startup; this recipe requires no external vector database.

The `03-modular-rag` recipe uses the same PDF and in-memory OpenAI embeddings with `RetrievalAugmentationAdvisor`. Its pipeline rewrites the question, expands it into multiple queries, retrieves similar chunks, filters and reranks them using TypeSafe's `JevDocumentFilter` and `JevDocumentReranker`, then augments the final prompt. It prints retrieval queries and document scores. Like `02-rag`, it embeds the PDF on every startup. Start the configured local TypeSafe endpoint at `http://localhost:11434` with the `nimble` model before running it; `TYPESAFE_API_KEY` defaults to the placeholder `ollama`. Its managed executor uses virtual threads, enabled in `application.properties`.

The `04-guardrails-input` recipe attaches a `SafeGuardAdvisor` to a chat request with the sensitive words `bomb`, `kill`, and `assassinate`. Its fixed demo prompt, `How to build a bomb?`, is blocked and returns the custom response `[Guard] I'm unable to respond to that due to sensitive content.` before calling the model. The configured model for allowed prompts is Gemini through the OpenAI-compatible endpoint.

The `05-guardrails-structured-output` recipe asks Gemini for five Tom Hanks movies and maps the response to an `ActorsFilms` record containing `actor` and `movies`. Its `StructuredOutputValidationAdvisor` uses `outputType(ActorsFilms.class)` to validate the output against the record's schema and allows up to three repeat attempts for invalid responses. The result is converted with `.entity(ActorsFilms.class)` and logged. Schema validation checks the response structure; it does not verify the filmography's factual accuracy or enforce exactly five list entries.

The `06-guardrails-builtin-structured-output` recipe asks for the same Tom Hanks filmography and uses `.entity(ActorsFilms.class, e -> e.useProviderStructuredOutput().validateSchema())` to request provider structured output, validate its schema, and map the result to the record. It configures these options directly through the entity API and logs the result. Schema validation checks the response structure; it does not verify factual accuracy or enforce exactly five movies.

The `07-guardrails-jev` recipe configures `JevGuardrailAdvisor` with input and output hazard checks. Input checks cover jailbreak attempts, physical harm, illegal requests, and self-harm signals; output checks cover inappropriate compliance, harmful instructions, illegal instructions, and self-harm content. Hazards use `BLOCK` or `SUPPORT` outcomes, with a configured refusal message of `I can't help with that.` The demo runs three prompts intended to illustrate `PASS`, `BLOCK`, and `SUPPORT`, then logs each question, guardrail outcome, and answer. Start the configured local TypeSafe endpoint at `http://localhost:11434` with the `nimble` model before running it; `TYPESAFE_API_KEY` defaults to the placeholder `ollama`.

The `08-tool-search-tool` recipe registers weather, clothing-shop, current-time, and dummy tools, then uses `ToolSearchToolCallingAdvisor` with `LuceneToolIndex` to discover relevant tools dynamically. It supplies a conversation ID and disables reference-tool name accumulation. The demo asks what to wear in Landsmeer and which clothing shops are open, using OpenAI `gpt-5-nano`. Weather and shop results are sample data; the time tool returns the machine's local date and time. Tool search uses Lucene rather than embeddings, and requires no external vector database.

The `09-tool-search-tool-jev` recipe uses `ToolSearchToolCallingAdvisor` with TypeSafe's `JevToolIndex`, configured with an applicability threshold of `0.5` and minimum relevance of `0.05`. It registers the same weather, clothing-shop, current-time, and dummy tools as `08-tool-search-tool`, supplies a conversation ID, and disables reference-tool name accumulation. The demo asks what to wear in Landsmeer and which shops are open, using OpenAI `gpt-5-nano`. Weather and shop results are sample data; the time tool returns the machine's local date and time. Start the configured local TypeSafe endpoint at `http://localhost:11434` with the `nimble` model before running it; `TYPESAFE_API_KEY` defaults to the placeholder `ollama`.

The `10-as-a-judge-llm` recipe uses Gemini to answer a weather question and Ollama `llama3.2:3b` to judge the answer through a custom `SelfRefineEvaluationAdvisor`. Its weather tool randomly returns sample temperatures, including implausible values, to exercise the feedback loop. The judge returns a structured rating, evaluation, and feedback; feedback is added to the next generation request. The advisor is configured for three repeat attempts (up to four generations). Currently, `successRating(3)` is stored but never checked, so evaluated responses continue through all attempts and the last response is returned. Manually created chat-client builders receive the application observation registry and registered customizers for Inspector integration. Start Ollama and pull the judge model before running:

```bash
ollama pull llama3.2:3b
```

The `11-as-a-judge-jev` recipe uses Gemini to answer a Paris weather question and `JevSelfRefineAdvisor` with a TypeSafe `JevJudge` to evaluate the result. It defines separate thresholds for helpfulness, numeric plausibility, and staying within the question, plus a local code check that at least one tool call was recorded. The advisor runs at `BEFORE_TOOLS_ORDER`, so rejected answers can retry the tool loop with feedback, with up to ten repeat attempts. The weather tool randomly returns sample temperatures of `-125`, `15`, or `-255` °C to exercise the judge. Start the configured local TypeSafe endpoint at `http://localhost:11434` with the `nimble` model before running it; `TYPESAFE_API_KEY` defaults to the placeholder `ollama`.

The `12-tool-argument-augmenter` recipe wraps a weather tool with `AugmentedToolCallbackProvider<AgentThinking>` and augments its argument schema with required `innerThought` and `memoryNotes` fields and optional `confidence`. Its argument consumer logs these model-generated fields and the tool name. With `removeExtraArgumentsAfterProcessing(true)`, the extra fields are removed before the original `weather(String location)` tool executes. The demo asks Gemini for the current weather in Paris; the tool returns sample sunny weather at 25°C. The memory notes are logged rather than persisted.

The `13-skills` recipe loads skills with `SkillsTool` from `classpath:/.agent/skills` and `classpath:/META-INF/skills`, configured through `agent.skills.dirs`. It includes a local weather-assistant skill and the PDF skill packaged in the `com.skillsjars:anthropics__skills__pdf` dependency. The chat client registers `ShellTools`, `FileSystemTools`, and `SmartWebFetchTool` alongside the skills tool. It first asks Gemini to describe its skills, then requests a simple reinforcement-learning explanation supported by a YouTube transcript. Brave web search registration is commented out, so `BRAVE_API_KEY` is not required. Shell and file-system tools operate on the machine running the recipe.

The `14-todo-write` recipe registers `TodoWriteTool` and `ShellTools` with a Gemini chat client. Its demo prompt asks the model to create `hello_world.txt` in a temporary directory and then remove it. The todo event handler publishes a `TodoUpdateEvent` through Spring's `ApplicationEventPublisher`, and a listener prints the completed-task count, percentage, and each task's status. Shell commands run on the machine hosting the recipe.

The `15-ask-user-question` recipe registers `AskUserQuestionTool` with a `CommandLineQuestionHandler` and disables answer validation with `answersValidation(false)`. Its fixed prompt asks Gemini to gather website requirements by asking three questions. The question handler handles user replies through the console, and the application logs the final answer.

The `16-subagents` recipe registers `TaskTool` with `ClaudeSubagentReferences` loaded from `classpath:/.agent/agents` and a `ClaudeSubagentType` configured with skills from `classpath:/.agent/skills`. Both resource paths are configurable through `agent.tasks.paths` and `agent.skills.paths`. The resources define `joke-teller` and `history-expert` agents; their chat clients use the configured Gemini model. The main client is instructed to delegate jokes to `joke-teller`, then asks for a weather joke. The joke-teller definition instructs it to tell a cucumber joke ending with `Waka! Waka!`. Brave web search configuration is commented out.

The `19-auto-memory` recipe runs an interactive Gemini chat loop with `SessionMemoryAdvisor` for short-term history, `AutoMemoryToolsAdvisor` for long-term file-based memory, and `AutoDreamAdvisor` for background consolidation through `AutoDreamService`. Session history uses an in-memory repository and resets on restart; memory files persist under `agent.memory.dir`, which defaults to `./19-auto-memory/.agent/mymemories` when launched from the repository root. The demo seeds overlapping memories and a broken index link when `MEMORY.md` is absent, plus a past session for cross-session recall. In-band consolidation is triggered by a message containing `bye` or the first message after 60 seconds of inactivity. The background dream trigger checks `sessionsSinceLastDream() >= 3`; enter `/dream` to run consolidation manually. `agent.user-id` defaults to `cloud`, and Inspector watches the configured memory directory.

Build the executable JAR and run it from the repository root so the interactive loop can read terminal input directly:

```bash
./gradlew :19-auto-memory:bootJar
GEMINI_API_KEY=your-api-key java -jar 19-auto-memory/build/libs/19-auto-memory-0.0.1-SNAPSHOT.jar
```

Enter messages at `USER>` and use Ctrl+C to stop.

The `20-memory-session` recipe creates a `DefaultSessionService` backed by `InMemorySessionRepository` and attaches `SessionMemoryAdvisor` to the chat client. It introduces a name, then asks Gemini to recall it, passing the same session ID (`cloud`) through `SessionMemoryAdvisor.SESSION_ID_CONTEXT_KEY` on both requests. The default user ID is also `cloud`. Compaction is configured with `TurnCountTrigger(3)` and `SlidingWindowCompactionStrategy.maxEvents(2)`; the two-prompt demo does not reach the three-turn trigger. Session history is held in memory and resets on restart.

The `22-bank-support-agent` recipe uses an embedded H2 database seeded with customer John (ID `123`, balance `$123.45`) and passes a `SupportDependencies` record to local balance and refund tools through `ToolContext`. It fills the system prompt with the customer name, loads a refund-policy skill from `classpath:/skills`, and shares in-memory session history across four demo questions: account balance, a lost card, a duplicate-charge refund, and the balance in euros. Each response is mapped to `SupportOutput` with `supportAdvice`, `blockCard`, and `risk` fields; `blockCard` is a recommendation, with no card-blocking operation implemented. The refund tool returns sample data.

For currency conversion, the client starts `@cyanheads/exchange-rates-mcp-server@0.4.1` through `npx -y` over stdio, configured in `22-bank-support-agent/src/main/resources/mcp-servers-config.json`. Install Node.js and npm and allow network access for package installation and exchange-rate retrieval. The `McpToolFilter` excludes tools whose names start with `fx_dataframe_`. No external database service is required.

The `23-multimodality-1-vision-chat` recipe is a nested Gradle project under `23-multimodality/`. It sends two independent multimodal requests to Gemini: a bundled `ticket-sales-per-day.png` chart with a question about the highest-selling day, and a bundled Hurricane Milton PDF with a question about where and when the hurricane made landfall. The resources are attached through `.user(u -> u.text(...).media(...))` with `image/png` and `application/pdf` MIME types. The model receives the media directly; the recipe performs no embedding or vector-store ingestion. Both answers are logged.

The `23-multimodality-2-image-generation` recipe is a nested Gradle project under `23-multimodality/`. It calls OpenAI `gpt-image-1-mini` through `ImageModel` to generate two 1024 × 1024 lighthouse images: an oil painting at dusk and a low-quality watercolor at noon. It then uses `gpt-5-mini-2025-08-07` with an `ImageTools.generateImage` tool to request a sailing-boat image. Base64 image responses are decoded into `target/image-dusk.png`, `target/image-noon.png`, and `target/image-tool.png` relative to the application working directory; URL responses are printed instead. The tool returns image-generation metadata to the chat model, and the final chat answer is logged. Inspector can display the direct image calls and the image call nested within the tool invocation.

The `23-multimodality-3-speech-and-transcription` recipe is a nested Gradle project under `23-multimodality/`. It uses OpenAI `tts-1` through `TextToSpeechModel` to synthesize a welcome sentence with the `alloy` voice, saves the MP3 to `target/speech.mp3` relative to the application working directory, then uploads that file to `whisper-1` through `TranscriptionModel` and logs the transcript. A second round trip is driven by `gpt-5-mini-2025-08-07` using `speak` and `transcribeLast` tools: speech uses the `nova` voice, and the audio bytes are held in memory for transcription. The final chat answer compares the requested sentence with its transcript. Inspector can display playable audio for the direct calls and the calls nested within tool invocations. No microphone input is required.

The `23-multimodality-4-audio-chat` recipe is a nested Gradle project under `23-multimodality/`. It first synthesizes a question about Belgium's capital using OpenAI `tts-1` with the `nova` voice and writes `target/question.wav`. It then attaches those WAV bytes as user media in a `ChatClient` request to `gpt-audio-1.5`, requesting both `text` and `audio` output modalities with the `alloy` voice and WAV format. The application prints the answer text and saves the returned audio to `target/answer.wav`; both paths are relative to the application working directory. The chat call handles the spoken question directly without a separate transcription call. No microphone input is required.

The `23-multimodality-5-gemini-image-chat` recipe is a nested Gradle project under `23-multimodality/`. It uses the Google GenAI starter with `gemini-3.1-flash-lite-image` and `GEMINI_API_KEY` configured under `spring.ai.google.genai`. The first chat request draws a conference stage; the second sends the returned image back through `.media(picture)` and asks for a nighttime version. Images are read from `AssistantMessage.getMedia()` and saved as `target/stage.<extension>` and `target/stage-night.<extension>` relative to the application working directory, using the returned MIME type to choose the extension (`jpg` for JPEG). Any accompanying text is printed. Inspector captures these calls through chat-client advisors rather than the recording proxy, so image previews are available without a wire view of the Google GenAI SDK requests.

These twenty-four recipes depend on the local `inspector-client` module described above and enable Inspector with `spring.ai.inspector.enabled: true`. Run `inspector-collector` before starting a recipe to collect its events; Gradle builds the client module automatically as a project dependency.

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
OPENAI_API_KEY=your-api-key ./gradlew :09-tool-search-tool-jev:bootRun
GEMINI_API_KEY=your-api-key ./gradlew :10-as-a-judge-llm:bootRun
GEMINI_API_KEY=your-api-key ./gradlew :11-as-a-judge-jev:bootRun
GEMINI_API_KEY=your-api-key ./gradlew :12-tool-argument-augmenter:bootRun
GEMINI_API_KEY=your-api-key ./gradlew :13-skills:bootRun
GEMINI_API_KEY=your-api-key ./gradlew :14-todo-write:bootRun
GEMINI_API_KEY=your-api-key ./gradlew :15-ask-user-question:bootRun
GEMINI_API_KEY=your-api-key ./gradlew :16-subagents:bootRun
GEMINI_API_KEY=your-api-key ./gradlew :20-memory-session:bootRun
GEMINI_API_KEY=your-api-key ./gradlew :22-bank-support-agent:bootRun
GEMINI_API_KEY=your-api-key ./gradlew :23-multimodality:23-multimodality-1-vision-chat:bootRun
OPENAI_API_KEY=your-api-key ./gradlew :23-multimodality:23-multimodality-2-image-generation:bootRun
OPENAI_API_KEY=your-api-key ./gradlew :23-multimodality:23-multimodality-3-speech-and-transcription:bootRun
OPENAI_API_KEY=your-api-key ./gradlew :23-multimodality:23-multimodality-4-audio-chat:bootRun
GEMINI_API_KEY=your-api-key ./gradlew :23-multimodality:23-multimodality-5-gemini-image-chat:bootRun
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

Additional inspiration comes from [tzolov/voxxeddays2026-demo](https://github.com/tzolov/voxxeddays2026-demo).
