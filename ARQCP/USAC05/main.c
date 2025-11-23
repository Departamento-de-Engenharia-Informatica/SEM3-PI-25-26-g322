#include <stdio.h>
#include "asm.h"

int main() {
    int buffer[3];
    int length = 3;
    int nelem = 0;
    int tail = 0;
    int head = 0;

    printf("enqueue 10 -> %d\n", enqueue_value(buffer, length, &nelem, &tail, &head, 10));
    printf("enqueue 20 -> %d\n", enqueue_value(buffer, length, &nelem, &tail, &head, 20));
    printf("enqueue 30 -> %d\n", enqueue_value(buffer, length, &nelem, &tail, &head, 30));
    printf("enqueue 40 (overwrite) -> %d\n", enqueue_value(buffer, length, &nelem, &tail, &head, 40));

    return 0;
}
