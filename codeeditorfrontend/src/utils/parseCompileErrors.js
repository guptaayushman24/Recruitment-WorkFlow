// Extracts 1-based line numbers referenced in javac/g++ compiler error output, e.g.
// "Main.java:14: error: ';' expected" or "main.cpp:10:5: error: expected ';'".
export function parseCompilerErrorLines(output) {
  if (!output) return [];
  const lines = new Set();
  const pattern = /\.(?:java|cpp):(\d+):/g;
  let match;
  while ((match = pattern.exec(output))) {
    lines.add(Number(match[1]));
  }
  return [...lines];
}

// The compiled file is questionCodingTemplate (driver) + '\n' + the candidate's code, so a
// compiler error's line number is relative to that combined file, not to what's shown in the
// editable editor. This maps combined-file line numbers down to editable-editor line numbers,
// dropping any that fall inside the (hidden/read-only) driver section.
export function toUserCodeLines(combinedFileLines, driverCode) {
  if (!driverCode) return combinedFileLines.filter((line) => line >= 1);
  const driverNewlines = (driverCode.match(/\n/g) || []).length;
  const userStartLine = driverNewlines + 2; // +1 for the joining '\n', +1 to land on the next line
  return combinedFileLines.map((line) => line - userStartLine + 1).filter((line) => line >= 1);
}
