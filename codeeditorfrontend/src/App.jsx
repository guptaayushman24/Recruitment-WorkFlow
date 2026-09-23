import { useState } from 'react';
import './App.css';
import { Group, Panel, Separator } from 'react-resizable-panels';
import Header from './components/Header';
import ProblemPanel from './components/ProblemPanel';
import CodePanel from './components/CodePanel';
import ConsolePanel from './components/ConsolePanel';
import { DEFAULT_LANGUAGE, LANGUAGES } from './constants/languages';
import { mockQuestion } from './data/mockQuestion';
import { runCode } from './services/mockRunner';

const initialCode = Object.fromEntries(LANGUAGES.map((l) => [l.id, l.template]));

export default function App() {
  const [question] = useState(mockQuestion);
  const [language, setLanguage] = useState(DEFAULT_LANGUAGE);
  // Code is kept per language so switching languages doesn't lose work.
  const [codeByLanguage, setCodeByLanguage] = useState(initialCode);
  const [consoleTab, setConsoleTab] = useState('testcase');
  const [results, setResults] = useState(null);
  const [running, setRunning] = useState(false);
  const [submissions, setSubmissions] = useState([]);

  const code = codeByLanguage[language.id];
  const setCode = (value) => setCodeByLanguage((prev) => ({ ...prev, [language.id]: value }));

  const execute = async () => {
    setRunning(true);
    setConsoleTab('result');
    try {
      const res = await runCode({
        language: language.extension,
        sourceCode: code,
        testCases: question.sampleTestCases,
      });
      setResults(res);
      return res;
    } finally {
      setRunning(false);
    }
  };

  const handleSubmit = async () => {
    const res = await execute();
    setSubmissions((prev) => [
      {
        passed: res.every((r) => r.passed),
        language: language.label,
        time: new Date().toLocaleTimeString(),
      },
      ...prev,
    ]);
  };

  return (
    <div className="app">
      <Header onRun={execute} onSubmit={handleSubmit} running={running} />

      <Group orientation="horizontal" className="workspace">
        <Panel defaultSize="42" minSize="20">
          <ProblemPanel question={question} submissions={submissions} />
        </Panel>
        <Separator className="resize-handle resize-handle-vertical" />
        <Panel minSize="30">
          <Group orientation="vertical">
            <Panel defaultSize="60" minSize="15">
              <CodePanel
                language={language}
                onLanguageChange={setLanguage}
                code={code}
                onCodeChange={setCode}
                onReset={() => setCode(language.template)}
              />
            </Panel>
            <Separator className="resize-handle resize-handle-horizontal" />
            <Panel minSize="10">
              <ConsolePanel
                activeTab={consoleTab}
                onTabChange={setConsoleTab}
                testCases={question.sampleTestCases}
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
