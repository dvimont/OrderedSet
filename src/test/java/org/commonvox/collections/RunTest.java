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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Manually run some of the tests that are run during Maven build process.
 *
 * @author Daniel Vimont
 */
public class RunTest {

  public static void main(String[] args) throws Exception {
      new RunTest().go();
  }

  public void go() throws Exception {
    List<Book> myBookList = new ArrayList<Book>();
    KeyComponentProfile<Book> genreComponent
            = new KeyComponentProfile<Book>(Book.class, Genre.class);
    KeyComponentProfile<Book> authorComponent
            = new KeyComponentProfile<Book>(Book.class, Author.class);
    OrderedSet<Book> booksByGenreAuthor
            = new OrderedSet<Book>(myBookList, genreComponent, authorComponent);
    Set<Object> authorSet = booksByGenreAuthor.keyComponentSet(authorComponent);
    for (Book book : booksByGenreAuthor) {

    }

    TestKeyComponentProfile testKeyComponentProfile = new TestKeyComponentProfile();
    testKeyComponentProfile.testKeyComponentProfileConstructors();
    try {
      testKeyComponentProfile.testWithNoncomparableAttribute();
    } catch (IllegalArgumentException e) {
      System.out.println("Caught appropriate exception.");
    }
    try {
      testKeyComponentProfile.testWithInaccessibleAttribute();
    } catch (IllegalArgumentException e) {
      System.out.println("Caught appropriate exception.");
    }

    TestOrderedSet testItNow = new TestOrderedSet();
    testItNow.testInitiallyEmptySetConstructor();
    testItNow.testInitiallyPopulatedSetConstructor();
    try {
      testItNow.testAddOfNullValue();
    } catch (IllegalArgumentException e) {
      System.out.println("Caught appropriate exception.");
    }
  }
}
