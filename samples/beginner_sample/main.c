/*
 * Ghidra Companion - Educational Beginner Sample
 * 
 * This program is designed for reverse engineering training, demonstrating
 * core binary concepts (XREFs, calling conventions, branching, and string references)
 * in an offline environment.
 * 
 * License: Apache-2.0
 */

#include <stdio.h>
#include <string.h>

#define SECRET_KEY "GHIDRA2026"

// Subroutine definition: Demonstrates internal function calls and ABI parameter passing
int check_license_key(const char *input_key) {
    if (input_key == NULL) {
        return 0;
    }
    // Comparison call: Demonstrates import API calls and zero flag (ZF) evaluation
    if (strcmp(input_key, SECRET_KEY) == 0) {
        return 1; // Success
    }
    return 0; // Failure
}

void print_banner(void) {
    printf("=========================================\n");
    printf("  Ghidra Companion - Training Sample\n");
    printf("  Reverse Engineering Practice Binary\n");
    printf("=========================================\n\n");
}

int main(int argc, char *argv[]) {
    print_banner();

    if (argc < 2) {
        printf("Usage: %s <license_key>\n", argv[0]);
        printf("Hint: Inspect the 'Defined Strings' view to locate the embedded key.\n");
        return 1;
    }

    printf("Validating input key: %s\n", argv[1]);

    if (check_license_key(argv[1])) {
        printf("\n[+] SUCCESS: Valid key provided. License verified.\n");
        printf("[+] Analysis objective completed.\n");
        return 0;
    } else {
        printf("\n[-] ERROR: Invalid key. Please verify credentials and retry.\n");
        return 1;
    }
}
