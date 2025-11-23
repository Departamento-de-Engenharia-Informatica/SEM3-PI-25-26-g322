.text
.globl encrypt_data

# int encrypt_data(char* in, int key, char *out)
# a0 = in, a1 = key, a2 = out
encrypt_data:
    # Validar inputs
    beq a0, zero, error_encrypt      # if (in == NULL) goto error
    beq a2, zero, error_encrypt      # if (out == NULL) goto error
    li t0, 1
    blt a1, t0, error_encrypt        # if (key < 1) goto error
    li t0, 26
    bgt a1, t0, error_encrypt        # if (key > 26) goto error

    mv t1, a0                        # t1 = pointer to in
    mv t2, a2                        # t2 = pointer to out
    li t3, 'A'                       # t3 = 'A'
    li t4, 'Z'                       # t4 = 'Z'

encrypt_loop:
    lb t0, 0(t1)                     # Carrega caracter atual
    beq t0, zero, encrypt_success    # Se null terminator, fim

    # Verificar se é letra maiúscula
    blt t0, t3, error_encrypt        # if (char < 'A') error
    bgt t0, t4, error_encrypt        # if (char > 'Z') error

    # Aplicar cifra de César
    sub t0, t0, t3                   # char - 'A'
    add t0, t0, a1                   # + key
    li t5, 26
    rem t0, t0, t5                   # % 26
    add t0, t0, t3                   # + 'A'

    sb t0, 0(t2)                     # Guarda resultado em out

    addi t1, t1, 1                   # Incrementa pointers
    addi t2, t2, 1
    j encrypt_loop

encrypt_success:
    sb zero, 0(t2)                   # Null terminator
    li a0, 1                         # return 1
    ret

error_encrypt:
    beq a2, zero, encrypt_ret        # Se out é NULL, não podemos escrever
    sb zero, 0(a2)                   # out[0] = '\0'
encrypt_ret:
    li a0, 0                         # return 0
    ret