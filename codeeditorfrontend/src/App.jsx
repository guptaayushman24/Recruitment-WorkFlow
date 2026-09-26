import { useEffect, useState } from 'react';
import './App.css';
import { Group, Panel, Separator } from 'react-resizable-panels';
import Header from './components/Header';
import ProblemPanel from './components/ProblemPanel';
import CodePanel from './components/CodePanel';
import ConsolePanel from './components/ConsolePanel';
import { DEFAULT_LANGUAGE } from './constants/languages';
import { fetchCodingQuestion } from './services/api';
import { runTestCases } from './services/runCode';
import { parseCompilerErrorLines, toUserCodeLines } from './utils/parseCompileErrors';

// Only Java is offered right now (see constants/languages.js), so the language is fixed.
const language = DEFAULT_LANGUAGE;

export default function App() {
  const [question, setQuestion] = useState(null);
  const [questionError, setQuestionError] = useState(null);
  // The candidate's editable Solution code. Seeded from the question's userCodingTemplate
  // once it loads; falls back to a generic template if the backend didn't send one.
  const [code, setCode] = useState(language.fallbackTemplate);
  const [consoleTab, setConsoleTab] = useState('testcase');
  const [results, setResults] = useState(null);
  const [running, setRunning] = useState(false);
  // Line numbers (in the editable editor) to highlight red, parsed from the compiler's output.
  const [errorLines, setErrorLines] = useState([]);

  useEffect(() => {
    let ignore = false;
    fetchCodingQuestion()
      .then((q) => {
        if (ignore) return;
        setQuestion(q);
        setCode(q.userCodingTemplate ?? language.fallbackTemplate);
        setErrorLines([]);
      })
      .catch((err) => !ignore && setQuestionError(err.message));
    return () => {
      ignore = true;
    };
  }, []);

  const testCases = question?.testCases ?? [];

  // Editing the code invalidates any error highlights left over from the last run.
  const updateCode = (value) => {
    setCode(value);
    setErrorLines([]);
  };

  const execute = async () => {
    setRunning(true);
    setConsoleTab('result');
    setErrorLines([]);
    try {
      const driverCode = question?.questionCodingTemplate;
      const res = await runTestCases({
        questionId: question?.id,
        languageExtension: language.extension,
        driverCode,
        userCode: code,
        testCases,
      });
      setResults(res);

      const combinedErrorLines = res.flatMap((r) => parseCompilerErrorLines(r.actual));
      setErrorLines(toUserCodeLines(combinedErrorLines, driverCode));

      return res;
    } finally {
      setRunning(false);
    }
  };

  return (
    <div className="app">
      <Header onRun={execute} running={running} />

      <Group orientation="horizontal" className="workspace">
        <Panel defaultSize="42" minSize="20">
          <ProblemPanel question={question} error={questionError} />
        </Panel>
        <Separator className="resize-handle resize-handle-vertical" />
        <Panel minSize="30">
          <Group orientation="vertical">
            <Panel defaultSize="60" minSize="15">
              <CodePanel
                language={language}
                driverCode={question?.questionCodingTemplate}
                code={code}
                onCodeChange={updateCode}
                onReset={() => updateCode(question?.userCodingTemplate ?? language.fallbackTemplate)}
                errorLines={errorLines}
              />
            </Panel>
            <Separator className="resize-handle resize-handle-horizontal" />
            <Panel minSize="10">
              <ConsolePanel
                activeTab={consoleTab}
                onTabChange={setConsoleTab}
                testCases={testCases}
                results={results}
                running={running}
              />
            </Panel>
          </Group>
        </Panel>
      </Group>
    </div>
  );
}
