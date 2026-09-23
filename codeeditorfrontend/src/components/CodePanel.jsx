import { useRef, useState } from 'react';
import Editor from '@monaco-editor/react';
import { Code2, ChevronDown, AlignLeft, RotateCcw, Maximize2, Minimize2 } from 'lucide-react';
import { LANGUAGES } from '../constants/languages';

export default function CodePanel({ language, onLanguageChange, code, onCodeChange, onReset }) {
  const editorRef = useRef(null);
  const [cursor, setCursor] = useState({ line: 1, column: 1 });
  const [maximized, setMaximized] = useState(false);

  const handleMount = (editor) => {
    editorRef.current = editor;
    editor.onDidChangeCursorPosition((e) =>
      setCursor({ line: e.position.lineNumber, column: e.position.column }),
    );
  };

  const defineTheme = (monaco) => {
    monaco.editor.defineTheme('panel-dark', {
      base: 'vs-dark',
      inherit: true,
      rules: [],
      colors: { 'editor.background': '#262626' },
    });
  };

  const formatCode =() => editorRef.current?.getAction('editor.action.formatDocument')?.run();

  return (
    <div className={`panel ${maximized ? 'panel-maximized' : ''}`}>
      <div className="panel-header">
        <span className="tab tab-active">
          <Code2 size={15} className="text-green" />
          Code
        </span>
      </div>

      <div className="editor-toolbar">
        <div className="select-wrap">
          <select
            value={language.id}
            onChange={(e) => onLanguageChange(LANGUAGES.find((l) => l.id === e.target.value))}
          >
            {LANGUAGES.map((l) => (
              <option key={l.id} value={l.id}>
                {l.label}
              </option>
            ))}
          </select>
          <ChevronDown size={14} className="select-caret" />
        </div>

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

      <div className="editor-container">
        <Editor
          language={language.monaco}
          value={code}
          onChange={(v) => onCodeChange(v ?? '')}
          beforeMount={defineTheme}
          onMount={handleMount}
          theme="panel-dark"
          options={{
            fontSize: 14,
            fontFamily: "Menlo, Monaco, 'Courier New', monospace",
            minimap: { enabled: false },
            scrollBeyondLastLine: false,
            automaticLayout: true,
            tabSize: 4,
            padding: { top: 12 },
            renderLineHighlight: 'line',
          }}
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
