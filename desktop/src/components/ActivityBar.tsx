import React from 'react';

export type ActivityTab = 'explorer' | 'strings' | 'assistant' | 'glossary';

interface ActivityBarProps {
  activeTab: ActivityTab;
  onSelectTab: (tab: ActivityTab) => void;
  walkthroughProgress: { completed: number; total: number };
}

export const ActivityBar: React.FC<ActivityBarProps> = ({
  activeTab,
  onSelectTab,
  walkthroughProgress,
}) => {
  return (
    <div className="activity-bar">
      {/* Explorer / Functions */}
      <button
        className={`activity-btn ${activeTab === 'explorer' ? 'active' : ''}`}
        onClick={() => onSelectTab('explorer')}
        title="Functions Explorer"
      >
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
          <rect x="3" y="3" width="18" height="18" rx="2" />
          <path d="M3 9h18M9 21V9" />
        </svg>
      </button>

      {/* Defined Strings */}
      <button
        className={`activity-btn ${activeTab === 'strings' ? 'active' : ''}`}
        onClick={() => onSelectTab('strings')}
        title="Defined Strings Table"
      >
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
          <polyline points="4 7 4 4 20 4 20 7" />
          <line x1="9" y1="20" x2="15" y2="20" />
          <line x1="12" y1="4" x2="12" y2="20" />
        </svg>
      </button>

      {/* Analysis Inspector */}
      <button
        className={`activity-btn ${activeTab === 'assistant' ? 'active' : ''}`}
        onClick={() => onSelectTab('assistant')}
        title="Analysis Inspector & Workflow"
      >
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
          <circle cx="12" cy="12" r="10" />
          <line x1="12" y1="16" x2="12" y2="12" />
          <line x1="12" y1="8" x2="12.01" y2="8" />
        </svg>
        {walkthroughProgress.completed < walkthroughProgress.total && (
          <span
            style={{
              position: 'absolute',
              top: '5px',
              right: '5px',
              width: '5px',
              height: '5px',
              borderRadius: '50%',
              backgroundColor: 'var(--accent)',
            }}
          />
        )}
      </button>

      {/* Technical Glossary */}
      <button
        className={`activity-btn ${activeTab === 'glossary' ? 'active' : ''}`}
        onClick={() => onSelectTab('glossary')}
        title="Binary Analysis Reference"
      >
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">
          <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" />
          <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z" />
        </svg>
      </button>
    </div>
  );
};
