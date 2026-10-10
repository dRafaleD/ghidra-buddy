import type {
  BridgeStatus,
  CodeContext,
  FunctionItem,
  GlossaryEntry,
  InstructionItem,
  StringItem,
  WalkthroughStep,
} from '../types';

const BRIDGE_URL = 'http://127.0.0.1:19842';

// Realistic sample data matching samples/beginner_sample/main.c
const MOCK_FUNCTIONS: FunctionItem[] = [
  { name: 'main', entryPoint: '0x140001000', size: 144, paramCount: 2, returnType: 'int', isExternal: false },
  { name: 'check_license_key', entryPoint: '0x140001090', size: 92, paramCount: 1, returnType: 'int', isExternal: false },
  { name: 'print_banner', entryPoint: '0x1400010f0', size: 48, paramCount: 0, returnType: 'void', isExternal: false },
  { name: 'strcmp', entryPoint: '0x140002100', size: 0, paramCount: 2, returnType: 'int', isExternal: true },
  { name: 'printf', entryPoint: '0x140002110', size: 0, paramCount: 1, returnType: 'int', isExternal: true },
  { name: 'entry', entryPoint: '0x140001200', size: 64, paramCount: 0, returnType: 'void', isExternal: false },
];

const MOCK_DISASSEMBLY: Record<string, InstructionItem[]> = {
  '0x140001090': [
    { address: '0x140001090', mnemonic: 'SUB', operands: 'RSP, 0x28', bytes: '48 83 EC 28', comment: 'Stack frame allocation', isCall: false, isBranch: false },
    { address: '0x140001094', mnemonic: 'TEST', operands: 'RCX, RCX', bytes: '48 85 C9', comment: 'Check if input pointer is NULL', isCall: false, isBranch: false },
    { address: '0x140001097', mnemonic: 'JZ', operands: '0x1400010BE', bytes: '74 25', comment: 'Jump to fail if NULL', isCall: false, isBranch: true },
    { address: '0x140001099', mnemonic: 'LEA', operands: 'RDX, [0x140003000]', bytes: '48 8D 15 60 1F 00 00', comment: '"GHIDRA2026"', isCall: false, isBranch: false },
    { address: '0x1400010A0', mnemonic: 'CALL', operands: 'strcmp', bytes: 'E8 5B 10 00 00', comment: 'Compare input with secret key', isCall: true, isBranch: false },
    { address: '0x1400010A5', mnemonic: 'TEST', operands: 'EAX, EAX', bytes: '85 C0', comment: 'Check strcmp return value (0 = match)', isCall: false, isBranch: false },
    { address: '0x1400010A7', mnemonic: 'JNZ', operands: '0x1400010BC', bytes: '75 13', comment: 'Jump if keys do not match', isCall: false, isBranch: true },
    { address: '0x1400010A9', mnemonic: 'MOV', operands: 'EAX, 0x1', bytes: 'B8 01 00 00 00', comment: 'Return 1 (Success)', isCall: false, isBranch: false },
    { address: '0x1400010AE', mnemonic: 'ADD', operands: 'RSP, 0x28', bytes: '48 83 C4 28', comment: 'Restore stack', isCall: false, isBranch: false },
    { address: '0x1400010B2', mnemonic: 'RET', operands: '', bytes: 'C3', comment: 'Return to caller', isCall: false, isBranch: false },
    { address: '0x1400010BC', mnemonic: 'XOR', operands: 'EAX, EAX', bytes: '31 C0', comment: 'Return 0 (Failed)', isCall: false, isBranch: false },
    { address: '0x1400010BE', mnemonic: 'ADD', operands: 'RSP, 0x28', bytes: '48 83 C4 28', comment: 'Restore stack', isCall: false, isBranch: false },
    { address: '0x1400010C2', mnemonic: 'RET', operands: '', bytes: 'C3', comment: 'Return to caller', isCall: false, isBranch: false },
  ],
  '0x140001000': [
    { address: '0x140001000', mnemonic: 'SUB', operands: 'RSP, 0x38', bytes: '48 83 EC 38', comment: 'Allocate main frame', isCall: false, isBranch: false },
    { address: '0x140001004', mnemonic: 'CALL', operands: 'print_banner', bytes: 'E8 E7 00 00 00', comment: 'Print welcome header', isCall: true, isBranch: false },
    { address: '0x140001009', mnemonic: 'CMP', operands: 'ECX, 0x2', bytes: '83 F9 02', comment: 'Check argc < 2', isCall: false, isBranch: false },
    { address: '0x14000100C', mnemonic: 'JGE', operands: '0x140001025', bytes: '7D 17', comment: 'Jump if argument provided', isCall: false, isBranch: true },
    { address: '0x14000100E', mnemonic: 'LEA', operands: 'RCX, [0x140003050]', bytes: '48 8D 0D 3B 20 00 00', comment: '"Usage: %s <license_key>"', isCall: false, isBranch: false },
    { address: '0x140001015', mnemonic: 'CALL', operands: 'printf', bytes: 'E8 F6 10 00 00', comment: 'Show usage instructions', isCall: true, isBranch: false },
    { address: '0x14000101A', mnemonic: 'MOV', operands: 'EAX, 0x1', bytes: 'B8 01 00 00 00', comment: 'Exit code 1', isCall: false, isBranch: false },
    { address: '0x14000101F', mnemonic: 'ADD', operands: 'RSP, 0x38', bytes: '48 83 C4 38', comment: 'Cleanup', isCall: false, isBranch: false },
    { address: '0x140001023', mnemonic: 'RET', operands: '', bytes: 'C3', comment: '', isCall: false, isBranch: false },
  ],
};

const MOCK_DECOMPILATION: Record<string, string> = {
  '0x140001090': `/* Decompiled by Ghidra 12.1.3 */
int check_license_key(char *input_key)
{
    int match;

    if (input_key == (char *)0x0) {
        return 0;
    }
    
    /* Compare against embedded license key */
    match = strcmp(input_key, "GHIDRA2026");
    if (match == 0) {
        return 1; /* Success */
    }
    
    return 0; /* Invalid key */
}`,
  '0x140001000': `/* Decompiled by Ghidra 12.1.3 */
int main(int argc, char **argv)
{
    int is_valid;

    print_banner();
    
    if (argc < 2) {
        printf("Usage: %s <license_key>\\n", *argv);
        printf("Hint: Inspect 'Defined Strings' to locate the embedded key!\\n");
        return 1;
    }
    
    printf("Validating input key: %s\\n", argv[1]);
    is_valid = check_license_key(argv[1]);
    
    if (is_valid != 0) {
        printf("\\n[+] SUCCESS: Valid key provided. License verified.\\n");
        return 0;
    }
    
    printf("\\n[-] ERROR: Invalid key. Please retry.\\n");
    return 1;
}`,
};

const MOCK_STRINGS: StringItem[] = [
  { address: '0x140003000', value: 'GHIDRA2026', length: 11, xrefCount: 1 },
  { address: '0x140003010', value: '=========================================', length: 42, xrefCount: 2 },
  { address: '0x140003050', value: 'Usage: %s <license_key>\\n', length: 27, xrefCount: 1 },
  { address: '0x140003080', value: '[+] SUCCESS: Valid key provided. License verified.', length: 50, xrefCount: 1 },
  { address: '0x1400030C0', value: '[-] ERROR: Invalid key. Please retry.', length: 37, xrefCount: 1 },
];

export const api = {
  async getStatus(): Promise<BridgeStatus> {
    try {
      const res = await fetch(`${BRIDGE_URL}/api/status`, { signal: AbortSignal.timeout(1500) });
      if (res.ok) return await res.json();
    } catch {}
    return {
      running: true,
      ghidraVersion: '12.1.3',
      mode: 'BEGINNER',
      program: {
        name: 'beginner_sample.exe',
        executablePath: 'C:\\samples\\beginner_sample\\beginner_sample.exe',
        language: 'x86:LE:64:default',
        compiler: 'gcc',
        minAddress: '0x140001000',
        maxAddress: '0x140003FFF',
      },
    };
  },

  async getFunctions(): Promise<FunctionItem[]> {
    try {
      const res = await fetch(`${BRIDGE_URL}/api/functions`, { signal: AbortSignal.timeout(1500) });
      if (res.ok) return await res.json();
    } catch {}
    return MOCK_FUNCTIONS;
  },

  async getDisassembly(address: string, count = 40): Promise<InstructionItem[]> {
    try {
      const res = await fetch(`${BRIDGE_URL}/api/disassembly?address=${address}&count=${count}`, { signal: AbortSignal.timeout(1500) });
      if (res.ok) return await res.json();
    } catch {}
    return MOCK_DISASSEMBLY[address] || MOCK_DISASSEMBLY['0x140001090'];
  },

  async getDecompilation(address: string): Promise<{ success: boolean; cCode: string; functionName: string }> {
    try {
      const res = await fetch(`${BRIDGE_URL}/api/decompile?address=${address}`, { signal: AbortSignal.timeout(2000) });
      if (res.ok) return await res.json();
    } catch {}
    const cCode = MOCK_DECOMPILATION[address] || MOCK_DECOMPILATION['0x140001090'];
    return { success: true, cCode, functionName: address === '0x140001000' ? 'main' : 'check_license_key' };
  },

  async getStrings(): Promise<StringItem[]> {
    try {
      const res = await fetch(`${BRIDGE_URL}/api/strings`, { signal: AbortSignal.timeout(1500) });
      if (res.ok) return await res.json();
    } catch {}
    return MOCK_STRINGS;
  },

  async analyzeContext(address: string): Promise<CodeContext> {
    try {
      const res = await fetch(`${BRIDGE_URL}/api/analyze-context?address=${address}`, { signal: AbortSignal.timeout(1500) });
      if (res.ok) return await res.json();
    } catch {}

    if (address === '0x1400010A0') {
      return {
        address: '0x1400010A0',
        itemName: 'CALL strcmp',
        contextType: 'IMPORT_CALL',
        typeLabelTr: 'Imported API Call',
        typeLabelEn: 'Imported API Call',
        whatItIs: "Standard C runtime library invocation: 'strcmp'. Compares two null-terminated string buffers byte-by-byte.",
        whyItMatters: "Critical decision point in license validation routine. Returns 0 in the EAX register when strings match identically.",
        whatToInspectNext: "Inspect the second argument register (RDX for x64 Windows ABI). It points to the hardcoded secret key in memory.",
        suggestions: [
          { label: 'Navigate to Embedded Key (0x140003000)', actionId: 'GOTO', targetAddress: '0x140003000' },
          { label: 'Append Inline Comment (;)', actionId: 'COMMENT', targetAddress: '0x1400010A0' },
        ],
      };
    }

    return {
      address,
      itemName: 'check_license_key',
      contextType: 'FUNCTION_DEF',
      typeLabelTr: 'Function Definition',
      typeLabelEn: 'Function Definition',
      whatItIs: "Internal subroutine validating user-supplied credentials: 'int check_license_key(char *key)'.",
      whyItMatters: "Referenced by 1 caller (XREF) from the main executable entry sequence. The 'key' argument is passed via the RCX register.",
      whatToInspectNext: "Inspect the 'strcmp' invocation inside the decompiler to locate the constant comparison target address.",
      suggestions: [
        { label: 'Inspect Disassembly Instructions', actionId: 'GOTO', targetAddress: '0x140001090' },
        { label: 'Rename Function (L)', actionId: 'RENAME', targetAddress: '0x140001090' },
      ],
    };
  },

  async getGlossary(query = ''): Promise<GlossaryEntry[]> {
    try {
      const res = await fetch(`${BRIDGE_URL}/api/glossary?q=${encodeURIComponent(query)}`, { signal: AbortSignal.timeout(1500) });
      if (res.ok) return await res.json();
    } catch {}
    return [
      {
        term: 'XREF (Cross Reference)',
        category: 'Core Concepts',
        summary: 'A reference link indicating where in the program a function, global variable, or memory address is accessed or invoked.',
        detailedDescription: "Press 'X' at any address or symbol to display the complete cross-reference dialog. Finding a critical string and inspecting its XREFs immediately reveals caller validation logic.",
        beginnerTip: 'Tracing cross-references is the fastest triage path in reverse engineering.',
      },
      {
        term: 'Decompiler vs Disassembler',
        category: 'Core Concepts',
        summary: 'A disassembler converts raw machine instructions into assembly mnemonics; a decompiler reconstructs assembly into high-level C-like pseudo-code.',
        detailedDescription: 'Assembly instructions represent exact processor execution semantics. Decompiler output is an abstraction; variable names and types are heuristic approximations.',
        beginnerTip: 'Never rely solely on decompiler pseudo-code for critical checks; always cross-verify against assembly mnemonics.',
      },
      {
        term: 'Stack Frame',
        category: 'Memory & Architecture',
        summary: 'The contiguous memory region allocated on the runtime stack to store local variables, register spills, and the return address.',
        detailedDescription: "Constructed at subroutine entry via instructions such as 'sub rsp, 0x28'. Negative offsets like local_10 represent stack variable slots.",
        beginnerTip: 'Examine stack frame buffer sizes when auditing for stack-based buffer overflows.',
      },
    ];
  },

  async getWalkthrough(): Promise<{ completedCount: number; totalCount: number; steps: WalkthroughStep[] }> {
    try {
      const res = await fetch(`${BRIDGE_URL}/api/walkthrough`, { signal: AbortSignal.timeout(1500) });
      if (res.ok) return await res.json();
    } catch {}
    return {
      completedCount: 2,
      totalCount: 5,
      steps: [
        { stepNumber: 1, title: '1. Binary Ingestion & Auto-Analysis', description: 'Accept and run the Auto-Analysis suite upon opening the binary.', targetHint: 'Wait for background analysis progress to reach 100%.', completed: true },
        { stepNumber: 2, title: '2. Locate Entry Point or Main Function', description: 'In the Symbol Tree navigation panel, locate and double-click entry or main.', targetHint: 'Trace control flow from CRT startup to main subroutine.', completed: true },
        { stepNumber: 3, title: '3. Triage Defined Strings & Cross-References', description: 'Inspect the Defined Strings view to locate the embedded license key.', targetHint: 'Press X on the target string to navigate to its referencing caller.', completed: false },
        { stepNumber: 4, title: '4. Correlate Assembly and Decompiler Views', description: 'Correlate the CMP/TEST assembly instructions with high-level C branch conditions.', targetHint: 'Compare highlighted lines between left and right code panes.', completed: false },
        { stepNumber: 5, title: '5. Symbol Annotation & Renaming', description: 'Assign descriptive variable names using hotkey L and add comments using semicolon.', targetHint: 'Press ; to append inline disassembly comments.', completed: false },
      ],
    };
  },
};
