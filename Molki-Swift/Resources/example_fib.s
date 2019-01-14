.function fib 1 1
    cmpq [ $1 | %@0 ]
    jle fib_basecase
    subl [ $1 | %@0d ] -> %@1d
    subl [ $2 | %@0d ] -> %@2d
    call fib [ %@1 ] -> %@3
    call fib [ %@2 ] -> %@4
    addl [ %@3d | %@4d ] -> %@r0 d
    jmp fib_end
fib_basecase:
    movq %@0 -> %@r0
fib_end:
.endfunction

.function minijava_main 0 1
    movq $9 -> %@0
    call fib [ %@0 ] -> %@1
    call __stdlib_println [ %@1 ]
.endfunction
