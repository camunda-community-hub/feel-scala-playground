const expressionTypeField = document.getElementById("expression-type");
const expressionField = document.getElementById("expression");
const inputValueField = document.getElementById("input-value");
const contextField = document.getElementById("context");
const inputValueGroup = document.getElementById("input-value-group");
const resultField = document.getElementById("result");
const warningsField = document.getElementById("warnings");
const serverStatusField = document.getElementById("server-status");
const feelVersionField = document.getElementById("feel-version");
const form = document.getElementById("playground-form");
const copyLinkButton = document.getElementById("copy-link");

function base64EncodeUtf8(value) {
  const encoded = new TextEncoder().encode(value);
  let binary = "";
  encoded.forEach((byte) => {
    binary += String.fromCharCode(byte);
  });
  return btoa(binary);
}

function base64DecodeUtf8(value) {
  const binary = atob(value);
  const bytes = Uint8Array.from(binary, (character) => character.charCodeAt(0));
  return new TextDecoder().decode(bytes);
}

function parseJson(text, fallback) {
  if (!text || text.trim() === "") {
    return fallback;
  }
  return JSON.parse(text);
}

function setEvaluationType(type) {
  expressionTypeField.value = type === "unary-tests" ? "unary-tests" : "expression";
  inputValueGroup.hidden = expressionTypeField.value !== "unary-tests";
}

function formatJson(value) {
  return JSON.stringify(value, null, 2);
}

function setOutput(result, warnings) {
  resultField.textContent = formatJson(result);
  warningsField.textContent = formatJson(warnings);
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
  const context = parseJson(contextField.value, {});

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
    setOutput(data, data.warnings || []);
  } catch (error) {
    setOutput({ error: error.message }, []);
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

function fillFromQueryParameters() {
  const params = new URLSearchParams(window.location.search);
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
}

expressionTypeField.addEventListener("change", () => {
  setEvaluationType(expressionTypeField.value);
});
form.addEventListener("submit", evaluate);
copyLinkButton.addEventListener("click", copyLink);

setEvaluationType("expression");
fillFromQueryParameters();
loadServerInformation();
setOutput({}, []);
