[![Community Extension](https://img.shields.io/badge/Community%20Extension-An%20open%20source%20community%20maintained%20project-FF4700)](https://github.com/camunda-community-hub/community)
![Compatible with: Camunda Platform 8](https://img.shields.io/badge/Compatible%20with-Camunda%20Platform%208-0072Ce)
[![](https://img.shields.io/badge/Lifecycle-Stable-brightgreen)](https://github.com/Camunda-Community-Hub/community/blob/main/extension-lifecycle.md#stable-)

# FEEL-Scala Playground

The playground allows evaluating FEEL expressions using the [FEEL-Scala engine](https://github.com/camunda/feel-scala). It contains:

- An interactive frontend for users
- A REST API for programmatic access
- An MCP (Model Context Protocol) server for AI model integration

## Install

The application is available as a Docker image. You can run it with the following command:

```bash
docker run -p 8080:8080 ghcr.io/camunda-community-hub/feel-scala-playground
```

- Playground frontend: http://localhost:8080/playground
- API endpoints: `http://localhost:8080/api/v1/*`
- MCP server: `http://localhost:8080/sse`

## Usage

The API has the following endpoints.

Example request:

```bash
curl --header "Content-Type: application/json" \
-X POST \
-d '{"expression":"x + y", "context":{"x": 2, "y": 3}, "metadata": {"source": "test"}}' \
API_URL/api/v1/feel/evaluate
```

### Evaluate expression

- Type: `POST`
- Path: `/api/v1/feel/evaluate`
- Properties:
  - `expression` - (String) required
  - `context` - (JSON object) required
  - `metadata` - (JSON object) optional - used for tracking purposes

Request:

```json
{
  "expression": "x * y",
  "context": {
    "x": 2,
    "y": 3
  },
  "metadata": {
    "source": "test"
  }
}
```

Response:

```json
{
  "result": 6,
  "error": null,
  "warnings": []
}
```

### Evaluate unary-tests

- Type: `POST`
- Path: `/api/v1/feel-unary-tests/evaluate`
- Properties:
  - `expression` - (String) required
  - `inputValue` - (JSON value) required
  - `context` - (JSON object) required
  - `metadata` - (JSON object) optional - used for tracking purposes

Request:

```json
{
  "expression": "< x",
  "inputValue": 3,
  "context": {
    "x": 5
  },
  "metadata": {
    "source": "test"
  }
}
```

Response:

```json
{
  "result": true,
  "error": null,
  "warnings": []
}
```

### Version

- Type: `GET`
- Path: `/api/v1/version`

Response:

```json
{
  "feelEngineVersion": "1.16.0"
}
```

### Playground

- Type: `GET`
- Path: `/playground`

The playground frontend supports evaluating FEEL expressions and unary-tests expressions, shows server status and FEEL version,
and can import/export share links.

## MCP Server

The application exposes the FEEL evaluation API as an [MCP (Model Context Protocol)](https://modelcontextprotocol.io/) server. This allows AI models and MCP-compatible clients to evaluate FEEL expressions as tools.

### Connection

The MCP server uses the SSE (Server-Sent Events) transport and is available at:

- SSE endpoint: `http://localhost:8080/sse`
- Message endpoint: `http://localhost:8080/mcp/message`

### Tools

The following tools are exposed:

#### `evaluate_feel_expression`

Evaluates a FEEL expression with an optional context.

| Parameter    | Type        | Required | Description                              |
|--------------|-------------|----------|------------------------------------------|
| `expression` | `string`    | yes      | The FEEL expression to evaluate          |
| `context`    | JSON object | no       | Context variables available to the expression |

#### `evaluate_feel_unary_tests`

Evaluates a FEEL unary-tests expression against an input value with an optional context.

| Parameter    | Type        | Required | Description                                          |
|--------------|-------------|----------|------------------------------------------------------|
| `expression` | `string`    | yes      | The FEEL unary-tests expression to evaluate          |
| `inputValue` | any         | yes      | The input value to test against                      |
| `context`    | JSON object | no       | Context variables available to the expression        |

### Resources

The following resources are exposed:

#### `feel://version`

Returns the version of the FEEL-Scala engine used to evaluate expressions.

### MCP Client Configuration Example

To connect an MCP client (e.g. Claude Desktop) to the server, use the following configuration:

```json
{
  "mcpServers": {
    "feel-scala-mcp": {
      "url": "http://localhost:8080/sse"
    }
  }
}
```

## Configuration

The following configuration options are available:

| Property                             | Default | Description |
|--------------------------------------|---------|-------------|
| `server.port`                        | `8080`  | HTTP port used by the application (can also be set via `PORT`). |
| `playground.tracking.enabled`        | `false` | Enables tracking of FEEL evaluations. |
| `playground.feel.evaluation.timeout` | `10s`   | Maximum time allowed for one FEEL evaluation or unary-tests evaluation. |
| `MIXPANEL_PROJECT_TOKEN`             | -       | Required when `playground.tracking.enabled=true`; Mixpanel project token used for tracking. |

## Development

The project uses Maven as the build tool. To run the application locally, use this command:

```bash
mvn spring-boot:run
```

Access the API at `http://localhost:8080/api/v1/*` and the frontend playground at http://localhost:8080/playground.
