// The backend sends test case input/output as raw JSON values (object, array, primitive)
// rather than pre-formatted strings. This renders them the way LeetCode-style UIs do,
// e.g. { s: "leetcode", wordDict: ["leet","code"] } -> `s = "leetcode"\nwordDict = ["leet","code"]`.
export function formatTestValue(value) {
  if (value === null || value === undefined) return String(value);

  if (Array.isArray(value) || typeof value !== 'object') {
    return JSON.stringify(value);
  }

  return Object.entries(value)
    .map(([key, val]) => `${key} = ${JSON.stringify(val)}`)
    .join('\n');
}
