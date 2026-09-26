import { FileText, Loader2 } from 'lucide-react';
import { formatTestValue } from '../utils/formatTestValue';

export default function ProblemPanel({ question, error }) {
  return (
    <div className="panel">
      <div className="panel-header">
        <span className="tab tab-active">
          <FileText size={15} className="tab-icon-blue" />
          Description
        </span>
      </div>

      <div className="panel-body problem-body">
        {error ? (
          <div className="empty-state text-red">{error}</div>
        ) : question ? (
          <Description question={question} />
        ) : (
          <div className="empty-state">
            <Loader2 size={18} className="spin" /> Loading question...
          </div>
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

      {question.difficulty && (
        <div className="chips">
          <span className={`chip difficulty-${question.difficulty.toLowerCase()}`}>
            {question.difficulty}
          </span>
        </div>
      )}

      <p className="problem-description">{question.codingQuestion}</p>

      {question.testCases?.map((tc, i) => (
        <div key={i} className="example">
          <p className="example-title">Example {i + 1}:</p>
          <div className="example-block">
            <div>
              <strong>Input:</strong> <span className="pre-wrap">{formatTestValue(tc.input)}</span>
            </div>
            <div>
              <strong>Output:</strong>{' '}
              <span className="pre-wrap">{formatTestValue(tc.output)}</span>
            </div>
          </div>
        </div>
      ))}
    </>
  );
}
