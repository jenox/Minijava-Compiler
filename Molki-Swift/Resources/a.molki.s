.function _LCG$initWithDefault  [ q | l ] -> q
L471:
    movl $2147483629d -> %@2d
    movl $2147483587d -> %@3d
    movl $2147483647d -> %@4d
    call _LCG$init [ %@0 | %@2d | %@3d | %@4d | %@1d ] -> %@5
    movq %@5 -> %@$
    jmp L469
L469:
.endfunction

.function _LCG$initWithDefault2  [ q | l ] -> q
L496:
    movl $2147480707d -> %@2d
    movl $2147480707d -> %@3d
    movl $2147482367d -> %@4d
    call _LCG$init [ %@0 | %@2d | %@3d | %@4d | %@1d ] -> %@5
    movq %@5 -> %@$
    jmp L494
L494:
.endfunction

.function _LCG$init  [ q | l | l | l | l ] -> q
L524:
    movl %@1d -> 0(%@0)d
    movl %@2d -> 4(%@0)d
    movl %@3d -> 8(%@0)d
    movl %@4d -> 12(%@0)d
    movq %@0 -> %@$
    jmp L522
L522:
.endfunction

.function _LCG$nextVal  [ q ] -> l
L554:
    movl 0(%@0)d -> %@1d
    movl 12(%@0)d -> %@2d
    movl 4(%@0)d -> %@3d
    movl 8(%@0)d -> %@4d
    mull [ %@1d | %@2d ] -> %@5d 
    addl [ %@5d | %@3d ] -> %@6d
    divl [ %@6d | %@4d ] -> [ %@8d | %@7d ]
    movl %@7d -> 12(%@0)d
    movl %@7d -> %@$d
    jmp L552
L552:
.endfunction

.function _LCG$abs  [ q | l ] -> l
L596:
    movl $0d -> %@2d
    cmpl [ %@1d | %@2d ]
    jge L610
    jmp L614
L610:
    movl %@1d -> %@$d
    jmp L594
L614:
    negl %@1d -> %@4d
    movl %@4d -> %@$d
    jmp L594
L594:
.endfunction

.function _LCG$runTest  [ q ] 
L624:
    movl $0d -> %@1d
    movl $1d -> %@2d
    movl $100d -> %@5d
    movl %@1d -> %@4d
    jmp L634
L641:
    addl [ %@4d | %@2d ] -> %@3d
    call _LCG$nextVal [ %@0 ] -> %@6d
    call _LCG$abs [ %@0 | %@6d ] -> %@7d
    call _system_out_println [ %@7d ] -> %@8d
    movl %@3d -> %@4d
    jmp L634
L634:
    cmpl [ %@4d | %@5d ]
    jl L641
    jmp L662
L662:
    jmp L622
L622:
.endfunction

.function _LCG$nextRange  [ q | l | l ] -> l
L673:
    call _LCG$nextVal [ %@0 ] -> %@3d
    subl [ %@2d | %@1d ] -> %@4d
    divl [ %@3d | %@4d ] -> [ %@6d | %@5d ]
    addl [ %@5d | %@1d ] -> %@7d
    movl %@7d -> %@$d
    jmp L671
L671:
.endfunction

.function _LehmerRandom$init  [ q | l ] -> q
L715:
    movl $2147483647d -> %@2d
    movl %@2d -> 0(%@0)d
    movl $16807d -> %@4d
    movl %@4d -> 4(%@0)d
    movl $127773d -> %@6d
    movl %@6d -> 8(%@0)d
    movl $2836d -> %@8d
    movl %@8d -> 12(%@0)d
    movl %@1d -> 16(%@0)d
    movq %@0 -> %@$
    jmp L713
L713:
.endfunction

.function _LehmerRandom$initWithDefault  [ q ] -> q
L749:
    movl $2147480677d -> %@1d
    call _LehmerRandom$init [ %@0 | %@1d ] -> %@2
    movq %@2 -> %@$
    jmp L747
L747:
.endfunction

.function _LehmerRandom$random  [ q ] -> l
L770:
    movl 16(%@0)d -> %@1d
    movl 8(%@0)d -> %@2d
    divl [ %@1d | %@2d ] -> [ %@3d | %@4d ]
    movl 16(%@0)d -> %@5d
    movl 8(%@0)d -> %@6d
    divl [ %@5d | %@6d ] -> [ %@8d | %@7d ]
    movl 4(%@0)d -> %@9d
    mull [ %@9d | %@7d ] -> %@10d 
    movl 12(%@0)d -> %@11d
    mull [ %@11d | %@3d ] -> %@12d 
    subl [ %@10d | %@12d ] -> %@13d
    movl $0d -> %@14d
    cmpl [ %@13d | %@14d ]
    jle L822
    jmp L831
L822:
    movl 0(%@0)d -> %@15d
    addl [ %@13d | %@15d ] -> %@17d
    movl %@17d -> %@18d
    jmp L832
L831:
    movl %@13d -> %@18d
    jmp L832
L832:
    movl %@18d -> 16(%@0)d
    movl %@18d -> %@$d
    jmp L768
L768:
.endfunction

.function _LehmerRandom$next  [ q ] -> l
L847:
    call _LehmerRandom$random [ %@0 ] -> %@1d
    movl %@1d -> %@$d
    jmp L845
L845:
.endfunction

.function _LehmerRandom$nextRange  [ q | l | l ] -> l
L869:
    call _LehmerRandom$next [ %@0 ] -> %@3d
    subl [ %@2d | %@1d ] -> %@4d
    divl [ %@3d | %@4d ] -> [ %@6d | %@5d ]
    addl [ %@5d | %@1d ] -> %@7d
    movl %@7d -> %@$d
    jmp L867
L867:
.endfunction

.function _LehmerRandom$intArray  [ q | l | l | l ] -> q
L902:
    movl $0d -> %@4d
    movl $1d -> %@5d
    movl $4d -> %@8d
    call _alloc_mem [ %@1d | %@8d ] -> %@9
    movl %@4d -> %@7d
    jmp L924
L931:
    addl [ %@7d | %@5d ] -> %@6d
    call _LehmerRandom$nextRange [ %@0 | %@2d | %@3d ] -> %@10d
    movl %@10d -> (%@9, %@7d, 4)d
    movl %@6d -> %@7d
    jmp L924
L924:
    cmpl [ %@7d | %@1d ]
    jl L931
    jmp L953
L953:
    movq %@9 -> %@$
    jmp L900
L900:
.endfunction

.function _LehmerRandom$nextBoolean  [ q ] -> b
L967:
    call _LehmerRandom$next [ %@0 ] -> %@1d
    movl $2d -> %@2d
    divl [ %@1d | %@2d ] -> [ %@4d | %@3d ]
    movl $0d -> %@5d
    movb $-1l -> %@6l
    movb $0l -> %@7l
    cmpl [ %@3d | %@5d ]
    je L995
    jmp L995
L995:
    movb %@6l -> %@8l
    je L998
    movb %@7l -> %@8l
    L998:
    movb %@8l -> %@$l
    jmp L965
L965:
.endfunction

.function _LehmerRandom$booleanArray  [ q | l ] -> q
L1005:
    movl $0d -> %@2d
    movl $1d -> %@3d
    movl $1d -> %@6d
    call _alloc_mem [ %@1d | %@6d ] -> %@7
    movl %@2d -> %@5d
    jmp L1025
L1032:
    addl [ %@5d | %@3d ] -> %@4d
    call _LehmerRandom$nextBoolean [ %@0 ] -> %@8l
    movb %@8l -> (%@7, %@5d, 1)l
    movl %@4d -> %@5d
    jmp L1025
L1025:
    cmpl [ %@5d | %@1d ]
    jl L1032
    jmp L1052
L1052:
    movq %@7 -> %@$
    jmp L1003
L1003:
.endfunction

.function _LehmerRandom$shuffleIntArray  [ q | q | l ] 
L1068:
    movl $1d -> %@3d
    subl [ %@2d | %@3d ] -> %@4d
    movl $1d -> %@5d
    movl $0d -> %@8d
    movl $0d -> %@9d
    movl $1d -> %@10d
    movl %@4d -> %@7d
    jmp L1081
L1088:
    subl [ %@7d | %@5d ] -> %@6d
    addl [ %@7d | %@10d ] -> %@11d
    call _LehmerRandom$nextRange [ %@0 | %@9d | %@11d ] -> %@12d
    movl ( %@1, %@12d, 4)d -> %@14d
    movl ( %@1, %@7d, 4)d -> %@16d
    movl %@16d -> (%@1, %@12d, 4)d
    movl %@14d -> (%@1, %@7d, 4)d
    movl %@6d -> %@7d
    jmp L1081
L1081:
    cmpl [ %@7d | %@8d ]
    jg L1088
    jmp L1134
L1134:
    jmp L1066
L1066:
.endfunction

.function _BigTensorProduct$run  [ q | l | b | l ] 
L1148:
    movl $0d -> %@4d
    movl $1d -> %@5d
    movl $7d -> %@8d
    movl $1d -> %@9d
    movl $16d -> %@10d
    call _alloc_mem [ %@9d | %@10d ] -> %@11
    call _LCG$initWithDefault2 [ %@11 | %@3d ] -> %@12d
    movl $7d -> %@13d
    movl $4d -> %@14d
    call _alloc_mem [ %@13d | %@14d ] -> %@15
    movl $0d -> %@17d
    movl $1d -> %@20d
    movl $7d -> %@23d
    movl $2d -> %@24d
    movl $0d -> %@27d
    movl $1d -> %@30d
    movl $2d -> %@33d
    movl $3d -> %@36d
    movl $4d -> %@39d
    movl $5d -> %@42d
    movl $6d -> %@45d
    movl $0d -> %@48d
    movl $1d -> %@49d
    movl $7d -> %@52d
    movl $0d -> %@60d
    movl $1d -> %@65d
    movl $2d -> %@70d
    movl $3d -> %@75d
    movl $4d -> %@80d
    movl $5d -> %@85d
    movl $6d -> %@90d
    movl $0d -> %@97d
    movl $1d -> %@102d
    movl $2d -> %@107d
    movl $3d -> %@112d
    movl $4d -> %@117d
    movl $5d -> %@122d
    movl $6d -> %@127d
    movl $6d -> %@132d
    movl $6d -> %@135d
    movl $1d -> %@137d
    movb $0l -> %@141l
    movl $6d -> %@142d
    movl $0d -> %@144d
    movl $5d -> %@146d
    movl $5d -> %@149d
    movl $1d -> %@151d
    movl $5d -> %@155d
    movl $0d -> %@157d
    movl $4d -> %@159d
    movl $4d -> %@162d
    movl $1d -> %@164d
    movl $4d -> %@168d
    movl $0d -> %@170d
    movl $3d -> %@172d
    movl $3d -> %@175d
    movl $1d -> %@177d
    movl $3d -> %@181d
    movl $0d -> %@183d
    movl $2d -> %@185d
    movl $2d -> %@188d
    movl $1d -> %@190d
    movl $2d -> %@194d
    movl $0d -> %@196d
    movl $1d -> %@198d
    movl $1d -> %@201d
    movl $1d -> %@203d
    movl $1d -> %@207d
    movl $0d -> %@209d
    movl $0d -> %@211d
    movl $0d -> %@214d
    movl $1d -> %@216d
    movl $0d -> %@220d
    movl $1d -> %@221d
    movl %@4d -> %@7d
    jmp L1184
L1191:
    addl [ %@7d | %@5d ] -> %@6d
    movl %@17d -> (%@15, %@7d, 4)d
    movl %@6d -> %@7d
    jmp L1184
L1184:
    cmpl [ %@7d | %@8d ]
    jl L1191
    jmp L1206
L1206:
    addl [ %@3d | %@20d ] -> %@21d
    call _BigTensorProduct$randomIntArray [ %@0 | %@1d | %@21d ] -> %@22
    addl [ %@3d | %@24d ] -> %@25d
    call _BigTensorProduct$randomMatrix [ %@0 | %@23d | %@1d | %@25d ] -> %@26
    movl %@220d -> %@232d
    jmp L1230
L1268:
    movl %@231d -> %@230d
    jmp L1270
L1306:
    movl %@229d -> %@228d
    jmp L1308
L1344:
    movl %@227d -> %@226d
    jmp L1346
L1374:
    addl [ %@51d | %@49d ] -> %@50d
    movq ( %@26, %@51d, 8) -> %@54
    movl ( %@15, %@51d, 4)d -> %@56d
    movl ( %@54, %@56d, 4)d -> %@58d
    mull [ %@223d | %@58d ] -> %@222d 
    movl %@50d -> %@51d
    movl %@222d -> %@223d
    jmp L1367
L1363:
    movl %@48d -> %@51d
    movl %@221d -> %@223d
    jmp L1367
L1367:
    cmpl [ %@51d | %@52d ]
    jl L1374
    jmp L1412
L1412:
    cmpb [ %@2l | %@141l ]
    jne L1426
    jmp L1636
L1426:
    movl ( %@15, %@60d, 4)d -> %@62d
    movq ( %@22, %@62d, 8) -> %@64
    movl ( %@15, %@65d, 4)d -> %@67d
    movq ( %@64, %@67d, 8) -> %@69
    movl ( %@15, %@70d, 4)d -> %@72d
    movq ( %@69, %@72d, 8) -> %@74
    movl ( %@15, %@75d, 4)d -> %@77d
    movq ( %@74, %@77d, 8) -> %@79
    movl ( %@15, %@80d, 4)d -> %@82d
    movq ( %@79, %@82d, 8) -> %@84
    movl ( %@15, %@85d, 4)d -> %@87d
    movq ( %@84, %@87d, 8) -> %@89
    movl ( %@15, %@90d, 4)d -> %@92d
    movl ( %@89, %@92d, 4)d -> %@94d
    call _system_out_println [ %@94d ] -> %@95d
    jmp L1637
L1636:
    jmp L1637
L1637:
    movl ( %@15, %@97d, 4)d -> %@99d
    movq ( %@22, %@99d, 8) -> %@101
    movl ( %@15, %@102d, 4)d -> %@104d
    movq ( %@101, %@104d, 8) -> %@106
    movl ( %@15, %@107d, 4)d -> %@109d
    movq ( %@106, %@109d, 8) -> %@111
    movl ( %@15, %@112d, 4)d -> %@114d
    movq ( %@111, %@114d, 8) -> %@116
    movl ( %@15, %@117d, 4)d -> %@119d
    movq ( %@116, %@119d, 8) -> %@121
    movl ( %@15, %@122d, 4)d -> %@124d
    movq ( %@121, %@124d, 8) -> %@126
    movl ( %@15, %@127d, 4)d -> %@129d
    movl ( %@126, %@129d, 4)d -> %@131d
    movl ( %@15, %@132d, 4)d -> %@134d
    addl [ %@134d | %@137d ] -> %@138d
    movl %@138d -> (%@15, %@135d, 4)d
    mull [ %@223d | %@131d ] -> %@224d 
    addl [ %@226d | %@224d ] -> %@225d
    movl %@225d -> %@226d
    jmp L1346
L1346:
    movl ( %@15, %@45d, 4)d -> %@47d
    cmpl [ %@47d | %@1d ]
    jl L1363
    jmp L1872
L1872:
    movl %@144d -> (%@15, %@142d, 4)d
    movl ( %@15, %@146d, 4)d -> %@148d
    addl [ %@148d | %@151d ] -> %@152d
    movl %@152d -> (%@15, %@149d, 4)d
    movl %@226d -> %@227d
    jmp L1327
L1325:
    movl %@228d -> %@227d
    jmp L1327
L1327:
    movl ( %@15, %@42d, 4)d -> %@44d
    cmpl [ %@44d | %@1d ]
    jl L1344
    jmp L1910
L1910:
    movl %@157d -> (%@15, %@155d, 4)d
    movl ( %@15, %@159d, 4)d -> %@161d
    addl [ %@161d | %@164d ] -> %@165d
    movl %@165d -> (%@15, %@162d, 4)d
    movl %@227d -> %@228d
    jmp L1308
L1308:
    movl ( %@15, %@39d, 4)d -> %@41d
    cmpl [ %@41d | %@1d ]
    jl L1325
    jmp L1949
L1949:
    movl %@170d -> (%@15, %@168d, 4)d
    movl ( %@15, %@172d, 4)d -> %@174d
    addl [ %@174d | %@177d ] -> %@178d
    movl %@178d -> (%@15, %@175d, 4)d
    movl %@228d -> %@229d
    jmp L1289
L1287:
    movl %@230d -> %@229d
    jmp L1289
L1289:
    movl ( %@15, %@36d, 4)d -> %@38d
    cmpl [ %@38d | %@1d ]
    jl L1306
    jmp L1988
L1988:
    movl %@183d -> (%@15, %@181d, 4)d
    movl ( %@15, %@185d, 4)d -> %@187d
    addl [ %@187d | %@190d ] -> %@191d
    movl %@191d -> (%@15, %@188d, 4)d
    movl %@229d -> %@230d
    jmp L1270
L1270:
    movl ( %@15, %@33d, 4)d -> %@35d
    cmpl [ %@35d | %@1d ]
    jl L1287
    jmp L2027
L2027:
    movl %@196d -> (%@15, %@194d, 4)d
    movl ( %@15, %@198d, 4)d -> %@200d
    addl [ %@200d | %@203d ] -> %@204d
    movl %@204d -> (%@15, %@201d, 4)d
    movl %@230d -> %@231d
    jmp L1251
L1249:
    movl %@232d -> %@231d
    jmp L1251
L1251:
    movl ( %@15, %@30d, 4)d -> %@32d
    cmpl [ %@32d | %@1d ]
    jl L1268
    jmp L2066
L2066:
    movl %@209d -> (%@15, %@207d, 4)d
    movl ( %@15, %@211d, 4)d -> %@213d
    addl [ %@213d | %@216d ] -> %@217d
    movl %@217d -> (%@15, %@214d, 4)d
    movl %@231d -> %@232d
    jmp L1230
L1230:
    movl ( %@15, %@27d, 4)d -> %@29d
    cmpl [ %@29d | %@1d ]
    jl L1249
    jmp L2105
L2105:
    call _system_out_println [ %@232d ] -> %@233d
    jmp L1146
L1146:
.endfunction

.function _BigTensorProduct$runWithNumbers  [ q | l | b | l ] 
L2141:
    movl $0d -> %@4d
    movl $1d -> %@5d
    movl $7d -> %@8d
    movl $1d -> %@9d
    movl $16d -> %@10d
    call _alloc_mem [ %@9d | %@10d ] -> %@11
    call _LCG$initWithDefault2 [ %@11 | %@3d ] -> %@12d
    movl $1d -> %@13d
    movl $4d -> %@14d
    call _alloc_mem [ %@13d | %@14d ] -> %@15
    movl $0d -> %@16d
    call _Number$init [ %@15 | %@16d ] -> %@17
    movl $7d -> %@18d
    movl $4d -> %@19d
    call _alloc_mem [ %@18d | %@19d ] -> %@20
    movl $0d -> %@22d
    movl $1d -> %@25d
    movl $7d -> %@28d
    movl $2d -> %@29d
    movl $0d -> %@32d
    movl $1d -> %@35d
    movl $2d -> %@38d
    movl $3d -> %@41d
    movl $4d -> %@44d
    movl $5d -> %@47d
    movl $6d -> %@50d
    movl $1d -> %@53d
    movl $4d -> %@54d
    movl $1d -> %@56d
    movl $0d -> %@58d
    movl $1d -> %@59d
    movl $7d -> %@62d
    movl $0d -> %@72d
    movl $1d -> %@77d
    movl $2d -> %@82d
    movl $3d -> %@87d
    movl $4d -> %@92d
    movl $5d -> %@97d
    movl $6d -> %@102d
    movl $0d -> %@110d
    movl $1d -> %@115d
    movl $2d -> %@120d
    movl $3d -> %@125d
    movl $4d -> %@130d
    movl $5d -> %@135d
    movl $6d -> %@140d
    movl $6d -> %@154d
    movl $6d -> %@157d
    movl $1d -> %@159d
    movb $0l -> %@163l
    movl $6d -> %@164d
    movl $0d -> %@166d
    movl $5d -> %@168d
    movl $5d -> %@171d
    movl $1d -> %@173d
    movl $5d -> %@177d
    movl $0d -> %@179d
    movl $4d -> %@181d
    movl $4d -> %@184d
    movl $1d -> %@186d
    movl $4d -> %@190d
    movl $0d -> %@192d
    movl $3d -> %@194d
    movl $3d -> %@197d
    movl $1d -> %@199d
    movl $3d -> %@203d
    movl $0d -> %@205d
    movl $2d -> %@207d
    movl $2d -> %@210d
    movl $1d -> %@212d
    movl $2d -> %@218d
    movl $0d -> %@220d
    movl $1d -> %@222d
    movl $1d -> %@225d
    movl $1d -> %@227d
    movl $1d -> %@231d
    movl $0d -> %@233d
    movl $0d -> %@235d
    movl $0d -> %@238d
    movl $1d -> %@240d
    movl %@4d -> %@7d
    jmp L2189
L2196:
    addl [ %@7d | %@5d ] -> %@6d
    movl %@22d -> (%@20, %@7d, 4)d
    movl %@6d -> %@7d
    jmp L2189
L2189:
    cmpl [ %@7d | %@8d ]
    jl L2196
    jmp L2211
L2211:
    addl [ %@3d | %@25d ] -> %@26d
    call _BigTensorProduct$randomNumberArray [ %@0 | %@1d | %@26d ] -> %@27
    addl [ %@3d | %@29d ] -> %@30d
    call _BigTensorProduct$randomNumberMatrix [ %@0 | %@28d | %@1d | %@30d ] -> %@31
    movq %@17 -> %@146
    jmp L2235
L2273:
    movq %@147 -> %@148
    jmp L2275
L2311:
    movq %@149 -> %@150
    jmp L2313
L2349:
    movq %@151 -> %@153
    jmp L2351
L2392:
    addl [ %@61d | %@59d ] -> %@60d
    movq ( %@31, %@61d, 8) -> %@64
    movl ( %@20, %@61d, 4)d -> %@66d
    movq ( %@64, %@66d, 8) -> %@68
    call _Number$mul [ %@70 | %@68 ] -> %@69
    movl %@60d -> %@61d
    movq %@69 -> %@70
    jmp L2385
L2368:
    call _alloc_mem [ %@53d | %@54d ] -> %@55
    call _Number$init [ %@55 | %@56d ] -> %@57
    movl %@58d -> %@61d
    movq %@57 -> %@70
    jmp L2385
L2385:
    cmpl [ %@61d | %@62d ]
    jl L2392
    jmp L2438
L2438:
    cmpb [ %@2l | %@163l ]
    jne L2452
    jmp L2680
L2452:
    movl ( %@20, %@72d, 4)d -> %@74d
    movq ( %@27, %@74d, 8) -> %@76
    movl ( %@20, %@77d, 4)d -> %@79d
    movq ( %@76, %@79d, 8) -> %@81
    movl ( %@20, %@82d, 4)d -> %@84d
    movq ( %@81, %@84d, 8) -> %@86
    movl ( %@20, %@87d, 4)d -> %@89d
    movq ( %@86, %@89d, 8) -> %@91
    movl ( %@20, %@92d, 4)d -> %@94d
    movq ( %@91, %@94d, 8) -> %@96
    movl ( %@20, %@97d, 4)d -> %@99d
    movq ( %@96, %@99d, 8) -> %@101
    movl ( %@20, %@102d, 4)d -> %@104d
    movq ( %@101, %@104d, 8) -> %@106
    movl 0(%@106)d -> %@107d
    call _system_out_println [ %@107d ] -> %@108d
    jmp L2681
L2680:
    jmp L2681
L2681:
    movl ( %@20, %@110d, 4)d -> %@112d
    movq ( %@27, %@112d, 8) -> %@114
    movl ( %@20, %@115d, 4)d -> %@117d
    movq ( %@114, %@117d, 8) -> %@119
    movl ( %@20, %@120d, 4)d -> %@122d
    movq ( %@119, %@122d, 8) -> %@124
    movl ( %@20, %@125d, 4)d -> %@127d
    movq ( %@124, %@127d, 8) -> %@129
    movl ( %@20, %@130d, 4)d -> %@132d
    movq ( %@129, %@132d, 8) -> %@134
    movl ( %@20, %@135d, 4)d -> %@137d
    movq ( %@134, %@137d, 8) -> %@139
    movl ( %@20, %@140d, 4)d -> %@142d
    movq ( %@139, %@142d, 8) -> %@144
    call _Number$mul [ %@70 | %@144 ] -> %@145
    call _Number$add [ %@153 | %@145 ] -> %@152
    movl ( %@20, %@154d, 4)d -> %@156d
    addl [ %@156d | %@159d ] -> %@160d
    movl %@160d -> (%@20, %@157d, 4)d
    movq %@152 -> %@153
    jmp L2351
L2351:
    movl ( %@20, %@50d, 4)d -> %@52d
    cmpl [ %@52d | %@1d ]
    jl L2368
    jmp L2938
L2938:
    movl %@166d -> (%@20, %@164d, 4)d
    movl ( %@20, %@168d, 4)d -> %@170d
    addl [ %@170d | %@173d ] -> %@174d
    movl %@174d -> (%@20, %@171d, 4)d
    movq %@153 -> %@151
    jmp L2332
L2330:
    movq %@150 -> %@151
    jmp L2332
L2332:
    movl ( %@20, %@47d, 4)d -> %@49d
    cmpl [ %@49d | %@1d ]
    jl L2349
    jmp L2976
L2976:
    movl %@179d -> (%@20, %@177d, 4)d
    movl ( %@20, %@181d, 4)d -> %@183d
    addl [ %@183d | %@186d ] -> %@187d
    movl %@187d -> (%@20, %@184d, 4)d
    movq %@151 -> %@150
    jmp L2313
L2313:
    movl ( %@20, %@44d, 4)d -> %@46d
    cmpl [ %@46d | %@1d ]
    jl L2330
    jmp L3015
L3015:
    movl %@192d -> (%@20, %@190d, 4)d
    movl ( %@20, %@194d, 4)d -> %@196d
    addl [ %@196d | %@199d ] -> %@200d
    movl %@200d -> (%@20, %@197d, 4)d
    movq %@150 -> %@149
    jmp L2294
L2292:
    movq %@148 -> %@149
    jmp L2294
L2294:
    movl ( %@20, %@41d, 4)d -> %@43d
    cmpl [ %@43d | %@1d ]
    jl L2311
    jmp L3054
L3054:
    movl %@205d -> (%@20, %@203d, 4)d
    movl ( %@20, %@207d, 4)d -> %@209d
    addl [ %@209d | %@212d ] -> %@213d
    movl %@213d -> (%@20, %@210d, 4)d
    movq %@149 -> %@148
    jmp L2275
L2275:
    movl ( %@20, %@38d, 4)d -> %@40d
    cmpl [ %@40d | %@1d ]
    jl L2292
    jmp L3093
L3093:
    movl 0(%@148)d -> %@216d
    call _system_out_println [ %@216d ] -> %@217d
    movl %@220d -> (%@20, %@218d, 4)d
    movl ( %@20, %@222d, 4)d -> %@224d
    addl [ %@224d | %@227d ] -> %@228d
    movl %@228d -> (%@20, %@225d, 4)d
    movq %@148 -> %@147
    jmp L2256
L2254:
    movq %@146 -> %@147
    jmp L2256
L2256:
    movl ( %@20, %@35d, 4)d -> %@37d
    cmpl [ %@37d | %@1d ]
    jl L2273
    jmp L3140
L3140:
    movl %@233d -> (%@20, %@231d, 4)d
    movl ( %@20, %@235d, 4)d -> %@237d
    addl [ %@237d | %@240d ] -> %@241d
    movl %@241d -> (%@20, %@238d, 4)d
    movq %@147 -> %@146
    jmp L2235
L2235:
    movl ( %@20, %@32d, 4)d -> %@34d
    cmpl [ %@34d | %@1d ]
    jl L2254
    jmp L3179
L3179:
    movl 0(%@146)d -> %@244d
    call _system_out_println [ %@244d ] -> %@245d
    jmp L2139
L2139:
.endfunction

.function _BigTensorProduct$randomMatrix  [ q | l | l | l ] -> q
L3218:
    movl $0d -> %@4d
    movl $1d -> %@5d
    movl $0d -> %@8d
    movl $1d -> %@9d
    movl $1d -> %@12d
    movl $20d -> %@13d
    call _alloc_mem [ %@12d | %@13d ] -> %@14
    call _LehmerRandom$init [ %@14 | %@3d ] -> %@15
    movl $8d -> %@16d
    call _alloc_mem [ %@1d | %@16d ] -> %@17
    movl $4d -> %@18d
    movl %@4d -> %@7d
    jmp L3254
L3261:
    call _alloc_mem [ %@2d | %@18d ] -> %@19
    movq %@19 -> (%@17, %@7d, 8)
    movl %@8d -> %@11d
    jmp L3288
L3298:
    addl [ %@11d | %@9d ] -> %@10d
    movq ( %@17, %@7d, 8) -> %@23
    call _LehmerRandom$next [ %@15 ] -> %@24d
    movl %@24d -> (%@23, %@11d, 4)d
    movl %@10d -> %@11d
    jmp L3288
L3288:
    cmpl [ %@11d | %@2d ]
    jl L3298
    jmp L3331
L3331:
    addl [ %@7d | %@5d ] -> %@6d
    movl %@6d -> %@7d
    jmp L3254
L3254:
    cmpl [ %@7d | %@1d ]
    jl L3261
    jmp L3342
L3342:
    movq %@17 -> %@$
    jmp L3216
L3216:
.endfunction

.function _BigTensorProduct$randomNumberMatrix  [ q | l | l | l ] -> q
L3360:
    movl $0d -> %@4d
    movl $1d -> %@5d
    movl $0d -> %@8d
    movl $1d -> %@9d
    movl $1d -> %@12d
    movl $20d -> %@13d
    call _alloc_mem [ %@12d | %@13d ] -> %@14
    call _LehmerRandom$init [ %@14 | %@3d ] -> %@15
    movl $8d -> %@16d
    call _alloc_mem [ %@1d | %@16d ] -> %@17
    movl $8d -> %@18d
    movl $1d -> %@24d
    movl $4d -> %@25d
    movl %@4d -> %@7d
    jmp L3397
L3404:
    call _alloc_mem [ %@2d | %@18d ] -> %@19
    movq %@19 -> (%@17, %@7d, 8)
    movl %@8d -> %@11d
    jmp L3434
L3444:
    addl [ %@11d | %@9d ] -> %@10d
    movq ( %@17, %@7d, 8) -> %@23
    call _alloc_mem [ %@24d | %@25d ] -> %@26
    call _LehmerRandom$next [ %@15 ] -> %@27d
    call _Number$init [ %@26 | %@27d ] -> %@28
    movq %@28 -> (%@23, %@11d, 8)
    movl %@10d -> %@11d
    jmp L3434
L3434:
    cmpl [ %@11d | %@2d ]
    jl L3444
    jmp L3493
L3493:
    addl [ %@7d | %@5d ] -> %@6d
    movl %@6d -> %@7d
    jmp L3397
L3397:
    cmpl [ %@7d | %@1d ]
    jl L3404
    jmp L3504
L3504:
    movq %@17 -> %@$
    jmp L3358
L3358:
.endfunction

.function _BigTensorProduct$randomIntArray  [ q | l | l ] -> q
L3521:
    movl $1d -> %@3d
    movl $16d -> %@4d
    call _alloc_mem [ %@3d | %@4d ] -> %@5
    call _LCG$initWithDefault [ %@5 | %@2d ] -> %@6
    movl $8d -> %@7d
    call _alloc_mem [ %@1d | %@7d ] -> %@8
    movl $7d -> %@9d
    movl $4d -> %@10d
    call _alloc_mem [ %@9d | %@10d ] -> %@11
    movl $0d -> %@12d
    movl $0d -> %@15d
    movl $8d -> %@18d
    movl $1d -> %@22d
    movl $0d -> %@25d
    movl $1d -> %@30d
    movl $8d -> %@33d
    movl $2d -> %@37d
    movl $0d -> %@40d
    movl $1d -> %@45d
    movl $2d -> %@50d
    movl $8d -> %@53d
    movl $3d -> %@57d
    movl $0d -> %@60d
    movl $1d -> %@65d
    movl $2d -> %@70d
    movl $3d -> %@75d
    movl $8d -> %@78d
    movl $4d -> %@82d
    movl $0d -> %@85d
    movl $1d -> %@90d
    movl $2d -> %@95d
    movl $3d -> %@100d
    movl $4d -> %@105d
    movl $8d -> %@108d
    movl $5d -> %@112d
    movl $0d -> %@115d
    movl $1d -> %@120d
    movl $2d -> %@125d
    movl $3d -> %@130d
    movl $4d -> %@135d
    movl $5d -> %@140d
    movl $4d -> %@143d
    movl $6d -> %@147d
    movl $0d -> %@150d
    movl $1d -> %@155d
    movl $2d -> %@160d
    movl $3d -> %@165d
    movl $4d -> %@170d
    movl $5d -> %@175d
    movl $6d -> %@180d
    movl $6d -> %@186d
    movl $6d -> %@189d
    movl $1d -> %@191d
    movl $6d -> %@195d
    movl $0d -> %@197d
    movl $5d -> %@199d
    movl $5d -> %@202d
    movl $1d -> %@204d
    movl $5d -> %@208d
    movl $0d -> %@210d
    movl $4d -> %@212d
    movl $4d -> %@215d
    movl $1d -> %@217d
    movl $4d -> %@221d
    movl $0d -> %@223d
    movl $3d -> %@225d
    movl $3d -> %@228d
    movl $1d -> %@230d
    movl $3d -> %@234d
    movl $0d -> %@236d
    movl $2d -> %@238d
    movl $2d -> %@241d
    movl $1d -> %@243d
    movl $2d -> %@247d
    movl $0d -> %@249d
    movl $1d -> %@251d
    movl $1d -> %@254d
    movl $1d -> %@256d
    movl $1d -> %@260d
    movl $0d -> %@262d
    movl $0d -> %@264d
    movl $0d -> %@267d
    movl $1d -> %@269d
    jmp L3575
L3675:
    movl ( %@11, %@25d, 4)d -> %@27d
    movq ( %@8, %@27d, 8) -> %@29
    movl ( %@11, %@30d, 4)d -> %@32d
    call _alloc_mem [ %@1d | %@33d ] -> %@34
    movq %@34 -> (%@29, %@32d, 8)
    jmp L3775
L3942:
    movl ( %@11, %@60d, 4)d -> %@62d
    movq ( %@8, %@62d, 8) -> %@64
    movl ( %@11, %@65d, 4)d -> %@67d
    movq ( %@64, %@67d, 8) -> %@69
    movl ( %@11, %@70d, 4)d -> %@72d
    movq ( %@69, %@72d, 8) -> %@74
    movl ( %@11, %@75d, 4)d -> %@77d
    call _alloc_mem [ %@1d | %@78d ] -> %@79
    movq %@79 -> (%@74, %@77d, 8)
    jmp L4100
L4317:
    movl ( %@11, %@115d, 4)d -> %@117d
    movq ( %@8, %@117d, 8) -> %@119
    movl ( %@11, %@120d, 4)d -> %@122d
    movq ( %@119, %@122d, 8) -> %@124
    movl ( %@11, %@125d, 4)d -> %@127d
    movq ( %@124, %@127d, 8) -> %@129
    movl ( %@11, %@130d, 4)d -> %@132d
    movq ( %@129, %@132d, 8) -> %@134
    movl ( %@11, %@135d, 4)d -> %@137d
    movq ( %@134, %@137d, 8) -> %@139
    movl ( %@11, %@140d, 4)d -> %@142d
    call _alloc_mem [ %@1d | %@143d ] -> %@144
    movq %@144 -> (%@139, %@142d, 8)
    jmp L4517
L4535:
    movl ( %@11, %@150d, 4)d -> %@152d
    movq ( %@8, %@152d, 8) -> %@154
    movl ( %@11, %@155d, 4)d -> %@157d
    movq ( %@154, %@157d, 8) -> %@159
    movl ( %@11, %@160d, 4)d -> %@162d
    movq ( %@159, %@162d, 8) -> %@164
    movl ( %@11, %@165d, 4)d -> %@167d
    movq ( %@164, %@167d, 8) -> %@169
    movl ( %@11, %@170d, 4)d -> %@172d
    movq ( %@169, %@172d, 8) -> %@174
    movl ( %@11, %@175d, 4)d -> %@177d
    movq ( %@174, %@177d, 8) -> %@179
    movl ( %@11, %@180d, 4)d -> %@182d
    call _LCG$nextVal [ %@6 ] -> %@183d
    movl %@183d -> (%@179, %@182d, 4)d
    movl ( %@11, %@186d, 4)d -> %@188d
    addl [ %@188d | %@191d ] -> %@192d
    movl %@192d -> (%@11, %@189d, 4)d
    jmp L4517
L4517:
    movl ( %@11, %@147d, 4)d -> %@149d
    cmpl [ %@149d | %@1d ]
    jl L4535
    jmp L4767
L4767:
    movl %@197d -> (%@11, %@195d, 4)d
    movl ( %@11, %@199d, 4)d -> %@201d
    addl [ %@201d | %@204d ] -> %@205d
    movl %@205d -> (%@11, %@202d, 4)d
    jmp L4299
L4118:
    movl ( %@11, %@85d, 4)d -> %@87d
    movq ( %@8, %@87d, 8) -> %@89
    movl ( %@11, %@90d, 4)d -> %@92d
    movq ( %@89, %@92d, 8) -> %@94
    movl ( %@11, %@95d, 4)d -> %@97d
    movq ( %@94, %@97d, 8) -> %@99
    movl ( %@11, %@100d, 4)d -> %@102d
    movq ( %@99, %@102d, 8) -> %@104
    movl ( %@11, %@105d, 4)d -> %@107d
    call _alloc_mem [ %@1d | %@108d ] -> %@109
    movq %@109 -> (%@104, %@107d, 8)
    jmp L4299
L4299:
    movl ( %@11, %@112d, 4)d -> %@114d
    cmpl [ %@114d | %@1d ]
    jl L4317
    jmp L4803
L4803:
    movl %@210d -> (%@11, %@208d, 4)d
    movl ( %@11, %@212d, 4)d -> %@214d
    addl [ %@214d | %@217d ] -> %@218d
    movl %@218d -> (%@11, %@215d, 4)d
    jmp L4100
L4100:
    movl ( %@11, %@82d, 4)d -> %@84d
    cmpl [ %@84d | %@1d ]
    jl L4118
    jmp L4839
L4839:
    movl %@223d -> (%@11, %@221d, 4)d
    movl ( %@11, %@225d, 4)d -> %@227d
    addl [ %@227d | %@230d ] -> %@231d
    movl %@231d -> (%@11, %@228d, 4)d
    jmp L3924
L3793:
    movl ( %@11, %@40d, 4)d -> %@42d
    movq ( %@8, %@42d, 8) -> %@44
    movl ( %@11, %@45d, 4)d -> %@47d
    movq ( %@44, %@47d, 8) -> %@49
    movl ( %@11, %@50d, 4)d -> %@52d
    call _alloc_mem [ %@1d | %@53d ] -> %@54
    movq %@54 -> (%@49, %@52d, 8)
    jmp L3924
L3924:
    movl ( %@11, %@57d, 4)d -> %@59d
    cmpl [ %@59d | %@1d ]
    jl L3942
    jmp L4875
L4875:
    movl %@236d -> (%@11, %@234d, 4)d
    movl ( %@11, %@238d, 4)d -> %@240d
    addl [ %@240d | %@243d ] -> %@244d
    movl %@244d -> (%@11, %@241d, 4)d
    jmp L3775
L3775:
    movl ( %@11, %@37d, 4)d -> %@39d
    cmpl [ %@39d | %@1d ]
    jl L3793
    jmp L4911
L4911:
    movl %@249d -> (%@11, %@247d, 4)d
    movl ( %@11, %@251d, 4)d -> %@253d
    addl [ %@253d | %@256d ] -> %@257d
    movl %@257d -> (%@11, %@254d, 4)d
    jmp L3657
L3592:
    movl ( %@11, %@15d, 4)d -> %@17d
    call _alloc_mem [ %@1d | %@18d ] -> %@19
    movq %@19 -> (%@8, %@17d, 8)
    jmp L3657
L3657:
    movl ( %@11, %@22d, 4)d -> %@24d
    cmpl [ %@24d | %@1d ]
    jl L3675
    jmp L4947
L4947:
    movl %@262d -> (%@11, %@260d, 4)d
    movl ( %@11, %@264d, 4)d -> %@266d
    addl [ %@266d | %@269d ] -> %@270d
    movl %@270d -> (%@11, %@267d, 4)d
    jmp L3575
L3575:
    movl ( %@11, %@12d, 4)d -> %@14d
    cmpl [ %@14d | %@1d ]
    jl L3592
    jmp L4983
L4983:
    movq %@8 -> %@$
    jmp L3519
L3519:
.endfunction

.function _BigTensorProduct$randomNumberArray  [ q | l | l ] -> q
L4997:
    movl $1d -> %@3d
    movl $16d -> %@4d
    call _alloc_mem [ %@3d | %@4d ] -> %@5
    call _LCG$initWithDefault [ %@5 | %@2d ] -> %@6
    movl $8d -> %@7d
    call _alloc_mem [ %@1d | %@7d ] -> %@8
    movl $7d -> %@9d
    movl $4d -> %@10d
    call _alloc_mem [ %@9d | %@10d ] -> %@11
    movl $0d -> %@12d
    movl $0d -> %@15d
    movl $8d -> %@18d
    movl $1d -> %@22d
    movl $0d -> %@25d
    movl $1d -> %@30d
    movl $8d -> %@33d
    movl $2d -> %@37d
    movl $0d -> %@40d
    movl $1d -> %@45d
    movl $2d -> %@50d
    movl $8d -> %@53d
    movl $3d -> %@57d
    movl $0d -> %@60d
    movl $1d -> %@65d
    movl $2d -> %@70d
    movl $3d -> %@75d
    movl $8d -> %@78d
    movl $4d -> %@82d
    movl $0d -> %@85d
    movl $1d -> %@90d
    movl $2d -> %@95d
    movl $3d -> %@100d
    movl $4d -> %@105d
    movl $8d -> %@108d
    movl $5d -> %@112d
    movl $0d -> %@115d
    movl $1d -> %@120d
    movl $2d -> %@125d
    movl $3d -> %@130d
    movl $4d -> %@135d
    movl $5d -> %@140d
    movl $8d -> %@143d
    movl $6d -> %@147d
    movl $0d -> %@150d
    movl $1d -> %@155d
    movl $2d -> %@160d
    movl $3d -> %@165d
    movl $4d -> %@170d
    movl $5d -> %@175d
    movl $6d -> %@180d
    movl $1d -> %@183d
    movl $4d -> %@184d
    movl $-1000d -> %@186d
    movl $1000d -> %@187d
    movl $6d -> %@192d
    movl $6d -> %@195d
    movl $1d -> %@197d
    movl $6d -> %@201d
    movl $0d -> %@203d
    movl $5d -> %@205d
    movl $5d -> %@208d
    movl $1d -> %@210d
    movl $5d -> %@214d
    movl $0d -> %@216d
    movl $4d -> %@218d
    movl $4d -> %@221d
    movl $1d -> %@223d
    movl $4d -> %@227d
    movl $0d -> %@229d
    movl $3d -> %@231d
    movl $3d -> %@234d
    movl $1d -> %@236d
    movl $3d -> %@240d
    movl $0d -> %@242d
    movl $2d -> %@244d
    movl $2d -> %@247d
    movl $1d -> %@249d
    movl $2d -> %@253d
    movl $0d -> %@255d
    movl $1d -> %@257d
    movl $1d -> %@260d
    movl $1d -> %@262d
    movl $1d -> %@266d
    movl $0d -> %@268d
    movl $0d -> %@270d
    movl $0d -> %@273d
    movl $1d -> %@275d
    jmp L5052
L5155:
    movl ( %@11, %@25d, 4)d -> %@27d
    movq ( %@8, %@27d, 8) -> %@29
    movl ( %@11, %@30d, 4)d -> %@32d
    call _alloc_mem [ %@1d | %@33d ] -> %@34
    movq %@34 -> (%@29, %@32d, 8)
    jmp L5260
L5434:
    movl ( %@11, %@60d, 4)d -> %@62d
    movq ( %@8, %@62d, 8) -> %@64
    movl ( %@11, %@65d, 4)d -> %@67d
    movq ( %@64, %@67d, 8) -> %@69
    movl ( %@11, %@70d, 4)d -> %@72d
    movq ( %@69, %@72d, 8) -> %@74
    movl ( %@11, %@75d, 4)d -> %@77d
    call _alloc_mem [ %@1d | %@78d ] -> %@79
    movq %@79 -> (%@74, %@77d, 8)
    jmp L5601
L5829:
    movl ( %@11, %@115d, 4)d -> %@117d
    movq ( %@8, %@117d, 8) -> %@119
    movl ( %@11, %@120d, 4)d -> %@122d
    movq ( %@119, %@122d, 8) -> %@124
    movl ( %@11, %@125d, 4)d -> %@127d
    movq ( %@124, %@127d, 8) -> %@129
    movl ( %@11, %@130d, 4)d -> %@132d
    movq ( %@129, %@132d, 8) -> %@134
    movl ( %@11, %@135d, 4)d -> %@137d
    movq ( %@134, %@137d, 8) -> %@139
    movl ( %@11, %@140d, 4)d -> %@142d
    call _alloc_mem [ %@1d | %@143d ] -> %@144
    movq %@144 -> (%@139, %@142d, 8)
    jmp L6042
L6060:
    movl ( %@11, %@150d, 4)d -> %@152d
    movq ( %@8, %@152d, 8) -> %@154
    movl ( %@11, %@155d, 4)d -> %@157d
    movq ( %@154, %@157d, 8) -> %@159
    movl ( %@11, %@160d, 4)d -> %@162d
    movq ( %@159, %@162d, 8) -> %@164
    movl ( %@11, %@165d, 4)d -> %@167d
    movq ( %@164, %@167d, 8) -> %@169
    movl ( %@11, %@170d, 4)d -> %@172d
    movq ( %@169, %@172d, 8) -> %@174
    movl ( %@11, %@175d, 4)d -> %@177d
    movq ( %@174, %@177d, 8) -> %@179
    movl ( %@11, %@180d, 4)d -> %@182d
    call _alloc_mem [ %@183d | %@184d ] -> %@185
    call _LCG$nextRange [ %@6 | %@186d | %@187d ] -> %@188d
    call _Number$init [ %@185 | %@188d ] -> %@189
    movq %@189 -> (%@179, %@182d, 8)
    movl ( %@11, %@192d, 4)d -> %@194d
    addl [ %@194d | %@197d ] -> %@198d
    movl %@198d -> (%@11, %@195d, 4)d
    jmp L6042
L6042:
    movl ( %@11, %@147d, 4)d -> %@149d
    cmpl [ %@149d | %@1d ]
    jl L6060
    jmp L6320
L6320:
    movl %@203d -> (%@11, %@201d, 4)d
    movl ( %@11, %@205d, 4)d -> %@207d
    addl [ %@207d | %@210d ] -> %@211d
    movl %@211d -> (%@11, %@208d, 4)d
    jmp L5811
L5619:
    movl ( %@11, %@85d, 4)d -> %@87d
    movq ( %@8, %@87d, 8) -> %@89
    movl ( %@11, %@90d, 4)d -> %@92d
    movq ( %@89, %@92d, 8) -> %@94
    movl ( %@11, %@95d, 4)d -> %@97d
    movq ( %@94, %@97d, 8) -> %@99
    movl ( %@11, %@100d, 4)d -> %@102d
    movq ( %@99, %@102d, 8) -> %@104
    movl ( %@11, %@105d, 4)d -> %@107d
    call _alloc_mem [ %@1d | %@108d ] -> %@109
    movq %@109 -> (%@104, %@107d, 8)
    jmp L5811
L5811:
    movl ( %@11, %@112d, 4)d -> %@114d
    cmpl [ %@114d | %@1d ]
    jl L5829
    jmp L6356
L6356:
    movl %@216d -> (%@11, %@214d, 4)d
    movl ( %@11, %@218d, 4)d -> %@220d
    addl [ %@220d | %@223d ] -> %@224d
    movl %@224d -> (%@11, %@221d, 4)d
    jmp L5601
L5601:
    movl ( %@11, %@82d, 4)d -> %@84d
    cmpl [ %@84d | %@1d ]
    jl L5619
    jmp L6392
L6392:
    movl %@229d -> (%@11, %@227d, 4)d
    movl ( %@11, %@231d, 4)d -> %@233d
    addl [ %@233d | %@236d ] -> %@237d
    movl %@237d -> (%@11, %@234d, 4)d
    jmp L5416
L5278:
    movl ( %@11, %@40d, 4)d -> %@42d
    movq ( %@8, %@42d, 8) -> %@44
    movl ( %@11, %@45d, 4)d -> %@47d
    movq ( %@44, %@47d, 8) -> %@49
    movl ( %@11, %@50d, 4)d -> %@52d
    call _alloc_mem [ %@1d | %@53d ] -> %@54
    movq %@54 -> (%@49, %@52d, 8)
    jmp L5416
L5416:
    movl ( %@11, %@57d, 4)d -> %@59d
    cmpl [ %@59d | %@1d ]
    jl L5434
    jmp L6428
L6428:
    movl %@242d -> (%@11, %@240d, 4)d
    movl ( %@11, %@244d, 4)d -> %@246d
    addl [ %@246d | %@249d ] -> %@250d
    movl %@250d -> (%@11, %@247d, 4)d
    jmp L5260
L5260:
    movl ( %@11, %@37d, 4)d -> %@39d
    cmpl [ %@39d | %@1d ]
    jl L5278
    jmp L6464
L6464:
    movl %@255d -> (%@11, %@253d, 4)d
    movl ( %@11, %@257d, 4)d -> %@259d
    addl [ %@259d | %@262d ] -> %@263d
    movl %@263d -> (%@11, %@260d, 4)d
    jmp L5137
L5069:
    movl ( %@11, %@15d, 4)d -> %@17d
    call _alloc_mem [ %@1d | %@18d ] -> %@19
    movq %@19 -> (%@8, %@17d, 8)
    jmp L5137
L5137:
    movl ( %@11, %@22d, 4)d -> %@24d
    cmpl [ %@24d | %@1d ]
    jl L5155
    jmp L6500
L6500:
    movl %@268d -> (%@11, %@266d, 4)d
    movl ( %@11, %@270d, 4)d -> %@272d
    addl [ %@272d | %@275d ] -> %@276d
    movl %@276d -> (%@11, %@273d, 4)d
    jmp L5052
L5052:
    movl ( %@11, %@12d, 4)d -> %@14d
    cmpl [ %@14d | %@1d ]
    jl L5069
    jmp L6536
L6536:
    movq %@8 -> %@$
    jmp L4995
L4995:
.endfunction

.function ___minijava_main
L6547:
    movl $1d -> %@0d
    movl $0d -> %@1d
    call _alloc_mem [ %@0d | %@1d ] -> %@2
    call _system_in_read [  ] -> %@3d
    movl $15d -> %@4d
    movb $0l -> %@5l
    call _BigTensorProduct$run [ %@2 | %@4d | %@5l | %@3d ] -> %@6d
    jmp L6545
L6545:
.endfunction

.function _Number$init  [ q | l ] -> q
L6581:
    movl %@1d -> 0(%@0)d
    movq %@0 -> %@$
    jmp L6579
L6579:
.endfunction

.function _Number$mul  [ q | q ] -> q
L6601:
    movl $1d -> %@2d
    movl $4d -> %@3d
    call _alloc_mem [ %@2d | %@3d ] -> %@4
    movl 0(%@1)d -> %@5d
    movl 0(%@0)d -> %@6d
    mull [ %@5d | %@6d ] -> %@7d 
    call _Number$init [ %@4 | %@7d ] -> %@8
    movq %@8 -> %@$
    jmp L6599
L6599:
.endfunction

.function _Number$add  [ q | q ] -> q
L6639:
    movl $1d -> %@2d
    movl $4d -> %@3d
    call _alloc_mem [ %@2d | %@3d ] -> %@4
    movl 0(%@1)d -> %@5d
    movl 0(%@0)d -> %@6d
    addl [ %@5d | %@6d ] -> %@7d
    call _Number$init [ %@4 | %@7d ] -> %@8
    movq %@8 -> %@$
    jmp L6637
L6637:
.endfunction

