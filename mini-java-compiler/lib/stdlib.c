#include <stdio.h>

int system_in_read() {
    printf("MiniJava runtime: read\n");
    return getchar();
}

void system_out_println(int x) {
    printf("MiniJava runtime: println\n");
    printf("%d\n", x);
}

void system_out_write(int x) {
    printf("MiniJava runtime: write\n");
    putchar(x);
}

void system_out_flush() {
    printf("MiniJava runtime: flush\n");
    // No-op since we do not buffer output (for now)
}
