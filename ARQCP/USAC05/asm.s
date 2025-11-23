.section .text
.global enqueue_value


enqueue_value:
    # Load *nelem, *tail, *head
    lw t0, 0(a2)      # t0 = nelem
    lw t1, 0(a3)      # t1 = tail
    lw t2, 0(a4)      # t2 = head

    # Check if full: nelem == length ???
    beq t0, a1, full_case

not_full:
    slli t3, t1, 2       # offset = tail * 4
    add  t3, t3, a0
    sw   a5, 0(t3)

    # tail++
    addi t1, t1, 1
    rem  t1, t1, a1
    sw   t1, 0(a3)

    # nelem++
    addi t0, t0, 1
    sw   t0, 0(a2)

    # return 1 if now full, else 0
    beq t0, a1, ret1
    li a0, 0
    ret

full_case:
    # overwrite oldest - head++
    addi t2, t2, 1
    rem  t2, t2, a1
    sw   t2, 0(a4)

    # write new value at tail
    slli t3, t1, 2
    add  t3, t3, a0
    sw   a5, 0(t3)

    # tail++
    addi t1, t1, 1
    rem  t1, t1, a1
    sw   t1, 0(a3)

    li a0, 1
    ret

ret1:
    li a0, 1
    ret
