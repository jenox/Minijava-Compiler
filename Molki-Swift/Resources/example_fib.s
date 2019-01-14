.function _fib 1 1
    cmpq [ %@0 | $1 ]
    jle fib_basecase
    subl [ %@0d | $1 ] -> %@1d
    subl [ %@0d | $2 ] -> %@2d
    call _fib [ %@1d ] -> %@3d
    call _fib [ %@2d ] -> %@4d
    addl [ %@3d | %@4d ] -> %@$d
    jmp fib_end
fib_basecase:
    movq %@0 -> %@$
fib_end:
.endfunction

.function _minijava_main 0 0
    movq $9 -> %@0
    call _fib [ %@0 ] -> %@1
    call _system_out_println [ %@1 ]
.endfunction
