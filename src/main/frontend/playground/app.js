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
const importPanel = document.getElementById("import-panel");
const formatContextButton = document.getElementById("format-context");

const modeState = {
  expression: {
    expression: "x + 3",
    context: "{\n  \"x\": 5\n}",
    inputValue: "5"
  },
  "unary-tests": {
    expression: "< 3",
    context: "{}",
    inputValue: "5"
  }
};

let activeMode = "expression";

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
  return escaped.replace(
      /"([^"\\]|\\.)*"|'([^'\\]|\\.)*'|\b(?:and|or|not|between|in|true|false|null)\b|-?\d+(?:\.\d+)?(?:[eE][+\-]?\d+)?|<=|>=|!=|[<>+\-*/=(),.\[\]{}]/g,
      (token) => {
        if (/^"/.test(token) || /^'/.test(token)) {
          return `<span class="token-string">${token}</span>`;
        }
        if (/^(and|or|not|between|in|true|false|null)$/.test(token)) {
          return `<span class="token-keyword">${token}</span>`;
        }
        if (/^-?\d/.test(token)) {
          return `<span class="token-number">${token}</span>`;
        }
        return `<span class="token-operator">${token}</span>`;
      });
}

function highlightCode(element, text, language) {
  if (language === "json") {
    element.innerHTML = `${highlightJson(text)}\n`;
  } else if (language === "feel") {
    element.innerHTML = `${highlightExpression(text)}\n`;
  } else {
    element.textContent = text;
  }
}

function syncEditorScroll(textarea, highlight) {
  highlight.scrollTop = textarea.scrollTop;
  highlight.scrollLeft = textarea.scrollLeft;
}

function formatJson(value) {
  return JSON.stringify(value, null, 2);
}

function parseJson(text, fallback) {
  if (!text || text.trim() === "") {
    return fallback;
  }
  return JSON.parse(text);
}

function clearOutput() {
  resultField.textContent = "";
  warningsField.textContent = "";
}

function formatWarnings(warnings) {
  if (!Array.isArray(warnings) || warnings.length === 0) {
    return "";
  }

  return warnings
      .map((warning) => {
        if (typeof warning === "string") {
          return `[warning] ${warning}`;
        }

        const type = warning.type || "warning";
        const message = warning.message || formatJson(warning);
        return `[${type}] ${message}`;
      })
      .join("\n");
}

function setOutput(result, warnings, isError = false) {
  highlightCode(resultField, result, isError ? null : "json");
  warningsField.textContent = formatWarnings(warnings);
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

function formatContextField() {
  const context = parseJson(contextField.value, {});
  contextField.value = formatJson(context);
  return context;
}

function captureCurrentMode() {
  modeState[activeMode] = {
    expression: expressionField.value,
    context: contextField.value,
    inputValue: inputValueField.value
  };
}

function applyMode(mode) {
  activeMode = mode === "unary-tests" ? "unary-tests" : "expression";
  expressionTypeField.value = activeMode;
  inputValueGroup.hidden = activeMode !== "unary-tests";

  const preset = modeState[activeMode];
  expressionField.value = preset.expression;
  contextField.value = preset.context;
  inputValueField.value = preset.inputValue;

  refreshHighlights();
}

function switchMode(mode) {
  captureCurrentMode();
  applyMode(mode);
}

function refreshHighlights() {
  highlightCode(expressionHighlightField, expressionField.value, "feel");
  highlightCode(contextHighlightField, contextField.value, "json");
  syncEditorScroll(expressionField, expressionHighlightField);
  syncEditorScroll(contextField, contextHighlightField);
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

async function evaluateCurrent() {
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

async function evaluate(event) {
  event.preventDefault();
  await evaluateCurrent();
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
    applyMode(expressionType);
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

  captureCurrentMode();
  refreshHighlights();
}

function fillFromQueryParameters() {
  const params = new URLSearchParams(window.location.search);
  if (params.size > 0) {
    applyQueryParameters(params);
  }
}

async function importLink() {
  clearOutput();
  try {
    const url = new URL(importLinkField.value.trim(), window.location.origin);
    applyQueryParameters(url.searchParams);
    importLinkField.value = "";
    importPanel.open = false;
    await evaluateCurrent();
  } catch (error) {
    setOutput("Invalid link", [], true);
  }
}

expressionTypeField.addEventListener("change", () => {
  switchMode(expressionTypeField.value);
});
expressionField.addEventListener("input", () => {
  captureCurrentMode();
  refreshHighlights();
});
expressionField.addEventListener("scroll", () => {
  syncEditorScroll(expressionField, expressionHighlightField);
});
contextField.addEventListener("input", () => {
  captureCurrentMode();
  refreshHighlights();
});
contextField.addEventListener("scroll", () => {
  syncEditorScroll(contextField, contextHighlightField);
});
inputValueField.addEventListener("input", captureCurrentMode);
form.addEventListener("submit", evaluate);
copyLinkButton.addEventListener("click", copyLink);
importLinkButton.addEventListener("click", importLink);
formatContextButton.addEventListener("click", () => {
  try {
    formatContextField();
    captureCurrentMode();
    refreshHighlights();
  } catch (error) {
    setOutput(error.message, [], true);
  }
});

applyMode("expression");
fillFromQueryParameters();
loadServerInformation();
clearOutput();
