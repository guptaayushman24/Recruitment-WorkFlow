import { postRunCode } from './api';
import { formatTestValue } from '../utils/formatTestValue';

// The backend runs one test case per /runcode call (single programmingInput in, single result
// out), and it compiles/writes to a fixed filename per language with no per-request isolation
// (see codeeditor's CLAUDE.md) — concurrent requests for the same language would clobber each
// other's source/binary. So test cases are run one at a time, in order, not in parallel.
export async function runTestCases({ questionId, languageExtension, driverCode, userCode, testCases }) {
  // The driver (Main, reads stdin / calls the Solution method / prints the result) and the
  // candidate's Solution class are two top-level classes compiled together as one file.
  const sourceCode = driverCode ? `${driverCode}\n${userCode}` : userCode;
  const results = [];

  for (const testCase of testCases) {
    const expected = formatTestValue(testCase.output);
    try {
      const result = await postRunCode({
        programmingLanguage: languageExtension,
        sourceCode,
        programmingInput: testCase.input,
        id: questionId,
      });
      results.push({
        input: formatTestValue(testCase.input),
        expected,
        actual: typeof result.output === 'string' ? result.output : formatTestValue(result.output),
        passed: result.isTestCasePassedORFailed === 1,
      });
    } catch (err) {
      results.push({
        input: formatTestValue(testCase.input),
        expected,
        actual: err.message,
        passed: false,
      });
    }
  }

  return results;
}
