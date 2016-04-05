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
 * @author Daniel Vimont
 */
public class Genre implements Comparable<Genre>, Cloneable {

    String genre;

    public Genre(String genre) {
        this.genre = genre;
    }

    @Override
    public int compareTo(Genre other) {
        if (other == null) {
            return 1;
        }
        return this.genre.toLowerCase().compareTo(other.genre.toLowerCase());
    }

    @Override
    public boolean equals(Object other) {
        if (other == null
                || !this.getClass().isAssignableFrom(other.getClass())) {
            return false;
        }
        return compareTo((Genre)other) == 0;
    }

    @Override
    public String toString() {
        return genre;
    }

    @Override
    public Genre clone() {
      return new Genre(genre);
    }
}
