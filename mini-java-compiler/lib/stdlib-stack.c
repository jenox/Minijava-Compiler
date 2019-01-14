// This standard library is based on the runtime provided by molki.
// It uses inline assembler to pass parameters on the stack instead
// of according to the standard x86_64 calling convention as followed
// by standard compilers like gcc and clang.

#include <stdio.h>
#include <inttypes.h>
#include <stdlib.h>

void __minijava_main(void);

int64_t system_in_read(void)
{
    int result = getchar();
    if (result == EOF) {
        return -1LL;
    } else {
        return result;
    }
}

void system_out_println(void)
{
    int64_t value;
    asm("movq 16(%%rbp), %0\n\t" : "=r" (value));
    printf("%" PRIi32 "\n", (int32_t)value);
}

void system_out_write(void)
{
    int64_t value;
    asm("movq 16(%%rbp), %0\n\t" : "=r" (value));
    printf("%c", (char)value);
}

void system_out_flush(void)
{
    fflush(stdout);
}

// Not used for now.
// void* __stdlib_mallolc(void)
// {
//     int64_t size;
//     asm("movq 16(%%rbp), %0\n\t" : "=r" (size));
//     return malloc(size);
// }

void* alloc_mem(void)
{
    uint32_t count;
    uint32_t size;
    asm("movl 24(%%rbp), %0\n\t" : "=r" (size));
    asm("movl 16(%%rbp), %0\n\t" : "=r" (count));

    uint32_t bytes = count * size;

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

int main(int argc, char **argv)
{
    __minijava_main();
    return 0;
}
