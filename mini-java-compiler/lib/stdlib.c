#include <stdio.h>
#include <stdlib.h>
#include <inttypes.h>

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

void* alloc_mem(uint32_t num, uint32_t size) {
    void* pointer = calloc(num, size);

    // Catch null pointer on zero-sized or failed allocation
    // We abort execution at this point
    if (pointer == NULL) {
        abort();
    }

    return pointer;
}
