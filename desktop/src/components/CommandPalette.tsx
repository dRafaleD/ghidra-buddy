import React, { useEffect, useState } from 'react';
import type { FunctionItem, StringItem } from '../types';

interface CommandPaletteProps {
  isOpen: boolean;
  onClose: () => void;
  functions: FunctionItem[];
  strings: StringItem[];
  onSelectFunction: (func: FunctionItem) => void;
  onSelectString: (str: StringItem) => void;
  onToggleMode: () => void;
  onOpenGlossary: () => void;
}

export const CommandPalette: React.FC<CommandPaletteProps> = ({
  isOpen,
  onClose,
  functions,
  strings,
  onSelectFunction,
  onSelectString,
  onToggleMode,
  onOpenGlossary,
}) => {
  const [query, setQuery] = useState('');
  const [selectedIndex, setSelectedIndex] = useState(0);

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape' && isOpen) {
        onClose();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  const items: Array<{
    type: 'func' | 'string' | 'action';
    title: string;
    subtitle: string;
    payload: any;
  }> = [];

  items.push({
    type: 'action',
    title: 'Toggle Workspace Mode (Guided / Full)',
    subtitle: 'Workspace Configuration',
    payload: onToggleMode,
  });
  items.push({
    type: 'action',
    title: 'Open Reverse Engineering Reference & Glossary',
    subtitle: 'Documentation Library',
    payload: onOpenGlossary,
  });

  for (const f of functions) {
    items.push({
      type: 'func',
      title: `${f.name}()`,
      subtitle: `${f.entryPoint} • ${f.size} bytes`,
      payload: f,
    });
  }

  for (const s of strings) {
    items.push({
      type: 'string',
      title: `"${s.value}"`,
      subtitle: `${s.address} • XREFs: ${s.xrefCount}`,
      payload: s,
    });
  }

  const filtered = items.filter((item) =>
    item.title.toLowerCase().includes(query.toLowerCase()) ||
    item.subtitle.toLowerCase().includes(query.toLowerCase())
  ).slice(0, 10);

  const handleSelect = (item: typeof items[0]) => {
    if (item.type === 'action') {
      item.payload();
    } else if (item.type === 'func') {
      onSelectFunction(item.payload);
    } else if (item.type === 'string') {
      onSelectString(item.payload);
    }
    onClose();
  };

  return (
    <div
      onClick={onClose}
      style={{
        position: 'fixed',
        inset: 0,
        backgroundColor: 'rgba(0, 0, 0, 0.6)',
        backdropFilter: 'blur(2px)',
        zIndex: 100,
        display: 'flex',
        justifyContent: 'center',
        paddingTop: '60px',
      }}
    >
      <div
        onClick={(e) => e.stopPropagation()}
        style={{
          width: '560px',
          maxHeight: '400px',
          backgroundColor: 'var(--bg-secondary)',
          border: '1px solid var(--border-muted)',
          borderRadius: '6px',
          boxShadow: '0 16px 36px rgba(0, 0, 0, 0.6)',
          overflow: 'hidden',
          display: 'flex',
          flexDirection: 'column',
        }}
      >
        <div style={{ padding: '8px', borderBottom: '1px solid var(--border-subtle)' }}>
          <input
            autoFocus
            type="text"
            placeholder="Type function, address or command to jump..."
            value={query}
            onChange={(e) => {
              setQuery(e.target.value);
              setSelectedIndex(0);
            }}
            style={{
              width: '100%',
              backgroundColor: 'var(--bg-primary)',
              border: '1px solid var(--border-muted)',
              borderRadius: '4px',
              padding: '8px 10px',
              color: 'var(--text-primary)',
              fontSize: '12px',
              fontFamily: 'var(--font-mono)',
              outline: 'none',
            }}
          />
        </div>

        <div style={{ flex: 1, overflowY: 'auto', padding: '4px' }}>
          {filtered.map((item, idx) => (
            <div
              key={idx}
              onClick={() => handleSelect(item)}
              style={{
                padding: '6px 10px',
                borderRadius: '4px',
                cursor: 'pointer',
                backgroundColor: idx === selectedIndex ? 'var(--bg-hover)' : 'transparent',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
              }}
            >
              <div style={{ display: 'flex', flexDirection: 'column' }}>
                <span style={{ fontSize: '12px', color: 'var(--text-primary)', fontFamily: item.type === 'func' || item.type === 'string' ? 'var(--font-mono)' : 'inherit' }}>
                  {item.title}
                </span>
                <span style={{ fontSize: '10px', color: 'var(--text-muted)' }}>
                  {item.subtitle}
                </span>
              </div>
              <span className="badge" style={{ backgroundColor: 'var(--bg-surface)', color: 'var(--text-muted)' }}>
                {item.type}
              </span>
            </div>
          ))}
          {filtered.length === 0 && (
            <div style={{ padding: '20px', textAlign: 'center', color: 'var(--text-muted)', fontSize: '11px' }}>
              No matching symbols found.
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
