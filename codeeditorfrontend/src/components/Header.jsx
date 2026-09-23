import { useEffect, useState } from 'react';
import { Play, CloudUpload, Timer, Loader2 } from 'lucide-react';

export default function Header({ onRun, onSubmit, running }) {
  return (
    <header className="app-header">
      <div className="brand">Coding Round</div>

      <div className="header-actions">
        <button className="btn btn-secondary" onClick={onRun} disabled={running}>
          {running ? <Loader2 size={15} className="spin" /> : <Play size={15} />}
          Run
        </button>
        <button className="btn btn-primary" onClick={onSubmit} disabled={running}>
          <CloudUpload size={15} />
          Submit
        </button>
      </div>

      <ElapsedTimer />
    </header>
  );
}

function ElapsedTimer() {
  const [seconds, setSeconds] = useState(0);

  useEffect(() => {
    const id = setInterval(() => setSeconds((s) => s + 1), 1000);
    return () => clearInterval(id);
  }, []);

  const pad = (n) => String(n).padStart(2, '0');
  const time = `${pad(Math.floor(seconds / 3600))}:${pad(Math.floor((seconds % 3600) / 60))}:${pad(seconds % 60)}`;

  return (
    <div className="timer">
      <Timer size={15} />
      {time}
    </div>
  );
}
