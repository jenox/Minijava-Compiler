.function fib 1 1
    cmpq $1, %@0
    jle fib_basecase
    subq [ $1 | %@0 ] -> %@1
    subq [ $2 | %@0 ] -> %@2
    call fib [ %@1 ] -> %@3
    call fib [ %@2 ] -> %@4
    addq [ %@3 | %@4 ] -> %@r0
    jmp fib_end

fib_basecase:
    movq %@0, %@r0
fib_end:
.endfunction

.function minijava_main 0 1
    movq $9, %@0
    call fib [ %@0 ] -> %@1
    call __stdlib_println [ %@1 ]
.endfunction
