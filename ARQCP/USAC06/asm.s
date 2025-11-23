.section .text
.global dequeue_value


dequeue_value:
    # load nelem
    lw t0, 0(a2)
    beq t0, x0, empty_case   # if nelem == 0 - return 0

    # load head index
    lw t1, 0(a4)

    # compute address buffer[head]
    slli t2, t1, 2
    add  t2, t2, a0

    # load buffer[head] → *value
    lw t3, 0(t2)
    sw t3, 0(a5)

    # head = (head + 1) % length
    addi t1, t1, 1
    rem  t1, t1, a1
    sw   t1, 0(a4)

    # nelem--
    addi t0, t0, -1
    sw   t0, 0(a2)

    li a0, 1
    ret

empty_case:
    li a0, 0
    ret
