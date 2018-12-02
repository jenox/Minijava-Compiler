#include <stdio.h>

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
