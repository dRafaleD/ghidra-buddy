import React from 'react';
import type { FunctionItem, InstructionItem } from '../types';

interface EditorAreaProps {
  selectedFunction: FunctionItem | null;
  decompiledCode: string;
  disassembly: InstructionItem[];
  activeAddress: string;
  onSelectAddress: (address: string) => void;
  openTabs: string[];
  activeTab: string;
  onSelectTab: (tab: string) => void;
}

export const EditorArea: React.FC<EditorAreaProps> = ({
  selectedFunction,
  decompiledCode,
  disassembly,
  activeAddress,
  onSelectAddress,
  openTabs,
  activeTab,
  onSelectTab,
}) => {
  return (
    <div className="editor-area">
      {/* Tabs Bar */}
      <div className="tabs-bar">
        {openTabs.map((tab) => (
          <div
            key={tab}
            className={`tab ${activeTab === tab ? 'active' : ''}`}
            onClick={() => onSelectTab(tab)}
          >
            <span style={{ fontFamily: 'var(--font-mono)', fontSize: '10px', color: 'var(--accent)' }}>C</span>
            <span>{tab}.c</span>
          </div>
        ))}
      </div>

      {/* Breadcrumbs */}
      <div className="breadcrumbs">
        <span>beginner_sample.exe</span>
        <span>/</span>
        <span>.text</span>
        <span>/</span>
        <span style={{ color: 'var(--text-primary)', fontFamily: 'var(--font-mono)' }}>
          {selectedFunction?.name || 'check_license_key'}()
        </span>
        {activeAddress && (
          <>
            <span>@</span>
            <span style={{ color: 'var(--accent)', fontFamily: 'var(--font-mono)' }}>{activeAddress}</span>
          </>
        )}
      </div>

      {/* Side-by-Side Split Views */}
      <div className="split-views">
        {/* Left: Decompiler C Code */}
        <div className="code-pane">
          <div className="pane-header">
            <span>Decompiler (C Representation)</span>
            <span style={{ fontSize: '9px', fontFamily: 'var(--font-mono)' }}>AST / P-CODE</span>
          </div>
          <div className="pane-body" style={{ padding: '8px 12px', lineHeight: '1.5' }}>
            <pre style={{ margin: 0, color: '#e4e4e7', whiteSpace: 'pre-wrap' }}>
              {decompiledCode.split('\n').map((line, idx) => {
                const isHighlight = line.includes('strcmp') || line.includes('check_license_key');
                return (
                  <div
                    key={idx}
                    style={{
                      display: 'flex',
                      gap: '12px',
                      backgroundColor: isHighlight ? 'var(--accent-muted)' : 'transparent',
                      padding: '1px 2px',
                    }}
                  >
                    <span style={{ color: 'var(--text-muted)', width: '22px', textAlign: 'right', userSelect: 'none' }}>
                      {idx + 1}
                    </span>
                    <span style={{ flex: 1 }}>
                      {colorizeCLine(line)}
                    </span>
                  </div>
                );
              })}
            </pre>
          </div>
        </div>

        {/* Right: Disassembly */}
        <div className="code-pane" style={{ borderRight: 'none' }}>
          <div className="pane-header">
            <span>Disassembly (Listing)</span>
            <span style={{ fontSize: '9px', fontFamily: 'var(--font-mono)' }}>x86_64 LE</span>
          </div>
          <div className="pane-body">
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
              <tbody>
                {disassembly.map((inst) => {
                  const isSelected = inst.address === activeAddress;
                  return (
                    <tr
                      key={inst.address}
                      onClick={() => onSelectAddress(inst.address)}
                      style={{
                        cursor: 'pointer',
                        backgroundColor: isSelected ? 'var(--accent-muted)' : 'transparent',
                        borderLeft: isSelected ? '2px solid var(--accent)' : '2px solid transparent',
                        height: '20px',
                      }}
                    >
                      <td style={{ padding: '2px 6px', color: 'var(--text-muted)', width: '100px' }}>
                        {inst.address}
                      </td>
                      <td style={{ padding: '2px 6px', color: '#52525b', width: '120px', fontSize: '10px' }}>
                        {inst.bytes}
                      </td>
                      <td
                        style={{
                          padding: '2px 6px',
                          color: inst.isCall ? '#fbbf24' : inst.isBranch ? '#60a5fa' : '#e4e4e7',
                          fontWeight: 600,
                          width: '60px',
                        }}
                      >
                        {inst.mnemonic}
                      </td>
                      <td style={{ padding: '2px 6px', color: '#d4d4d8' }}>
                        {inst.operands}
                      </td>
                      <td style={{ padding: '2px 6px', color: '#10b981', fontSize: '10px' }}>
                        {inst.comment ? `// ${inst.comment}` : ''}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
};

function colorizeCLine(line: string) {
  if (line.trim().startsWith('/*') || line.trim().startsWith('*')) {
    return <span style={{ color: '#10b981' }}>{line}</span>;
  }
  if (line.includes('return ') || line.includes('if ') || line.includes('int ') || line.includes('char ')) {
    return <span style={{ color: '#93c5fd' }}>{line}</span>;
  }
  if (line.includes('"')) {
    return <span style={{ color: '#fbbf24' }}>{line}</span>;
  }
  return line;
}
