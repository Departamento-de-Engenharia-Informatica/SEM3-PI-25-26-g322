.text
.globl extract_data

# int extract_data(char* str, char* token, char* unit, int* value)
# a0 = str, a1 = token, a2 = unit, a3 = value
extract_data:
    # Validar inputs
    beq a0, zero, error_extract
    beq a1, zero, error_extract
    beq a2, zero, error_extract
    beq a3, zero, error_extract

    mv t0, a0                        # t0 = current position in str

find_token_loop:
    # Procurar pelo token
    mv t1, t0                        # t1 = start of current block
    mv t2, a1                        # t2 = token

    # Verificar se o token existe no início deste bloco
token_match_loop:
    lb t3, 0(t1)                     # char from str
    lb t4, 0(t2)                     # char from token
    beq t4, zero, token_found        # Se fim do token, encontramos
    beq t3, zero, next_block         # Se fim da string, próximo bloco
    bne t3, t4, next_block           # Se chars diferentes, próximo bloco

    addi t1, t1, 1
    addi t2, t2, 1
    j token_match_loop

token_found:
    # Verificar se após o token vem '&'
    lb t3, 0(t1)
    li t4, '&'
    bne t3, t4, next_block           # Se não tem '&', não é nosso token

    # Encontrar "unit::"
    addi t1, t1, 1                   # Avança após o '&'

    # Verificar padrão "unit::"
    li t2, 'u'
    lb t3, 0(t1)
    bne t3, t2, next_block
    addi t1, t1, 1

    li t2, 'n'
    lb t3, 0(t1)
    bne t3, t2, next_block
    addi t1, t1, 1

    li t2, 'i'
    lb t3, 0(t1)
    bne t3, t2, next_block
    addi t1, t1, 1

    li t2, 't'
    lb t3, 0(t1)
    bne t3, t2, next_block
    addi t1, t1, 1

    li t2, ':'
    lb t3, 0(t1)
    bne t3, t2, next_block
    addi t1, t1, 1

    lb t3, 0(t1)
    bne t3, t2, next_block
    addi t1, t1, 1

    # Extrair unidade (até próximo '&')
    mv t3, a2                        # t3 = pointer to unit
extract_unit_loop:
    lb t4, 0(t1)
    beq t4, zero, error_extract      # Fim inesperado da string
    li t5, '&'
    beq t4, t5, unit_done            # Encontrou próximo '&'

    sb t4, 0(t3)                     # Guarda caractere na unit
    addi t1, t1, 1
    addi t3, t3, 1
    j extract_unit_loop

unit_done:
    sb zero, 0(t3)                   # Null terminate unit

    # Encontrar "value::"
    addi t1, t1, 1                   # Avança após o '&'

    # Verificar padrão "value::"
    li t2, 'v'
    lb t3, 0(t1)
    bne t3, t2, error_extract
    addi t1, t1, 1

    li t2, 'a'
    lb t3, 0(t1)
    bne t3, t2, error_extract
    addi t1, t1, 1

    li t2, 'l'
    lb t3, 0(t1)
    bne t3, t2, error_extract
    addi t1, t1, 1

    li t2, 'u'
    lb t3, 0(t1)
    bne t3, t2, error_extract
    addi t1, t1, 1

    li t2, 'e'
    lb t3, 0(t1)
    bne t3, t2, error_extract
    addi t1, t1, 1

    li t2, ':'
    lb t3, 0(t1)
    bne t3, t2, error_extract
    addi t1, t1, 1

    lb t3, 0(t1)
    bne t3, t2, error_extract
    addi t1, t1, 1

    # Converter valor para int
    mv a0, t1
    jal ra, atoi
    sw a0, 0(a3)                     # Guarda valor em *value

    li a0, 1                         # return 1
    ret

next_block:
    # Encontrar próximo '#' ou o fim da string
    lb t3, 0(t0)
    beq t3, zero, error_extract      # Fim da string -> não encontrou token
    li t4, '#'
    beq t3, t4, found_hash
    addi t0, t0, 1
    j next_block

found_hash:
    addi t0, t0, 1                   # Avança depois do '#'
    j find_token_loop

error_extract:
    # Limpar outputs
    beq a2, zero, clear_value
    sb zero, 0(a2)                   # unit[0] = '\0'
clear_value:
    beq a3, zero, extract_ret
    sw zero, 0(a3)                   # *value = 0
extract_ret:
    li a0, 0                         # return 0
    ret

# Função auxiliar: converter string para inteiro
# a0 = string, retorna inteiro em a0
atoi:
    li a1, 0                         # resultado
    li t1, 10                        # base 10
    li t2, '0'                       # t2 = '0'
    li t3, '9'                       # t3 = '9'

atoi_loop:
    lb t0, 0(a0)
    beq t0, zero, atoi_done          # null terminator
    blt t0, t2, atoi_done            # não dígito
    bgt t0, t3, atoi_done            # não dígito

    sub t0, t0, t2                   # char para int
    mul a1, a1, t1                   # resultado * 10
    add a1, a1, t0                   # + dígito atual

    addi a0, a0, 1
    j atoi_loop

atoi_done:
    mv a0, a1
    ret