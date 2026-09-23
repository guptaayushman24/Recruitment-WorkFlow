// `extension` is what the backend's POST /runcode expects in `programmingLanguage`
// (validated against codeeditor's Constant.java). `monaco` is the editor language id.
// Only Java and C++ are executable on the backend today.
export const LANGUAGES = [
  {
    id: 'java',
    label: 'Java',
    monaco: 'java',
    extension: '.java',
    template: `public class Main {
    public static void main(String[] args) {
        // Read input from stdin and print the answer
    }
}
`,
  },
  {
    id: 'cpp',
    label: 'C++',
    monaco: 'cpp',
    extension: '.cpp',
    template: `#include <bits/stdc++.h>
using namespace std;

int main() {
    // Read input from stdin and print the answer
    return 0;
}
`,
  },
];

export const DEFAULT_LANGUAGE = LANGUAGES[0];
