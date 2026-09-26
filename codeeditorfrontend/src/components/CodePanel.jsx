import { useEffect, useRef, useState } from 'react';
import Editor from '@monaco-editor/react';
import { Code2, ChevronRight, AlignLeft, RotateCcw, Maximize2, Minimize2, Lock } from 'lucide-react';
import { formatJavaCode } from '../utils/formatJavaCode';

const defineTheme = (monaco) => {
  monaco.editor.defineTheme('panel-dark', {
    base: 'vs-dark',
    inherit: true,
    rules: [],
    colors: { 'editor.background': '#262626' },
  });
};

const EDITOR_OPTIONS = {
  fontSize: 14,
  fontFamily: "Menlo, Monaco, 'Courier New', monospace",
  minimap: { enabled: false },
  scrollBeyondLastLine: false,
  automaticLayout: true,
  tabSize: 4,
  padding: { top: 12 },
  renderLineHighlight: 'line',
};

export default function CodePanel({ language, driverCode, code, onCodeChange, onReset, errorLines }) {
  const editorRef = useRef(null);
  const monacoRef = useRef(null);
  const decorationIdsRef = useRef([]);
  const [cursor, setCursor] = useState({ line: 1, column: 1 });
  const [maximized, setMaximized] = useState(false);
  const [driverExpanded, setDriverExpanded] = useState(false);

  const handleMount = (editor, monaco) => {
    editorRef.current = editor;
    monacoRef.current = monaco;
    editor.onDidChangeCursorPosition((e) =>
      setCursor({ line: e.position.lineNumber, column: e.position.column }),
    );
  };

  // Monaco has no built-in Java formatter, so this reindents the code directly rather than
  // relying on a document-formatting provider (which would be a no-op for Java).
  const formatCode = () => onCodeChange(formatJavaCode(code));

  useEffect(() => {
    const editor = editorRef.current;
    const monaco = monacoRef.current;
    if (!editor || !monaco) return;

    const decorations = (errorLines ?? []).map((line) => ({
      range: new monaco.Range(line, 1, line, 1),
      options: {
        isWholeLine: true,
        className: 'error-line-highlight',
        linesDecorationsClassName: 'error-line-marker',
      },
    }));
    decorationIdsRef.current = editor.deltaDecorations(decorationIdsRef.current, decorations);
  }, [errorLines]);

  return (
    <div className={`panel ${maximized ? 'panel-maximized' : ''}`}>
      <div className="panel-header">
        <span className="tab tab-active">
          <Code2 size={15} className="text-green" />
          Code
        </span>
      </div>

      <div className="editor-toolbar">
        <span className="language-badge">{language.label}</span>

        <div className="toolbar-actions">
          <IconButton title="Format code" onClick={formatCode}>
            <AlignLeft size={16} />
          </IconButton>
          <IconButton title="Reset to default code" onClick={onReset}>
            <RotateCcw size={16} />
          </IconButton>
          <IconButton
            title={maximized ? 'Exit full screen' : 'Full screen'}
            onClick={() => setMaximized((m) => !m)}
          >
            {maximized ? <Minimize2 size={16} /> : <Maximize2 size={16} />}
          </IconButton>
        </div>
      </div>

      {driverCode && (
        <div className="driver-section">
          <button className="driver-toggle" onClick={() => setDriverExpanded((e) => !e)}>
            <ChevronRight size={14} className={driverExpanded ? 'rotate-90' : ''} />
            <Lock size={12} />
            Driver code (read-only)
          </button>
          {driverExpanded && (
            <div className="driver-editor">
              <Editor
                language={language.monaco}
                value={driverCode}
                beforeMount={defineTheme}
                theme="panel-dark"
                options={{ ...EDITOR_OPTIONS, readOnly: true, domReadOnly: true, lineNumbers: 'off' }}
              />
            </div>
          )}
        </div>
      )}

      <div className="editor-container">
        <Editor
          language={language.monaco}
          value={code}
          onChange={(v) => onCodeChange(v ?? '')}
          beforeMount={defineTheme}
          onMount={handleMount}
          theme="panel-dark"
          options={EDITOR_OPTIONS}
        />
      </div>

      <div className="editor-footer">
        <span>Saved</span>
        <span>
          Ln {cursor.line}, Col {cursor.column}
        </span>
      </div>
    </div>
  );
}

function IconButton({ children, ...props }) {
  return (
    <button className="icon-btn" {...props}>
      {children}
    </button>
  );
}
