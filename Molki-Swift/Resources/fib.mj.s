.function _Fib$fib 2 1
L77:
    movl $2d -> %@2d
    cmpl [ %@1d | %@2d ]
    movl %@1d -> %@$d
    movl $1d -> %@4d
    movl $2d -> %@7d
    jl L75
L91:
    movl %@1d -> %@$d
L95:
    subl [ %@1d | %@4d ] -> %@5d
    call _Fib$fib [ %@0 | %@5d ] -> %@6d
    subl [ %@1d | %@7d ] -> %@8d
    call _Fib$fib [ %@0 | %@8d ] -> %@9d
    addl [ %@6d | %@9d ] -> %@10d
    movl %@10d -> %@$d
L75:
.endfunction

.function ___minijava_main 0 0
L120:
    movl $1d -> %@0d
    movl $0d -> %@1d
    call _alloc_mem [ %@0d | %@1d ] -> %@2
    movl $24d -> %@3d
    call _Fib$fib [ %@2 | %@3d ] -> %@4d
    call _system_out_println [ %@4d ]
L118:
.endfunction
