/*
 * Copyright (c) 2018 Uber Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package testcases.E009_duplicate_factory_method_cross_scope;

import common.DuplicateFactoryMethodsSubject;
import motif.models.errors.DuplicateFactoryMethodsError;
import motif.models.errors.MotifError;

public class Test {

    public static MotifError error;

    public static void run() {
        DuplicateFactoryMethodsError error = (DuplicateFactoryMethodsError) Test.error;
        DuplicateFactoryMethodsSubject.assertThat(error).matches("sb", Scope.class);
    }
}
