#include <stdio.h>
#include "asm.h"

int main() {
    int buffer[3] = {10,20,30};
    int length = 3;
    int nelem = 3;
    int tail = 0;
    int head = 0;
    int value;

    printf("dequeue -> %d | value=%d\n", dequeue_value(buffer, length, &nelem, &tail, &head, &value), value);
    printf("dequeue -> %d | value=%d\n", dequeue_value(buffer, length, &nelem, &tail, &head, &value), value);
    printf("dequeue -> %d | value=%d\n", dequeue_value(buffer, length, &nelem, &tail, &head, &value), value);
    printf("dequeue (empty) -> %d\n", dequeue_value(buffer, length, &nelem, &tail, &head, &value));

    return 0;
}
