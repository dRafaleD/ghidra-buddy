import React, { useState } from 'react';
import type { FunctionItem, StringItem } from '../types';
import type { ActivityTab } from './ActivityBar';

interface SidebarProps {
  activeTab: ActivityTab;
  functions: FunctionItem[];
  strings: StringItem[];
  selectedFunction: FunctionItem | null;
  onSelectFunction: (func: FunctionItem) => void;
  onSelectString: (str: StringItem) => void;
}

export const Sidebar: React.FC<SidebarProps> = ({
  activeTab,
  functions,
  strings,
  selectedFunction,
  onSelectFunction,
  onSelectString,
}) => {
  const [filter, setFilter] = useState('');

  if (activeTab === 'assistant' || activeTab === 'glossary') {
    return null;
  }

  const filteredFunctions = functions.filter((f) =>
    f.name.toLowerCase().includes(filter.toLowerCase()) ||
    f.entryPoint.toLowerCase().includes(filter.toLowerCase())
  );

  const filteredStrings = strings.filter((s) =>
    s.value.toLowerCase().includes(filter.toLowerCase()) ||
    s.address.toLowerCase().includes(filter.toLowerCase())
  );

  return (
    <div className="sidebar">
      <div className="sidebar-header">
        <span>{activeTab === 'explorer' ? `Functions (${functions.length})` : `Defined Strings (${strings.length})`}</span>
      </div>

      {/* Search Input */}
      <div style={{ padding: '6px 8px', borderBottom: '1px solid var(--border-subtle)' }}>
        <input
          type="text"
          placeholder={activeTab === 'explorer' ? 'Filter functions or addresses...' : 'Filter string values...'}
          value={filter}
          onChange={(e) => setFilter(e.target.value)}
          style={{
            width: '100%',
            backgroundColor: 'var(--bg-primary)',
            border: '1px solid var(--border-muted)',
            borderRadius: '3px',
            padding: '4px 6px',
            color: 'var(--text-primary)',
            fontSize: '11px',
            fontFamily: 'var(--font-mono)',
            outline: 'none',
          }}
        />
      </div>

      <div className="sidebar-content">
        {activeTab === 'explorer' ? (
          <div>
            {filteredFunctions.map((func) => {
              const isSelected = selectedFunction?.entryPoint === func.entryPoint;
              return (
                <div
                  key={func.entryPoint}
                  onClick={() => onSelectFunction(func)}
                  style={{
                    padding: '4px 8px',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    cursor: 'pointer',
                    backgroundColor: isSelected ? 'var(--bg-hover)' : 'transparent',
                    borderLeft: isSelected ? '2px solid var(--accent)' : '2px solid transparent',
                    fontSize: '11px',
                    fontFamily: 'var(--font-mono)',
                  }}
                >
                  <div style={{ display: 'flex', alignItems: 'center', gap: '6px', overflow: 'hidden' }}>
                    <span className={`badge ${func.isExternal ? 'badge-import' : 'badge-func'}`}>
                      {func.isExternal ? 'IMP' : 'LOC'}
                    </span>
                    <span
                      style={{
                        whiteSpace: 'nowrap',
                        overflow: 'hidden',
                        textOverflow: 'ellipsis',
                        color: isSelected ? 'var(--text-primary)' : 'var(--text-secondary)',
                      }}
                    >
                      {func.name}
                    </span>
                  </div>
                  <span style={{ fontSize: '10px', color: 'var(--text-muted)' }}>
                    {func.entryPoint}
                  </span>
                </div>
              );
            })}
          </div>
        ) : (
          <div>
            {filteredStrings.map((str) => (
              <div
                key={str.address}
                onClick={() => onSelectString(str)}
                style={{
                  padding: '4px 8px',
                  display: 'flex',
                  flexDirection: 'column',
                  gap: '2px',
                  cursor: 'pointer',
                  borderBottom: '1px solid var(--border-subtle)',
                }}
              >
                <div
                  style={{
                    fontSize: '11px',
                    fontFamily: 'var(--font-mono)',
                    color: 'var(--status-ok)',
                    whiteSpace: 'nowrap',
                    overflow: 'hidden',
                    textOverflow: 'ellipsis',
                  }}
                >
                  "{str.value}"
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '9px', color: 'var(--text-muted)' }}>
                  <span style={{ fontFamily: 'var(--font-mono)' }}>{str.address}</span>
                  <span>XREF: {str.xrefCount}</span>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};
