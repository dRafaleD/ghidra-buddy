import { useEffect, useState } from 'react';
import { ActivityBar } from './components/ActivityBar';
import type { ActivityTab } from './components/ActivityBar';
import { Sidebar } from './components/Sidebar';
import { EditorArea } from './components/EditorArea';
import { LearningAssistantPanel } from './components/LearningAssistantPanel';
import { CommandPalette } from './components/CommandPalette';
import { GlossaryModal } from './components/GlossaryModal';
import { StatusBar } from './components/StatusBar';
import { api } from './services/api';
import type {
  CodeContext,
  FunctionItem,
  InstructionItem,
  StringItem,
  WalkthroughStep,
  WorkspaceMode,
} from './types';

export function App() {
  const [activeActivityTab, setActiveActivityTab] = useState<ActivityTab>('explorer');
  const [functions, setFunctions] = useState<FunctionItem[]>([]);
  const [strings, setStrings] = useState<StringItem[]>([]);
  const [selectedFunction, setSelectedFunction] = useState<FunctionItem | null>(null);

  const [disassembly, setDisassembly] = useState<InstructionItem[]>([]);
  const [decompiledCode, setDecompiledCode] = useState<string>('');
  const [activeAddress, setActiveAddress] = useState<string>('0x140001090');

  const [context, setContext] = useState<CodeContext | null>(null);
  const [mode, setMode] = useState<WorkspaceMode>('BEGINNER');

  const [walkthrough, setWalkthrough] = useState<{
    completedCount: number;
    totalCount: number;
    steps: WalkthroughStep[];
  }>({ completedCount: 0, totalCount: 5, steps: [] });

  const [openTabs, setOpenTabs] = useState<string[]>(['check_license_key', 'main']);
  const [activeTab, setActiveTab] = useState<string>('check_license_key');

  const [isCommandPaletteOpen, setIsCommandPaletteOpen] = useState(false);
  const [isGlossaryModalOpen, setIsGlossaryModalOpen] = useState(false);

  useEffect(() => {
    api.getFunctions().then((funcs) => {
      setFunctions(funcs);
      if (funcs.length > 0) {
        const initialFunc = funcs.find((f) => f.name === 'check_license_key') || funcs[0];
        handleSelectFunction(initialFunc);
      }
    });

    api.getStrings().then(setStrings);
    api.getWalkthrough().then(setWalkthrough);
  }, []);

  const handleSelectFunction = (func: FunctionItem) => {
    setSelectedFunction(func);
    setActiveAddress(func.entryPoint);

    if (!openTabs.includes(func.name)) {
      setOpenTabs([...openTabs, func.name]);
    }
    setActiveTab(func.name);

    api.getDisassembly(func.entryPoint).then(setDisassembly);
    api.getDecompilation(func.entryPoint).then((res) => {
      setDecompiledCode(res.cCode);
    });
    api.analyzeContext(func.entryPoint).then(setContext);
  };

  const handleSelectAddress = (address: string) => {
    setActiveAddress(address);
    api.analyzeContext(address).then(setContext);
  };

  const handleSelectString = (str: StringItem) => {
    setActiveAddress(str.address);
    api.analyzeContext(str.address).then(setContext);
  };

  const handleToggleMode = () => {
    setMode((prev) => (prev === 'BEGINNER' ? 'ADVANCED' : 'BEGINNER'));
  };

  const handleToggleStep = (stepNumber: number) => {
    setWalkthrough((prev) => {
      const updatedSteps = prev.steps.map((s) =>
        s.stepNumber === stepNumber ? { ...s, completed: !s.completed } : s
      );
      const count = updatedSteps.filter((s) => s.completed).length;
      return {
        ...prev,
        completedCount: count,
        steps: updatedSteps,
      };
    });
  };

  return (
    <div className="app-container">
      {/* Top Titlebar */}
      <header className="top-bar">
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <span style={{ fontWeight: 600, fontSize: '11px', letterSpacing: '0.4px', textTransform: 'uppercase', color: 'var(--text-secondary)' }}>
            Ghidra Companion
          </span>
          <span style={{ fontSize: '10px', color: 'var(--text-muted)' }}>|</span>
          <span style={{ fontSize: '10px', color: 'var(--text-muted)', fontFamily: 'var(--font-mono)' }}>v0.2.0</span>
        </div>

        {/* Global Command Palette Trigger */}
        <div
          onClick={() => setIsCommandPaletteOpen(true)}
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '8px',
            backgroundColor: 'var(--bg-tertiary)',
            border: '1px solid var(--border-subtle)',
            borderRadius: '4px',
            padding: '3px 10px',
            cursor: 'pointer',
            width: '300px',
            justifyContent: 'space-between',
            fontSize: '11px',
            color: 'var(--text-muted)',
          }}
        >
          <span>Search functions, symbols or addresses...</span>
          <kbd
            style={{
              backgroundColor: 'var(--bg-surface)',
              border: '1px solid var(--border-muted)',
              borderRadius: '2px',
              padding: '0 4px',
              fontSize: '9px',
              fontFamily: 'var(--font-mono)',
              color: 'var(--text-muted)',
            }}
          >
            Ctrl+P
          </kbd>
        </div>

        {/* Right Top Bar Controls */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <button
            onClick={() => setIsGlossaryModalOpen(true)}
            className="btn-sm"
          >
            Reference
          </button>

          <button
            onClick={handleToggleMode}
            className="btn-sm"
            style={{
              borderColor: mode === 'BEGINNER' ? 'var(--border-focus)' : 'var(--border-muted)',
              color: mode === 'BEGINNER' ? 'var(--accent)' : 'var(--text-secondary)',
              fontWeight: 500,
            }}
          >
            {mode === 'BEGINNER' ? 'Mode: Guided' : 'Mode: Full'}
          </button>
        </div>
      </header>

      {/* Main Layout */}
      <div className="main-layout">
        <ActivityBar
          activeTab={activeActivityTab}
          onSelectTab={setActiveActivityTab}
          walkthroughProgress={{
            completed: walkthrough.completedCount,
            total: walkthrough.totalCount,
          }}
        />

        <Sidebar
          activeTab={activeActivityTab}
          functions={functions}
          strings={strings}
          selectedFunction={selectedFunction}
          onSelectFunction={handleSelectFunction}
          onSelectString={handleSelectString}
        />

        <EditorArea
          selectedFunction={selectedFunction}
          decompiledCode={decompiledCode}
          disassembly={disassembly}
          activeAddress={activeAddress}
          onSelectAddress={handleSelectAddress}
          openTabs={openTabs}
          activeTab={activeTab}
          onSelectTab={(tabName) => {
            setActiveTab(tabName);
            const target = functions.find((f) => f.name === tabName);
            if (target) handleSelectFunction(target);
          }}
        />

        <LearningAssistantPanel
          context={context}
          mode={mode}
          onToggleMode={handleToggleMode}
          walkthrough={walkthrough}
          onToggleStep={handleToggleStep}
          onExecuteAction={handleSelectAddress}
        />
      </div>

      {/* Status Bar */}
      <StatusBar
        mode={mode}
        onToggleMode={handleToggleMode}
        activeAddress={activeAddress}
        functionName={selectedFunction?.name || 'check_license_key'}
      />

      {/* Modals */}
      <CommandPalette
        isOpen={isCommandPaletteOpen}
        onClose={() => setIsCommandPaletteOpen(false)}
        functions={functions}
        strings={strings}
        onSelectFunction={handleSelectFunction}
        onSelectString={handleSelectString}
        onToggleMode={handleToggleMode}
        onOpenGlossary={() => setIsGlossaryModalOpen(true)}
      />

      <GlossaryModal
        isOpen={isGlossaryModalOpen}
        onClose={() => setIsGlossaryModalOpen(false)}
      />
    </div>
  );
}

export default App;
