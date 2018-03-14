/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js

import org.jetbrains.kotlin.backend.common.output.OutputFileCollection

interface CompilerPass<in I, out O> {
    // description
    // name
    fun process(input: I): O
}

interface CompilerPassFactory<in I, out O> {
    // prerequests
    // description
    fun create(): CompilerPass<I, O>
}

// Compiler<Arguments, Status>

//class FrontendPass<Environment, PSI + BindingContext>
//class Psi2IrPass<PSI + BindingContext, IrModuleFragment + ?>

//subpasses
// AbstractBackendPass<BackendContext, BackendContext>
//  call lowering
// IrToJsPass<BackendContext, Outputs>
//

class JsBackendContext {

}

class IrToJsPass : CompilerPass<JsBackendContext, OutputFileCollection> {
    override fun process(input: JsBackendContext): OutputFileCollection {
        TODO()
    }
}

class KotlinJsBackendPass : CompilerPass<JsBackendContext, OutputFileCollection> {
    override fun process(input: JsBackendContext): OutputFileCollection {

        TODO()
    }

}

interface ChainedPassBuilderUntyped {
    fun <I, O> add(compilerPass: CompilerPass<I, O>): ChainedPassBuilder<I, O>
}

interface ChainedPassBuilder<F, I> : CompilerPass<F, I> {
    fun <O> add(compilerPass: CompilerPass<I, O>): ChainedPassBuilder<F, O>
}

fun chainedPass(name: String, description: String): ChainedPassBuilderUntyped {
    return null!!
}



fun test(Pass1: CompilerPass<JsBackendContext, JsBackendContext>, Pass2: CompilerPass<JsBackendContext, OutputFileCollection>) {
    fun jsBackendPasses() =
        chainedPass("K2JSBackend", "")
            .add(Pass2)


    val r: OutputFileCollection = chainedPass("K2JS", "")
        .add(Pass1)
//        .add(Pass2)
        .add(Pass1)
        .add(jsBackendPasses())
        .process(JsBackendContext())
}

/*

chainedPass("ShortName", "Long description")
    .add(Pass1)
    .add(Pass2)
    .add(
        if(expr) {
            Pass3
        }
        else {
            Pass4
        }
    )

    chainedPass("K2JS")
        .add(Frontend)
        .add(Psi2Ir)
        .addJsBackendPasses()
        .process()

    addJsBackendPasses():
        chainedPass("K2JSBackend")
            .add(IrToEs5Pass)

 */

class ChainedCompilerPass<I, O> : CompilerPass<I, O> {
    override fun process(input: I): O {
        TODO()
    }
}


//class CombinedCompilerPass<I, O>() : CompilerPass<I, O>

//compilerPass(I) {
//    .add(MyPass1)
//    .add(MyPass2)
//
//}