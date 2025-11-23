.text
.globl decrypt_data

# int decrypt_data(char* in, int key, char *out)
# a0 = in, a1 = key, a2 = out
decrypt_data:
    # Validar inputs
    beq a0, zero, error_decrypt
    beq a2, zero, error_decrypt
    li t0, 1
    blt a1, t0, error_decrypt
    li t0, 26
    bgt a1, t0, error_decrypt

    mv t1, a0                        # t1 = pointer to in
    mv t2, a2                        # t2 = pointer to out
    li t3, 'A'                       # t3 = 'A'
    li t4, 'Z'                       # t4 = 'Z'
    li t5, 26                        # t5 = 26

decrypt_loop:
    lb t0, 0(t1)                     # Carrega caracter atual
    beq t0, zero, decrypt_success    # Se null terminator, fim

    # Verificar se é letra maiúscula
    blt t0, t3, error_decrypt
    bgt t0, t4, error_decrypt

    # Aplicar descriptografia
    sub t0, t0, t3                   # char - 'A'
    sub t0, t0, a1                   # - key
    add t0, t0, t5                   # + 26 (para evitar negativos)
    rem t0, t0, t5                   # % 26
    add t0, t0, t3                   # + 'A'

    sb t0, 0(t2)                     # Guarda resultado em out

    addi t1, t1, 1
    addi t2, t2, 1
    j decrypt_loop

decrypt_success:
    sb zero, 0(t2)                   # Null terminator
    li a0, 1                         # return 1
    ret

error_decrypt:
    beq a2, zero, decrypt_ret
    sb zero, 0(a2)                   # out[0] = '\0'
decrypt_ret:
    li a0, 0                         # return 0
    ret