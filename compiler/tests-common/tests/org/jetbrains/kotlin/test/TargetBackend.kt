/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test

import java.io.File

enum class TargetBackend(
    val whitelist: List<File>? = null // null means that whitelist must not be used
) {
    ANY,
    JVM,
    JVM_IR,
    JS,
    JS_IR(JS_IR_BACKEND_TEST_WHITELIST);
}
