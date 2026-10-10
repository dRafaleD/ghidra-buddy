package ghidracompanion.service;

import ghidracompanion.model.GlossaryEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Provides an offline, searchable knowledge base of core reverse engineering concepts.
 */
public class GlossaryService {

    private final List<GlossaryEntry> entries = new ArrayList<>();

    public GlossaryService() {
        populateDefaultEntries();
    }

    private void populateDefaultEntries() {
        entries.add(new GlossaryEntry(
            "XREF (Cross Reference)",
            "Core Concepts",
            "A reference link indicating where in the program a function, global variable, or memory address is accessed or invoked.",
            "In the Ghidra Listing view, labels such as 'XREF[2]' appear adjacent to symbols. This indicates that the target is referenced from two distinct locations. Press 'X' at any address or symbol to display the complete cross-reference dialog.",
            "Tracing cross-references is often the fastest reverse engineering workflow. Finding a critical string or API call and inspecting its XREFs immediately leads to referencing validation or caller logic."
        ));

        entries.add(new GlossaryEntry(
            "Decompiler vs Disassembler",
            "Core Concepts",
            "A disassembler converts raw machine instructions into assembly mnemonics; a decompiler reconstructs assembly into high-level C-like pseudo-code.",
            "Assembly instructions in the Listing represent the exact processor execution semantics. Decompiler output is an abstraction synthesized from Ghidra's p-code analysis; variable names, types, and structure layouts are heuristic approximations and do not guarantee 1:1 parity with the original source code.",
            "Never rely solely on decompiler pseudo-code when verifying critical conditions such as integer overflows, signedness checks, or low-level bitwise operations. Cross-check the assembly instructions."
        ));

        entries.add(new GlossaryEntry(
            "Stack Frame",
            "Memory & Architecture",
            "The contiguous memory region allocated on the runtime stack to store local variables, register spills, and the return address for a function invocation.",
            "At function entry, instructions such as 'sub rsp, 0x40' or 'push rbp; mov rbp, rsp' allocate and anchor the stack frame. Ghidra labels variables such as 'local_10' or 'local_18' to represent negative stack offsets relative to the frame base.",
            "Functions allocating large local arrays or contiguous buffers are primary targets when auditing for stack-based buffer overflows and stack canary protections."
        ));

        entries.add(new GlossaryEntry(
            "Calling Convention",
            "Memory & Architecture",
            "The standardized ABI contract governing parameter passing (registers vs. stack), return value locations, and stack cleanup responsibilities.",
            "Standard conventions include:\n• x64 Microsoft ABI: RCX, RDX, R8, R9 with a mandatory 32-byte shadow space.\n• x64 System V ABI (Linux/macOS): RDI, RSI, RDX, RCX, R8, R9.\n• x86 cdecl: Parameters pushed right-to-left; caller cleans the stack.\n• x86 stdcall: Parameters pushed right-to-left; callee cleans the stack (Win32 API standard).",
            "Ghidra can occasionally misidentify calling conventions on unexported or stripped functions. Inspect argument registers and edit the function signature (hotkey 'F') to refine decompilation accuracy."
        ));

        entries.add(new GlossaryEntry(
            "PE / ELF Binary Sections (.text, .data, .rdata)",
            "Binary Formats",
            "Structured binary containers that segregate executable instructions and data according to memory protection and access attributes.",
            "• .text / .code: Executable machine instructions (Read + Execute).\n• .data: Initialized mutable global variables (Read + Write).\n• .rdata / .rodata: Read-only constants, string literals, and import descriptors (Read-Only).\n• .bss: Uninitialized global memory zero-allocated at load time (Read + Write).",
            "A .text section marked writable, or unusual non-standard section names (such as UPX0), strongly indicate packed, protected, or obfuscated binaries."
        ));

        entries.add(new GlossaryEntry(
            "IAT (Import Address Table)",
            "Binary Formats",
            "A lookup table resolved by the OS dynamic loader containing function pointers to external shared libraries (DLLs, shared objects).",
            "Accessible in Ghidra under Symbol Tree > Imports. Critical APIs such as 'CreateFileW', 'RegOpenKeyEx', 'InternetOpenUrl', or 'connect' quickly reveal binary file, registry, and networking behavior.",
            "Triage imports as your first step when analyzing an unknown binary; they provide an immediate signature of system interactions and capability footprints."
        ));

        entries.add(new GlossaryEntry(
            "Prologue and Epilogue",
            "Memory & Architecture",
            "Standard instruction sequences that construct the stack frame upon function entry (prologue) and restore state prior to return (epilogue).",
            "A standard prologue consists of: 'push rbp; mov rbp, rsp; sub rsp, N'. A standard epilogue consists of: 'mov rsp, rbp; pop rbp; ret' or 'add rsp, N; ret'.",
            "Ghidra identifies function boundaries based on these prologue/epilogue signatures. If auto-analysis misses a function boundary, identify the prologue pattern and press 'F' to create a function manually."
        ));

        entries.add(new GlossaryEntry(
            "Endianness (Little-Endian vs Big-Endian)",
            "Memory & Architecture",
            "The ordering convention used to store multi-byte scalar values in physical memory.",
            "• Little-Endian (x86, x64, ARM default): Least significant byte (LSB) stored at the lowest memory address. 0x12345678 is stored in memory as [78 56 34 12].\n• Big-Endian (network byte order, SPARC): Most significant byte (MSB) stored at the lowest memory address: [12 34 56 78].",
            "When searching for magic numbers or constants in raw Hex views, remember byte sequences will appear reversed on little-endian architectures."
        ));

        entries.add(new GlossaryEntry(
            "Control Flow Graph (CFG)",
            "Analysis Techniques",
            "A directed graphical representation modeling all execution paths across basic blocks within a function.",
            "Accessible via the 'Function Graph' icon on the Ghidra toolbar. Green edges indicate condition met (True), red edges indicate condition not met (False), and blue edges indicate unconditional branches.",
            "The Function Graph view is the most effective representation for analyzing complex state machines, switch statements, and loop structures."
        ));
    }

    public List<GlossaryEntry> getAllEntries() {
        return Collections.unmodifiableList(entries);
    }

    public List<GlossaryEntry> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllEntries();
        }
        String q = query.trim().toLowerCase(Locale.ROOT);
        return entries.stream()
            .filter(e -> e.getTerm().toLowerCase(Locale.ROOT).contains(q)
                || e.getCategory().toLowerCase(Locale.ROOT).contains(q)
                || e.getSummary().toLowerCase(Locale.ROOT).contains(q)
                || e.getDetailedDescription().toLowerCase(Locale.ROOT).contains(q)
                || e.getBeginnerTip().toLowerCase(Locale.ROOT).contains(q))
            .collect(Collectors.toList());
    }
}
