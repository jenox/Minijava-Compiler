#include <stdio.h>
#include <stdlib.h>

int __minijava_main(void);

int main(void) {
    __minijava_main();
    return 0;
}

int system_in_read() {
    return getchar();
}

void system_out_println(int x) {
    printf("%d\n", x);
}

void system_out_write(int x) {
    putchar(x);
}

void system_out_flush() {
    fflush(stdout);
}

void* alloc_mem(int num, int size) {
    int bytes = num * size;

    // allocate some multiple of quadwords (8 bytes)
    bytes = (bytes + 8 - 1) / 8 * 8;

    // prevent null pointer for zero-size types or arrays
    bytes = (bytes == 0) ? 8 : bytes;

    void* pointer = calloc(1, bytes);

    // Catch null pointer on zero-sized or failed allocation
    // We abort execution at this point
    if (pointer == NULL) {
        abort();
    }

    return pointer;
}
