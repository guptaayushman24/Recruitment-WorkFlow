import { useState } from 'react';
import { SquareCheck, Terminal, Loader2 } from 'lucide-react';

export default function ConsolePanel({ activeTab, onTabChange, testCases, results, running }) {
  return (
    <div className="panel">
      <div className="panel-header">
        <button
          className={`tab ${activeTab === 'testcase' ? 'tab-active' : ''}`}
          onClick={() => onTabChange('testcase')}
        >
          <SquareCheck size={15} className="text-green" />
          Testcase
        </button>
        <span className="tab-divider" />
        <button
          className={`tab ${activeTab === 'result' ? 'tab-active' : ''}`}
          onClick={() => onTabChange('result')}
        >
          <Terminal size={15} className="text-green" />
          Test Result
        </button>
      </div>

      <div className="panel-body">
        {activeTab === 'testcase' ? (
          <TestCases testCases={testCases} />
        ) : (
          <TestResults results={results} running={running} />
        )}
      </div>
    </div>
  );
}

function CaseTabs({ items, active, onSelect, renderLabel }) {
  return (
    <div className="case-tabs">
      {items.map((item, i) => (
        <button
          key={i}
          className={`case-tab ${active === i ? 'case-tab-active' : ''}`}
          onClick={() => onSelect(i)}
        >
          {renderLabel(item, i)}
        </button>
      ))}
    </div>
  );
}

function TestCases({ testCases }) {
  const [active, setActive] = useState(0);
  if (!testCases.length) return <div className="empty-state">No test cases</div>;

  return (
    <>
      <CaseTabs
        items={testCases}
        active={active}
        onSelect={setActive}
        renderLabel={(_, i) => `Case ${i + 1}`}
      />
      <Field label="Input" value={testCases[active].input} />
    </>
  );
}

function TestResults({ results, running }) {
  const [active, setActive] = useState(0);

  if (running) {
    return (
      <div className="empty-state">
        <Loader2 size={18} className="spin" /> Running...
      </div>
    );
  }
  if (!results) return <div className="empty-state">You must run your code first</div>;

  const allPassed = results.every((r) => r.passed);
  const current = results[Math.min(active, results.length - 1)];

  return (
    <>
      <h3 className={`result-status ${allPassed ? 'text-green' : 'text-red'}`}>
        {allPassed ? 'Accepted' : 'Wrong Answer'}
      </h3>
      <CaseTabs
        items={results}
        active={active}
        onSelect={setActive}
        renderLabel={(r, i) => (
          <>
            <span className={`dot ${r.passed ? 'dot-green' : 'dot-red'}`} />
            Case {i + 1}
          </>
        )}
      />
      <Field label="Input" value={current.input} />
      <Field label="Output" value={current.actual} />
      <Field label="Expected" value={current.expected} />
    </>
  );
}

function Field({ label, value }) {
  return (
    <div className="field">
      <div className="field-label">{label}</div>
      <pre className="field-value">{value}</pre>
    </div>
  );
}
