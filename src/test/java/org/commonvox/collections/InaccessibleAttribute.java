/*
 * Copyright (C) 2016 Daniel Vimont
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.commonvox.collections;

/**
 * This class in "unkeyable" in the context of the Book class,
 * because no #get method for it exists in the Book class.
 *
 * @author Daniel Vimont
 */
public class InaccessibleAttribute implements Comparable<InaccessibleAttribute> {

    String anyOldString = "some value";

    public InaccessibleAttribute() { }

    @Override
    public int compareTo(InaccessibleAttribute other) {
        return this.anyOldString.compareTo(other.anyOldString);
    }
}
