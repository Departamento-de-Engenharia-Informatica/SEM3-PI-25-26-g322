#include <stdio.h>
#include <string.h>
#include "../../Utils/asm.h"

int callfunc(int (*f)(char* in, int key, char* out), char* in, int key, char* out);

void test_decrypt(const char* input, int key, const char* expected_output, int expected_return) {
    char output[100];
    memset(output, 0, sizeof(output));

    printf("=== Testando decrypt_data ===\n");
    printf("Input: '%s'\n", input);
    printf("Key: %d\n", key);

    int result = callfunc(decrypt_data, (char*)input, key, output);

    printf("Output: '%s'\n", output);
    printf("Return: %d\n", result);
    printf("Expected Output: '%s'\n", expected_output);
    printf("Expected Return: %d\n", expected_return);

    if (strcmp(output, expected_output) == 0 && result == expected_return) {
        printf("TEST PASSADO\n");
    } else {
        printf("TEST FALHADO\n");
    }
    printf("\n");
}

int main() {
    printf("=== USAC02 - Cifra de César (Decrypt) ===\n\n");

    // Teste 1: Caso básico (reverso do encrypt)
    test_decrypt("KHOOR", 3, "HELLO", 1);

    // Teste 2: Rotação com wrap-around
    test_decrypt("ABC", 3, "XYZ", 1);

    // Teste 3: Chave máxima
    test_decrypt("ABC", 26, "ABC", 1);

    // Teste 4: String vazia
    test_decrypt("", 5, "", 1);

    // Teste 5: Caracteres inválidos
    test_decrypt("khoor", 3, "", 0);

    // Teste 6: Chave inválida
    test_decrypt("KHOOR", 0, "", 0);

    return 0;
}