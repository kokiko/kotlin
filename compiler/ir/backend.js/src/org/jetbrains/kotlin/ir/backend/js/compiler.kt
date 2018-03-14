/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.js.analyze.TopDownAnalyzerFacadeForJS
import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.progress.ProgressIndicatorAndCompilationCanceledStatus
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi2ir.Psi2IrTranslator

fun compile(
    project: Project,
    files: List<KtFile>,
    configuration: CompilerConfiguration,
    export: FqName
): String {
    val analysisResult = TopDownAnalyzerFacadeForJS.analyzeFiles(files, project, configuration, emptyList(), emptyList())
    ProgressIndicatorAndCompilationCanceledStatus.checkCanceled()

    TopDownAnalyzerFacadeForJS.checkForErrors(files, analysisResult.bindingContext)

    val moduleFragment = Psi2IrTranslator().generateModule(analysisResult.moduleDescriptor, files, analysisResult.bindingContext)

    val program = transformIrModuleToJs(moduleFragment)
    return program.toString()
}

fun transformIrModuleToJs(module: IrModuleFragment): JsProgram {
    val program = JsProgram()

    module.files.forEach {
        it.accept(object : IrElementVisitorVoid {
            override fun visitElement(element: IrElement) {
                TODO()
            }

            override fun visitFile(declaration: IrFile) {
                program.globalBlock.statements.add(transformIrFileToJs(declaration, program.scope))
            }
        }, null)
    }

    return program
}

fun transformIrFileToJs(file: IrFile, scope: JsObjectScope): JsBlock {
    val block = JsBlock()
    file.declarations.forEach {
        it.accept(object : IrElementVisitorVoid {
            override fun visitElement(element: IrElement) {
                TODO()
            }

            override fun visitSimpleFunction(declaration: IrSimpleFunction) {
                block.statements.add(JsExpressionStatement(transformIrFunctionToJsFunction(declaration)))
            }

            private fun transformIrFunctionToJsFunction(declaration: IrSimpleFunction): JsFunction {
                val funName = declaration.name.asString()
                val body = declaration.body?.accept(StatementGenerator(), null) as? JsBlock ?: JsBlock()
                val function = JsFunction(JsFunctionScope(scope, "scope for $funName"), body, "function $funName")

                function.name = scope.declareName(funName)

                fun JsFunction.addParameter(parameterName: String) {
                    val parameter = function.scope.declareName(parameterName)
                    parameters.add(JsParameter(parameter))
                }

                declaration.extensionReceiverParameter?.let { function.addParameter("\$receiver") }
                declaration.valueParameters.forEach {
                    function.addParameter(it.name.asString())
                }

                return function
            }
        }, null)
    }

    return block
}

class StatementGenerator : IrElementVisitor<JsStatement, Nothing?> {
    override fun visitElement(element: IrElement, data: Nothing?): JsStatement {
        TODO()
    }

    override fun visitBlockBody(body: IrBlockBody, data: Nothing?): JsStatement {
        return JsBlock(body.statements.map { it.accept(this, data) })
    }

    override fun visitExpression(expression: IrExpression, data: Nothing?): JsStatement {
        return JsExpressionStatement(expression.accept(ExpressionGenerator(), data))
    }

    override fun visitReturn(expression: IrReturn, data: Nothing?): JsStatement {
        return JsReturn(expression.value.accept(ExpressionGenerator(), null))
    }
}

class ExpressionGenerator : IrElementVisitor<JsExpression, Nothing?> {
    override fun visitElement(element: IrElement, data: Nothing?): JsExpression {
        TODO()
    }

    override fun <T> visitConst(expression: IrConst<T>, data: Nothing?): JsExpression {
        return JsStringLiteral(expression.value.toString())
    }
}