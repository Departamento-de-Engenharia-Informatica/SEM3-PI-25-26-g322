#include <stdio.h>
#include <string.h>
#include "../../Utils/asm.h"

int callfunc(int (*f)(char* str, char* token, char* unit, int* value),
             char* str, char* token, char* unit, int* value);

void test_extract(const char* str, const char* token, const char* expected_unit, int expected_value, int expected_return) {
    char unit[50];
    int value;
    memset(unit, 0, sizeof(unit));

    printf("=== Testando extract_data ===\n");
    printf("String: '%s'\n", str);
    printf("Token: '%s'\n", token);

    int result = callfunc(extract_data, (char*)str, (char*)token, unit, &value);

    printf("Unit: '%s'\n", unit);
    printf("Value: %d\n", value);
    printf("Return: %d\n", result);
    printf("Expected Unit: '%s'\n", expected_unit);
    printf("Expected Value: %d\n", expected_value);
    printf("Expected Return: %d\n", expected_return);

    if (strcmp(unit, expected_unit) == 0 && value == expected_value && result == expected_return) {
        printf("TEST PASSADO\n");
    } else {
        printf("TEST FALHADO\n");
    }
    printf("\n");
}

int main() {
    printf("=== USAC03 - Extrair Dados Formatados ===\n\n");

    // String de teste complexa
    char test_str[] = "TEMP&unit::celsius&value::25#HUM&unit::percentage&value::80#PRESS&unit::hpa&value::1013";

    // Teste 1: Extrair temperatura
    test_extract(test_str, "TEMP", "celsius", 25, 1);

    // Teste 2: Extrair humidade
    test_extract(test_str, "HUM", "percentage", 80, 1);

    // Teste 3: Extrair pressão
    test_extract(test_str, "PRESS", "hpa", 1013, 1);

    // Teste 4: Token não existente
    test_extract(test_str, "WIND", "", 0, 0);

    // Teste 5: String vazia
    test_extract("", "TEMP", "", 0, 0);

    // Teste 6: Token vazio
    test_extract(test_str, "", "", 0, 0);

    // Teste 7: Caso com múltiplos valores iguais
    char test_str2[] = "TEMP&unit::celsius&value::30#TEMP&unit::fahrenheit&value::86";
    test_extract(test_str2, "TEMP", "celsius", 30, 1);

    return 0;
}