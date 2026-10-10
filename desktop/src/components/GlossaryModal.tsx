import React, { useEffect, useState } from 'react';
import type { GlossaryEntry } from '../types';
import { api } from '../services/api';

interface GlossaryModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const GlossaryModal: React.FC<GlossaryModalProps> = ({ isOpen, onClose }) => {
  const [entries, setEntries] = useState<GlossaryEntry[]>([]);
  const [search, setSearch] = useState('');
  const [selectedEntry, setSelectedEntry] = useState<GlossaryEntry | null>(null);

  useEffect(() => {
    if (isOpen) {
      api.getGlossary(search).then((data) => {
        setEntries(data);
        if (data.length > 0 && !selectedEntry) {
          setSelectedEntry(data[0]);
        }
      });
    }
  }, [isOpen, search]);

  if (!isOpen) return null;

  return (
    <div
      onClick={onClose}
      style={{
        position: 'fixed',
        inset: 0,
        backgroundColor: 'rgba(0, 0, 0, 0.65)',
        backdropFilter: 'blur(3px)',
        zIndex: 110,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
      }}
    >
      <div
        onClick={(e) => e.stopPropagation()}
        style={{
          width: '740px',
          height: '500px',
          backgroundColor: 'var(--bg-secondary)',
          border: '1px solid var(--border-muted)',
          borderRadius: '6px',
          display: 'flex',
          flexDirection: 'column',
          overflow: 'hidden',
          boxShadow: '0 20px 40px rgba(0, 0, 0, 0.6)',
        }}
      >
        {/* Header */}
        <div
          style={{
            padding: '10px 14px',
            borderBottom: '1px solid var(--border-subtle)',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
          }}
        >
          <span style={{ fontSize: '12px', fontWeight: 600, letterSpacing: '0.3px', textTransform: 'uppercase', color: 'var(--text-secondary)' }}>
            Binary Analysis Reference Database
          </span>
          <button
            onClick={onClose}
            className="btn-sm"
          >
            Esc
          </button>
        </div>

        {/* Search */}
        <div style={{ padding: '8px 12px', borderBottom: '1px solid var(--border-subtle)' }}>
          <input
            type="text"
            placeholder="Search concepts (XREF, Calling Conventions, Stack Frame, PE Sections)..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            style={{
              width: '100%',
              backgroundColor: 'var(--bg-primary)',
              border: '1px solid var(--border-muted)',
              borderRadius: '4px',
              padding: '6px 10px',
              color: 'var(--text-primary)',
              fontSize: '11px',
              fontFamily: 'var(--font-mono)',
              outline: 'none',
            }}
          />
        </div>

        {/* Body Split */}
        <div style={{ flex: 1, display: 'grid', gridTemplateColumns: '240px 1fr', overflow: 'hidden' }}>
          {/* List */}
          <div style={{ borderRight: '1px solid var(--border-subtle)', overflowY: 'auto', padding: '4px' }}>
            {entries.map((entry) => (
              <div
                key={entry.term}
                onClick={() => setSelectedEntry(entry)}
                style={{
                  padding: '6px 8px',
                  borderRadius: '3px',
                  cursor: 'pointer',
                  backgroundColor: selectedEntry?.term === entry.term ? 'var(--bg-hover)' : 'transparent',
                  marginBottom: '2px',
                }}
              >
                <div style={{ fontSize: '11px', fontWeight: 500, color: 'var(--text-primary)' }}>
                  {entry.term}
                </div>
                <div style={{ fontSize: '9px', color: 'var(--text-muted)', marginTop: '1px' }}>
                  {entry.category}
                </div>
              </div>
            ))}
          </div>

          {/* Details */}
          <div style={{ padding: '16px', overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: '12px' }}>
            {selectedEntry ? (
              <>
                <div>
                  <div style={{ fontSize: '15px', fontWeight: 600, color: 'var(--text-primary)', fontFamily: 'var(--font-mono)' }}>
                    {selectedEntry.term}
                  </div>
                  <div style={{ fontSize: '10px', color: 'var(--accent)', textTransform: 'uppercase', letterSpacing: '0.4px', marginTop: '2px' }}>
                    Domain: {selectedEntry.category}
                  </div>
                </div>

                <div style={{ fontSize: '12px', color: '#e4e4e7', lineHeight: '1.5' }}>
                  {selectedEntry.summary}
                </div>

                <div
                  style={{
                    backgroundColor: 'var(--bg-tertiary)',
                    borderRadius: '4px',
                    padding: '10px',
                    border: '1px solid var(--border-subtle)',
                  }}
                >
                  <div style={{ fontSize: '10px', fontWeight: 600, color: 'var(--text-muted)', textTransform: 'uppercase', marginBottom: '4px' }}>
                    Technical Elaboration
                  </div>
                  <div style={{ fontSize: '11px', color: 'var(--text-secondary)', lineHeight: '1.5' }}>
                    {selectedEntry.detailedDescription}
                  </div>
                </div>

                <div
                  style={{
                    backgroundColor: 'var(--accent-muted)',
                    borderRadius: '4px',
                    padding: '8px 10px',
                    border: '1px solid rgba(59, 130, 246, 0.25)',
                  }}
                >
                  <div style={{ fontSize: '10px', fontWeight: 600, color: 'var(--accent)', textTransform: 'uppercase', marginBottom: '2px' }}>
                    Practical Analysis Note
                  </div>
                  <div style={{ fontSize: '11px', color: '#bfdbfe', lineHeight: '1.4' }}>
                    {selectedEntry.beginnerTip}
                  </div>
                </div>
              </>
            ) : (
              <div style={{ color: 'var(--text-muted)', fontSize: '11px' }}>Select an entry from the left index.</div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
