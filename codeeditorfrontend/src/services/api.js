// Requests go to /api, which vite.config.js proxies to the codeeditor backend (localhost:8087).
const API_BASE = '/api';

// GET /startCodingRound → an array with one CodingQuestion:
// {
//   id, title, codingQuestion,
//   questionCodingTemplate,  // driver code (reads stdin, calls the Solution method, prints
//                             // the result) — shown read-only, never edited by the candidate
//   userCodingTemplate,      // the Solution class stub — this is what the candidate edits
//   testCases: [{ input, output, isTestCasePassedORFailed }]
// }
// `input`/`output` are raw JSON (object, array, or primitive), not pre-formatted strings.
// sourceCode sent to /runcode must be questionCodingTemplate + the candidate's edited code
// (Main and Solution as two top-level classes in one file) — see services/runCode.js.
export async function fetchCodingQuestion() {
  const res = await fetch(`${API_BASE}/startCodingRound`);
  if (!res.ok) {
    throw new Error(`Failed to load question (HTTP ${res.status})`);
  }
  const data = await res.json();
  return Array.isArray(data) ? data[0] : data;
}

// POST /runcode → runs sourceCode once against a single programmingInput and returns
// SampleCodingTestCaseDTO: { input, output, isTestCasePassedORFailed } where output is the
// program's raw stdout (or an error/compile message) and isTestCasePassedORFailed is 1/0/null.
// `programmingLanguage` must be the extension form (".java" / ".cpp").
export async function postRunCode({ programmingLanguage, sourceCode, programmingInput, id }) {
  const res = await fetch(`${API_BASE}/runcode`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ programmingLanguage, sourceCode, programmingInput, id }),
  });
  if (!res.ok) {
    throw new Error(`Run failed (HTTP ${res.status})`);
  }
  return res.json();
}
