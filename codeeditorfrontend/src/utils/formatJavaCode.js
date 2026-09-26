// Monaco ships no built-in formatter for Java, so the toolbar's "Format code" button would
// otherwise do nothing. This is a lightweight brace-based reindenter: it doesn't parse the
// language (so a brace inside a string or comment can throw off the depth count), but it
// handles ordinary method bodies well enough for interview-style solutions.
export function formatJavaCode(source) {
  const INDENT = '    ';
  const lines = source.split('\n');
  let depth = 0;
  const out = [];

  for (const rawLine of lines) {
    const line = rawLine.trim();
    if (!line) {
      out.push('');
      continue;
    }

    // A line that starts by closing a block dedents itself, not just what follows it.
    const startsWithCloser = /^[}\])]/.test(line);
    const lineDepth = startsWithCloser ? Math.max(depth - 1, 0) : depth;
    out.push(INDENT.repeat(lineDepth) + line);

    const opens = (line.match(/[{([]/g) || []).length;
    const closes = (line.match(/[})\]]/g) || []).length;
    depth = Math.max(depth + opens - closes, 0);
  }

  return out.join('\n');
}
