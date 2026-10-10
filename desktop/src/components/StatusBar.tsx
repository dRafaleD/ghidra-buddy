import React from 'react';
import type { WorkspaceMode } from '../types';

interface StatusBarProps {
  mode: WorkspaceMode;
  onToggleMode: () => void;
  activeAddress: string;
  functionName: string;
}

export const StatusBar: React.FC<StatusBarProps> = ({
  mode,
  onToggleMode,
  activeAddress,
  functionName,
}) => {
  return (
    <div className="status-bar">
      {/* Left */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '5px' }}>
          <span style={{ width: '6px', height: '6px', borderRadius: '50%', backgroundColor: 'var(--status-ok)' }} />
          <span>GHIDRA ENGINE 12.1.3</span>
        </div>
        <span>|</span>
        <span>beginner_sample.exe</span>
        <span>|</span>
        <span style={{ fontFamily: 'var(--font-mono)' }}>x86:LE:64:gcc</span>
      </div>

      {/* Center / Mode */}
      <div>
        <button
          onClick={onToggleMode}
          style={{
            background: 'transparent',
            border: 'none',
            color: mode === 'BEGINNER' ? 'var(--accent)' : 'var(--text-secondary)',
            fontSize: '10px',
            fontFamily: 'var(--font-mono)',
            cursor: 'pointer',
            fontWeight: 600,
          }}
        >
          [{mode === 'BEGINNER' ? 'MODE: GUIDED' : 'MODE: FULL'}]
        </button>
      </div>

      {/* Right */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
        <span style={{ fontFamily: 'var(--font-mono)' }}>{functionName}()</span>
        <span>|</span>
        <span style={{ fontFamily: 'var(--font-mono)', color: 'var(--text-primary)' }}>{activeAddress}</span>
        <span>|</span>
        <span>UTF-8</span>
      </div>
    </div>
  );
};
