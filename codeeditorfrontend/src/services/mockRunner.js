// Stand-in for POST /runcode until the backend is wired up.
// Replace with a real fetch that sends { programmingLanguage, sourceCode, programmingInput }
// and reads RunCodeResponseDTO.response.
export function runCode({ testCases }) {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve(
        testCases.map((tc) => ({
          input: tc.input,
          expected: tc.output,
          actual: '(not connected to backend)',
          passed: false,
        })),
      );
    }, 800);
  });
}
