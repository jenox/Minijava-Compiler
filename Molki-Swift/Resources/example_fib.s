.function _fib 1 1
    cmpl [ %@0d | $1d ]
    jle fib_basecase
    subl [ %@0d | $1d ] -> %@1d
    subl [ %@0d | $2d ] -> %@2d
    call _fib [ %@1d ] -> %@3d
    call _fib [ %@2d ] -> %@4d
    addl [ %@3d | %@4d ] -> %@$d
    jmp fib_end
fib_basecase:
    movq %@0 -> %@$
fib_end:
.endfunction

.function ___minijava_main 0 0
    movl $9d -> %@0d
    call _fib [ %@0d ] -> %@1d
    call _system_out_println [ %@1d ]
.endfunction
