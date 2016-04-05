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
 *
 * @author dv
 */
public class Author implements Comparable<Author>, Cloneable {
    String lastName, firstName;

    public Author(String lastName, String firstName) {
        setName(lastName, firstName);
    }

    public final void setName(String lastName, String firstName) {
      this.lastName = lastName;
      this.firstName = firstName;
    }

    @Override
    public int compareTo(Author other) {
        if (other == null) {
            return 1;
        }
        int returnedInt = this.lastName.toLowerCase().compareTo(other.lastName.toLowerCase());
        if (returnedInt == 0) {
            returnedInt = this.firstName.toLowerCase().compareTo(other.firstName.toLowerCase());
        }
        return returnedInt;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null
                || !this.getClass().isAssignableFrom(other.getClass())) {
            return false;
        }
        return compareTo((Author)other) == 0;
    }

    @Override
    public String toString() {
        return firstName + " " + lastName;
    }

    @Override
    public Author clone() {
      return new Author(lastName, firstName);
    }
}
