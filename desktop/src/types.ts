export type WorkspaceMode = 'BEGINNER' | 'ADVANCED';

export interface FunctionItem {
  name: string;
  entryPoint: string;
  size: number;
  paramCount: number;
  returnType: string;
  isExternal: boolean;
}

export interface InstructionItem {
  address: string;
  mnemonic: string;
  operands: string;
  bytes: string;
  comment: string | null;
  isCall: boolean;
  isBranch: boolean;
}

export interface StringItem {
  address: string;
  value: string;
  length: number;
  xrefCount: number;
}

export interface ActionSuggestion {
  label: string;
  actionId: string;
  targetAddress: string;
}

export interface CodeContext {
  address: string;
  itemName: string;
  contextType: string;
  typeLabelTr: string;
  typeLabelEn: string;
  whatItIs: string;
  whyItMatters: string;
  whatToInspectNext: string;
  suggestions: ActionSuggestion[];
}

export interface GlossaryEntry {
  term: string;
  category: string;
  summary: string;
  detailedDescription: string;
  beginnerTip: string;
}

export interface WalkthroughStep {
  stepNumber: number;
  title: string;
  description: string;
  targetHint: string;
  completed: boolean;
}

export interface BridgeStatus {
  running: boolean;
  ghidraVersion: string;
  mode: WorkspaceMode;
  program: {
    name: string;
    executablePath: string;
    language: string;
    compiler: string;
    minAddress: string;
    maxAddress: string;
  } | null;
}
