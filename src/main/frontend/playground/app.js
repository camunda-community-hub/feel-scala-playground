const expressionTypeField = document.getElementById("expression-type");
const expressionField = document.getElementById("expression");
const inputValueField = document.getElementById("input-value");
const contextField = document.getElementById("context");
const inputValueGroup = document.getElementById("input-value-group");
const expressionHighlightField = document.getElementById("expression-highlight");
const contextHighlightField = document.getElementById("context-highlight");
const resultField = document.getElementById("result");
const warningsField = document.getElementById("warnings");
const serverStatusField = document.getElementById("server-status");
const feelVersionField = document.getElementById("feel-version");
const form = document.getElementById("playground-form");
const copyLinkButton = document.getElementById("copy-link");
const importLinkField = document.getElementById("import-link");
const importLinkButton = document.getElementById("import-link-button");
const formatContextButton = document.getElementById("format-context");

function highlightCode(element, text, language) {
  if (language === "json") {
    element.innerHTML = highlightJson(text);
  } else if (language === "javascript") {
    element.innerHTML = highlightExpression(text);
  } else {
    element.textContent = text;
  }
}

function escapeHtml(value) {
  return value
      .replaceAll("&", "&amp;")
      .replaceAll("<", "&lt;")
      .replaceAll(">", "&gt;");
}

function highlightJson(value) {
  const escaped = escapeHtml(value);
  return escaped.replace(
      /("(\\u[\da-fA-F]{4}|\\[^u]|[^\\"])*"(\s*:)?|\btrue\b|\bfalse\b|\bnull\b|-?\d+(?:\.\d+)?(?:[eE][+\-]?\d+)?)/g,
      (token) => {
        if (token.startsWith("\"") && token.endsWith(":")) {
          return `<span class="token-key">${token}</span>`;
        }
        if (token.startsWith("\"")) {
          return `<span class="token-string">${token}</span>`;
        }
        if (token === "true" || token === "false") {
          return `<span class="token-boolean">${token}</span>`;
        }
        if (token === "null") {
          return `<span class="token-null">${token}</span>`;
        }
        return `<span class="token-number">${token}</span>`;
      });
}

function highlightExpression(value) {
  const escaped = escapeHtml(value);
  let highlighted = escaped.replace(
      /"([^"\\]|\\.)*"|'([^'\\]|\\.)*'/g,
      (token) => `<span class="token-string">${token}</span>`);
  highlighted = highlighted.replace(
      /\b(and|or|not|true|false|null)\b/g,
      (token) => `<span class="token-keyword">${token}</span>`);
  highlighted = highlighted.replace(
      /-?\d+(?:\.\d+)?/g,
      (token) => `<span class="token-number">${token}</span>`);
  return highlighted.replace(
      /(&lt;|&gt;|<=|>=|=|!=|\+|-|\*|\/)/g,
      (token) => `<span class="token-operator">${token}</span>`);
}

function base64EncodeUtf8(value) {
  const encoded = new TextEncoder().encode(value);
  let binary = "";
  encoded.forEach((byte) => {
    binary += String.fromCharCode(byte);
  });
  return btoa(binary);
}

function base64DecodeUtf8(value) {
  try {
    const binary = atob(value);
    const bytes = Uint8Array.from(binary, (character) => character.charCodeAt(0));
    return new TextDecoder().decode(bytes);
  } catch (error) {
    return "";
  }
}

function parseJson(text, fallback) {
  if (!text || text.trim() === "") {
    return fallback;
  }
  return JSON.parse(text);
}

function formatJson(value) {
  return JSON.stringify(value, null, 2);
}

function formatContextField() {
  const context = parseJson(contextField.value, {});
  contextField.value = formatJson(context);
  return context;
}

function setEvaluationType(type) {
  expressionTypeField.value = type === "unary-tests" ? "unary-tests" : "expression";
  inputValueGroup.hidden = expressionTypeField.value !== "unary-tests";
}

function setOutput(result, warnings, isError = false) {
  highlightCode(resultField, result, isError ? null : "json");
  warningsField.textContent = formatJson(warnings);
}

function refreshHighlights() {
  highlightCode(expressionHighlightField, expressionField.value, "javascript");
  highlightCode(contextHighlightField, contextField.value, "json");
}

async function loadServerInformation() {
  try {
    const [healthResponse, versionResponse] = await Promise.all([
      fetch("/actuator/health"),
      fetch("/api/v1/version")
    ]);
    const health = await healthResponse.json();
    const version = await versionResponse.json();
    serverStatusField.textContent = health.status || "unknown";
    feelVersionField.textContent = version.feelEngineVersion || "unknown";
  } catch (error) {
    serverStatusField.textContent = "unavailable";
    feelVersionField.textContent = "unknown";
  }
}

function readForm() {
  const expressionType = expressionTypeField.value;
  const expression = expressionField.value;
  const context = formatContextField();

  const payload = { expression, context };
  if (expressionType === "unary-tests") {
    payload.inputValue = parseJson(inputValueField.value, null);
  }

  return { expressionType, payload };
}

async function evaluate(event) {
  event.preventDefault();

  try {
    const { expressionType, payload } = readForm();
    const path =
        expressionType === "unary-tests"
            ? "/api/v1/feel-unary-tests/evaluate"
            : "/api/v1/feel/evaluate";

    const response = await fetch(path, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    });

    const data = await response.json();
    if (data.error) {
      setOutput(data.error, data.warnings || [], true);
    } else {
      setOutput(formatJson(data.result), data.warnings || []);
    }
  } catch (error) {
    setOutput(error.message, [], true);
  } finally {
    refreshHighlights();
  }
}

function buildShareableLink() {
  const expressionType = expressionTypeField.value;
  const params = new URLSearchParams();
  params.set("expression-type", expressionType);
  params.set("expression", base64EncodeUtf8(expressionField.value));
  params.set("context", base64EncodeUtf8(contextField.value || "{}"));
  if (expressionType === "unary-tests") {
    params.set("input-value", base64EncodeUtf8(inputValueField.value || "null"));
  }

  return `${window.location.origin}${window.location.pathname}?${params.toString()}`;
}

async function copyLink() {
  const link = buildShareableLink();
  try {
    await navigator.clipboard.writeText(link);
    copyLinkButton.textContent = "Copied";
    setTimeout(() => {
      copyLinkButton.textContent = "Copy Share Link";
    }, 1200);
  } catch (error) {
    window.prompt("Copy this link", link);
  }
}

function applyQueryParameters(params) {
  const expressionType = params.get("expression-type");
  if (expressionType) {
    setEvaluationType(expressionType);
  }

  const expression = params.get("expression");
  if (expression) {
    expressionField.value = base64DecodeUtf8(expression);
  }

  const context = params.get("context");
  if (context) {
    contextField.value = base64DecodeUtf8(context);
  }

  const inputValue = params.get("input-value");
  if (inputValue) {
    inputValueField.value = base64DecodeUtf8(inputValue);
  }

  try {
    formatContextField();
  } catch (error) {
    // ignore invalid context from imported links
  }
}

function fillFromQueryParameters() {
  const params = new URLSearchParams(window.location.search);
  applyQueryParameters(params);
}

function importLink() {
  try {
    const url = new URL(importLinkField.value.trim(), window.location.origin);
    applyQueryParameters(url.searchParams);
    refreshHighlights();
  } catch (error) {
    setOutput("Invalid link", [], true);
  }
}

expressionTypeField.addEventListener("change", () => {
  setEvaluationType(expressionTypeField.value);
});
expressionField.addEventListener("input", refreshHighlights);
contextField.addEventListener("input", refreshHighlights);
form.addEventListener("submit", evaluate);
copyLinkButton.addEventListener("click", copyLink);
importLinkButton.addEventListener("click", importLink);
formatContextButton.addEventListener("click", () => {
  try {
    formatContextField();
    refreshHighlights();
  } catch (error) {
    setOutput(error.message, []);
  }
});

setEvaluationType("expression");
fillFromQueryParameters();
loadServerInformation();
refreshHighlights();
setOutput("", []);
