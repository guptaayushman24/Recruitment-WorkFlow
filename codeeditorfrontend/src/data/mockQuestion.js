// Placeholder data until the UI is wired to the backend.
// Shape mirrors codeeditor's CodingQuestion DTO (GET /startCodingRound): { id, title, codingQuestion }.
// `difficulty` and `sampleTestCases` are UI-only extras for now.
export const mockQuestion = {
  id: 1658,
  title: 'Minimum Operations to Reduce X to Zero',
  codingQuestion: `You are given an integer array nums and an integer x. In one operation, you can either remove the leftmost or the rightmost element from the array nums and subtract its value from x. Note that this modifies the array for future operations.

Return the minimum number of operations to reduce x to exactly 0 if it is possible, otherwise, return -1.`,
  difficulty: 'Medium',
  sampleTestCases: [
    {
      input: 'nums = [1,1,4,2,3], x = 5',
      output: '2',
      explanation: 'The optimal solution is to remove the last two elements to reduce x to zero.',
    },
    { input: 'nums = [5,6,7,8,9], x = 4', output: '-1' },
    {
      input: 'nums = [3,2,20,1,1,3], x = 10',
      output: '5',
      explanation:
        'The optimal solution is to remove the last three elements and the first two elements (5 operations in total) to reduce x to zero.',
    },
  ],
};
