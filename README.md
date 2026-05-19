[![Community Extension](https://img.shields.io/badge/Community%20Extension-An%20open%20source%20community%20maintained%20project-FF4700)](https://github.com/camunda-community-hub/community)
![Compatible with: Camunda Platform 8](https://img.shields.io/badge/Compatible%20with-Camunda%20Platform%208-0072Ce)
[![](https://img.shields.io/badge/Lifecycle-Stable-brightgreen)](https://github.com/Camunda-Community-Hub/community/blob/main/extension-lifecycle.md#stable-)

# FEEL-Scala Playground

This repository contains an API for evaluating FEEL expressions using the [FEEL-Scala engine](https://github.com/camunda/feel-scala).

```bash
curl --header "Content-Type: application/json" \
-X POST \
-d '{"expression":"x + y", "context":{"x": 2, "y": 3}, "metadata": {"source": "test"}}' \
API_URL/api/v1/feel/evaluate
```

Additionally, the repository also contains a lightweight frontend playground available at `/playground`.

## Usage

The API has the following endpoints:

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

### Frontend playground

- Type: `GET`
- Path: `/playground`

The frontend supports evaluating FEEL expressions and unary-tests expressions, shows server status and FEEL version,
and can import/export share links.

## Install

The application is available as a docker image on DockerHub: `camunda/feel-scala-playground`. You can run it like so:

```bash
docker run -p 8080:8080 camunda/feel-scala-playground
```

## Development

The project uses Maven as the build tool. To run the application locally, use this command:

```bash
mvn spring-boot:run
```

Access the API at `http://localhost:8080/api/v1/feel/evaluate` and the frontend playground at http://localhost:8080/playground.
