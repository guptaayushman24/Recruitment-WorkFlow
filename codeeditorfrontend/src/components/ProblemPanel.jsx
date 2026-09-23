import { useState } from 'react';
import { FileText, History } from 'lucide-react';

const TABS = [
  { id: 'description', label: 'Description', icon: FileText },
  { id: 'submissions', label: 'Submissions', icon: History },
];

export default function ProblemPanel({ question, submissions }) {
  const [activeTab, setActiveTab] = useState('description');

  return (
    <div className="panel">
      <div className="panel-header">
        {TABS.map(({ id, label, icon: Icon }, i) => (
          <div key={id} className="tab-wrap">
            {i > 0 && <span className="tab-divider" />}
            <button
              className={`tab ${activeTab === id ? 'tab-active' : ''}`}
              onClick={() => setActiveTab(id)}
            >
              <Icon size={15} className="tab-icon-blue" />
              {label}
            </button>
          </div>
        ))}
      </div>

      <div className="panel-body problem-body">
        {activeTab === 'description' ? (
          <Description question={question} />
        ) : (
          <Submissions submissions={submissions} />
        )}
      </div>
    </div>
  );
}

function Description({ question }) {
  return (
    <>
      <h1 className="problem-title">
        {question.id}. {question.title}
      </h1>

      <div className="chips">
        <span className={`chip difficulty-${question.difficulty?.toLowerCase()}`}>
          {question.difficulty}
        </span>
      </div>

      <p className="problem-description">{question.codingQuestion}</p>

      {question.sampleTestCases?.map((tc, i) => (
        <div key={i} className="example">
          <p className="example-title">Example {i + 1}:</p>
          <div className="example-block">
            <div>
              <strong>Input:</strong> {tc.input}
            </div>
            <div>
              <strong>Output:</strong> {tc.output}
            </div>
            {tc.explanation && (
              <div>
                <strong>Explanation:</strong> {tc.explanation}
              </div>
            )}
          </div>
        </div>
      ))}
    </>
  );
}

function Submissions({ submissions }) {
  if (submissions.length === 0) {
    return <div className="empty-state">No submissions yet</div>;
  }
  return (
    <table className="submissions-table">
      <thead>
        <tr>
          <th>Status</th>
          <th>Language</th>
          <th>Time</th>
        </tr>
      </thead>
      <tbody>
        {submissions.map((s, i) => (
          <tr key={i}>
            <td className={s.passed ? 'text-green' : 'text-red'}>
              {s.passed ? 'Accepted' : 'Wrong Answer'}
            </td>
            <td>{s.language}</td>
            <td>{s.time}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
