// `extension` is what the backend's POST /runcode expects in `programmingLanguage`
// (validated against codeeditor's Constant.java). `monaco` is the editor language id.
//
// startCodingRound now returns a Java-specific driver/stub pair per question
// (questionCodingTemplate / userCodingTemplate) — there's no C++ equivalent yet, so only
// Java is offered until the backend sends a template pair for it too. `fallbackTemplate` is
// only used if a question is loaded without those fields (e.g. an older backend response).
export const LANGUAGES = [
  {
    id: 'java',
    label: 'Java',
    monaco: 'java',
    extension: '.java',
    fallbackTemplate: `public class Main {
    public static void main(String[] args) {
        // Read input from stdin and print the answer
    }
}
`,
  },
];

export const DEFAULT_LANGUAGE = LANGUAGES[0];
