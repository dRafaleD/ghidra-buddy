import React from 'react';
import type { CodeContext, WalkthroughStep, WorkspaceMode } from '../types';

interface LearningAssistantPanelProps {
  context: CodeContext | null;
  mode: WorkspaceMode;
  onToggleMode: () => void;
  walkthrough: { completedCount: number; totalCount: number; steps: WalkthroughStep[] };
  onToggleStep: (stepNumber: number) => void;
  onExecuteAction: (targetAddress: string) => void;
}

export const LearningAssistantPanel: React.FC<LearningAssistantPanelProps> = ({
  context,
  mode,
  onToggleMode,
  walkthrough,
  onToggleStep,
  onExecuteAction,
}) => {
  return (
    <div className="learning-panel">
      {/* Panel Header */}
      <div className="sidebar-header">
        <span>Context Inspector</span>
        <button
          onClick={onToggleMode}
          className="btn-sm"
          style={{
            borderColor: mode === 'BEGINNER' ? 'var(--border-focus)' : 'var(--border-muted)',
            color: mode === 'BEGINNER' ? 'var(--accent)' : 'var(--text-secondary)',
          }}
        >
          {mode === 'BEGINNER' ? 'Mode: Guided' : 'Mode: Full'}
        </button>
      </div>

      <div style={{ flex: 1, overflowY: 'auto', padding: '10px' }}>
        {/* Context Card */}
        {context ? (
          <div
            style={{
              backgroundColor: 'var(--bg-tertiary)',
              border: '1px solid var(--border-subtle)',
              borderRadius: '4px',
              padding: '10px',
              display: 'flex',
              flexDirection: 'column',
              gap: '10px',
            }}
          >
            {/* Context Identification */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
              <div>
                <div style={{ fontSize: '13px', fontWeight: 600, fontFamily: 'var(--font-mono)' }}>
                  {context.itemName || 'Selected Entity'}
                </div>
                <div style={{ fontSize: '10px', color: 'var(--accent)', marginTop: '1px', textTransform: 'uppercase', letterSpacing: '0.4px' }}>
                  {context.typeLabelEn}
                </div>
              </div>
              <span
                style={{
                  fontSize: '10px',
                  fontFamily: 'var(--font-mono)',
                  color: 'var(--text-muted)',
                  backgroundColor: 'var(--bg-primary)',
                  padding: '1px 5px',
                  borderRadius: '2px',
                  border: '1px solid var(--border-subtle)',
                }}
              >
                {context.address}
              </span>
            </div>

            {/* Assessment */}
            <div>
              <div style={{ fontSize: '10px', fontWeight: 600, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.4px', marginBottom: '2px' }}>
                Definition & Semantics
              </div>
              <div style={{ color: 'var(--text-primary)', fontSize: '11px', lineHeight: '1.4' }}>
                {context.whatItIs}
              </div>
            </div>

            {/* Significance */}
            <div>
              <div style={{ fontSize: '10px', fontWeight: 600, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.4px', marginBottom: '2px' }}>
                Reverse Engineering Significance
              </div>
              <div style={{ color: 'var(--text-secondary)', fontSize: '11px', lineHeight: '1.4' }}>
                {context.whyItMatters}
              </div>
            </div>

            {/* Recommendation */}
            <div>
              <div style={{ fontSize: '10px', fontWeight: 600, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.4px', marginBottom: '2px' }}>
                Next Investigation Step
              </div>
              <div style={{ color: 'var(--text-secondary)', fontSize: '11px', lineHeight: '1.4' }}>
                {context.whatToInspectNext}
              </div>
            </div>

            {/* Quick Actions */}
            {context.suggestions.length > 0 && (
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: '4px', paddingTop: '4px' }}>
                {context.suggestions.map((action, idx) => (
                  <button
                    key={idx}
                    onClick={() => onExecuteAction(action.targetAddress)}
                    className="btn-sm"
                  >
                    {action.label}
                  </button>
                ))}
              </div>
            )}
          </div>
        ) : (
          <div style={{ padding: '24px 12px', textAlign: 'center', color: 'var(--text-muted)', fontSize: '11px' }}>
            No instruction or function selected.
          </div>
        )}

        {/* Guided Workflow Progress */}
        <div style={{ marginTop: '16px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '6px' }}>
            <span style={{ fontSize: '10px', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.4px', color: 'var(--text-muted)' }}>
              Analysis Milestones
            </span>
            <span style={{ fontSize: '10px', fontFamily: 'var(--font-mono)', color: 'var(--accent)' }}>
              {walkthrough.completedCount}/{walkthrough.totalCount} Done
            </span>
          </div>

          <div style={{ height: '2px', backgroundColor: 'var(--border-subtle)', borderRadius: '1px', overflow: 'hidden', marginBottom: '8px' }}>
            <div
              style={{
                height: '100%',
                width: `${(walkthrough.completedCount / walkthrough.totalCount) * 100}%`,
                backgroundColor: 'var(--accent)',
              }}
            />
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
            {walkthrough.steps.map((step) => (
              <div
                key={step.stepNumber}
                style={{
                  backgroundColor: 'var(--bg-tertiary)',
                  borderRadius: '3px',
                  padding: '6px 8px',
                  border: '1px solid var(--border-subtle)',
                }}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                  <input
                    type="checkbox"
                    checked={step.completed}
                    onChange={() => onToggleStep(step.stepNumber)}
                    style={{ accentColor: 'var(--accent)' }}
                  />
                  <span
                    style={{
                      fontSize: '11px',
                      fontWeight: 500,
                      color: step.completed ? 'var(--text-muted)' : 'var(--text-primary)',
                      textDecoration: step.completed ? 'line-through' : 'none',
                    }}
                  >
                    {step.title}
                  </span>
                </div>
                {!step.completed && (
                  <div style={{ fontSize: '10px', color: 'var(--text-muted)', marginTop: '3px', paddingLeft: '18px' }}>
                    {step.description}
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};
